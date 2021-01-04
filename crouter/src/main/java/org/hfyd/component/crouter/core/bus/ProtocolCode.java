package org.hfyd.component.crouter.core.bus;

public enum ProtocolCode {

    SUCCESS(200, "success"),
    TIMEOUT(300, "timeout"),
    PATH_NOT_FIND(10004,"接口未找到"),
    PATH_EXE_ERROR(10005,"Server端异常"),
    PARAMS_ERROR(10006, "缺少参数"),
    INNER_ERROR(10008, "内部异常");


    int code;
    String msg;

    ProtocolCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

