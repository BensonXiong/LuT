package com.lu.qa.lut.Activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.lu.qa.lut.Fragment.MainTab01;
import com.lu.qa.lut.Fragment.MainTab02;
import com.lu.qa.lut.Fragment.MainTab03;
import com.lu.qa.lut.Fragment.MainTab04;
import com.lu.qa.lut.R;

public class FragmentMainActivity extends Activity implements View.OnClickListener {

    private MainTab01 mTab01;
    private MainTab02 mTab02;
    private MainTab03 mTab03;
    private MainTab04 mTab04;

    private LinearLayout mTabBtnWeixin;
    private LinearLayout mTabBtnFrd;
    private LinearLayout mTabBtnAddress;
    private LinearLayout mTabBtnSettings;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_main);
        fragmentManager = getFragmentManager();
        initViews();
        setTabSelection(0);

    }

    private void initViews() {

        mTabBtnWeixin = (LinearLayout) findViewById(R.id.id_tab_bottom_weixin);
        mTabBtnFrd = (LinearLayout) findViewById(R.id.id_tab_bottom_friend);
        mTabBtnAddress = (LinearLayout) findViewById(R.id.id_tab_bottom_contact);
        mTabBtnSettings = (LinearLayout) findViewById(R.id.id_tab_bottom_setting);

        mTabBtnWeixin.setOnClickListener(this);
        mTabBtnFrd.setOnClickListener(this);
        mTabBtnAddress.setOnClickListener(this);
        mTabBtnSettings.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.id_tab_bottom_weixin:
                setTabSelection(0);
                break;
            case R.id.id_tab_bottom_friend:
                setTabSelection(1);
                break;
            case R.id.id_tab_bottom_contact:
                setTabSelection(2);
                break;
            case R.id.id_tab_bottom_setting:
                setTabSelection(3);
                break;
            default:
                break;
        }
    }


    private void setTabSelection(int index)
    {
        resetBtn();
        
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch(index)
        {
            case 0:
                if(mTab01 == null)
                {
                    mTab01 = new MainTab01();
                    transaction.add(R.id.id_content,mTab01);
                }
                else
                {
                    transaction.show(mTab01);
                }
                break;
            case 1:
                if(mTab02 == null)
                {
                    mTab02 = new MainTab02();
                    transaction.add(R.id.id_content,mTab02);
                }
                else
                {
                    transaction.show(mTab02);
                }
                break;
            case 2:
                if(mTab03 == null)
                {
                    mTab03 = new MainTab03();
                    transaction.add(R.id.id_content,mTab03);
                }
                else
                {
                    transaction.show(mTab03);
                }
                break;
            case 3:
                if(mTab04 == null)
                {
                    mTab04 = new MainTab04();
                    transaction.add(R.id.id_content,mTab04);
                }
                else
                {
                    transaction.show(mTab04);
                }
                break;

        }
        transaction.commit();
    }

    private void resetBtn() {
    }
}
