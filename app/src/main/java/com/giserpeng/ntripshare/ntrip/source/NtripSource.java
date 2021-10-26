package com.giserpeng.ntripshare.ntrip.source;

public interface NtripSource {

    SourceType getType();

    String getRawLine();

    String getSourceJson();

}
