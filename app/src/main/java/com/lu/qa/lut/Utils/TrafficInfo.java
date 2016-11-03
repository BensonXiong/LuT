package com.lu.qa.lut.Utils;

import android.net.TrafficStats;
import android.util.Log;

/**
 * Created by Benson on 10/10/16.
 */
public class TrafficInfo {
    private static final String LOG_TAG = "LuT"+ TrafficInfo.class.getName();
    private static final int UNSUPPORTED = -1;
    private String uid;

    public TrafficInfo(String uid){
        this.uid = uid;
    }
    public long getTrafficInfo(){
        Log.i(LOG_TAG,"get traffic info");
        long rcvTraffic = UNSUPPORTED;
        long sndTraffic = UNSUPPORTED;

        rcvTraffic = TrafficStats.getUidRxBytes(Integer.parseInt(uid));
        sndTraffic = TrafficStats.getUidTxBytes(Integer.parseInt(uid));

        if ( rcvTraffic == UNSUPPORTED || sndTraffic == UNSUPPORTED){
            return UNSUPPORTED;
        }
        else
        {
            return rcvTraffic + sndTraffic;
        }

    }
}
