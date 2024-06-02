package com.wavjaby.javaworld.util;

import com.wavjaby.Serializer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static com.wavjaby.SerialClassId.serialClassId;
import static com.wavjaby.javaworld.util.TCPDataWriter.COMPRESS_FLAG;

public class TCPDataReader implements Closeable {
    private static final Logger logger = Logger.getLogger(TCPDataReader.class.getSimpleName());
    private final InputStream in;
    private final ByteArrayOutputStream dataBuff = new ByteArrayOutputStream();
    private final byte[] buffer = new byte[1024];

    private int offset = 0, bufLen = 0;
    private int currentDataLength = 0;
    private byte flag = 0;
    private byte dataLengthReadState = 0;

    public TCPDataReader(InputStream in) {
        this.in = in;
    }

    public byte[] read() throws IOException {
        while (true) {
            // Data buffer is empty, read new data
            if (offset == bufLen) {
                bufLen = in.read(buffer, 0, buffer.length);
                // Disconnected
                if (bufLen == -1)
                    return null;
                offset = 0;
            }

            // Read data length
            if (dataLengthReadState == 0 && offset < bufLen) {
                currentDataLength = buffer[offset++] << 24;
                dataLengthReadState++;
            }
            if (dataLengthReadState == 1 && offset < bufLen) {
                currentDataLength |= buffer[offset++] << 16;
                dataLengthReadState++;
            }
            if (dataLengthReadState == 2 && offset < bufLen) {
                currentDataLength |= buffer[offset++] << 8;
                dataLengthReadState++;
            }
            if (dataLengthReadState == 3 && offset < bufLen) {
                currentDataLength |= buffer[offset++] & 0xFF;
                dataLengthReadState++;
            }
            if (dataLengthReadState == 4 && offset < bufLen) {
                flag = buffer[offset++];
                dataLengthReadState++;
            }
            // Check message read state
            if (dataLengthReadState < 5) {
                continue;
            }

            // Read remaining data
            int readLen = Math.min(currentDataLength - dataBuff.size(), bufLen - offset);
            dataBuff.write(buffer, offset, readLen);
            offset += readLen;

            // Data ready
            if (dataBuff.size() == currentDataLength) {
                dataLengthReadState = 0;
                byte[] data = dataBuff.toByteArray();
                // Decompress if flag set
                if ((flag & COMPRESS_FLAG) != 0)
                    data = decompress(data);

                dataBuff.reset();
                return data;
            }
        }
    }

    public Serializer readObject() throws IOException {
        byte[] data = read();
        if (data == null)
            return null;
        int classId = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | data[3] & 0xFF;
        if (classId >= serialClassId.length)
            return null;
        try {
            return (Serializer) serialClassId[classId].invoke(null, (Object) data);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
        return null;
    }

    private byte[] decompress(byte[] data) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            int len;
            byte[] buff = new byte[1024];
            while ((len = gzip.read(buff)) != -1) {
                buffer.write(buff, 0, len);
            }
            return buffer.toByteArray();
        } catch (IOException ignore) {
        }
        return data;
    }

    @Override
    public void close() throws IOException {
        dataBuff.close();
        in.close();
    }
}
