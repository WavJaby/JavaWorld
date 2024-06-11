package com.wavjaby.serializer;

import java.io.ByteArrayOutputStream;

public abstract class ArrayWriter {
    public static void writeFloatArray(ByteArrayOutputStream dest, float[] src) {
        if (src == null) return;
        for (float j : src) {
            int intBit = Float.floatToIntBits(j);
            dest.write(intBit >> 24);
            dest.write(intBit >> 16);
            dest.write(intBit >> 8);
            dest.write(intBit);
        }
    }

    public static float[] readFloatArray(byte[] data, int offset, int len) {
        if (len == -1) return null;
        float[] dest = new float[len];
        for (int i = 0; i < len; i++) {
            int off = offset + (i << 2);
            dest[i] = Float.intBitsToFloat((data[off] << 24) & 0xFF000000 |
                    (data[off + 1] << 16) & 0xFF0000 |
                    (data[off + 2] << 8) & 0xFF00 |
                    data[off + 3] & 0xFF);
        }
        return dest;
    }

    public static void writeDoubleArray(ByteArrayOutputStream dest, double[] src) {
        if (src == null) return;
        for (double j : src) {
            long longBit = Double.doubleToRawLongBits(j);
            dest.write((int) (longBit >> 56));
            dest.write((int) (longBit >> 48));
            dest.write((int) (longBit >> 40));
            dest.write((int) (longBit >> 32));

            dest.write((int) (longBit >> 24));
            dest.write((int) (longBit >> 16));
            dest.write((int) (longBit >> 8));
            dest.write((int) longBit);
        }
    }

    public static double[] readDoubleArray(byte[] data, int offset, int len) {
        if (len == -1) return null;
        double[] dest = new double[len];
        for (int i = 0; i < len; i++) {
            int off = offset + (i << 3);
            dest[i] = Double.longBitsToDouble(((data[off] & 0xFFL) << 56) |
                    ((data[off + 1] & 0xFFL) << 48) |
                    ((data[off + 2] & 0xFFL) << 40) |
                    ((data[off + 3] & 0xFFL) << 32) |

                    ((data[off + 4] & 0xFFL) << 24) |
                    ((data[off + 5] & 0xFF) << 16) |
                    ((data[off + 6] & 0xFF) << 8) |
                    data[off + 7] & 0xFF);
        }
        return dest;
    }

    public static void writeBooleanArray(ByteArrayOutputStream dest, boolean[] src) {
        if (src == null) return;
        for (boolean j : src) dest.write(j ? 1 : 0);
    }

    public static boolean[] readBooleanArray(byte[] data, int offset, int len) {
        if (len == -1) return null;
        boolean[] dest = new boolean[len];
        for (int i = 0; i < len; i++) dest[i] = data[offset + i] == (byte) 1;
        return dest;
    }

    public static void writeCharArray(ByteArrayOutputStream dest, char[] src) {
        if (src == null) return;
        for (char j : src) dest.write(j);
    }

    public static char[] readCharArray(byte[] data, int offset, int len) {
        if (len == -1) return null;
        char[] dest = new char[len];
        for (int i = 0; i < len; i++) dest[i] = (char) data[offset + i];
        return dest;
    }

    public static void writeByteArray(ByteArrayOutputStream dest, byte[] src) {
        if (src == null) return;
        for (byte j : src) dest.write(j);
    }

    public static byte[] readByteArray(byte[] data, int offset, int len) {
        if (len == -1) return null;
        byte[] dest = new byte[len];
        System.arraycopy(data, offset, dest, 0, len);
        return dest;
    }

    public static void writeShortArray(ByteArrayOutputStream dest, short[] src) {
        if (src == null) return;
        for (short j : src) {
            dest.write(j >> 8);
            dest.write(j);
        }
    }

    public static short[] readShortArray(byte[] data, int offset, int len) {
        if (len == -1) return null;
        short[] dest = new short[len];
        for (int i = 0; i < len; i++) {
            int off = offset + (i << 1);
            dest[i] = (short) ((data[off] << 8) & 0xFF00 | data[off + 1] & 0xFF);
        }
        return dest;
    }

    public static void writeIntArray(ByteArrayOutputStream dest, int[] src) {
        if (src == null) return;
        for (int j : src) {
            dest.write(j >> 24);
            dest.write(j >> 16);
            dest.write(j >> 8);
            dest.write(j);
        }
    }

    public static int[] readIntArray(byte[] data, int offset, int len) {
        if (len == -1) return null;
        int[] dest = new int[len];
        for (int i = 0; i < len; i++) {
            int off = offset + (i << 2);
            dest[i] = (data[off] << 24) & 0xFF000000 |
                    (data[off + 1] << 16) & 0xFF0000 |
                    (data[off + 2] << 8) & 0xFF00 |
                    data[off + 3] & 0xFF;
        }
        return dest;
    }

    public static void writeLongArray(ByteArrayOutputStream dest, long[] src) {
        if (src == null) return;
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

    public static long[] readLongArray(byte[] data, int offset, int len) {
        if (len == -1) return null;
        long[] dest = new long[len];
        for (int i = 0; i < len; i++) {
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
