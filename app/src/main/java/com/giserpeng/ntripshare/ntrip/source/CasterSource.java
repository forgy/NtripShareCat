package com.giserpeng.ntripshare.ntrip.source;

import java.math.BigDecimal;

public class CasterSource implements NtripSource {

    private String rawLine;

    private SourceType type;
    private String host;
    private int port;
    private String identifier;
    private String operator;
    private NmeaEnabled nmea;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String fallbackHost;
    private int fallbackPort;

    public CasterSource(String sourceLine) {
        this.rawLine = sourceLine;

        String[] strArr = sourceLine.split(";");
        this.type = SourceType.getSourceType(strArr[0]);
        this.host = strArr[1];
        this.port = Integer.parseInt(strArr[2]);
        this.identifier = strArr[3];
        this.operator = strArr[4];
        this.nmea = NmeaEnabled.getNmeaEnabled(strArr[5]);
        this.country = strArr[6];
        this.latitude = new BigDecimal(strArr[7]);
        this.longitude = new BigDecimal(strArr[8]);
        this.fallbackHost = strArr[9];
        this.fallbackPort = Integer.parseInt(strArr[10]);
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public NmeaEnabled getNmea() {
        return nmea;
    }

    public void setNmea(NmeaEnabled nmea) {
        this.nmea = nmea;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getFallbackHost() {
        return fallbackHost;
    }

    public void setFallbackHost(String fallbackHost) {
        this.fallbackHost = fallbackHost;
    }

    public int getFallbackPort() {
        return fallbackPort;
    }

    public void setFallbackPort(int fallbackPort) {
        this.fallbackPort = fallbackPort;
    }

    public String getRawLine() {
        return rawLine;
    }

    public String getSourceJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{').append('\n');
        sb.append("  type: \"").append(type.name()).append('\"').append(',').append('\n');
        sb.append("  host: \"").append(host).append('\"').append(',').append('\n');
        sb.append("  port: ").append(port).append(',').append('\n');
        sb.append("  identifier: \"").append(identifier).append('\"').append(',').append('\n');
        sb.append("  operator: \"").append(operator).append('\"').append(',').append('\n');
        sb.append("  nmea: ").append(nmea.getNmeaId()).append(',').append('\n');
        sb.append("  country: \"").append(country).append('\"').append(',').append('\n');
        sb.append("  latitude: ").append(latitude).append(',').append('\n');
        sb.append("  longitude: ").append(longitude).append(',').append('\n');
        sb.append("  fallbackHost: \"").append(fallbackHost).append('\"').append(',').append('\n');
        sb.append("  fallbackPort: ").append(fallbackPort).append('\n');
        sb.append('}').append('\n');
        return sb.toString();
    }

}
