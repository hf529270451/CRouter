package org.hfyd.component.a.api;

import java.io.Serializable;

public class ModuleAParam implements Serializable {

    private String name;

    public ModuleAParam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ModuleAParam{" +
                "name='" + name + '\'' +
                '}';
    }
}
