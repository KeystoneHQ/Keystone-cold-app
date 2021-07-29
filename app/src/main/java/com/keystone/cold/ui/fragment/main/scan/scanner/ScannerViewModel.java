package com.keystone.cold.ui.fragment.main.scan.scanner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.keystone.cold.protocol.ZipUtil;
import com.keystone.cold.protocol.parser.ProtoParser;

import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

public class ScannerViewModel extends AndroidViewModel {
    private ScannerState state;

    public ScannerViewModel(@NonNull Application application) {
        super(application);
    }

    public void setState(ScannerState state) {
        this.state = state;
    }

    public ScannerState getState() {
        return state;
    }

    public void reset() {
        this.state = null;
    }

    public JSONObject decodeProtocolBuffer(byte[] bytes) {
        String hex = Hex.toHexString(bytes);
        JSONObject object;
        hex = ZipUtil.unzip(hex);
        object = new ProtoParser(Hex.decode(hex)).parseToJson();
        return object;
    }
}
