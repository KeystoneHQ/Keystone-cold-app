package com.keystone.cold.cryptocore;

import android.util.Log;

import com.keystone.cold.cryptocore.lib.RCC;
import com.keystone.cold.cryptocore.protocol.CardanoRequestBuilder;
import com.keystone.cold.cryptocore.protocol.ResponseParser;

public class CardanoService {
    public static String deriveAddress(String xpub, int index, int type) {
        RCC rcc = new RCC();
        CardanoRequestBuilder cardanoRequestBuilder = new CardanoRequestBuilder();
        cardanoRequestBuilder.setGenerateAddress(xpub, index, type);
        String command = cardanoRequestBuilder.build();
        String response = rcc.processCommand(command);
        return parseResponse(response);
    }

    public static String parseResponse(String response) {
        ResponseParser parser = new ResponseParser(response);
        int status = parser.getStatus();
        if (status == 200) {
            response = parser.getResponse();
            Log.d("Cardano RCC response:", response);
            return response;
        } else {
            String error = parser.getError();
            Log.e("Cardano RCC Error:", error);
            return null;
        }
    }
}
