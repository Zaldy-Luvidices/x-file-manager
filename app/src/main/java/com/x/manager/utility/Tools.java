package com.x.manager.utility;

import java.nio.file.Path;
import java.text.*;
import java.math.*;
import java.io.*;
import java.util.*;

import android.content.*;
import android.net.wifi.aware.DiscoverySession;
import android.widget.*;
import android.view.inputmethod.*;
import android.view.*;
import android.app.*;
import android.graphics.*;

import com.x.manager.R;
import com.x.manager.function.FileDelete;
import com.x.manager.view.MainActivity;

public class Tools {

    public static class Sorting {
        // sort type
        public static final int TYPE_FILENAME = 0;
        public static final int TYPE_FILE_SIZE = 1;
        public static final int TYPE_FOLDER_ITEMS = 2;
        public static final int TYPE_DATE_MODIFIED = 3;
        // grouping
        public static final int GROUPING_COMBINED = 0;
        public static final int GROUPING_FOLDER_FIRST = 1;
        public static final int GROUPING_FILE_FIRST = 2;

        public int SortType = 0;
        public int SortGrouping = 1;
        public boolean IsReverse = false;
    }

    public enum CopyMode {
        Exclusive,
        FolderMerge,
        FolderKeep,
        FileOverwrite,
        FileKeep,
    }

    public static String ToDataUnitString(long val) {
        long KB = 1024;
        long MB = KB * KB;
        long GB = MB * KB;

        DecimalFormat df = new DecimalFormat("0.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        if (val >= GB) {
            float ret = (float) val / (float) GB;
            return df.format(ret) + " G";
        } else if (val >= MB) {
            float ret = (float) val / (float) MB;
            return df.format(ret) + " M";
        } else if (val >= KB) {
            float ret = (float) val / (float) KB;
            return df.format(ret) + " K";
        } else {
            return df.format(val) + " B";
        }
    }

    public static void SortFileList(File[] list, int sortType, int grouping, boolean isReversed) {
        final int rev = isReversed ? -1 : 1;
        final int gp = grouping;
        final int st = sortType;
        Arrays.sort(list, (f1, f2) -> {
            boolean isDiff = (f1.isDirectory() && f2.isFile()) ||
                    (f1.isFile() && f2.isDirectory());
            if (isDiff && gp != Sorting.GROUPING_COMBINED) {
                if (gp == Sorting.GROUPING_FOLDER_FIRST) {
                    return ((f1.isDirectory() ? -1 : 1));
                } else if (gp == Sorting.GROUPING_FILE_FIRST) {
                    return ((f1.isFile() ? -1 : 1));
                }
            }

            if (st == Sorting.TYPE_DATE_MODIFIED) {
                Date d1 = new Date(f1.lastModified());
                Date d2 = new Date(f2.lastModified());
                return d1.compareTo(d2) * rev;
            } else if (isDiff) {
                // compare by filename
                return (f1.getName().compareToIgnoreCase(f2.getName())) * rev;
            } else if (f1.isFile() && st == Sorting.TYPE_FILE_SIZE) {
                return (int) (f1.length() - f2.length()) * rev;
            } else if (f1.isDirectory() && st == Sorting.TYPE_FOLDER_ITEMS) {
                File[] lt = f1.listFiles();
                if (lt == null) {
                    lt = new File[]{};
                }
                int i1 = lt.length;

                lt = f2.listFiles();
                if (lt == null) {
                    lt = new File[]{};
                }
                int i2 = lt.length;

                return (i1 - i2) * rev;
            } else {
                return (f1.getName().compareToIgnoreCase(f2.getName())) * rev;
            }
        });
    }

