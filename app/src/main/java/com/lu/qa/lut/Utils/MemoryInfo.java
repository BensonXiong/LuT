package com.lu.qa.lut.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Created by Benson on 9/26/16.
 */
public class MemoryInfo {
    private static final String LOG_TAG = "LuT"+ MemoryInfo.class.getName();

    private static Process process;
    private static String MEMINFO_PATH = "/proc/meminfo";

    public long getTotalMemory(){
        String readTmp = "";
        String memTotal = "";
        long memory = 0;
        String []total ;
        String []memKb;
        try{
            RandomAccessFile file = new RandomAccessFile(MEMINFO_PATH,"r");
            while ( null != (readTmp = file.readLine())){
                if ( readTmp.contains("MemTotal")){
                    total = readTmp.split(":");
                    memTotal = total[1].trim().split(" ")[0].trim();
                }
            }
            file.close();
            memory = Long.parseLong(memTotal);
            Log.d(LOG_TAG,"memTotal: "+ memTotal);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return memory;
    }

    public long getFreeMemorySize(Context context){
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(outInfo);
        return outInfo.availMem / 1024;
    }

    public long getPidMemorySize(int pid, Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int []memIds = new int[]{pid};
        Debug.MemoryInfo[] memInfos = am.getProcessMemoryInfo(memIds);
        memInfos[0].getTotalSharedDirty();
        return memInfos[0].getTotalPss() ;
    }

    public String getSDKVersion(){
        return Build.VERSION.RELEASE;
    }

    public String getModel(){
        return Build.MODEL;
    }

    public static String [][] getHeapSize(int pid, Context context){
        String [][]heapData = parseMeminfo(pid);
        return heapData;
    }

    public static String [][] parseMeminfo(int pid){
        boolean infoStart = false;
        String[][] heapData = new String[2][2];
        try{
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec("su");
            DataOutputStream out = new DataOutputStream(process.getOutputStream());
            out.writeBytes("dumpsys meminfo " + pid + "\n");
            out.writeBytes("exit\n");
            out.flush();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            String [] lineItems;
            int length;
            while( null != (line = bufferedReader.readLine())){
                if(line.trim().contains("Permission Denial")){
                    Log.d(LOG_TAG,"Get dumpsys meminfo permission Denial");
                    break;
                }
                else{
                    /* 当读取到MEMINFO in pid 这一行时，下一行就是需要获取的数据
                    ** 版本不同时，显示的不同 size, allocated  vs  native dalvik
                    */
                    if(line.contains("MEMINFO in pid")){
                        infoStart = true;
                    }
                    else if (infoStart){
                        lineItems = line.split("\\s+");
                        length = lineItems.length;
                        if(line.startsWith("size")){
                            heapData[0][0] = lineItems[1];
                            heapData[1][0] = lineItems[2];
                        }else if (line.startsWith("allocated")){
                            heapData[0][1] = lineItems[1];
                            heapData[1][1] = lineItems[2];
                            break;
                        }else if (line.startsWith("Native")){
                            heapData[0][0] = lineItems[length-3];
                            heapData[0][1] = lineItems[length-2];
                        }else if (line.startsWith("Dalvik")){
                            heapData[1][0] = lineItems[length-3];
                            heapData[1][1] = lineItems[length-2];
                            break;
                        }

                    }
                }
            }
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return heapData;
    }
}
