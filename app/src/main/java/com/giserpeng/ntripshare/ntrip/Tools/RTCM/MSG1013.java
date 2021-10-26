package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

public class MSG1013 extends RTCM {
    private int messageNumber;
    private int stationID;
    private int MJD; //Modified Julian Day Number
    private int UTC; //Seconds of Day UTC
    private int LeapSeconds; // Leap Seconds, GPS-UTC
    private Message[] list;


    public MSG1013(byte[] msg) {
        super.rawMsg = msg;
        super.setToBinaryBuffer(msg);

        messageNumber = toUnsignedInt(getBits(16, 12));
        stationID = toUnsignedInt(getBits(28, 12));
        MJD = toUnsignedInt(getBits(40, 16));
        UTC = toUnsignedInt(getBits(56, 17));
        int messageCounter = toUnsignedInt(getBits(73, 5));
        list = new Message[messageCounter];
        LeapSeconds = toUnsignedInt(getBits(78, 8));//86

        for (int i = 0; i < messageCounter; i++) {
            int shift = i * 29;
            Message m = new Message();

            m.setMessageID(toUnsignedInt(getBits(89 + shift, 12)));
            m.setSyncFlag(toUnsignedInt(getBits(101 + shift, 1)));
            m.setTransmissionInterval(toUnsignedInt(getBits(102 + shift, 16)));

            list[i] = m;
        }

    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public int getStationID() {
        return stationID;
    }

    public int getMJD() {
        return MJD;
    }

    public int getUTC() {
        return UTC;
    }

    public int getLeapSeconds() {
        return LeapSeconds;
    }

    public Message[] getList() {
        return list;
    }

    public class Message {
        private int MessageID;
        private int SyncFlag;
        private int TransmissionInterval;

        public int getMessageID() {
            return MessageID;
        }

        public void setMessageID(int messageID) {
            MessageID = messageID;
        }

        public int getSyncFlag() {
            return SyncFlag;
        }

        public void setSyncFlag(int syncFlag) {
            SyncFlag = syncFlag;
        }

        public int getTransmissionInterval() {
            return TransmissionInterval;
        }

        public void setTransmissionInterval(int transmissionInterval) {
            TransmissionInterval = transmissionInterval;
        }

    }
}


