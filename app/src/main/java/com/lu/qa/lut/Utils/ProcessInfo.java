package com.lu.qa.lut.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import java.util.List;


/**
 * Created by xiongzhihui on 16/3/15.
 */
public class ProcessInfo {
    private static final String LOG_TAG = ProcessInfo.class.getName();

    public void getRunningProcess(Context context){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
        for(RunningAppProcessInfo appProcessInfo:run){

        }
    }
}
