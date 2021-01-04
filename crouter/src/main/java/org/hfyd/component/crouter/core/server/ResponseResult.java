package org.hfyd.component.crouter.core.server;

import org.hfyd.component.crouter.core.bus.ProtocolCode;

public class ResponseResult {

    private int code;
    private String msg;
    private Object body;

    private ResponseResult(int code, String msg, Object body) {
        this.code = code;
        this.msg = msg;
        this.body = body;
    }

    public static ResponseResult success() {
        return success(null);
    }

    public static ResponseResult success(Object object) {
        return new ResponseResult(ProtocolCode.SUCCESS.getCode(), ProtocolCode.SUCCESS.getMsg(), object);
    }

    public static ResponseResult error(int code, String msg) {
        return new ResponseResult(code, msg, null);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getBody() {
        return body;
    }
}
