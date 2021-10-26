package com.giserpeng.ntripshare.ntrip.Tools.Decoders;

import com.giserpeng.ntripshare.ntrip.Tools.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;


public class RTCM_3X implements IDecoder {
    private static Logger logger = LoggerFactory.getLogger(RTCM_3X.class.getName());

    private final byte RTCM_PREAMBLE = -45;

    private ByteBuffer previousResidue;

    DecoderType decoderType = DecoderType.RTCM3;

    @Override
    public DecoderType getType() {
        return decoderType;
    }

    public MessagePack separate(ByteBuffer bb) throws IllegalArgumentException {
        MessagePack messagePack = new MessagePack();
        ByteBuffer buffer = bb;

        if (buffer.limit() == 0)
            return messagePack;

        int preamble, shift, nmb;

        if (previousResidue != null) {
            buffer = concatByteBuffer(previousResidue, buffer);
            previousResidue = null;
        }

        if (buffer.get(0) != RTCM_PREAMBLE) {
            errorCounter();
            return messagePack;
        }

        preamble = 0;

        while (buffer.hasRemaining()) {
            shift = buffer.getShort(preamble + 1) + 6 & 0x3FF; // to zero first 6(reserved) bits;
            nmb = (buffer.getShort(preamble + 3) & 0xffff) >> 4;
            buffer.position(preamble);

            if (!checkExistsMessageNumber(nmb)) {
                logger.error("RTCM decode error!");
                break;
            }

            try {
                byte[] msg = new byte[shift];
                buffer.get(msg, 0, shift);
                messagePack.addMessage(nmb, msg);

                if (preamble + shift == buffer.limit())
                    break;

                if (buffer.get(preamble + shift) == RTCM_PREAMBLE) {
                    preamble = preamble + shift;
                } else {
                    logger.error("MISS");
                    break;
                }

                buffer.position(preamble);

            } catch (BufferUnderflowException e) {
                buffer.position(preamble);
                previousResidue = ByteBuffer.allocate(buffer.remaining());
                previousResidue.put(buffer);
                previousResidue.flip();
                logger.debug("stream was cut off");
            }
        }

        return messagePack;
    }

    private boolean checkExistsMessageNumber(int nmb) {
        // 1001-1039
        if (1001 <= nmb && nmb <= 1039)
            return true;

        //1057-1068
        if (1057 <= nmb && nmb <= 1068)
            return true;

        //1071-1230
        if (1071 <= nmb && nmb <= 1230)
            return true;

        //4001-4095
        return 4001 <= nmb && nmb <= 4095;
    }

    private int errorCount = 0;

    private void errorCounter() {
        logger.error("Error counter " + errorCount);
        errorCount++;
        if (errorCount > 10)
            throw new IllegalArgumentException("NO RTCM3.X DATA!");
    }

    public ByteBuffer concatByteBuffer(ByteBuffer residue, ByteBuffer buffer) {
        ByteBuffer b3 = ByteBuffer.allocate(residue.remaining() + buffer.remaining());
        b3.put(residue);
        b3.put(buffer);
        b3.flip();
        return b3;
    }
}
