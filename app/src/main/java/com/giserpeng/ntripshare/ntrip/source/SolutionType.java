package com.giserpeng.ntripshare.ntrip.source;

public enum SolutionType {
    UNKNOWN(-1), SINGLE(0), NETWORK(1);

    private int solutionId;

    SolutionType(int solutionId) {
        this.solutionId = solutionId;
    }

    public static SolutionType getSolutionType(int id) {
        SolutionType rc;

        switch (id) {
            case 0:
                rc = SINGLE;
                break;
            case 1:
                rc = NETWORK;
                break;
            default:
                rc = UNKNOWN;
                break;
        }

        return rc;
    }

    public static SolutionType getSolutionType(String id) {
        SolutionType rc = UNKNOWN;
        int nId = Integer.parseInt(id);
        return getSolutionType(nId);
    }

    public int getSolutionId() {
        return solutionId;
    }

}
