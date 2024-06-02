package com.wavjaby.serializer;

import java.io.ByteArrayOutputStream;

public abstract class ArrayWriter {
    public static void writeFloatArray(ByteArrayOutputStream dest, float[] src) {
        for (float j : src) {
            int intBit = Float.floatToIntBits(j);
            dest.write(intBit >> 24);
            dest.write(intBit >> 16);
            dest.write(intBit >> 8);
            dest.write(intBit);
        }
    }

    public static float[] readFloatArray(byte[] data, int offset, int intLen) {
        float[] dest = new float[intLen];
        for (int i = 0; i < intLen; i++) {
            int off = offset + (i << 2);
            dest[i] = Float.intBitsToFloat((data[off] << 24) & 0xFF000000 |
                    (data[off + 1] << 16) & 0xFF0000 |
                    (data[off + 2] << 8) & 0xFF00 |
                    data[off + 3] & 0xFF);
        }
        return dest;
    }

    public static void writeByteArray(ByteArrayOutputStream dest, byte[] src) {
        for (byte j : src)
            dest.write(j);
    }

    public static byte[] readByteArray(byte[] data, int offset, int intLen) {
        byte[] dest = new byte[intLen];
        System.arraycopy(data, offset, dest, 0, intLen);
        return dest;
    }

    public static void writeIntArray(ByteArrayOutputStream dest, int[] src) {
        for (int j : src) {
            dest.write(j >> 24);
            dest.write(j >> 16);
            dest.write(j >> 8);
            dest.write(j);
        }
    }

    public static int[] readIntArray(byte[] data, int offset, int intLen) {
        int[] dest = new int[intLen];
        for (int i = 0; i < intLen; i++) {
            int off = offset + (i << 2);
            dest[i] = (data[off] << 24) & 0xFF000000 |
                    (data[off + 1] << 16) & 0xFF0000 |
                    (data[off + 2] << 8) & 0xFF00 |
                    data[off + 3] & 0xFF;
        }
        return dest;
    }

    public static void writeLongArray(ByteArrayOutputStream dest, long[] src) {
        for (long j : src) {
            dest.write((int) (j >> 56));
            dest.write((int) (j >> 48));
            dest.write((int) (j >> 40));
            dest.write((int) (j >> 32));

            dest.write((int) (j >> 24));
            dest.write((int) (j >> 16));
            dest.write((int) (j >> 8));
            dest.write((int) j);
        }
    }

    public static long[] readLongArray(byte[] data, int offset, int intLen) {
        long[] dest = new long[intLen];
        for (int i = 0; i < intLen; i++) {
            int off = offset + (i << 3);
            dest[i] = ((data[off] & 0xFFL) << 56) |
                    ((data[off + 1] & 0xFFL) << 48) |
                    ((data[off + 2] & 0xFFL) << 40) |
                    ((data[off + 3] & 0xFFL) << 32) |

                    ((data[off + 4] & 0xFFL) << 24) |
                    ((data[off + 5] & 0xFF) << 16) |
                    ((data[off + 6] & 0xFF) << 8) |
                    data[off + 7] & 0xFF;
        }
        return dest;
    }
}
