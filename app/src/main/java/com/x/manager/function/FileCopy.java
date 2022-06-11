package com.x.manager.function;

import android.content.Intent;
import android.util.Log;

import com.x.manager.R;
import com.x.manager.dialog.PasteOptionsDialog;
import com.x.manager.dialog.ProgressDialog;
import com.x.manager.model.FileListItem;
import com.x.manager.utility.SanctionBase;
import com.x.manager.utility.Tools;
import com.x.manager.view.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FileCopy {
    private static List<FileListItem> files;
    private static List<Integer> options;
    private static int fileIndex;
    private static File outputFolder;

    private static int rememberedFileOption;
    private static int rememberedFolderOption;
    private static int rememberedConflictOption; // file to folder, or folder to file

    public static void run(List<FileListItem> selFiles, File destFolder) {
        files = selFiles;
        options = new ArrayList<>();
        fileIndex = -1;
        for (FileListItem ignored : selFiles) {
            options.add(PasteOptionsDialog.OPTION_COPY);
        }
        outputFolder = destFolder;
        rememberedFileOption = PasteOptionsDialog.OPTION_NONE;
        rememberedFolderOption = PasteOptionsDialog.OPTION_NONE;
        rememberedConflictOption = PasteOptionsDialog.OPTION_NONE;

        // TODO: Also show progress bar on initialization process
        SanctionBase.LogNormal(MainActivity.Self, "Copy initialization started.");
        new Thread(() -> {
            fileCheck(++fileIndex);
        }).start();
    }

    public static void handleResult(int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            SanctionBase.LogNormal(MainActivity.Self, "Copy operation cancelled - dialog cancel.");
            cancelOperation();
            return;
        }

        int selOp = intent.getIntExtra(PasteOptionsDialog.RESULT_SELECTED_OPTION, PasteOptionsDialog.OPTION_ABORT);
        if (selOp == PasteOptionsDialog.OPTION_ABORT) {
            SanctionBase.LogNormal(MainActivity.Self, "Copy operation cancelled - user abort.");
            cancelOperation();
            return;
        }

        options.set(fileIndex, selOp);
        if (intent.getBooleanExtra(PasteOptionsDialog.RESULT_REMEMBER_ALL, false)) {
            int opType = intent.getIntExtra(PasteOptionsDialog.RESULT_OPERATION_TYPE, PasteOptionsDialog.OP_TYPE_FILE_TO_FILE);
            if (opType == PasteOptionsDialog.OP_TYPE_FILE_TO_FILE
                    || opType == PasteOptionsDialog.OP_TYPE_SAME_FILE) {
                rememberedFileOption = selOp;
            } else if (opType == PasteOptionsDialog.OP_TYPE_FOLDER_TO_FOLDER) {
                rememberedFolderOption = selOp;
            } else if (opType == PasteOptionsDialog.OP_TYPE_FILE_TO_FOLDER
                    || opType == PasteOptionsDialog.OP_TYPE_FOLDER_TO_FILE) {
                rememberedConflictOption = selOp;
            }
        }

        fileCheck(++fileIndex);
    }

    private static void startOperation() {
        SanctionBase.LogNormal(MainActivity.Self, "Copy operation started.");
        int success = 0, failed = 0, skipped = 0;

        ProgressDialog.InitProgress();
        ProgressDialog.ProgressMax = files.size();
        Intent intent = new Intent(MainActivity.Self, ProgressDialog.class);
        intent.putExtra(ProgressDialog.PARAM_TITLE, "Copy");
        intent.putExtra(ProgressDialog.PARAM_MESSAGE, "Initializing...");
        intent.putExtra(ProgressDialog.PARAM_SHOW_CANCEL, true);
        intent.putExtra(ProgressDialog.PARAM_HAS_PROGRESS, true);
        MainActivity.Self.startActivityForResult(intent, MainActivity.REQUEST_PROGRESS);
        MainActivity.Self.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

        for (int i = 0; i < files.size(); ++i) {
            if (ProgressDialog.ProgressCancelled) {
                ProgressDialog.ProgressCancelled = false;
                SanctionBase.LogNormal(MainActivity.Self, "Copy operation cancelled - progress cancel.");
                cancelOperation();
                return;
            }

            if (options.get(i) == PasteOptionsDialog.OPTION_ABORT) {
                SanctionBase.LogNormal(MainActivity.Self, "Copy operation cancelled - user abort.");
                cancelOperation();
                return;
            }

            FileListItem s = files.get(i);
            File f = new File(s.ID);
            String oldPath = f.getAbsolutePath();

            ProgressDialog.ChangeProgress("Copying " + i + " of " + files.size() + ": "
                    + "\n" + oldPath, true, true);

            String fName = oldPath.substring(oldPath.lastIndexOf("/") + 1);
            String newPath = Tools.ValidFolderPath(outputFolder.getAbsolutePath());

            File destFolder = new File(newPath);
            boolean res = false;
            if (options.get(i) == PasteOptionsDialog.OPTION_NONE) {
                res = Tools.CopyFile(f, destFolder, Tools.CopyMode.Exclusive);
            } else if (options.get(i) == PasteOptionsDialog.OPTION_COPY) {
                String newName = new File(Tools.GenerateCopyName(newPath, fName)).getName();
                res = Tools.CopyFile(f, destFolder, newName, Tools.CopyMode.Exclusive);
            } else if (options.get(i) == PasteOptionsDialog.OPTION_SKIP) {
                skipped++;
                continue;
            } else if (options.get(i) == PasteOptionsDialog.OPTION_OVERWRITE
                    || options.get(i) == PasteOptionsDialog.OPTION_MERGE) {
                File outFile = new File(newPath + fName);
                if (outFile.exists()) {
                    if (f.isFile() && outFile.isFile() && options.get(i) == PasteOptionsDialog.OPTION_OVERWRITE) {
                        res = Tools.CopyFile(f, destFolder, Tools.CopyMode.FileOverwrite);
                    } else if (f.isDirectory() && outFile.isDirectory() && options.get(i) == PasteOptionsDialog.OPTION_MERGE) {
                        res = Tools.CopyFile(f, destFolder, Tools.CopyMode.FolderMerge);
                    } else {
                        SanctionBase.LogWarning(MainActivity.Self, "Unknown overwrite operation.");
                        skipped++;
                        continue;
                    }
                } else { // ignore file check, file no longer exists so do normal copy
                    res = Tools.CopyFile(f, destFolder, Tools.CopyMode.Exclusive);
                }
            }

            if (res) {
                success++;
            } else {
                failed++;
            }
        }

        int finalSuccess = success;
        int finalFailed = failed;
        int finalSkipped = skipped;
        MainActivity.Self.txtRoot.post(() -> {
            Tools.Toast(MainActivity.Self, "Success: " + finalSuccess
                            + "\nFailed: " + finalFailed
                            + "\nSkipped: " + finalSkipped,
                    true, false);
        });
        SanctionBase.LogNormal(MainActivity.Self, "Copy operation completed.");
        SanctionBase.LogNormal(MainActivity.Self, "Success: " + success);
        SanctionBase.LogNormal(MainActivity.Self, "Failed: " + failed);
        SanctionBase.LogNormal(MainActivity.Self, "Skipped: " + skipped);
        ProgressDialog.ProgressCompleted = true;
        endOperation();
    }

    private static void cancelOperation() {
        ProgressDialog.ProgressCancelled = false;
        endOperation();
    }

    private static void endOperation() {
        MainActivity.Self.txtRoot.post(() -> {
            MainActivity.Self.refreshList();
            MainActivity.Self.endDestMode();
            MainActivity.Self.finishActivity(MainActivity.REQUEST_PROGRESS);
            MainActivity.Self.overridePendingTransition(0, R.anim.slide_down);
        });
    }

    private static void fileCheck(int index) {
        if (index >= files.size()) {
            startOperation();
            return;
        }

//        if (ProgressDialog.ProgressCancelled) {
//            ProgressDialog.ProgressCancelled = false;
//            SanctionBase.LogNormal(MainActivity.Self, "Copy operation cancelled - progress cancel.");
//            cancelOperation();
//            return;
//        }

        FileListItem s = files.get(index);
        File f = new File(s.ID);
        String oldPath = Tools.DeletePathTrail(f.getAbsolutePath());

//        String oldPathFolder = Tools.ValidFolderPath(oldPath.substring(0, oldPath.lastIndexOf("/") + 1));
        String newPath = Tools.ValidFolderPath(outputFolder.getAbsolutePath());

//        if (oldPathFolder.equalsIgnoreCase(newPath)) {
//            SanctionBase.LogError(MainActivity.Self, "No change in folder location!");
//            options.set(fileIndex, PasteOptionsDialog.OPTION_SKIP);
//            fileCheck(++fileIndex);
//            return;
//        }

        newPath += oldPath.substring(oldPath.lastIndexOf("/") + 1);
        File newFile = new File(newPath);
        if (newFile.exists()) {
            // same filename already exists in destination
            int opType = PasteOptionsDialog.OP_TYPE_FILE_TO_FILE;
            int remOption = rememberedFileOption;
            if (f.isDirectory() && newFile.isFile()) {
                opType = PasteOptionsDialog.OP_TYPE_FOLDER_TO_FILE;
                remOption = rememberedConflictOption;
            } else if (f.isFile() && newFile.isDirectory()) {
                opType = PasteOptionsDialog.OP_TYPE_FILE_TO_FOLDER;
                remOption = rememberedConflictOption;
            } else if (f.isDirectory() && newFile.isDirectory()) {
                opType = PasteOptionsDialog.OP_TYPE_FOLDER_TO_FOLDER;
                remOption = rememberedFolderOption;
            } else if (f.isFile() && newFile.isFile()
                    && f.getAbsolutePath().equalsIgnoreCase(newFile.getAbsolutePath())) {
                opType = PasteOptionsDialog.OP_TYPE_SAME_FILE;
                remOption = rememberedFileOption;
            }

            if (remOption != PasteOptionsDialog.OPTION_NONE
                    && remOption != PasteOptionsDialog.OPTION_ABORT) {
                options.set(fileIndex, remOption);
            } else {
                Intent intent = new Intent(MainActivity.Self, PasteOptionsDialog.class);
                intent.putExtra(PasteOptionsDialog.PARAM_TEXT_ORIGINAL, f.getAbsolutePath());
                intent.putExtra(PasteOptionsDialog.PARAM_TEXT_NEW, newFile.getAbsolutePath());
                intent.putExtra(PasteOptionsDialog.PARAM_OP_TYPE, opType);
                MainActivity.Self.startActivityForResult(intent, MainActivity.REQUEST_PASTE_OPTION_COPY);
                MainActivity.Self.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                return;
            }
        } else {
            options.set(fileIndex, PasteOptionsDialog.OPTION_NONE);
        }

        fileCheck(++fileIndex);
    }
}
