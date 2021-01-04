package org.hfyd.component.crouter.core.client;

import android.content.Context;

import org.hfyd.component.crouter.core.client.interceptor.ConnectionInterceptor;
import org.hfyd.component.crouter.core.client.interceptor.LoggerInterceptor;
import org.hfyd.component.crouter.core.client.interceptor.RouterContext;
import org.hfyd.component.crouter.core.client.interceptor.RealRouterClientInterceptorChain;
import org.hfyd.component.crouter.core.client.interceptor.RouterClientInterceptor;

import java.util.ArrayList;
import java.util.List;

public class RouterCall implements Call<RouterResult> {

    private Context context;
    private RouterConnection routerConnection;
    private List<RouterClientInterceptor> beforeInterface;
    private List<RouterClientInterceptor> afterInterface;
    private Scheme scheme;
    private boolean isAsync;
    private RouterCallback routerCallback;
    private long timeout;

    RouterCall(Context context, RouterConnection routerConnection, Scheme scheme, boolean isAsync, long timeout, RouterCallback routerCallback) {
        this.context = context;
        this.routerConnection = routerConnection;
        this.scheme = scheme;
        this.isAsync = isAsync;
        this.routerCallback = routerCallback;
        this.timeout = timeout;
    }

    void setBeforeInterface(List<RouterClientInterceptor> beforeInterface) {
        this.beforeInterface = beforeInterface;
    }

    void setAfterInterface(List<RouterClientInterceptor> afterInterface) {
        this.afterInterface = afterInterface;
    }

    @Override
    public RouterResult call() throws Exception {
        List<RouterClientInterceptor> interceptors = new ArrayList<>(beforeInterface);
        interceptors.add(new ConnectionInterceptor(routerConnection, timeout));
        interceptors.addAll(afterInterface);
        interceptors.add(new LoggerInterceptor());

        RouterContext routerContext = new RouterContext();
        routerContext.setScheme(scheme);
        routerContext.setAsync(isAsync);
        routerContext.setCallback(routerCallback);
        routerContext.setContext(context);

        RouterClientInterceptor.Chain chain = new RealRouterClientInterceptorChain(interceptors, routerContext, 0);
        return chain.proceed(scheme);
    }

    @Override
    public void cancel() {
        this.routerCallback = null;
    }
}
