package com.x.manager.function;

import android.content.Context;
import android.content.Intent;

import com.x.manager.R;
import com.x.manager.utility.SanctionBase;
import com.x.manager.utility.Tools;
import com.x.manager.dialog.ProgressDialog;
import com.x.manager.model.FileListItem;
import com.x.manager.view.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileDelete {
    public static void run() {
        ProgressDialog.InitProgress();
        Intent intent = new Intent(MainActivity.Self, ProgressDialog.class);
        intent.putExtra(ProgressDialog.PARAM_TITLE, "Delete");
        intent.putExtra(ProgressDialog.PARAM_MESSAGE, "Deleting files...");
        intent.putExtra(ProgressDialog.PARAM_SHOW_CANCEL, true);
        intent.putExtra(ProgressDialog.PARAM_HAS_PROGRESS, true);
        MainActivity.Self.startActivityForResult(intent, MainActivity.REQUEST_PROGRESS);
        MainActivity.Self.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

        new Thread(() -> {
            ProgressDialog.ChangeProgress("Deleting files...", true, false);
            final List<File> files = new ArrayList<>();
            for (FileListItem i : MainActivity.Self.selFiles) files.add(new File(i.ID));
            SanctionBase.LogNormal(MainActivity.Self, "Deleting files: " + files.size() + "...");
            final List<String> ret = deleteFileTree(MainActivity.Self,
                    Arrays.copyOf(files.toArray(), files.toArray().length, File[].class));

            MainActivity.Self.txtRoot.post(() -> {
                if (!ProgressDialog.ProgressCancelled && Integer.parseInt(ret.get(1)) > 0) {
                    if (files.size() == 1) {
                        String s = "file";
                        if (files.get(0).isDirectory()) s = "folder";
                        Tools.Toast(MainActivity.Self, "Failed to delete " + s, true, true);
                    } else {
                        Tools.Toast(MainActivity.Self, "Failed to delete " + ret.get(1)
                                + " of " + ret.get(0) + " items", true, true);
                    }
                }
                if (ProgressDialog.ProgressCancelled) {
                    ProgressDialog.ProgressCancelled = false;
                }
                if (Integer.parseInt(ret.get(0)) > 1) {
                    SanctionBase.LogNormal(MainActivity.Self, "Total: " + ret.get(0));
                    SanctionBase.LogInformation(MainActivity.Self, "Deleted: "
                            + (Integer.parseInt(ret.get(0)) - Integer.parseInt(ret.get(1))));
                    SanctionBase.LogError(MainActivity.Self, "Failed: " + ret.get(1));
                }
                MainActivity.Self.refreshList();
                MainActivity.Self.finishActivity(MainActivity.REQUEST_PROGRESS);
                MainActivity.Self.overridePendingTransition(0, R.anim.slide_down);
            });
        }).start();
    }

    private static List<String> deleteFileTree(Context context, File[] files) {
        Tools.SortFileList(files, Tools.Sorting.TYPE_FILENAME,
                Tools.Sorting.GROUPING_FOLDER_FIRST, false);

        int total = 0, failed = 0;
        for (File f : files) {
            if (com.x.manager.dialog.ProgressDialog.ProgressCancelled) break;
            if (f.isDirectory()) {
                File[] items = f.listFiles();
                if (items != null) {
                    if (items.length > 0) {
                        List<String> ret = deleteFileTree(context, items);
                        total += Integer.parseInt(ret.get(0));
                        failed += Integer.parseInt(ret.get(1));
                    }
                    if (!ProgressDialog.ProgressCancelled) {
                        if (!deleteFile(context, f)) failed++;
                        total++;
                    }
                }
                continue;
            }
            if (!deleteFile(context, f)) failed++;
            total++;
        }

        List<String> ret = new ArrayList<>();
        ret.add(String.valueOf(total));
        ret.add(String.valueOf(failed));
        return ret;
    }

    public static boolean deleteFileTree(Context context, File f) {
        if (!f.exists()) {
            return true;
        }

        if (f.isFile()) {
            return deleteFile(context, f);
        } else { // folder
            boolean isSuccess = true;
            File[] contents = null;
            try {
                contents = f.listFiles();
                for (File c : contents) {
                    isSuccess |= deleteFileTree(context, c); // marked as success if all contents are deleted successfully
                }
            } catch (Exception e) {
                isSuccess = false;
            }
            isSuccess |= deleteFile(context, f); // delete parent
            return isSuccess;
        }
    }

    public static boolean deleteFile(Context context, File f) {
        SanctionBase.WriteLog(context,
                SanctionBase.LogType.NORMAL,
                "Deleting: " + f.getAbsolutePath() + (f.isDirectory() ? "/" : ""));
        if (!f.delete()) {
            SanctionBase.WriteLog(context,
                    SanctionBase.LogType.ERROR,
                    "Failed");
            return false;
        }

        return true;
    }
}
