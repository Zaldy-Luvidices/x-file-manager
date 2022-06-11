package com.x.manager.function;

import android.content.Intent;

import com.x.manager.dialog.TextInputDialog;
import com.x.manager.model.FileListItem;
import com.x.manager.utility.SanctionBase;
import com.x.manager.view.MainActivity;

import java.io.File;

public class ChangeExt {
    public static void run(Intent data) {
        for (FileListItem s : MainActivity.Self.selFiles) {
            File f = new File(s.ID);
            String oldPath = f.getAbsolutePath();
            SanctionBase.LogNormal(MainActivity.Self, "Changing extension: " + oldPath);
            String oldName = oldPath.substring(oldPath.lastIndexOf("/") + 1);
            String newName = changeExtension(oldName, data.getStringExtra(TextInputDialog.RETURN_INPUT));
            if (oldName.equals(newName)) {
                SanctionBase.LogWarning(MainActivity.Self, "No change in name");
                continue;
            }

            String newPath = oldPath.substring(0, oldPath.lastIndexOf("/") + 1);
            newPath += newName;
            if (f.renameTo(new File(newPath)))
                SanctionBase.LogInformation(MainActivity.Self, "Success: " + newPath);
            else
                SanctionBase.LogError(MainActivity.Self, "Failed: " + newPath);
        }
    }

    private static String changeExtension(String filename, String newExt) {
        String nameOnly = filename;
        int dot = nameOnly.lastIndexOf(".");
        if (dot > 0) {
            nameOnly = nameOnly.substring(0, dot);
        }
        return nameOnly + "." + newExt;
    }
}
