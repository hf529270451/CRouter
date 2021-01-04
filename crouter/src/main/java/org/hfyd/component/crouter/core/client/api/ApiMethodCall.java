package org.hfyd.component.crouter.core.client.api;

import android.content.Context;

import java.util.List;
import java.util.Map;

class ApiMethodCall {

    String scheme;
    String host;
    String path;
    List<ParamAttr> params;

    static class ParamAttr{

        Type type;
        String paramName;

        enum Type{
            Id,
            Context,
            Param,
        }
    }
}
