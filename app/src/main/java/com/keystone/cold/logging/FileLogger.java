/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.logging;

import android.content.Context;
import android.util.Log;

import com.keystone.cold.update.utils.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileLogger {
    private static final String TAG = "Vault.FileLogger";
    private static final String DATE_FORMAT = "yyyyMMddHHmmssSSS";
    private static final long LOG_PERSIST_LIMIT = 20;
    private static final String LOG_PATTERN = "^(main|system|crash)\\.(\\d+)\\.log$";

    public static void init(Context context) {
        File logDir = getLogDir(context);
        long logSequence = getLogSequence(context);
        File keystoneLog = new File(logDir, "main." + logSequence + ".log");
        File sysLog = new File(logDir, "system." + logSequence + ".log");
        File crashLog = new File(logDir, "crash." + logSequence + ".log");

        try {
            Runtime.getRuntime().exec("logcat -c");
            Runtime.getRuntime().exec("logcat -f " + keystoneLog + " -b main *:I");
            Runtime.getRuntime().exec("logcat -f " + sysLog + " -b system *:E");
            Runtime.getRuntime().exec("logcat -f " + crashLog + " -b crash");
            int pid = android.os.Process.myPid();
            Log.i(TAG, "Package: " + context.getPackageName() + " Pid: " + pid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean exportLogfiles(Context context) {
        Storage storage = Storage.createByEnvironment();
        if (storage == null) {
            Log.e(TAG, "failed to export log files, removable storage not found");
            return false;
        }
        File removableStorage = storage.getExternalDir();
        if (removableStorage == null) {
            Log.e(TAG, "failed to export log files, removable storage not found");
            return false;
        }
        Log.i(TAG, "export log files: " + removableStorage);
        File exportFile = new File(removableStorage, "logs-" + getTimestamp() + ".zip");
        return compressLogs(getLogDir(context), exportFile);
    }

    public static void purgeLogs(Context context) {
        long logSequence = getLogSequence(context);
        File logDir = getLogDir(context);
        File[] files = logDir.listFiles();
        if (files != null) {
            Pattern pattern = Pattern.compile(LOG_PATTERN);
            for (File f : files) {
                Matcher matcher = pattern.matcher(f.getName());
                if (matcher.matches()) {
                    try {
                        long seq = Long.parseLong(matcher.group(2));
                        if (seq < logSequence - LOG_PERSIST_LIMIT) {
                            f.delete();
                        }
                    } catch (SecurityException | NumberFormatException e) {
                        Log.e(TAG, "failed to delete log file: " + e.toString());
                    }
                }
            }
        }
    }

    private static File getLogDir(Context context) {
        String cacheDir = "/storage/emulated/0/Android/data/" + context.getPackageName() + "/cache";
        File logDir = new File(cacheDir + "/logs");
        if (!logDir.exists()) {
            context.getExternalCacheDir();
            logDir.mkdir();

        }
        Log.d(TAG, logDir.getAbsolutePath());
        return logDir;
    }

    private static long getLogSequence(Context context) {
        long maxSequence = -1;
        File logDir = getLogDir(context);
        File[] files = logDir.listFiles();
        if (files != null) {
            Pattern pattern = Pattern.compile(LOG_PATTERN);
            for (File f : files) {
                Matcher matcher = pattern.matcher(f.getName());
                if (matcher.matches()) {
                    try {
                        long seq = Long.parseLong(matcher.group(2));
                        maxSequence = Math.max(seq, maxSequence);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return maxSequence + 1;
    }

    private static String getTimestamp() {
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return formatter.format(new Date());
    }

    private static boolean compressLogs(File dir, File outputFile) {
        Log.d(TAG, dir.getAbsolutePath());
        Log.d(TAG, outputFile.getAbsolutePath());
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    byte[] buffer = new byte[1024];
                    FileInputStream fis = new FileInputStream(f);
                    zos.putNextEntry(new ZipEntry(f.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                    fis.close();
                }
            }
            zos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
