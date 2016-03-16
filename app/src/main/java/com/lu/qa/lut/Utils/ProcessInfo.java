package com.lu.qa.lut.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lu.qa.lut.Model.AppInfoModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by xiongzhihui on 16/3/15.
 */
public class ProcessInfo {
    private static final String LOG_TAG = ProcessInfo.class.getName();
    private static final String PACKAGE_NAME = "com.lu.qa.lut";

    public List<AppInfoModel> getRunningProcess(Context context){
        AppInfoModel appInfoModel = new AppInfoModel();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
        List<AppInfoModel> appRunningList = new ArrayList<AppInfoModel>();
        for(ApplicationInfo appinfo:appList){
            if( ((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) || ((appinfo.processName != null) && (appinfo.processName.equals(PACKAGE_NAME))))
            {
                    continue;
            }
            for(RunningAppProcessInfo runningProcess:run){
                if ((runningProcess.processName != null) && runningProcess.processName.equals(appinfo.processName)){
                    appInfoModel.setPid(runningProcess.pid);
                    appInfoModel.setUid(runningProcess.uid);
                    break;
                }
            }
            appInfoModel.setPackageName(appinfo.packageName);
            appInfoModel.setProcessName(appinfo.loadLabel(pm).toString());
            appInfoModel.setIcon(appinfo.loadIcon(pm));
            appRunningList.add(appInfoModel);
        }
        Collections.sort(appRunningList);

        return appRunningList;
    }
}
