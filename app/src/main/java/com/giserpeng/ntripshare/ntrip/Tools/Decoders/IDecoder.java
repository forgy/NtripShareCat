package com.giserpeng.ntripshare.ntrip.Tools.Decoders;

import com.giserpeng.ntripshare.ntrip.Tools.MessagePack;

import java.nio.ByteBuffer;

public interface IDecoder {

    MessagePack separate(ByteBuffer bb) throws IllegalArgumentException;

    DecoderType getType();

}
