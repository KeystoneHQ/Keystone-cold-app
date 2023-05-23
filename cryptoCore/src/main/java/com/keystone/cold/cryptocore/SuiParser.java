package com.keystone.cold.cryptocore;

import android.util.Log;

import com.keystone.cold.cryptocore.lib.RCC;
import com.keystone.cold.cryptocore.protocol.AptosRequestBuilder;
import com.keystone.cold.cryptocore.protocol.ResponseParser;
import com.keystone.cold.cryptocore.protocol.SuiRequestBuilder;

public class SuiParser {

    static {
        System.loadLibrary("rcc_android");
    }

    public static String parseTransaction(String data) {
        return parse(data, SuiRequestBuilder.Type.Transaction);
    }

    public static String parseMessage(String data) {
        return parse(data, SuiRequestBuilder.Type.Message);
    }

    public static String parse(String data, SuiRequestBuilder.Type type) {
        RCC rcc = new RCC();
        String command = composeCommand(data, type);
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    private static String composeCommand(String data, SuiRequestBuilder.Type type) {
        SuiRequestBuilder rb = new SuiRequestBuilder();
        rb.setData(data);
        return rb.build(type);
    }

    private static String parseResponse(String response) {
        ResponseParser parser = new ResponseParser(response);
        int status = parser.getStatus();
        if (status == 200) {
            response = parser.getResponse();
            Log.d("aptos parse response:", response);
            return response;
        } else {
            String error = parser.getError();
            Log.e("aptos parse Error:", error);
            return null;
        }
    }
}
