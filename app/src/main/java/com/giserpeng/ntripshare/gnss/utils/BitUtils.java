package com.giserpeng.ntripshare.gnss.utils;

public class BitUtils {

    private static final int[] LOW = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255};

    private static final long[] POW = new long[63];

    static {
        for (int i = 0; i < POW.length; i++) {
            POW[i] = 1L << i;
        }
    }

    public static long bytesDecode(byte[] data, int startOffset, final int lengthIn) {
        long result = 0;
        int startByte = startOffset / 8;
        int startBit = startOffset % 8;
        int length = lengthIn;
        while (length > 0) {
            if (startBit + length < 8) {
                result += get(data[startByte], length, 8 - startBit - length) << (lengthIn - length);
                break;
            } else {
                result += get(data[startByte], 8 - startBit, startBit) << (lengthIn - length);
                length -= 8 - startBit;
                startBit = 0;
                startByte += 1;
            }
        }
        return result;
    }

    public static long bytesDecodeR(byte[] data, int startOffset, final int lengthIn) {
        long result = 0;
        int startByte = startOffset / 8;
        int startBit = startOffset % 8;
        int length = lengthIn;
        while (length > 0) {
            if (startBit + length < 8) {
                result += get(data[startByte], length, 8 - startBit - length);
                break;
            } else {
                length -= 8 - startBit;
                result += get(data[startByte], 8 - startBit, 0) << length;
                startBit = 0;
                startByte += 1;
            }
        }
        return result;
    }

    public static double bytesDouble(byte[] data, int startOffset, final int lengthIn) {
        return (double) getDouble(bytesDecodeR(data, startOffset, lengthIn), lengthIn);
    }

    public static long bytesToDouble(byte[] data, int startOffset, int length) {
        long result = bytesDecode(data, startOffset, length);
        if ((result >> (length - 1)) == 1) return result - POW[length];
        return result;
    }

    public static long get(byte value, int length, int offset) {
        return (value & LOW[length + offset]) >> offset;
    }

    public static int getLow(byte value, int length) {
        return (LOW[length] & value);
    }

    public static int getHigh(byte value, int length) {
        return (value & 0xFF) >> (8 - length);
    }

    public static final long getDouble(long value, int length) {
        return (value >> (length - 1)) == 0 ? value : value - POW[length];
    }

    public static final long getDouble(double value, int length) {
        return getDouble((long) value, length);
    }

    public static int exor(long data, int length) {
        int result = 0;
        for (int i = 0; i < length; i++, data = data >> 1)
            if ((data & 1) == 1)
                result++;
        return result;
    }
}
