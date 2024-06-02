package com.javaworld.util;

import com.wavjaby.serializer.Serializable;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class TCPDataWriter implements Closeable {
    public static final byte COMPRESS_FLAG = 0b00000001;
    public static int COMPRESS_THRESHOLD = 1024;
    private static final Logger logger = Logger.getLogger(TCPDataWriter.class.getSimpleName());
    private final OutputStream out;

    public TCPDataWriter(OutputStream out) {
        this.out = out;
    }

    public void write(byte[] data) throws IOException {
        byte flag = 0;
        if (data.length > COMPRESS_THRESHOLD)
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(buffer);
                gzip.write(data, 0, data.length);
                gzip.finish();
                data = buffer.toByteArray();
                flag = COMPRESS_FLAG;
            } catch (IOException ignore) {
            }
//        logger.info("Send: " + data.length);

        byte[] len = {(byte) (data.length >> 24), (byte) (data.length >> 16), (byte) (data.length >> 8), (byte) (data.length), flag};
        out.write(len, 0, len.length);
        out.write(data, 0, data.length);
    }

    public void write(Serializable object) throws IOException {
        if (object == null) return;
        write(object.serialize());
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
