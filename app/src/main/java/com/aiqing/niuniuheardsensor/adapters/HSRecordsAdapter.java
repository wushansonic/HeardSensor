package com.aiqing.niuniuheardsensor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aiqing.niuniuheardsensor.R;
import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blue on 16/3/17.
 */
public class HSRecordsAdapter extends BaseAdapter {
    private List<HSRecord> recordList = new ArrayList<>();
    private Context context;


    public HSRecordsAdapter(List<HSRecord> recordList, Context context) {
        this.recordList = recordList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int position) {
        return recordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_records_item, parent, false);
        }

        HSRecord record = (HSRecord) getItem(position);

        TextView tv_mobile = (TextView) convertView.findViewById(R.id.mobile);
        TextView tv_type = (TextView) convertView.findViewById(R.id.type);
        TextView tv_time = (TextView) convertView.findViewById(R.id.time);
        TextView tv_duration = (TextView) convertView.findViewById(R.id.duration);


        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time = sfd.format(record.getDate());

        String type = "";
        switch (record.getType()) {
            case HSRecordsUploadHelper.INCOMING_TYPE:
                type = "接听";
                break;
            case HSRecordsUploadHelper.MISSED_TYPE:
                type = "未接";
                break;
            case HSRecordsUploadHelper.OUTGOING_TYPE:
                type = "打出";
                break;
        }

        tv_mobile.setText(record.getNumber() + "");
        tv_time.setText(time);
        tv_type.setText(type);
        tv_duration.setText((record.getType() == 3 ? "0" : String.valueOf(record.getDuration()))+"秒");


        return convertView;
    }
}
