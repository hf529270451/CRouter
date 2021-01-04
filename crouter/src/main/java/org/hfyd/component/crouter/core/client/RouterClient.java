package org.hfyd.component.crouter.core.client;

import android.util.Log;

import org.hfyd.component.crouter.RouterConfig;
import org.hfyd.component.crouter.core.CRouterLogger;
import org.hfyd.component.crouter.core.bus.ProtocolCode;
import org.hfyd.component.crouter.core.client.interceptor.RouterClientInterceptor;

import java.util.ArrayList;
import java.util.List;

public class RouterClient {

    private RouterConnection routerConnection;
    private Dispatcher dispatcher;

    private List<RouterClientInterceptor> beforeRouterClientInterceptor = new ArrayList<>();
    private List<RouterClientInterceptor> afterRouterClientInterceptor = new ArrayList<>();

    public RouterClient(RouterConnection routerConnection, Dispatcher dispatcher) {
        this.routerConnection = routerConnection;
        this.dispatcher = dispatcher;
    }

    public void addBeforeRouterClientInterceptor(RouterClientInterceptor interceptor) {
        this.beforeRouterClientInterceptor.add(interceptor);
    }

    public void addAfterRouterClientInterceptor(RouterClientInterceptor interceptor) {
        this.afterRouterClientInterceptor.add(interceptor);
    }

    public RouterResult routeSync(RouterConfig config) {
        RouterResult result;
        try {
            RouterCall routerCall = createRouterCall(config, false, null);
            result = routerCall.call();
        } catch (Exception e) {
            result = new RouterResult();
            result.setCode(ProtocolCode.INNER_ERROR.getCode());
            result.setMsg(e.getMessage());
            CRouterLogger.error(Log.getStackTraceString(e));
        }

        return result;
    }

    public Call routeAsync(RouterConfig config, RouterCallback routerCallback) {
        Call routerCall = null;
        try {
            routerCall = createRouterCall(config, true, routerCallback);
            dispatcher.emit(routerCall);
        } catch (Exception e) {
            RouterResult result = new RouterResult();
            result.setCode(ProtocolCode.INNER_ERROR.getCode());
            result.setMsg(e.getMessage());
            routerCallback.onComplete(result);
        }

        return routerCall;
    }

    private RouterCall createRouterCall(RouterConfig config, boolean isAsync, RouterCallback routerCallback) {
        RouterCall routerCall = new RouterCall(config.getContext(), routerConnection,
                new Scheme(config.getScheme(), config.getHost(), config.getPath(), config.getParams()),
                isAsync, config.getTimeout(), routerCallback);

        routerCall.setBeforeInterface(beforeRouterClientInterceptor);
        routerCall.setAfterInterface(afterRouterClientInterceptor);
        return routerCall;
    }
}
