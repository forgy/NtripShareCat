package com.giserpeng.ntripshare.ntrip.Models;

import com.giserpeng.ntripshare.ntrip.Clients.Authentication.Authenticator;
import com.giserpeng.ntripshare.ntrip.Clients.Authentication.Basic;
import com.giserpeng.ntripshare.ntrip.Clients.Authentication.Digest;
import com.giserpeng.ntripshare.ntrip.Clients.Authentication.None;
import com.giserpeng.ntripshare.ntrip.Clients.Client;
import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import com.giserpeng.ntripshare.ntrip.Spatial.PointLla;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MountPointModel {
    final static private Logger logger = LoggerFactory.getLogger(MountPointModel.class.getName());
    private long id;
    private String mountpoint = "";
    private String identifier = "";
    private String format = "";
    private String formatDetails = "";
    private int carrier;
    private String navSystem = "";
    private String network = "";
    private String country = "";
    private PointLla lla;
    private boolean nmea;
    private boolean solution;
    private String generator = "";
    private String compression = "";
    private Authenticator authenticator;
    private boolean fee;
    private int bitrate;
    private String misc = "";
    private boolean available;
    private int casterId;
    private ArrayList<ReferenceStation> stationsPool;
    private int plugin_id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMountpoint() {
        return mountpoint == null ? "" : mountpoint;
    }

    public void setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
    }

    public String getIdentifier() {
        return identifier == null ? "" : identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFormat() {
        return format == null ? "" : format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatDetails() {
        return formatDetails == null ? "" : formatDetails;
    }

    public void setFormatDetails(String formatDetails) {
        this.formatDetails = formatDetails;
    }

    public int getCarrier() {
        return carrier;
    }

    public void setCarrier(int carrier) {
        this.carrier = carrier;
    }

    public String getNavSystem() {
        return navSystem == null ? "" : navSystem;
    }

    public void setNavSystem(String navSystem) {
        this.navSystem = navSystem;
    }

    public String getNetwork() {
        return network == null ? "" : network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getCountry() {
        return country == null ? "" : country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public PointLla getLla() {
        return lla;
    }

    public void setLatitude(Double lat) {
        if (this.lla == null) {
            lla = new PointLla();
        }
        lla.setLat(new BigDecimal(lat));
    }

    public void setLongitude(Double lon) {
        if (this.lla == null) {
            lla = new PointLla();
        }
        lla.setLon(new BigDecimal(lon));
    }

    public boolean isNmea() {
        return nmea;
    }

    public void setNmea(boolean nmea) {
        this.nmea = nmea;
    }

    public boolean isSolution() {
        return solution;
    }

    public void setSolution(boolean solution) {
        this.solution = solution;
    }

    public String getGenerator() {
        return generator == null ? "" : generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getCompression() {
        return compression == null ? "none" : compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public Authenticator getAuthenticator() {
        return this.authenticator;
    }

    public void setAuthenticator(String authenticator) {
        switch (authenticator) {
            case "Basic":
                this.authenticator = new Basic();
                break;
            case "Digest":
                this.authenticator = new Digest();
                break;
            default:
                this.authenticator = new None();
                break;
        }
    }

    public boolean isFee() {
        return fee;
    }

    public void setFee(boolean fee) {
        this.fee = fee;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getMisc() {
        return misc == null ? "none" : misc;
    }

    public void setMisc(String misc) {
        this.misc = misc;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getCasterId() {
        return casterId;
    }

    public void setCasterId(int casterId) {
        this.casterId = casterId;
    }

    public ArrayList<ReferenceStation> getStationsPool() {
        return stationsPool;
    }

    public String getBasesIdsJoin() {
        StringBuilder sb = new StringBuilder();

        for (ReferenceStation referenceStation : stationsPool) {
            sb.append(referenceStation.getId());
        }
        return sb.toString();
    }

    public void setStationsPool(String ids) {
        this.stationsPool = new ArrayList<>();

        for (String id : ids.split(",")) {
            int i_id = Integer.parseInt(id);
            ReferenceStation station = ReferenceStation.getStationById(i_id);
            if (station != null) {
                stationsPool.add(station);
            } else {
                logger.error(mountpoint + ": station by id " + i_id + " not exists.");
            }
        }
    }

    public ReferenceStation initStationPool(){
        this.stationsPool = new ArrayList<>();
        ReferenceStationModel WarszawaStationModel = new ReferenceStationModel();
        WarszawaStationModel.setLla(new PointLla("POINT(117.238056 40.011723)"));
        WarszawaStationModel.setIs_online(true);
        ReferenceStation Warszawa = new ReferenceStation(WarszawaStationModel);
        stationsPool.add(Warszawa);
        return  Warszawa;
    }

    public int getPlugin_id() {
        return plugin_id;
    }

    public void setPlugin_id(int plugin_id) {
        this.plugin_id = plugin_id;
    }

    //todo if not have suitable station need throw exception
    public ReferenceStation getReferenceStation() throws IllegalStateException {

//        if(stationsPool.size() > 0){
//            return  stationsPool.get(0);
//        }
//        return  null;
//        //available have priority
//        for (ReferenceStation referenceStation : stationsPool) {
//            if (referenceStation.available)
//                return referenceStation;
//        }

        //any
        for (ReferenceStation referenceStation : stationsPool) {
            return referenceStation;
        }
        //no one
        throw new IllegalStateException("Not have suitable reference station!");
    }

    /**
     * Return nearest available reference station from station pool.
     * If not have suitable station throw Exception.
     *
     * @param client
     * @return Return nearest reference station for client, from mountpoint station pool.
     */
    public ReferenceStation getNearestReferenceStation(Client client) throws IllegalStateException {
//        TreeMap<Float, ReferenceStation> sortedRange = new TreeMap<>();
//        NMEA.GPSPosition clientPosition = client.getPosition();
//
//        if (clientPosition != null) {
//            for (ReferenceStation station : stationsPool) {
//                if (station.available) {
//                    try {
//                        sortedRange.put(station.distance(clientPosition), station);
//                    } catch (IllegalStateException e) {
//                        logger.error(e.getMessage());
//                    }
//                }
//            }
//        } else {
//            logger.error("Connection " + client.getSocketId() + " hasn't position for nearest reference station!");
//            return null;
//        }
//
//        if (sortedRange.size() > 0) {
//            logger.info("Nearest reference station " + sortedRange.firstEntry().getValue().getName());
//            return sortedRange.firstEntry().getValue();
//        }
//
//        throw new IllegalStateException();
        return  stationsPool.get(0);
    }

    @Override
    public String toString() {
        return "STR" + ';' + getMountpoint() + ';' + getIdentifier() + ';' + getFormat() + ';' + getFormatDetails() + ';' + getCarrier() + ';' + getNavSystem() + ';' + getNetwork() + ';' + getCountry()
                + ';' + String.format("%.2f", getLla().getLat()) + ';' + String.format("%.2f", getLla().getLon()) + ';' + (isNmea() ? 1 : 0) + ';' + (isSolution() ? 1 : 0) + ';' + getGenerator() + ';' + getCompression()
                + ';' + getAuthenticator().toString() + ';' + (isFee() ? 'Y' : 'N') + ';' + getBitrate() + ';' + getMisc() + "\r\n";
    }
}
