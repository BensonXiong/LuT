package com.lu.qa.lut.Common;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benson on 10/20/16.
 * 活动管理器
 */
public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<Activity>();

    public static void addActivity(Activity activity){
        activities.add(activity);
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    public static void finishAll(){
        for (Activity activity : activities){
            if( ! activity.isFinishing()){
                activity.finish();
            }
        }
    }

}
