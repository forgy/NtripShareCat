package com.giserpeng.ntripshare.ntrip.Tools.Decoders;

import com.giserpeng.ntripshare.ntrip.Tools.MessagePack;

import java.nio.ByteBuffer;

public class RAW implements IDecoder {

    @Override
    public MessagePack separate(ByteBuffer bb)  {
        MessagePack messagePack = new MessagePack();
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        messagePack.addMessage(0, bytes);
        return messagePack;
    }

    @Override
    public DecoderType getType() {
        return DecoderType.RAW;
    }
}
