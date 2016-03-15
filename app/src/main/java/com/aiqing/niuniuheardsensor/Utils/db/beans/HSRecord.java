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
    Long number;
    @DatabaseField(columnName = "duration")
    Long duration;

    public HSRecord() {
    }

    public HSRecord(Date date, Integer type, Long number, Long duration) {
        this.date = date;
        this.type = type;
        this.number = number;
        this.duration = duration;
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

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
