package org.hfyd.component.c.api;

import java.io.Serializable;

public class Module3Param implements Serializable {

    private String msg;

    public Module3Param(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "Module3Param{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
