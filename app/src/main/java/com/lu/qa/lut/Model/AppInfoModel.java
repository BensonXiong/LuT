package com.lu.qa.lut.Model;

import android.graphics.drawable.Drawable;

import java.util.Comparator;

/**
 * Created by xiongzhihui on 16/3/15.
 */
public class AppInfoModel implements Comparable<AppInfoModel> {
    private Drawable icon;
    private String processName;
    private String packageName;
    private int pid;
    private int uid;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Override
    public int compareTo(AppInfoModel arg0) {
        return (this.getProcessName().compareTo(arg0.getProcessName()));
    }
}
