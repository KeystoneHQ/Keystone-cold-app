package com.keystone.cold.cryptocore;

import android.util.Log;

import com.keystone.cold.cryptocore.lib.RCC;
import com.keystone.cold.cryptocore.protocol.PolkadotRequestBuilder;
import com.keystone.cold.cryptocore.protocol.ResponseParser;
import com.keystone.cold.cryptocore.protocol.SolanaRequestBuilder;

import java.util.List;

public class PolkadotService {
    static {
        System.loadLibrary("rcc_android");
    }

    public static String parse(String data, String dbPath) {
        RCC rcc = new RCC();
        PolkadotRequestBuilder rb = new PolkadotRequestBuilder();
        rb.parseTransaction(data, dbPath);
        String command = rb.build();
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public static String handleStub(String dbPath, int checksum) {
        RCC rcc = new RCC();
        PolkadotRequestBuilder rb = new PolkadotRequestBuilder();
        rb.handleStub(dbPath, checksum);
        String command = rb.build();
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public static String getSignContent(String dbPath, int checksum) {
        RCC rcc = new RCC();
        PolkadotRequestBuilder rb = new PolkadotRequestBuilder();
        rb.getSignContent(dbPath, checksum);
        String command = rb.build();
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public static String importAddress(String dbPath, String publicKey, String derivationPath) {
        RCC rcc = new RCC();
        PolkadotRequestBuilder rb = new PolkadotRequestBuilder();
        rb.importAddress(dbPath, publicKey, derivationPath);
        String command = rb.build();
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public static String initialDB(String dbPath) {
        RCC rcc = new RCC();
        PolkadotRequestBuilder rb = new PolkadotRequestBuilder();
        rb.initialDB(dbPath);
        String command = rb.build();
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public static String getPacketsTotal(String scanned) {
        RCC rcc = new RCC();
        PolkadotRequestBuilder rb = new PolkadotRequestBuilder();
        rb.getPacketsTotal(scanned);
        String command = rb.build();
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public static String decodeSequence(List<String> scanned) {
        RCC rcc = new RCC();
        PolkadotRequestBuilder rb = new PolkadotRequestBuilder();
        rb.decodeSequence(scanned);
        String command = rb.build();
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public static String parseResponse(String response) {
        ResponseParser parser = new ResponseParser(response);
        int status = parser.getStatus();
        if (status == 200) {
            response = parser.getResponse();
            Log.d("Polkadot parse RCC response:", response);
            return response;
        } else {
            String error = parser.getError();
            Log.e("Polkadot parse RCC Error:", error);
            return null;
        }
    }

}
