package android.hardware;

import android.content.Context;

import java.io.IOException;

/**
 * sub class
 */
public class SerialManager {
    public SerialManager(Context context, Object service) {
        throw new RuntimeException();
    }

    public String[] getSerialPorts() {
        throw new RuntimeException();
    }

    public SerialPort openSerialPort(String name, int speed) throws IOException {
        throw new RuntimeException();
    }
}