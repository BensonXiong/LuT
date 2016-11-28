package com.lu.qa.lut.Service;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lu.qa.lut.MainActivity;
import com.lu.qa.lut.Model.AppInfoModel;
import com.lu.qa.lut.R;
import com.lu.qa.lut.Utils.Constants;
import com.lu.qa.lut.Utils.CpuInfo;
import com.lu.qa.lut.Utils.CurrentInfo;
import com.lu.qa.lut.Utils.FpsInfo;
import com.lu.qa.lut.Utils.MemoryInfo;
import com.lu.qa.lut.Utils.ProcessInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

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

    public static final String SERVICE_ACTION = "com.lu.action.LuService";
    private int pid;
    private String processName;
    private String packageName;
    private String startActivity;
    private boolean isFloating;
    private View floatingView;
    private TextView txtUnusedMem;
    private TextView txtTotalMem;
    private TextView txtTraffic;
    private Button btnWifi;
    private WifiManager wifiManager;
    private Button btnStp;
    private WindowManager windowManager;
    private WindowManager.LayoutParams wmParams;
    private float x;
    private float y;
    private float mTouchStartX;
    private float mTouchStartY;
    private int statusBarHeight;
    private String resultFilePath;
    private FileOutputStream out;
    private OutputStreamWriter osw;

    private CpuInfo cpuInfo;
    private int uid;
    private boolean isRoot;
    private android.os.Handler handler = new android.os.Handler();
    private boolean isServiceStop = false;
    private long delaytime;

    // get start time
    private static final int MAX_START_TIME_COUNT = 5;
    private static final String START_TIME = "#startTime";
    private int getStartTimeCount = 0;
    private boolean isGetStartTime = true;
    private String startTime = "";
    private DecimalFormat formatter;


    private static final String BLANK_STRING = "";
    private boolean isAutoStop = false;
    private boolean isStop = false;


    public static  BufferedWriter bw;


    //监听信息入口
    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "LuService onCreate");
        super.onCreate();
        isServiceStop = false;
        formatter = new DecimalFormat();
        formatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        formatter.setGroupingUsed(false);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(0);
        fpsInfo = new FpsInfo();
        memoryInfo = new MemoryInfo();
        processInfo = new ProcessInfo();
        currentInfo = new CurrentInfo();
        batteryBroadcast = new BatteryInfoBroadcastReceiver();
        statusBarHeight = getStatusBarHeight();

        registerReceiver(batteryBroadcast, new IntentFilter(BATTERY_CHANGED));

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "service onStart Command");
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0, new Intent(this, MainActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.icon)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("Lu");
        startForeground(startId, builder.build());

        pid = intent.getExtras().getInt("pid");
        processName = intent.getExtras().getString("processName");
        packageName = intent.getExtras().getString("packageName");
        startActivity = intent.getExtras().getString("startActivity");

        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES);
            uid = applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        cpuInfo = new CpuInfo(getBaseContext(),pid, Integer.toString(uid));
        readSettingInfo();
        if (isFloating) {
            floatingView = LayoutInflater.from(this).inflate(R.layout.floating, null);
            txtUnusedMem = (TextView) floatingView.findViewById(R.id.memunused);
            txtTotalMem = (TextView) floatingView.findViewById(R.id.memtotal);
            txtTraffic = (TextView) floatingView.findViewById(R.id.traffic);
            btnWifi = (Button) floatingView.findViewById(R.id.wifi);
            btnStp = (Button) floatingView.findViewById(R.id.stop);

            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                btnWifi.setText(R.string.close_wifi);
            } else {
                btnWifi.setText(R.string.open_wifi);
            }
            txtUnusedMem.setText(getString(R.string.calculating));
            txtUnusedMem.setTextColor(Color.RED);
            txtTotalMem.setTextColor(android.graphics.Color.RED);
            txtTraffic.setTextColor(android.graphics.Color.RED);

            btnStp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("isServiceStop", true);
                    intent.setAction(SERVICE_ACTION);
                    sendBroadcast(intent);
                    stopSelf();
                }
            });

            btnWifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String buttonText = (String) btnWifi.getText();
                    String wifiText = getResources().getString(R.string.open_wifi);
                    if (buttonText.equals(wifiText)) {
                        wifiManager.setWifiEnabled(true);
                        btnWifi.setText(R.string.close_wifi);
                    } else {
                        wifiManager.setWifiEnabled(false);
                        btnWifi.setText(wifiText);
                    }

                }
            });

            createFloaingWindow();
        }
        createResultCsv();
        handler.postDelayed(task, 1000);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "service onDestroy");
        if (windowManager != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
        handler.removeCallbacks(task);
        closeOpenedStream();
        // replace the start time in file
        if (!BLANK_STRING.equals(startTime)) {
            replaceFileString(resultFilePath, START_TIME,
                    getString(R.string.start_time) + startTime
                            + Constants.LINE_END);
        } else {
            replaceFileString(resultFilePath, START_TIME, BLANK_STRING);
        }
        isStop = true;
        unregisterReceiver(batteryBroadcast);

        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0); //剩余电量
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1); //总电量
                totalBatt = String.valueOf(level * 100 / scale);
                voltage = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) * 1.0 / 1000);
                temperature = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) * 1.0 / 10);
            }

        }
    }


    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if (!isServiceStop) {
                dataRefresh();
                handler.postDelayed(this, delaytime);
                if (isFloating && floatingView != null) {
                    windowManager.updateViewLayout(floatingView, wmParams);
                }
                getStartTimeFromLogcat();
            } else {
                Intent intent = new Intent();
                intent.putExtra("isServiceStop", true);
                intent.setAction(SERVICE_ACTION);
                sendBroadcast(intent);
                stopSelf();

            }

        }
    };

    public static void writeToCSV(String content){
        try {
            bw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dataRefresh() {
        int pidMemory = memoryInfo.getPidMemorySize(pid, getBaseContext());
        long freeMemory = memoryInfo.getFreeMemorySize(getBaseContext());
        String freeMemoryKb = formatter.format((double) freeMemory / 1024);
        String processMemory = formatter.format((double) pidMemory / 1024);
        String currentBatt = String.valueOf(currentInfo.getCurrentValue());
        // 异常数据过滤
        try {
            if (Math.abs(Double.parseDouble(currentBatt)) >= 500) {
                currentBatt = Constants.NA;
            }
        } catch (Exception e) {
            currentBatt = Constants.NA;
        }
        ArrayList<String> processInfoList = cpuInfo.getCpuRationInfo(totalBatt, currentBatt, temperature, voltage,
                String.valueOf(fpsInfo.getFps()), isRoot);
        if (isFloating) {
            String processCpuRatio = "0.00";
            String totalCpuRatio = "0.00";
            String trafficSize = "0";
            long tempTraffic = 0L;
            double trafficMb = 0;
            boolean isMb = false;
            if (!processInfoList.isEmpty()) {
                processCpuRatio = processInfoList.get(0);
                totalCpuRatio = processInfoList.get(1);
                trafficSize = processInfoList.get(2);
                if (!(BLANK_STRING.equals(trafficSize))
                        && !("-1".equals(trafficSize))) {
                    tempTraffic = Long.parseLong(trafficSize);
                    if (tempTraffic > 1024) {
                        isMb = true;
                        trafficMb = (double) tempTraffic / 1024;
                    }
                }
                // 如果cpu使用率存在且都不小于0，则输出
                if (processCpuRatio != null && totalCpuRatio != null) {
                    txtUnusedMem.setText(getString(R.string.process_free_mem)
                            + processMemory + "/" + freeMemoryKb + "MB");
                    txtTotalMem.setText(getString(R.string.process_overall_cpu)
                            + processCpuRatio + "%/" + totalCpuRatio + "%");
                    String batt = getString(R.string.current) + currentBatt;
                    if ("-1".equals(trafficSize)) {
                        txtTraffic.setText(batt + Constants.COMMA
                                + getString(R.string.traffic) + Constants.NA);
                    } else if (isMb)
                        txtTraffic.setText(batt + Constants.COMMA
                                + getString(R.string.traffic)
                                + formatter.format(trafficMb) + "MB");
                    else
                        txtTraffic.setText(batt + Constants.COMMA
                                + getString(R.string.traffic) + trafficSize
                                + "KB");
                }
                // 当内存为0切cpu使用率为0时则是被测应用退出
                if ("0".equals(processMemory)) {
                    if (isAutoStop) {
                        closeOpenedStream();
                        isServiceStop = true;
                        return;
                    } else {
                        Log.i(LOG_TAG, "未设置自动停止测试，继续监听");
                        // 如果设置应用退出后不自动停止，则需要每次监听时重新获取pid
                        AppInfoModel programe = processInfo.getProgrameByPackageName(
                                this, packageName);
                        if (programe != null && programe.getPid() > 0) {
                            pid = programe.getPid();
                            uid = programe.getUid();
                            cpuInfo = new CpuInfo(getBaseContext(),pid,
                                    Integer.toString(uid));
                        }
                    }
                }
            }
        }
    }

    private void getStartTimeFromLogcat() {
        if (!isGetStartTime || getStartTimeCount >= MAX_START_TIME_COUNT) {
            return;
        }
        // filter logcat by Tag:ActivityManager and Level:Info
        String logcatCommand = "logcat -v time -d ActivityManager:I *:S";
        try {
            Process process = Runtime.getRuntime().exec(logcatCommand);
            BufferedReader bufferReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();
            String line;

            while (null != (line = bufferReader.readLine())) {
                strBuilder.append(line);
                strBuilder.append(Constants.LINE_END);
                String regex = ".*Displayed.*" + startActivity + ".*\\*(.*)ms.*";
                if (line.matches(regex)) {
                    Log.w("my logs", line);
                    if (line.contains("total")) {
                        line = line.substring(0, line.indexOf("total"));
                    }
                    startTime = line.substring(line.lastIndexOf("+") + 1,
                            line.lastIndexOf("ms") + 2);
                    Toast.makeText(LuService.this,
                            getString(R.string.start_time) + startTime,
                            Toast.LENGTH_LONG).show();
                    isGetStartTime = false;
                    break;
                }
            }
            getStartTimeCount++;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createResultCsv() {
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String mDateTime;
        String heapData = "";
        // Todo if sdk google_sdk else
        mDateTime = dateFormater.format(calender.getTime().getTime());
        if (android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 在4.0以下的低版本上/sdcard连接至/mnt/sdcard，而4.0以上版本则连接至/storage/sdcard0，所以有外接sdcard，/sdcard路径一定存在
            resultFilePath = "/sdcard" + File.separator + "Lu_TestResult_"
                    + mDateTime + ".csv";
        } else {
            resultFilePath = getBaseContext().getFilesDir().getPath() +
                    File.separator + "Lu_TestResult_" + mDateTime + ".csv";
        }
        try {
            File resultFile = new File(resultFilePath);
            resultFile.createNewFile();
            out = new FileOutputStream(resultFile);
            osw = new OutputStreamWriter(out);
            bw = new BufferedWriter(osw);
            long totalMemorySize = memoryInfo.getTotalMemory();
            String totalMemory = formatter.format((double) totalMemorySize / 1024);
            String multiCpuTitle = "";

            ArrayList<String> cpuList = cpuInfo.getCpuList();
            for (int i = 0; i < cpuList.size(); i++) {
                multiCpuTitle = Constants.COMMA + cpuList.get(i) + getString(R.string.total_usage);
            }

            writeToCSV(getString(R.string.process_package) + Constants.COMMA
                    + packageName + Constants.LINE_END
                    + getString(R.string.process_name) + Constants.COMMA
                    + processName + Constants.LINE_END
                    + getString(R.string.process_pid) + Constants.COMMA + pid
                    + Constants.LINE_END + getString(R.string.mem_size)
                    + Constants.COMMA + totalMemory + "MB" + Constants.LINE_END
                    + getString(R.string.cpu_type) + Constants.COMMA
                    + cpuInfo.getCpuName() + Constants.LINE_END
                    + getString(R.string.android_system_version)
                    + Constants.COMMA + memoryInfo.getSDKVersion()
                    + Constants.LINE_END + getString(R.string.mobile_type)
                    + Constants.COMMA + memoryInfo.getModel()
                    + Constants.LINE_END + "UID" + Constants.COMMA + uid
                    + Constants.LINE_END);

            if (isAllowedToReadLogs()) {
                writeToCSV(START_TIME);

            }

            if (isRoot) {
                heapData = getString(R.string.native_heap) + Constants.COMMA
                        + getString(R.string.dalvik_heap) + Constants.COMMA;
            }
            writeToCSV(getString(R.string.timestamp) + Constants.COMMA
                    + getString(R.string.top_activity) + Constants.COMMA
                    + heapData + getString(R.string.used_mem_PSS)
                    + Constants.COMMA + getString(R.string.used_mem_ratio)
                    + Constants.COMMA + getString(R.string.mobile_free_mem)
                    + Constants.COMMA + getString(R.string.app_used_cpu_ratio)
                    + Constants.COMMA
                    + getString(R.string.total_used_cpu_ratio) + multiCpuTitle
                    + Constants.COMMA + getString(R.string.traffic)
                    + Constants.COMMA + getString(R.string.battery)
                    + Constants.COMMA + getString(R.string.current)
                    + Constants.COMMA + getString(R.string.temperature)
                    + Constants.COMMA + getString(R.string.voltage)
                    + Constants.COMMA + getString(R.string.fps)
                    + Constants.LINE_END);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }


    }

    private void readSettingInfo() {
        isRoot = false;
        delaytime = 1000;
        isAutoStop = false;
        isStop = false;
        isFloating = true;

    }

    private void createFloaingWindow() {
        SharedPreferences shared = getSharedPreferences("float_flag", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("float", 1);
        editor.commit();

        windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.format = PixelFormat.RGBA_8888;

        windowManager.addView(floatingView, wmParams);
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                x = event.getRawX();
                y = event.getRawY() - statusBarHeight;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchStartX = event.getX();
                        mTouchStartY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updateViewPosition();
                        break;
                    case MotionEvent.ACTION_UP:
                        updateViewPosition();
                        mTouchStartX = mTouchStartY = 0;

                }
                return true;
            }
        });

    }


    private void updateViewPosition() {
        wmParams.x = (int) (x - mTouchStartX);
        wmParams.y = (int) (y - mTouchStartY);
        if (floatingView != null) {
            windowManager.updateViewLayout(floatingView, wmParams);
        }

    }

    private int getStatusBarHeight() {
        int barHeight = 25;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            barHeight = getResources().getDimensionPixelOffset(resourceId);
        }
        return barHeight;
    }

    private boolean isAllowedToReadLogs() {
        int state = getPackageManager().checkPermission(Manifest.permission.READ_LOGS, getPackageName());
        return state == PackageManager.PERMISSION_GRANTED;
    }

    private void closeOpenedStream() {
        try {
            if (bw != null) {
                bw.write(getString(R.string.comment1) + Constants.LINE_END
                        + getString(R.string.comment2) + Constants.LINE_END
                        + getString(R.string.comment3) + Constants.LINE_END
                        + getString(R.string.comment4) + Constants.LINE_END);
                bw.close();
            }
            if (osw != null)
                osw.close();
            if (out != null)
                out.close();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    private void replaceFileString(String filePath, String replaceType, String replaceString) {
        try {
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = BLANK_STRING;
            String oldtext = BLANK_STRING;
            while ((line = reader.readLine()) != null) {
                oldtext += line + Constants.LINE_END;
            }
            reader.close();
            // replace a word in a file
            String newtext = oldtext.replaceAll(replaceType, replaceString);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath),
                    getString(R.string.csv_encoding)));
            writer.write(newtext);
            writer.close();
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }
}
