package com.lu.qa.lut.Common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.lu.qa.lut.Model.AppInfoModel;
import com.lu.qa.lut.R;
import com.lu.qa.lut.Utils.ProcessInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Benson on 10/25/16.
 */
public class AppListAdapter extends BaseAdapter {
    private LayoutInflater mInflater = null;
    public List<AppInfoModel> mData;
    private RadioButton lastCheckedPosition;
    public AppInfoModel checkedAppInfo;


    public AppListAdapter(Context context, ProcessInfo processInfo) {
        this.mData = processInfo.getInstalledApps(context);
        this.mInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppInfoModel appInfoModel = (AppInfoModel) getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.image);
            holder.appName = (TextView) convertView.findViewById(R.id.text);
            holder.radioButton = (RadioButton) convertView.findViewById(R.id.rb);
            holder.radioButton.setFocusable(false);
            holder.radioButton.setOnCheckedChangeListener(checkedChangeListener);
            convertView.setTag(holder);
        } else {

            holder = (ViewHolder) convertView.getTag();
        }
        holder.appIcon.setImageDrawable(appInfoModel.getIcon());
        holder.appName.setText(appInfoModel.getProcessName());
        holder.radioButton.setId(position);
        holder.radioButton.setChecked(checkedAppInfo != null && getItem(position) == checkedAppInfo);

        return convertView;
    }

    CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                if ((lastCheckedPosition != null) && (lastCheckedPosition.getId() != compoundButton.getId())) {
                    lastCheckedPosition.setChecked(false);
                }
                checkedAppInfo = mData.get(compoundButton.getId());
                lastCheckedPosition = (RadioButton) compoundButton;

            }


        }
    };


    static class ViewHolder {
        public TextView appName;
        public ImageView appIcon;
        public RadioButton radioButton;
    }
}