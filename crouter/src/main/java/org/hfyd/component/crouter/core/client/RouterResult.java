package org.hfyd.component.crouter.core.client;

import org.hfyd.component.crouter.core.bus.ProtocolCode;

public class RouterResult {

    private int code;

    private String msg;

    private Object result;

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return this.code == ProtocolCode.SUCCESS.getCode();
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RouterResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", result=" + result +
                '}';
    }
}
