package org.hfyd.component.crouter.core.server;

import org.hfyd.component.crouter.core.bus.Bus;

import java.util.HashMap;
import java.util.Map;

public class ResponseDispatch {

    private static Map<String, ServerController.Response> sResponseMap = new HashMap<>();

    public static void send(String callId, ResponseResult responseResult) {
        ServerController.Response response = sResponseMap.get(callId);
        if (response != null) {
            if (response.isAsync() && response.getCallback() != null) {
                response.getCallback().onComplete(new Bus.Result(responseResult.getCode(), responseResult.getMsg(), responseResult.getBody()));
            } else {
                response.setCode(responseResult.getCode());
                response.setMsg(responseResult.getMsg());
                response.setBody(responseResult.getBody());
            }

            sResponseMap.remove(callId);
        }
    }

    static void register(String callId, ServerController.Response response) {
        sResponseMap.put(callId, response);
    }

    static void unregister(String callId) {
        sResponseMap.remove(callId);
    }

}
