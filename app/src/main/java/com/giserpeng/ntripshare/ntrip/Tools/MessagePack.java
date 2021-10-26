package com.giserpeng.ntripshare.ntrip.Tools;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class MessagePack {
    ArrayList<Map.Entry<Integer, byte[]>> messagePack = new ArrayList<>();

    public ArrayList<Map.Entry<Integer, byte[]>> getArray() {
        return messagePack;
    }

    public void addMessage(final int nmb, final byte[] bytes) {
        messagePack.add(new Map.Entry<Integer, byte[]>() {
            @Override
            public Integer getKey() {
                return nmb;
            }

            @Override
            public byte[] getValue() {
                return bytes;
            }

            @Override
            public byte[] setValue(byte[] value) {
                return value;
            }
        });
    }

    public Map.Entry<Integer, byte[]> getMessageByNmb(int nmb) {
        for (Map.Entry<Integer, byte[]> entry : messagePack) {
            if (entry.getKey() == nmb)
                return entry;
        }
        return null;
    }

    public void removeMessage(int nmb) {
        messagePack.removeIf(msg -> msg.getKey() == nmb);
    }

    public ByteBuffer getFullBytes() {
        int capacity = 0;
        for (Map.Entry<Integer, byte[]> msg : messagePack) {
            capacity += msg.getValue().length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        for (Map.Entry<Integer, byte[]> msg : messagePack) {
            buffer.put(msg.getValue());
        }

        return buffer;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (Map.Entry<Integer, byte[]> msg : messagePack) {
            stringBuilder.append(msg.getKey());
            stringBuilder.append(" have ");
            stringBuilder.append(msg.getValue().length);
            stringBuilder.append(" bytes, ");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public int size() {
        return messagePack.size();
    }
}
