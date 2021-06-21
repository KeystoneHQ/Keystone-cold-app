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

package com.keystone.cold.selfcheck;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ahmed Moussa on 7/15/18.
 * This is the main class that is responsible for checking if the device is rooted or not
 */
public class RootChecker {

    /**
     * application context to use to search the installed packages for a rooting application
     */
    private Context context;

    /**
     * List of application that can be used to root the device
     * TODO: Make this list populated from a remote DB Source
     */
    private final String[] RootedAPKs = new String[]{
            "com.noshufou.andriod.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.devadvance.rootcloak2",
            "com.zachspong.temprootremovejb",
            "com.ramdroid.appquarantine"
    };

    /**
     * RootChecker Constructor
     *
     * @param context access to package manager which will be used to check if rooted application are installed or not
     */
    public RootChecker(Context context) {
        this.context = context;
    }

    /**
     * Main function to run to check if device is rooted or not
     *
     * @return true if device is rooted, false if not
     */
    public boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3() || checkRootMethod4(this.context);
    }

    /**
     * Device is rooted if the build tags contains the provided tag
     *
     * @return true if build tag exist, false if not
     */
    private boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    /**
     * Device is rooted if the app have access to any of the provided directories
     *
     * @return true if app have access to any of the directories, false if not
     */
    private boolean checkRootMethod2() {
        try {
            String[] paths = {
                    "/system/app/Superuser.apk",
                    "/sbin/su",
                    "/system/bin/su",
                    "/system/xbin/su",
                    "/data/local/xbin/su",
                    "/data/local/bin/su",
                    "/system/sd/xbin/su",
                    "/system/bin/failsafe/su",
                    "/data/local/su",
                    "/su/bin/su",
                    "/sbin/su",
                    "/system/su",
                    "/system/bin/.ext/.su"
            };
            for (String path : paths) {
                if (new File(path).exists()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Run a shell command
     *
     * @return true if command was executed and returned result, false if returned null
     */
    private boolean checkRootMethod3() {
        ExecShell execShell = new ExecShell();
        return execShell.executeCommand(ExecShell.SHELL_CMD.check_su_binary) != null;
    }

    /**
     * Check if the provided packages names are installed on the device or not
     *
     * @param context current application context
     * @return true if any of the provided packages exist, false if none is installed
     */
    private boolean checkRootMethod4(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        for (String s : this.RootedAPKs) {
            Intent intent = packageManager.getLaunchIntentForPackage(s);
            if (intent != null) {
                List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                return list.size() > 0;
            }
        }
        return false;
    }


    /**
     * Created by Ahmed Moussa on 7/15/18.
     * This class represent my interface with the shell commands
     */
    private static class ExecShell {

        /**
         * list of shell commands that I will be suing
         */
        public enum SHELL_CMD {
            check_su_binary(new String[]{"/system/xbin/which", "su"});
            String[] command;

            SHELL_CMD(String[] command) {
                this.command = command;
            }
        }

        /**
         * Run a shell command
         *
         * @param shellCmd Shell command that will be executed
         * @return result of the given shell command
         */
        public ArrayList<String> executeCommand(SHELL_CMD shellCmd) {
            String line;
            ArrayList<String> fullResponse = new ArrayList<>();
            Process localProcess;
            try {
                localProcess = Runtime.getRuntime().exec(shellCmd.command);
            } catch (Exception e) {
                return null;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            try {
                while ((line = in.readLine()) != null) {
                    fullResponse.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return fullResponse;
        }

    }

}
