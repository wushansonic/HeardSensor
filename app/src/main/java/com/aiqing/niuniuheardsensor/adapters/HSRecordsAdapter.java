package com.aiqing.niuniuheardsensor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aiqing.niuniuheardsensor.R;
import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.api.HSApiHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.db.dao.HSRecordsDaos;
import com.aiqing.niuniuheardsensor.Utils.record.HSRecordHelper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blue on 16/3/17.
 */
public class HSRecordsAdapter extends BaseAdapter {
    private List<HSRecord> recordList = new ArrayList<HSRecord>();
    private Context context;
    private boolean showSelect = false;


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
        return recordList.get(recordList.size() - 1 - position);
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

        final HSRecord record = (HSRecord) getItem(position);

        TextView tv_mobile = (TextView) convertView.findViewById(R.id.mobile);
        TextView tv_type = (TextView) convertView.findViewById(R.id.type);
        TextView tv_time = (TextView) convertView.findViewById(R.id.time);
        TextView tv_duration = (TextView) convertView.findViewById(R.id.duration);


        SimpleDateFormat sfd = new SimpleDateFormat("MM-dd HH:mm:ss");
        String time = sfd.format(record.getDate());

        String type = "";
        switch (record.getType()) {
            case HSRecordsUploadHelper.INCOMING_TYPE:
                type = "接听";
                tv_type.setTextColor(context.getResources().getColor(R.color.green));
                tv_mobile.setTextColor(context.getResources().getColor(R.color.green));
                tv_time.setTextColor(context.getResources().getColor(R.color.green));
                tv_duration.setTextColor(context.getResources().getColor(R.color.green));
                break;
            case HSRecordsUploadHelper.MISSED_TYPE:
                type = "未接";
                tv_type.setTextColor(context.getResources().getColor(R.color.red));
                tv_mobile.setTextColor(context.getResources().getColor(R.color.red));
                tv_time.setTextColor(context.getResources().getColor(R.color.red));
                tv_duration.setTextColor(context.getResources().getColor(R.color.red));
                break;
            case HSRecordsUploadHelper.OUTGOING_TYPE:
                type = "打出";
                tv_type.setTextColor(context.getResources().getColor(R.color.blue));
                tv_mobile.setTextColor(context.getResources().getColor(R.color.blue));
                tv_time.setTextColor(context.getResources().getColor(R.color.blue));
                tv_duration.setTextColor(context.getResources().getColor(R.color.blue));
                break;
        }

        tv_mobile.setText(record.getNumber() + "");
        tv_time.setText(time);
        tv_type.setText(type);
        tv_duration.setText(String.valueOf(record.getDuration()) + "秒");

        ImageView play_record = (ImageView) convertView.findViewById(R.id.play_record);
        if (record.getDuration() > 0) {
            play_record.setVisibility(View.VISIBLE);

            final boolean isPlay = record.isPlay_state();

            if (isPlay) {
                play_record.setImageResource(R.drawable.icon_pause);
            } else {
                play_record.setImageResource(R.drawable.icon_play);
            }

            play_record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    boolean isPlaying = record.isPlay_state();
                    if (isPlaying) {
                        HSRecordHelper.stopPlay();
                        ((ImageView) v).setImageResource(R.drawable.icon_play);
                        record.setPlay_state(false);
                    } else {
                        if (HSRecordHelper.isPlaying)
                            return;

                        HSRecordHelper.play(record.getFile_path());
                        ((ImageView) v).setImageResource(R.drawable.icon_pause);
                        record.setPlay_state(true);
                    }

                }
            });

        } else {
            play_record.setVisibility(View.GONE);
        }

        final TextView reupload = (TextView) convertView.findViewById(R.id.reupload);
        if (record.getStatus() != HSRecord.RECORD_NEED_REUPLOAD) {
            reupload.setVisibility(View.GONE);
        } else {
            reupload.setVisibility(View.VISIBLE);

            reupload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<HSRecord> records = new ArrayList<HSRecord>();
                    records.add(record);
                    reupload.setText("正在重传...");
                    HSApiHelper.requestReleaseRecord(records, context, new HSApiHelper.CallBack() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            int status = response.optInt("status");

                            if (status != 200) {
                                reupload.setText("重传");
                                if (records != null && records.size() > 0) {

                                    for (HSRecord record : records) {
                                        record.setStatus(HSRecord.RECORD_NEED_REUPLOAD);
                                        HSRecordsDaos.getInstance(context).addOneRecord(record);
                                    }
                                    notifyDataSetChanged();
                                }
                            } else {
                                reupload.setVisibility(View.GONE);
                                for (HSRecord record : records) {
                                    record.setStatus(HSRecord.RECORD_UPLOAD_COMPLETE);
                                    HSRecordsDaos.getInstance(context).addOneRecord(record);
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure() {
                            reupload.setText("重传");

                            if (records != null && records.size() > 0) {
                                for (HSRecord record : records) {
                                    record.setStatus(HSRecord.RECORD_NEED_REUPLOAD);
                                    HSRecordsDaos.getInstance(context).addOneRecord(record);
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFileUploadSuccess(HSRecord record) {
                            record.setStatus(HSRecord.RECORD_FILE_UPLOAD_COMPLETE);
                            HSRecordsDaos.getInstance(context).addOneRecord(record);
                            notifyDataSetChanged();
                        }
                    });
                }
            });
        }


        TextView status_tv = (TextView) convertView.findViewById(R.id.status);
        int status = record.getStatus();
        switch (status) {
            case HSRecord.RECORD_UPLOADING:
                status_tv.setVisibility(View.VISIBLE);
                status_tv.setText("正在上传记录...");
                break;
            case HSRecord.RECORD_UPLOAD_COMPLETE:
                if (record.getDuration() <= 0) {
                    status_tv.setVisibility(View.GONE);
                } else {
                    status_tv.setVisibility(View.VISIBLE);
                    status_tv.setText("正在上传音频...");
                }
                break;
            case HSRecord.RECORD_FILE_UPLOAD_COMPLETE:
                status_tv.setVisibility(View.GONE);
                break;
            default:
                status_tv.setVisibility(View.GONE);
        }

        final ImageView select = (ImageView) convertView.findViewById(R.id.select);
        if (showSelect) {
            select.setBackgroundResource(record.select ? R.drawable.btn_selected : R.drawable.btn_unselect);
            select.setVisibility(View.VISIBLE);
            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (record.select) {
                        record.select = false;
                        select.setBackgroundResource(R.drawable.btn_unselect);
                    } else {
                        record.select = true;
                        select.setBackgroundResource(R.drawable.btn_selected);
                    }
                }
            });
        } else {
            select.setVisibility(View.GONE);
        }


        return convertView;
    }

    public void setShowSelect(boolean showSelect) {
        this.showSelect = showSelect;
    }
}
