package com.lu.qa.lut.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lu.qa.lut.Utils.CurrentInfo;
import com.lu.qa.lut.Utils.FpsInfo;
import com.lu.qa.lut.Utils.MemoryInfo;
import com.lu.qa.lut.Utils.ProcessInfo;

/**
 * Created by Benson on 10/16/16.
 */
public class LuService extends Service {
    private static final String LOG_TAG = "Lu" + LuService.class.getName();
    private static final String BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";

    private FpsInfo fpsInfo;
    private MemoryInfo memoryInfo;
    private ProcessInfo processInfo;
    private CurrentInfo currentInfo;
    private String totalBatt;
    private String voltage;
    private String temperature;
    private BatteryInfoBroadcastReceiver batteryBroadcast;

    //监听信息入口
    @Override
    public void onCreate() {
        Log.i(LOG_TAG,"LuService onCreate");
        super.onCreate();
        fpsInfo = new FpsInfo();
        memoryInfo = new MemoryInfo();
        processInfo = new ProcessInfo();
        currentInfo = new CurrentInfo();
        batteryBroadcast = new BatteryInfoBroadcastReceiver();
        registerReceiver(batteryBroadcast,new IntentFilter(BATTERY_CHANGED));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class BatteryInfoBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0); //剩余电量
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1); //总电量
                totalBatt = String.valueOf(level * 100 / scale);
                voltage = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1) * 1.0 / 1000);
                temperature = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1) * 1.0 / 10);
            }

        }
    }
}