    public static int DpsToPixel(Context context, float dps) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static String GetFileExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return "";
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }

    public static String GetFilenameWithoutExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }

        int dot = url.lastIndexOf(".");
        if (dot != -1) {
            url = url.substring(0, dot);
        }

        return url;
    }

    public static String GetFilenameAfterExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }

        int dot = url.lastIndexOf(".");
        if (dot != -1) {
            return url.substring(dot);
        } else {
            return "";
        }
    }

    public static String GenerateCopyName(String dir, final String fName) {
        for (int j = 1; j <= Integer.MAX_VALUE; ++j) {
            String newPath = "";
            if (new File(ValidFolderPath(dir) + fName).isFile()) {
                newPath = Tools.GetFilenameWithoutExt(fName);
                newPath += " (" + j + ")";
                newPath += Tools.GetFilenameAfterExt(fName);
            } else {
                newPath = fName + " (" + j + ")";
            }
            newPath = dir + newPath;
            File outFile = new File(newPath);
            if (!outFile.exists()) {
                return newPath;
            }
        }

        return dir + fName; // expected to not be reached
    }

    public static void Toast(Context context, String msg, boolean isLong, boolean isError) {
        Toast toast = Toast.makeText(context, msg, (isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT));
        View v = toast.getView();
        v.setBackgroundResource(isError ? R.drawable.back_toast_error : R.drawable.back_toast_normal);
        TextView txt = v.findViewById(android.R.id.message);
        txt.setTextColor(Color.WHITE);
        txt.setTypeface(DefaultFont(context, false));
        toast.show();
    }

    public static void ShowSoftKeyboard(Context context, boolean show, EditText editTxt) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(editTxt.getWindowToken(), 0);
        }
    }

    public static void SetDialogPosition(Activity activity, int gravity) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.gravity = gravity;
        lp.y = (gravity == Gravity.RIGHT) ? 380 : 200;
        win.setAttributes(lp);
    }

    public static Typeface DefaultFont(Context context, boolean isBold) {
        if (isBold) {
            return Typeface.createFromAsset(context.getAssets(), "fonts/Titillium-Bold.otf");
        } else {
            return Typeface.createFromAsset(context.getAssets(), "fonts/Titillium-Regular.otf");
        }
    }

    public static int GetFolderIcon(String folderName) {
        if (folderName.equalsIgnoreCase("documents")) {
            return R.mipmap.ic_folder_doc;
        } else if (folderName.equalsIgnoreCase("songs")) {
            return R.mipmap.ic_folder_audio;
        } else if (folderName.equalsIgnoreCase("download")) {
            return R.mipmap.ic_folder_download;
        } else if (folderName.equalsIgnoreCase("pictures")) {
            return R.mipmap.ic_folder_image;
        } else if (folderName.equalsIgnoreCase("videos")) {
            return R.mipmap.ic_folder_video;
        } else {
            return R.mipmap.ic_folder;
        }
    }

    public static boolean RenameFile(File oldFile, File newFile) {
        SanctionBase.LogNormal(MainActivity.Self, "Renaming: " + oldFile.getAbsolutePath());
        if (oldFile.renameTo(newFile)) {
            SanctionBase.LogInformation(MainActivity.Self, "Success: " + newFile.getAbsolutePath());
            return true;
        } else {
            SanctionBase.LogError(MainActivity.Self, "Failed: " + newFile.getAbsolutePath());
            return false;
        }
    }

    public static boolean CopyFile(File oldFile, File destFolder, CopyMode copyMode) {
        return CopyFile(oldFile, destFolder, "", copyMode);
    }

    public static boolean CopyFile(File sourceFile, File destFolder, String newName, CopyMode copyMode) {
        if (!sourceFile.exists()) {
            SanctionBase.LogError(MainActivity.Self, "Error: Source no longer exists");
            return false;
        }

        if (sourceFile.isDirectory()) {
            return folderCopy(sourceFile, destFolder, newName, copyMode);
        } else {
            return fileCopy(sourceFile, destFolder, newName, copyMode);
        }
    }

    private static boolean folderCopy(File sourceFile, File destFolder, String newName, CopyMode copyMode) {
        File newFolder = new File(Tools.ValidFolderPath(destFolder.getAbsolutePath())
                + (newName.equals("") ? sourceFile.getName() : newName));
//        if (Tools.ValidFolderPath(sourceFile.getAbsolutePath())
//                .equals(Tools.ValidFolderPath(newFolder.getAbsolutePath()))) {
//            copyMode = CopyMode.FolderKeep;
//        }
        SanctionBase.LogNormal(MainActivity.Self, "Creating folder: " + newFolder.getAbsolutePath());

        if (newFolder.exists()) {
            SanctionBase.LogWarning(MainActivity.Self, "Destination folder exists");
            if (newFolder.isFile()) {
                SanctionBase.LogError(MainActivity.Self, "Destination is a file");
                return false;
            }

            if (copyMode == CopyMode.FolderMerge) { // merge
                SanctionBase.LogNormal(MainActivity.Self, "Merging contents...");
                File[] contents = null;
                boolean isSuccess = true;
                try {
                    contents = sourceFile.listFiles();
                    for (File f : contents) {
                        if (f.isFile()) {
                            isSuccess |= fileCopy(f, newFolder, newName, CopyMode.FileKeep);
                        } else {
                            isSuccess |= folderCopy(f, newFolder, newName, CopyMode.FolderMerge);
                        }
                    }
                } catch (Exception e) {
                    SanctionBase.LogError(MainActivity.Self, "Failed to access folder contents: " + e.toString());
                    isSuccess = false;
                }
                return isSuccess;
            } else if (copyMode == CopyMode.FolderKeep) {
                newFolder = new File(Tools.GenerateCopyName(
                        Tools.ValidFolderPath(destFolder.getAbsolutePath()), sourceFile.getName()));
            } else {
                return false;
            }
        }

        if (newFolder.mkdirs()) {
            SanctionBase.LogInformation(MainActivity.Self, "Success: ");
            File[] contents = null;
            try {
                contents = sourceFile.listFiles();
                boolean isSuccess = true;
                for (File f : contents) {
                    isSuccess |= CopyFile(f, newFolder, copyMode);
                }
                return isSuccess; // marked as success if all contents are copied successfully
            } catch (Exception e) {
                SanctionBase.LogError(MainActivity.Self, "Cannot access source contents: " + sourceFile.getAbsolutePath());
                return false;
            }
        } else {
            SanctionBase.LogError(MainActivity.Self, "Failed");
            return false;
        }
    }

    private static boolean fileCopy(File sourceFile, File destFolder, String newName, CopyMode copyMode) {
        SanctionBase.LogNormal(MainActivity.Self, "Copying: " + sourceFile.getAbsolutePath());
        File newFile = new File(Tools.ValidFolderPath(destFolder.getAbsolutePath())
                + (newName.equals("") ? sourceFile.getName() : newName));
        if (sourceFile.getAbsolutePath().equalsIgnoreCase(newFile.getAbsolutePath())) {
            if (copyMode == CopyMode.FileOverwrite) {
                return false;
            }
            copyMode = CopyMode.FileKeep;
        }

        if (newFile.exists()) {
            SanctionBase.LogWarning(MainActivity.Self, "Destination file exists: " + newFile.getAbsolutePath());
            if (newFile.isDirectory()) {
                SanctionBase.LogError(MainActivity.Self, "Destination is a folder");
                return false;
            }

            if (copyMode == CopyMode.FileOverwrite) {
                if (!FileDelete.deleteFile(MainActivity.Self, newFile)) {
                    return false;
                }
            } else if (copyMode == CopyMode.FileKeep) {
                newFile = new File(Tools.GenerateCopyName(
                        Tools.ValidFolderPath(destFolder.getAbsolutePath()), sourceFile.getName()));
            } else {
                return false;
            }
        }

        InputStream in = null;
        try {
            in = new FileInputStream(sourceFile);
        } catch (Exception e) {
            SanctionBase.LogError(MainActivity.Self, "Cannot access source: " + e.toString());
            return false;
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(newFile);
        } catch (Exception e) {
            SanctionBase.LogError(MainActivity.Self, "Cannot create destination: " + e.toString());
            closeInStream(in);
            return false;
        }

        try {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            SanctionBase.LogError(MainActivity.Self, "Copy error: " + e.toString());
            closeInStream(in);
            closeOutStream(out);
            return false;
        }

        closeInStream(in);
        closeOutStream(out);
        SanctionBase.LogInformation(MainActivity.Self, "Success: " + newFile.getAbsolutePath());
        return true;
    }

    public static String ValidFolderPath(String path) {
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }

        return path;
    }

    public static String DeletePathTrail(final String path) {
        if (path.endsWith(File.separator)) {
            return path.substring(0, path.length() - 1);
        } else {
            return path;
        }
    }

    public static void ShowButtonTip(Context context, View view) {
        if (view == null || view.getTag() == null) {
            return;
        }

        String tag = view.getTag().toString();
        Toast(context, tag, false, false);
    }

    private static void closeInStream(InputStream stream) {
        try {
            stream.close();
        } catch (Exception e) {
            SanctionBase.LogWarning(MainActivity.Self, "Can't close IN stream: " + e.toString());
        }
    }

    private static void closeOutStream(OutputStream stream) {
        try {
            stream.close();
        } catch (Exception e) {
            SanctionBase.LogWarning(MainActivity.Self, "Can't close OUT stream: " + e.toString());
        }
    }
}
