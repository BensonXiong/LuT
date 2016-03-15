package com.lu.qa.lut.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lu.qa.lut.Model.AppInfoModel;

import java.util.List;


/**
 * Created by xiongzhihui on 16/3/15.
 */
public class ProcessInfo {
    private static final String LOG_TAG = ProcessInfo.class.getName();

    public void getRunningProcess(Context context){
        AppInfoModel appInfoModel = new AppInfoModel();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
        for(ApplicationInfo appinfo:appList){
            
        }

        for(RunningAppProcessInfo appProcessInfo:run){

        }
    }
}
