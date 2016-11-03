package com.lu.qa.lut.Utils;

import android.os.Build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

/**
 * Created by Benson on 10/16/16.
 */
public class CurrentInfo {

    private static final String LOG_TAG = "Lu" + CurrentInfo.class.getName();
    private static final String BUILD_MODEL = Build.MODEL.toLowerCase(Locale.US);
    private static final String I_MBAT = "I_MBAT: ";
    private static final String CURRENT_NOW ="/sys/class/power_supply/battery/current_now";
    private static final String BATT_CURRENT ="/sys/class/power_supply/battery/batt_current";
    private static final String SMEM_EXT = "/sys/class/power_supply/battery/smem_text";
    private static final String BATT_CURRENT_ADC = "/sys/class/power_supply/battery/batt_current_adc";
    private static final String CURRENT_AVG = "/sys/class/power_supply/battery/current_avg";


    /*To do
    public Long getCurrentValue(){

    }
    */

    private Long parseCurrentValue(File file, boolean convertToMillis){
        String line ;
        Long value = 0L;
        try{
            RandomAccessFile reader = new RandomAccessFile(file,"r");
            while ( null != (line = reader.readLine())){
                value = Long.parseLong(line);
            }
            if ( convertToMillis ){
                value = value / 1000;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

}
