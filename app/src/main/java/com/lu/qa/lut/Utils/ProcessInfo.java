package com.lu.qa.lut.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.lu.qa.lut.Model.AppInfoModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by xiongzhihui on 16/3/15.
 */
public class ProcessInfo {
    private static final String LOG_TAG = ProcessInfo.class.getName();
    private static final String PACKAGE_NAME = "com.lu.qa.lut";
    private static final int ANDROID_M = 22;

    public List<AppInfoModel> getRunningProcess(Context context) {
        AppInfoModel appInfoModel = new AppInfoModel();
        PackageManager pm = context.getPackageManager();

        List<AppInfoModel> appRunningList = new ArrayList<AppInfoModel>();
        for (ApplicationInfo appinfo : getApplicationList(context)) {
            if (((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) || ((appinfo.processName != null) && (appinfo.processName.equals(PACKAGE_NAME)))) {
                continue;
            }
            for (AndroidAppProcess runningProcess : getRunningAppProcess(context)) {
                if ((runningProcess.name != null) && runningProcess.name.equals(appinfo.processName)) {
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

    public List<AppInfoModel> getInstalledApps(Context context) {
        Log.i(LOG_TAG, "get installed apps");
        List<AppInfoModel> processList = new ArrayList<AppInfoModel>();
        PackageManager pm = context.getPackageManager();
        for (ApplicationInfo appInfo : getApplicationList(context)) {
            AppInfoModel appInfoModel = new AppInfoModel();
            if (((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) || ((appInfo.processName != null) &&
                    (appInfo.processName.equals(PACKAGE_NAME)))) {
                continue;
            }
            appInfoModel.setPackageName(appInfo.processName);
            appInfoModel.setProcessName(appInfo.loadLabel(pm).toString());
            appInfoModel.setIcon(appInfo.loadIcon(pm));
            processList.add(appInfoModel);
        }
        Collections.sort(processList);
        return processList;
    }

    public int getPidByPackageName(Context context, String packageName) {
        Log.i(LOG_TAG, "start to get the launched pid");

        for (AndroidAppProcess runProcessInfo : getRunningAppProcess(context)) {
            if ((runProcessInfo.name != null) && runProcessInfo.name.equals(packageName)) {
                return runProcessInfo.pid;
            }
        }
        return 0;


    }


    public AppInfoModel getProgrameByPackageName(Context context, String packageName) {
        for (AppInfoModel appInfoModel : getRunningProcess(context)) {
            if ((appInfoModel.getPackageName() != null) && appInfoModel.getPackageName().equals(packageName)) {
                return appInfoModel;
            }
        }
        return null;

    }

    public static String getTopActivity(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = am.getRunningTasks(1);
        if(runningTaskInfos != null){
            return (runningTaskInfos.get(0).topActivity).toString();
        }
        return  null;
    }

    /*
    use the third-party library to resolve the problem
    getRunningAppProcesses return itself in API 22
    https://github.com/jaredrummler/AndroidProcesses
     */
    private List<AndroidAppProcess> getRunningAppProcess(Context context) {
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        return processes;
    }


    private List<ApplicationInfo> getApplicationList(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        return pm.getInstalledApplications(0);   // 0 to get all installed packages
    }


}
