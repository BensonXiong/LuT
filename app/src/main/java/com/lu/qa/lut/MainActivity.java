package com.lu.qa.lut;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lu.qa.lut.Activity.BaseActivity;
import com.lu.qa.lut.Common.AppListAdapter;
import com.lu.qa.lut.Model.AppInfoModel;
import com.lu.qa.lut.Service.LuService;
import com.lu.qa.lut.Utils.ProcessInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = "LuT" + MainActivity.class.getName();
    private static final int TIMEOUT = 20000;

    private List<AppInfoModel> processList;
    private ProcessInfo processInfo;
    private ImageView ivGoBack;
    private TextView nbTitle;
    private ImageView ivBtnSet;
    private ListView lstViProgramme;
    private Button btnTest;
    private LinearLayout layBtnSet;
    private Intent monitorServiceIntent;
    private int pid, uid;
    private UpdateReceiver updateReceiver;

    private Long mLastExitime = (long) 0;
    private boolean isServiceStop = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initViews();
        processInfo = new ProcessInfo();
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stop_test = getString(R.string.stop_test);
                String start_test = getString(R.string.start_test);
                monitorServiceIntent = new Intent(MainActivity.this, LuService.class);
                if (btnTest.getText().toString().equals(start_test)) {
                    AppListAdapter adapter = (AppListAdapter) lstViProgramme.getAdapter();
                    if (adapter.checkedAppInfo != null) {
                        String packageName = adapter.checkedAppInfo.getPackageName();
                        String processName = adapter.checkedAppInfo.getProcessName();
                        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                        String fromActivity = intent.resolveActivity(getPackageManager()).getShortClassName();
                        // clear logcat
                        try {
                            Runtime.getRuntime().exec("logcat -c");
                        } catch (IOException e) {
                            Log.d(LOG_TAG, e.getMessage());
                        }
                        startActivity(intent);
                        waitForAppStart(packageName);
                        monitorServiceIntent.putExtra("processName", processName);
                        monitorServiceIntent.putExtra("pid", pid);
                        monitorServiceIntent.putExtra("uid", uid);
                        monitorServiceIntent.putExtra("packageName", packageName);
                        monitorServiceIntent.putExtra("startActivity", fromActivity);
                        startService(monitorServiceIntent);
                        btnTest.setText(stop_test);
                    } else {
                        btnTest.setText(start_test);
                        Toast.makeText(MainActivity.this, getString(R.string.choose_app_toast), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    btnTest.setText(start_test);
                    Toast.makeText(MainActivity.this, "stop now", Toast.LENGTH_SHORT).show();
                    stopService(monitorServiceIntent);
                }


            }
        });
        lstViProgramme.setAdapter(new AppListAdapter(this, processInfo));
        lstViProgramme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RadioButton radioButton = (RadioButton) ((LinearLayout) view).getChildAt(0);
                radioButton.setChecked(true);
            }
        });

        nbTitle.setText(getString(R.string.app_name));
        ivGoBack.setVisibility(ImageView.INVISIBLE);
        ivBtnSet.setImageResource(R.drawable.settings_button);
        ivBtnSet.setVisibility(ImageView.INVISIBLE);

        updateReceiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LuService.SERVICE_ACTION);
        registerReceiver(updateReceiver, filter);


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mLastExitime) > 2000) {
                Toast.makeText(this, R.string.quite_alert, Toast.LENGTH_LONG).show();
                mLastExitime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isServiceStop) {
            btnTest.setText(getString(R.string.start_test));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateReceiver);
    }

    private void initViews() {
        ivGoBack = (ImageView) findViewById(R.id.go_back);
        nbTitle = (TextView) findViewById(R.id.nb_title);
        ivBtnSet = (ImageView) findViewById(R.id.btn_set);
        lstViProgramme = (ListView) findViewById(R.id.processList);
        btnTest = (Button) findViewById(R.id.test);
        layBtnSet = (LinearLayout) findViewById(R.id.lay_btn_set);
    }

    private void waitForAppStart(String packageName) {
        Log.d(LOG_TAG, "wait for app start");
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + TIMEOUT) {
            pid = processInfo.getPidByPackageName(this, packageName);
            if (pid != 0) {
                break;
            }

        }

    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isServiceStop = intent.getExtras().getBoolean("isServiceStop");
            if (isServiceStop) {
                btnTest.setText(getString(R.string.start_test));
            }

        }
    }
}
