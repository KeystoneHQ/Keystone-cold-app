package android.hardware;

import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * sub class
 */
public class SerialPort {

    public SerialPort(String name) {
        throw new RuntimeException();
    }

    public void open(ParcelFileDescriptor pfd, int speed) throws IOException {
        throw new RuntimeException();
    }

    public void close() throws IOException {
        throw new RuntimeException();
    }

    public String getName() {
        throw new RuntimeException();
    }

    public int read(ByteBuffer buffer, int offset) throws IOException {
        throw new RuntimeException();
    }

    public void write(ByteBuffer buffer, int length) throws IOException {
        throw new RuntimeException();
    }

    public void sendBreak() {
        throw new RuntimeException();
    }
}
