package org.hfyd.component.b.api;

import java.io.Serializable;

public class ModuleBInnerParam implements Serializable {

    private String msg;

    public ModuleBInnerParam(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ModuleBInnerParam{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
