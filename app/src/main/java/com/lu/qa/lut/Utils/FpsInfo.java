package com.lu.qa.lut.Utils;

import android.provider.ContactsContract;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

/**
 * Created by Benson on 10/16/16.
 */
public class FpsInfo {
    private static final String LOG_TAG = "LuT"+ FpsInfo.class.getName();

    private static long startTime = 0L;
    private static int lastFrameNum = 0;
    private static boolean FLAG = true;
    private static Process process;
    private static DataOutputStream out ;
    private static BufferedReader reader;

    public static float getFps(){

        long nowTime = System.nanoTime();
        float time = (float)(nowTime - startTime) / 1000000.0F;
        startTime = nowTime;
        int nowFrameNum = getFrameNum();
        float fps = Math.round((nowFrameNum - lastFrameNum) * 1000 / time );
        lastFrameNum = nowFrameNum ;
        return fps;

    }

    public static final int getFrameNum(){
        try{

            if ( process == null ){
                process = Runtime.getRuntime().exec("su");
                out = new DataOutputStream(process.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            }
            out.writeBytes("service call SurfaceFlinger 1013 \n");
            out.flush();
            String line;
            if ( null != (line = reader.readLine())){
                int start = line.indexOf("(");
                int end = line.indexOf(" ");
                if( (start != -1) && (end > start)){
                    String str = line.substring(start + 1,end);
                    return Integer.parseInt(str,16);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

}
