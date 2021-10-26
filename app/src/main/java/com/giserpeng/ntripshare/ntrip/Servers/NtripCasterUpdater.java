package com.giserpeng.ntripshare.ntrip.Servers;


import com.giserpeng.ntripshare.ntrip.Models.MountPointModel;
import com.giserpeng.ntripshare.ntrip.Models.NtripCasterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/* Contains methods for update caster */
public abstract class NtripCasterUpdater {
    final static private Logger logger = LoggerFactory.getLogger(NtripCasterUpdater.class.getName());
    protected static Timer timer = new Timer();
    protected NtripCasterModel model;
    protected HashMap<String, MountPointModel> mountPoints = new HashMap<>();

    protected abstract void close();
}
