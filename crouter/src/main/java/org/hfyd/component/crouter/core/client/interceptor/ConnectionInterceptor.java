package org.hfyd.component.crouter.core.client.interceptor;

import android.text.TextUtils;

import org.hfyd.component.crouter.core.bus.ProtocolCode;
import org.hfyd.component.crouter.core.client.RouterResult;
import org.hfyd.component.crouter.core.client.RouterConnection;
import org.hfyd.component.crouter.core.client.Scheme;

public class ConnectionInterceptor implements RouterClientInterceptor {

    private RouterConnection mConnection;
    private long timeout;

    public ConnectionInterceptor(RouterConnection connection, long timeout) {
        this.mConnection = connection;
        this.timeout = timeout;
    }

    @Override
    public RouterResult intercept(Chain chain) throws Exception {
        RouterContext routerContext = chain.context();

        Scheme scheme = routerContext.getScheme();
        if (scheme == null) {
            throw new IllegalStateException(ProtocolCode.PARAMS_ERROR.getMsg() + " scheme");
        }

        String schemePre = scheme.getScheme();
        String host = scheme.getHost();
        String path = scheme.getPath();

        if (TextUtils.isEmpty(schemePre)) {
            throw new IllegalStateException(ProtocolCode.PARAMS_ERROR.getMsg() + " scheme");
        }

        if (TextUtils.isEmpty(host)) {
            throw new IllegalStateException(ProtocolCode.PARAMS_ERROR.getMsg() + " host");
        }

        if (TextUtils.isEmpty(path)) {
            throw new IllegalStateException(ProtocolCode.PARAMS_ERROR.getMsg() + " path");
        }

        if (this.mConnection == null) {
            throw new RuntimeException("connection must not null");
        }

        RouterResult routerResult = this.mConnection.connect(routerContext.getContext(),
                scheme, routerContext.isAsync(), routerContext.getCallback(), this.timeout);
        routerContext.setRouterResult(routerResult);
        return routerResult;
    }
}
