package com.keystone.cold.cryptocore;

import android.util.Log;

import com.keystone.cold.cryptocore.lib.RCC;
import com.keystone.cold.cryptocore.protocol.NearRequestBuilder;
import com.keystone.cold.cryptocore.protocol.ResponseParser;

public class NearParser {

    static {
        System.loadLibrary("rcc_android");
    }

    public static String parse(String data) {
        RCC rcc = new RCC();
        String command = composeCommand(data);
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }


    private static String composeCommand(String data) {
        NearRequestBuilder rb = new NearRequestBuilder();
        rb.setData(data);
        return rb.build();
    }

    private static String parseResponse(String response) {
        ResponseParser parser = new ResponseParser(response);
        int status = parser.getStatus();
        if (status == 200) {
            response = parser.getResponse();
            Log.d("near parse reponse:", response);
            return response;
        } else {
            String error = parser.getError();
            Log.e("near parse Error:", error);
            return null;
        }
    }
}
