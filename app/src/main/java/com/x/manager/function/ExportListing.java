package com.x.manager.function;

import android.content.Intent;

import com.x.manager.R;
import com.x.manager.utility.SanctionBase;
import com.x.manager.utility.Tools;
import com.x.manager.dialog.ProgressDialog;
import com.x.manager.dialog.TextInputDialog;
import com.x.manager.view.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportListing {
    public static void run(Intent data) {
        ProgressDialog.InitProgress();
        SanctionBase.LogNormal(MainActivity.Self, "Exporting file list...");
        Intent intent = new Intent(MainActivity.Self, ProgressDialog.class);
        intent.putExtra(ProgressDialog.PARAM_TITLE, "Export");
        intent.putExtra(ProgressDialog.PARAM_MESSAGE, "Exporting file tree list...");
        intent.putExtra(ProgressDialog.PARAM_SHOW_CANCEL, true);
        intent.putExtra(ProgressDialog.PARAM_HAS_PROGRESS, true);
        MainActivity.Self.startActivityForResult(intent, MainActivity.REQUEST_PROGRESS);
        MainActivity.Self.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

        final Intent dat = data;
        new Thread(() -> {
            SanctionBase.LogNormal(MainActivity.Self, "Listing files...");
            ProgressDialog.ChangeProgress("Listing files...", true, false);
            String filename = dat.getStringExtra(TextInputDialog.RETURN_INPUT);
            String path = MainActivity.Self.currFolder.getAbsolutePath();
            if (!path.endsWith("/")) path += "/";
            path += filename;
            StringBuilder sb = new StringBuilder(MainActivity.Self.currFolder.getAbsolutePath() + "\n");
            sb.append(getFileListTree(MainActivity.Self.currFolder, 1));
            if (!ProgressDialog.ProgressCancelled) {
                String res = "";
                try {
                    SanctionBase.LogNormal(MainActivity.Self, "Writing to file...");
                    ProgressDialog.ChangeProgress("Writing to file...", false, false);
                    FileOutputStream os = new FileOutputStream(new File(path), false);
                    os.write(sb.toString().getBytes());
                    os.flush();
                    os.close();
                } catch (IOException ex) {
                    res = ex.getMessage();
                }

                final String s = res;
                final String p = path;
                MainActivity.Self.lvwContent.post(() -> {
                    assert s != null;
                    if (s.equals("")) {
                        MainActivity.Self.refreshList();
                        SanctionBase.LogInformation(MainActivity.Self, "File list exported: " + p);
                    } else {
                        Tools.Toast(MainActivity.Self, "Write-error: " + s, true, true);
                        SanctionBase.LogError(MainActivity.Self, "File list export failed: " + p + "\n" + s);
                    }
                    ProgressDialog.ProgressCompleted = true;
                });
            } else { // cancelled
                final String p = path;
                MainActivity.Self.lvwContent.post(() -> {
                    MainActivity.Self.finishActivity(MainActivity.REQUEST_PROGRESS);
                    MainActivity.Self.overridePendingTransition(0, R.anim.slide_down);
                    SanctionBase.LogNormal(MainActivity.Self, "File list export cancelled: " + p);
                });
            }
        }).start();
    }

    private static StringBuilder getFileListTree(File root, int depth) {
        if (!root.exists()) return null;
        if (!root.isDirectory()) return null;

        StringBuilder sb = new StringBuilder();
        if (ProgressDialog.ProgressCancelled) {
            return sb;
        }

        File[] items = root.listFiles();
        if (items == null) {
            return sb;
        }

        Tools.SortFileList(items, Tools.Sorting.TYPE_FILENAME, Tools.Sorting.GROUPING_FOLDER_FIRST, false);
        for (File f : items) {
            if (ProgressDialog.ProgressCancelled) {
                return sb;
            }
            if (f.isDirectory()) {
                sb.append(makeIndent(depth)).append("[").append(f.getName()).append("]\n");
                StringBuilder b = getFileListTree(f, depth + 1);
                if (sb.length() > 0) {
                    assert b != null;
                    sb.append(b.toString());
                }
            } else {
                sb.append(makeIndent(depth)).append(f.getName()).append("\n");
            }
        }

        return sb;
    }

    private static String makeIndent(int depth) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < (depth * 4); ++i) {
            if (((i + 1) % 4) > 0) ret.append(" ");
            else ret.append("|");
        }
        ret.append("——");
        return ret.toString();
    }
}
