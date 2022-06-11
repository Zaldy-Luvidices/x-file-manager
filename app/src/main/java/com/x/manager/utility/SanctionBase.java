package com.x.manager.utility;

import java.io.*;

import android.os.*;

import java.util.*;
import java.text.*;

import android.content.*;

public class SanctionBase {
    public static final String SEPARATOR_LOG = "~";
    public static final String ID_NORMAL = "N";
    public static final String ID_INFO = "I";
    public static final String ID_WARNING = "W";
    public static final String ID_ERROR = "E";

    public static final String SETT_FILENAME = "set.dat";
    public static final String SEPARATOR_SETTINGS = ";";
    public static final int SETT_IND_DOT_HIDDEN = 0;

    private static String basePath;
    private static String logPath;
    private static String dataPath;
    private static FileOutputStream logOutStream;
    private static boolean newLogCreated;

    public static boolean IsHiddenDotShown = true;

    public enum LogType {
        NORMAL, // white
        INFORMATION, // blue
        WARNING, // orange
        ERROR, // red
    }

    private static boolean baseExists() {
        File f = new File(basePath);
        if (f.exists()) return true;
        return f.mkdirs();
    }

    private static boolean logFileExists() {
        if (!baseExists()) return false;
        logPath = basePath + ".logs" + File.separator;
        File f = new File(logPath);
        boolean success = false;
        if (!f.exists()) {
            success = f.mkdirs();
            if (!success) return false;
        }

        Date today = Calendar.getInstance(Locale.getDefault()).getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        logPath += sdf.format(today) + ".src";
        f = new File(logPath);
        newLogCreated = false;
        if (!f.exists()) {
            try {
                success = f.createNewFile();
                if (success) newLogCreated = true;
                return success;
            } catch (IOException ex) {
                return false;
            }
        }
        return true;
    }

    private static boolean settingsFileExists() {
        if (!baseExists()) return false;
        dataPath = basePath + ".data" + File.separator;
        File f = new File(dataPath);
        boolean success = false;
        if (!f.exists()) {
            success = f.mkdirs();
            if (!success) return false;
        }

        dataPath = dataPath + SETT_FILENAME;
        f = new File(dataPath);
        if (!f.exists()) {
            try {
                success = f.createNewFile();
                if (success) SettingsWriteValues(true);
                return success;
            } catch (IOException ex) {
                return false;
            }
        }
        return true;
    }

    private static boolean openLogStream(Context context) {
        if (!logFileExists()) return false;
        if (newLogCreated) {
            if (logOutStream != null) {
                try {
                    logOutStream.flush();
                    logOutStream.close();
                    logOutStream = null;
                } catch (IOException ex) {
                    return false;
                }
            }
        }
        if (logOutStream == null) {
            try {
                logOutStream = new FileOutputStream(new File(logPath), true);
            } catch (IOException ex) {
                return false;
            }
        }
        if (newLogCreated) {
            String name = new File(logPath).getName();
            name = name.substring(0, name.length() - 4);
            try {
                logOutStream.write((name + "\n").getBytes());
            } catch (IOException ex) {
            }
        }
        return true;
    }

    public static void Initialize() {
        basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!basePath.endsWith(File.separator))
            basePath += File.separator;
        basePath += ".com" + File.separator;
        basePath += ".src" + File.separator;
        logOutStream = null;
    }

    public static boolean WriteLog(Context context, LogType lType, String text) {
        if (!openLogStream(context)) return false;
        Date now = Calendar.getInstance(Locale.getDefault()).getTime();
        String line = ID_NORMAL;
        if (lType == LogType.INFORMATION) line = ID_INFO;
        else if (lType == LogType.WARNING) line = ID_WARNING;
        else if (lType == LogType.ERROR) line = ID_ERROR;
        line += SEPARATOR_LOG + (new SimpleDateFormat("hh:mm:ss a").format(now)) +
                SEPARATOR_LOG + text + "\n";
        try {
            logOutStream.write(line.getBytes());
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public static File[] GetLogsList() {
        File[] logs = new File[]{};
        File source = new File(basePath, ".logs");
        if (!source.exists()) return logs;
        logs = source.listFiles();
        if (logs == null) logs = new File[]{};
        return logs;
    }

    public static boolean SettingsLoadValues() {
        if (!settingsFileExists()) return false;

        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(new File(dataPath)));
            String all = reader.readLine();
            reader.close();
            String[] setts = all.split(SEPARATOR_SETTINGS);
            IsHiddenDotShown = setts[0].equals("1");

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean SettingsWriteValues(boolean defs) {
        if (!defs && !settingsFileExists()) return false;

        try {
            String dat = IsHiddenDotShown ? "1" : "0";
            FileOutputStream out = new FileOutputStream(
                    new File(dataPath), false);
            out.write(dat.getBytes());
            out.flush();
            out.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void LogNormal(Context context, String txt) {
        SanctionBase.WriteLog(context, SanctionBase.LogType.NORMAL, txt);
    }

    public static void LogInformation(Context context, String txt) {
        SanctionBase.WriteLog(context, SanctionBase.LogType.INFORMATION, txt);
    }

    public static void LogWarning(Context context, String txt) {
        SanctionBase.WriteLog(context, SanctionBase.LogType.WARNING, txt);
    }

    public static void LogError(Context context, String txt) {
        SanctionBase.WriteLog(context, SanctionBase.LogType.ERROR, txt);
    }
}
