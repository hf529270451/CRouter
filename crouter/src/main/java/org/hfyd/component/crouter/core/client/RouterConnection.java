package org.hfyd.component.crouter.core.client;


import android.content.Context;

import org.hfyd.component.crouter.core.bus.Bus;

public class RouterConnection {

    private Bus mBus;

    public RouterConnection(Bus bus) {
        this.mBus = bus;
    }

    public RouterResult connect(Context context, Scheme scheme, boolean isAsync, final RouterCallback callback, long timeout) throws Exception {
        Bus.Call call = new Bus.Call(scheme.getScheme(), scheme.getHost(),
                scheme.getPath(), scheme.getParams(), isAsync);

        call.setTimeout(timeout);
        call.setContext(context);

        if (isAsync && callback != null) {
            call.setCallback(new Bus.Callback() {
                @Override
                public void onComplete(Bus.Result result) {
                    callback.onComplete(parseBusResult(result));
                }
            });
        }

        Bus.Result busResult = mBus.connect(call);
        return parseBusResult(busResult);
    }

    private RouterResult parseBusResult(Bus.Result busResult) {
        RouterResult routerResult = new RouterResult();
        routerResult.setCode(busResult.getCode());
        routerResult.setMsg(busResult.getMsg());
        routerResult.setResult(busResult.getResult());
        return routerResult;
    }
}
