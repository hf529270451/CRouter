package org.hfyd.component.crouter.core.client.interceptor;

import android.content.Context;

import org.hfyd.component.crouter.core.client.RouterResult;
import org.hfyd.component.crouter.core.client.RouterCallback;
import org.hfyd.component.crouter.core.client.Scheme;

public class RouterContext {

    private Scheme scheme;

    private boolean isAsync;

    private RouterCallback callback;

    private RouterResult routerResult;

    private Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public RouterResult getRouterResult() {
        return routerResult;
    }

    public void setRouterResult(RouterResult routerResult) {
        this.routerResult = routerResult;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public RouterCallback getCallback() {
        return callback;
    }

    public void setCallback(RouterCallback callback) {
        this.callback = callback;
    }
}
