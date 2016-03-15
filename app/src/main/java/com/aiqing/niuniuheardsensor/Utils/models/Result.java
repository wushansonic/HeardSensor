package com.aiqing.niuniuheardsensor.Utils.models;

/**
 * Created by blue on 16/3/15.
 */
public class Result<T> {
    public int status;

    public String notice;
    public String message;
    public String forbidden;
    public T data;
    public T datas;
}
