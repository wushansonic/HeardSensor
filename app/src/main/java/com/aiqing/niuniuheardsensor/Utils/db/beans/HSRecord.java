package com.aiqing.niuniuheardsensor.Utils.db.beans;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by blue on 16/3/14.
 */
@DatabaseTable(tableName = "hs_records")
public class HSRecord {

    @DatabaseField(columnName = "id", generatedId = true)
    Integer id;
    @DatabaseField(columnName = "date")
    Date date;
    @DatabaseField(columnName = "type")
    Integer type;
    @DatabaseField(columnName = "number")
    String number;
    @DatabaseField(columnName = "duration")
    Long duration;
    @DatabaseField(columnName = "file_path")
    String file_path;
    @DatabaseField(columnName = "play_state")
    boolean play_state;
    @DatabaseField(columnName = "reupload_id")
    String reupload_id;

    public boolean select;


    public HSRecord() {
    }

    public HSRecord(Date date, Integer type, String number, Long duration, String filePath, boolean play_state, String reupload_id) {
        this.date = date;
        this.type = type;
        this.number = number;
        this.duration = duration;
        this.file_path = filePath;
        this.play_state = play_state;
        this.reupload_id = reupload_id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public boolean isPlay_state() {
        return play_state;
    }

    public void setPlay_state(boolean play_state) {
        this.play_state = play_state;
    }

    public String getReupload_id() {
        return reupload_id;
    }

    public void setReupload_id(String reupload_id) {
        this.reupload_id = reupload_id;
    }
}
