package com.keystone.coinlib.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.keystone.coinlib.Coinlib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class FileUtil {
    public static void writeToFile(String filepath, String content) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath))) {
            bufferedWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(String filepath) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bfr = new BufferedReader(new FileReader(filepath))) {
            String line = bfr.readLine();
            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
                line = bfr.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
