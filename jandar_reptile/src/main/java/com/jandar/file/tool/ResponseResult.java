package com.jandar.file.tool;

import java.util.HashMap;

public class ResponseResult extends HashMap<String, Object> {

    private final static String FINAL_STATUS = "status";
    private final static String FINAL_INFO = "data";
    private final static String FINAL_ERROR = "msg";

    public static ResponseResult success(Object info) {
        ResponseResult result = new ResponseResult();
        result.put(FINAL_STATUS, true);
        result.put(FINAL_INFO, info);
        return result;
    }


    public static ResponseResult error(String info) {
        ResponseResult result = new ResponseResult();
        result.put(FINAL_STATUS, false);
        result.put(FINAL_INFO, info);
        return result;
    }

    public static ResponseResult error(Exception e) {
        ResponseResult result = new ResponseResult();
        result.put(FINAL_STATUS, false);
        result.put(FINAL_INFO, "发生异常:" + e.getMessage());
        result.put(FINAL_ERROR, e);
        return result;
    }
}
