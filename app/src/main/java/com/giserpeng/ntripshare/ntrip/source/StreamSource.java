package com.giserpeng.ntripshare.ntrip.source;

import java.math.BigDecimal;

public class StreamSource implements NtripSource {

    private String rawLine;

    private SourceType type;
    private String mountPoint;
    private String identifier;
    private RtcmType format;
    private String formatDetail;
    private CarrierType carrier;
    private String navSystem;
    private String network;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private NmeaEnabled nmea;
    private SolutionType solution;
    private String generator;
    private String compressEncrypt;
    private AuthType authentication;
    private FeeType fee;
    private int bitrate;

    public StreamSource(String sourceLine) {
        this.rawLine = sourceLine;

        String[] strArr = sourceLine.split(";");
        this.type = SourceType.getSourceType(strArr[0]);
        this.mountPoint = strArr[1];
        this.identifier = strArr[2];
        this.format = (strArr[3].length() > 0) ? RtcmType.getRtcmType(strArr[3]) : RtcmType.UNKNOWN;
        if (this.format == RtcmType.UNKNOWN) {
            String tid = this.identifier.substring(this.identifier.indexOf("-") + 1);
            this.format = (tid.length() > 0) ? RtcmType.getRtcmTypeByName(tid) : RtcmType.UNKNOWN;
        }
        this.formatDetail = strArr[4];
        this.carrier = (strArr[5].length() > 0) ? CarrierType.getCarrierType(strArr[5]) : CarrierType.UNKNOWN;
        this.navSystem = strArr[6];
        this.network = strArr[7];
        this.country = strArr[8];
        this.latitude = (strArr[9].length() > 0) ? new BigDecimal(strArr[9]) : new BigDecimal("0");
        this.longitude = (strArr[10].length() > 0) ? new BigDecimal(strArr[10]) : new BigDecimal("0");
        this.nmea = (strArr[11].length() > 0) ? NmeaEnabled.getNmeaEnabled(strArr[11]) : NmeaEnabled.UNKNOWN;
        this.solution = (strArr[12].length() > 0) ? SolutionType.getSolutionType(strArr[12]) : SolutionType.UNKNOWN;
        this.generator = strArr[13];
        this.compressEncrypt = strArr[14];
        this.authentication = AuthType.getAuthType(strArr[15]);
        this.fee = FeeType.getFeeType(strArr[16]);
        this.bitrate = (strArr[17].length() > 0) ? Integer.parseInt(strArr[17]) : 0;
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public RtcmType getFormat() {
        return format;
    }

    public void setFormat(RtcmType format) {
        this.format = format;
    }

    public String getFormatDetail() {
        return formatDetail;
    }

    public void setFormatDetail(String formatDetail) {
        this.formatDetail = formatDetail;
    }

    public CarrierType getCarrier() {
        return carrier;
    }

    public void setCarrier(CarrierType carrier) {
        this.carrier = carrier;
    }

    public String getNavSystem() {
        return navSystem;
    }

    public void setNavSystem(String navSystem) {
        this.navSystem = navSystem;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
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

    public NmeaEnabled getNmea() {
        return nmea;
    }

    public void setNmea(NmeaEnabled nmea) {
        this.nmea = nmea;
    }

    public SolutionType getSolution() {
        return solution;
    }

    public void setSolution(SolutionType solution) {
        this.solution = solution;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getCompressEncrypt() {
        return compressEncrypt;
    }

    public void setCompressEncrypt(String compressEncrypt) {
        this.compressEncrypt = compressEncrypt;
    }

    public AuthType getAuthentication() {
        return authentication;
    }

    public void setAuthentication(AuthType authentication) {
        this.authentication = authentication;
    }

    public FeeType getFee() {
        return fee;
    }

    public void setFee(FeeType fee) {
        this.fee = fee;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getRawLine() {
        return rawLine;
    }

    public String getSourceJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{').append('\n');
        sb.append("  type: \"").append(type.name()).append('\"').append(',').append('\n');
        sb.append("  mountPoint: \"").append(mountPoint).append('\"').append(',').append('\n');
        sb.append("  identifier: \"").append(identifier).append('\"').append(',').append('\n');
        sb.append("  format: \"").append(format.name()).append('\"').append(',').append('\n');
        sb.append("  formatDetail: \"").append(formatDetail).append('\"').append(',').append('\n');
        sb.append("  carrier: ").append(carrier.getCarrierId()).append(',').append('\n');
        sb.append("  navSystem: \"").append(navSystem).append('\"').append(',').append('\n');
        sb.append("  network: \"").append(network).append('\"').append(',').append('\n');
        sb.append("  country: \"").append(country).append('\"').append(',').append('\n');
        sb.append("  latitude: ").append(latitude).append(',').append('\n');
        sb.append("  longitude: ").append(longitude).append(',').append('\n');
        sb.append("  nmea: ").append(nmea.getNmeaId()).append(',').append('\n');
        sb.append("  solution: ").append(solution.getSolutionId()).append(',').append('\n');
        sb.append("  generator: \"").append(generator).append('\"').append(',').append('\n');
        sb.append("  compressEncrypt: \"").append(compressEncrypt).append('\"').append(',').append('\n');
        sb.append("  authentication: \"").append(authentication.name()).append('\"').append(',').append('\n');
        sb.append("  fee: \"").append(fee.name()).append('\"').append(',').append('\n');
        sb.append("  bitrate: ").append(bitrate).append('\n');
        sb.append('}').append('\n');
        return sb.toString();
    }

}
