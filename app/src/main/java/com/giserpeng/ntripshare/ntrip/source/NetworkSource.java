package com.giserpeng.ntripshare.ntrip.source;

public class NetworkSource implements NtripSource {

    private String rawLine;

    private SourceType type;
    private String identifier;
    private String operator;
    private AuthType authentication;
    private FeeType fee;
    private String webNet;
    private String webStr;
    private String webReg;

    public NetworkSource(String sourceLine) {
        this.rawLine = sourceLine;

        String[] strArr = sourceLine.split(";");
        this.type = SourceType.getSourceType(strArr[0]);
        this.identifier = strArr[1];
        this.operator = strArr[2];
        this.authentication = AuthType.getAuthType(strArr[3]);
        this.fee = FeeType.getFeeType(strArr[4]);
        this.webNet = strArr[5];
        this.webStr = strArr[6];
        this.webReg = strArr[7];
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
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

    public String getWebNet() {
        return webNet;
    }

    public void setWebNet(String webNet) {
        this.webNet = webNet;
    }

    public String getWebStr() {
        return webStr;
    }

    public void setWebStr(String webStr) {
        this.webStr = webStr;
    }

    public String getWebReg() {
        return webReg;
    }

    public void setWebReg(String webReg) {
        this.webReg = webReg;
    }

    public String getRawLine() {
        return rawLine;
    }

    public String getSourceJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{').append('\n');
        sb.append("  type: \"").append(type.name()).append('\"').append(',').append('\n');
        sb.append("  identifier: \"").append(identifier).append('\"').append(',').append('\n');
        sb.append("  operator: \"").append(operator).append('\"').append(',').append('\n');
        sb.append("  authentication: \"").append(authentication.name()).append('\"').append(',').append('\n');
        sb.append("  fee: \"").append(fee.name()).append('\"').append(',').append('\n');
        sb.append("  webNet: \"").append(webNet).append('\"').append(',').append('\n');
        sb.append("  webStr: \"").append(webStr).append('\"').append(',').append('\n');
        sb.append("  webReg: \"").append(webReg).append('\"').append('\n');
        sb.append('}').append('\n');
        return sb.toString();
    }
}
