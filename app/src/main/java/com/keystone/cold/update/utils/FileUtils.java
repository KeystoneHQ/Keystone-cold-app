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

package com.keystone.cold.update.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class FileUtils {
    @NonNull
    public static String readString(@NonNull File file) {
        final StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public static boolean writeString(@NonNull File file, String content) {
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
            fos.getFD().sync();
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable
    public static byte[] bufferlize(@NonNull File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            final byte[] bytes = new byte[inputStream.available()];

            if (bytes.length > 0) {
                final byte[] buffer = new byte[1024];
                int position = 0;
                int read;

                while ((read = inputStream.read(buffer)) > 0) {
                    System.arraycopy(buffer, 0, bytes, position, read);
                    position += read;
                }

                return bytes;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deleteRecursive(@NonNull File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            return;
        }

        if (fileOrDirectory.isDirectory()) {
            final File[] files = fileOrDirectory.listFiles();
            if (files != null && files.length > 0) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }

        fileOrDirectory.delete();
    }

    public static void copyFile(@NonNull File from, @NonNull File to) throws IOException {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);

        try (final FileInputStream inputStream = new FileInputStream(from);
             final FileOutputStream outputStream = new FileOutputStream(to)) {
            final FileChannel inputChannel = inputStream.getChannel();
            final FileChannel outputChannel = outputStream.getChannel();

            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
        }
    }
}
