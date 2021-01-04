package org.hfyd.component.b.api;

import java.io.Serializable;

public class ModuleBParam implements Serializable {

    private ModuleBInnerParam param;

    public ModuleBParam(ModuleBInnerParam param) {
        this.param = param;
    }

    public ModuleBInnerParam getParam() {
        return param;
    }

    @Override
    public String toString() {
        return "ModuleBParam{" +
                "param=" + param +
                '}';
    }
}
