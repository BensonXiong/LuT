package com.lu.qa.lut.Utils;

import android.os.Build;
import android.provider.SyncStateContract;
import android.util.Log;
import android.util.StringBuilderPrinter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by Benson on 9/25/16.
 * Todo add a memoryinfo
 */
public class CpuInfo {
    private static final String LOG_TAG = "LuT" + CpuInfo.class.getSimpleName();


    private ArrayList<Long> idleCpu = new ArrayList<>();
    private ArrayList<Long> totalCpu = new ArrayList<>();
    private  boolean isInitialStatics = true;
    private SimpleDateFormat dateFormatter;
    private long processCpu;
    private long preTraffic;
    private long lastestTraffic;
    private long traffic;
    private int pid;
    private long processCpuTmp;
    private TrafficInfo trafficInfo;
    private String processCpuRatio = "";
    private ArrayList<String> cpuUsedRation = new ArrayList<String>();
    private ArrayList<String> totalCpuRatio = new ArrayList<String>();
    private ArrayList<Long> totalCpuTmp = new ArrayList<Long>();
    private ArrayList<Long> idleCpuTmp = new ArrayList<Long>();


    private static final String INTEL_CPU_NAME = "model name";
    private static final String CPU_STAT = "/proc/stat";
    private static final String CPU_DIR_PATH = "/sys/devices/system/cpu/";
    private static final String CPU_INFO_PATH = "/proc/cpuinfo";


    public CpuInfo(int pid,String uid){
        this.pid = pid;
        trafficInfo = new TrafficInfo(uid);
    }

    public void readCpuStat(){
        String processPid = Integer.toString(pid);
        String cpuStatPath = "/proc/"+ processPid + "/stat";
        try {
            RandomAccessFile processInfo = new RandomAccessFile(cpuStatPath,"r");
            String line;
            StringBuffer strBuffer = new StringBuffer();

            while( null != (line = processInfo.readLine())){
                strBuffer.append(line + "\n");
            }
            String str[] = strBuffer.toString().split(" ");
            processCpu = Long.parseLong(str[13]) + Long.parseLong(str[14]);
            processInfo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        readTotalCpuStat();


    }

    public void readTotalCpuStat(){
        try {
            RandomAccessFile cpuInfo = new RandomAccessFile(CPU_STAT,"r");
            String line;
            while ( null != (line = cpuInfo.readLine())  && line.startsWith("cpu")){
                String []strs = line.split("\\s+");
                idleCpu.add(Long.parseLong(strs[4]));
                totalCpu.add(Long.parseLong(strs[1])+Long.parseLong(strs[2]) + Long.parseLong(strs[3]) +Long.parseLong(strs[4])
                        + Long.parseLong(strs[5]) + Long.parseLong(strs[6]) + Long.parseLong(strs[7]));
            }
            cpuInfo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> getCpuRationInfo(String totalBatt,String currentBatt,String temperature,String voltage,String fps,boolean isRoot){
        String heapData = "";
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        cpuUsedRation.clear();
        idleCpu.clear();
        totalCpu.clear();
        totalCpuRatio.clear();
        readCpuStat();

        try {
            String mDateTime2;
            Calendar calendar = Calendar.getInstance();
            StringBuffer totalCpuBuffer = new StringBuffer();
            if ((Build.MODEL.equals("sdk")) || Build.MODEL.equals("google_sdk")){
                mDateTime2 = dateFormatter.format(calendar.getTime().getTime() + 8*60*60*1000 );
                totalBatt = Constants.NA;
                currentBatt = Constants.NA;
                temperature = Constants.NA;
                voltage = Constants.NA;
            }
            else{
                mDateTime2 = dateFormatter.format(calendar.getTime().getTime());
            }

            if(isInitialStatics){
                preTraffic = trafficInfo.getTrafficInfo();
                isInitialStatics = false;
            }else {
                lastestTraffic = trafficInfo.getTrafficInfo();
                if ( preTraffic == -1)
                    traffic = -1;
                else{
                    if (lastestTraffic > preTraffic){
                        traffic += (lastestTraffic - preTraffic + 1023) / 1024;
                    }
                }
                Log.d(LOG_TAG,"preTraffic======"+ preTraffic);
                preTraffic = lastestTraffic;
                Log.d(LOG_TAG,"latestTraffic=====" + lastestTraffic);

                if ( null != totalCpuTmp && totalCpuTmp.size() > 0 ){
                    processCpuRatio = format.format(100 * (double)(processCpu - processCpuTmp) / (double)(totalCpu.get(0) - totalCpuTmp.get(0)));
                    String cpuRatio = "0.00";
                    for(int i=0 ; i< ( totalCpu.size() > totalCpuTmp.size() ? totalCpuTmp.size() : totalCpu.size()); i++){
                        if (totalCpu.get(i) - totalCpuTmp.get(i) > 0){
                            cpuRatio = format.format( 100 *(double)(( totalCpu.get(i) - idleCpu.get(i)) - (totalCpuTmp.get(i) - idleCpuTmp.get(i)))
                                        / ((double)(totalCpu.get(i)- totalCpuTmp.get(i))));
                        }
                        totalCpuRatio.add(cpuRatio);
                        totalCpuBuffer.append(cpuRatio + Constants.COMMA);
                    }
                }else{
                    processCpuRatio = "0";
                    totalCpuRatio.add("0");
                    totalCpuBuffer.append("0,");
                    totalCpuTmp = (ArrayList<Long>) totalCpu.clone();
                    processCpuTmp = processCpu;
                    idleCpuTmp = (ArrayList<Long>) idleCpu.clone();
                }

                if (isPosiive(processCpuRatio) && isPosiive(totalCpuRatio.get(0))){
                    totalCpuTmp = (ArrayList<Long>) totalCpu.clone();
                    processCpuTmp = processCpu;
                    idleCpuTmp = (ArrayList<Long>) idleCpuTmp.clone();
                    cpuUsedRation.add(processCpuRatio);
                    cpuUsedRation.add(totalCpuRatio.get(0));
                    cpuUsedRation.add(String.valueOf(traffic));
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return cpuUsedRation;

    }

    public String getCpuName(){
        try{
            RandomAccessFile cpuStat = new RandomAccessFile(CPU_INFO_PATH,"r");
            String line;
            while( null != (line = cpuStat.readLine()) ){
                String []strs = line.split(":");
                if(strs[0].contains(INTEL_CPU_NAME) ){
                    cpuStat.close();
                    Log.d(LOG_TAG,"CPU name=" + strs[1]);
                    return strs[1];
                }

            }

        }catch (IOException e){
            Log.e(LOG_TAG,"IOException: "+ e.getMessage());
        }
        return "";
    }

    public int getCpuNum(){
        try{
            File dir = new File(CPU_DIR_PATH);
            File []files = dir.listFiles(new CpuFilter());
            return files.length;
        }catch (Exception e)
        {
            e.printStackTrace();
            return 1;
        }
    }

    public ArrayList<String> getCpuList(){
        ArrayList<String> cpuList = new ArrayList<String>();
        try {
            File dir = new File(CPU_DIR_PATH);
            File[] files = dir.listFiles(new CpuFilter());
            for (int i = 0; i < files.length; i++) {
                cpuList.add(files[i].getName());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            cpuList.add("cpu0");
        }
        return cpuList;
    }

    private boolean isPosiive(String text){
        Double num;
        try {
            num = Double.parseDouble(text);
        }
        catch (NumberFormatException e){
            return false;
        }
        return num >= 0;
    }

    class CpuFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if(Pattern.matches("cpu[0-9]",pathname.getName())){
                return true;
            }
            return false;
        }


    }
}
