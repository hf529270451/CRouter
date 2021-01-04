package org.hfyd.component.crouter;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.hfyd.component.crouter.core.bus.ServerRegister;
import org.hfyd.component.crouter.core.client.Call;
import org.hfyd.component.crouter.core.client.Dispatcher;
import org.hfyd.component.crouter.core.client.RouterClient;
import org.hfyd.component.crouter.core.client.RouterGlobal;
import org.hfyd.component.crouter.core.bus.Bus;
import org.hfyd.component.crouter.core.client.RouterResult;
import org.hfyd.component.crouter.core.client.RouterCallback;
import org.hfyd.component.crouter.core.client.RouterConnection;
import org.hfyd.component.crouter.core.client.activity.CRouterActivityBinder;
import org.hfyd.component.crouter.core.client.api.RouterClientExecutor;
import org.hfyd.component.crouter.core.client.interceptor.RouterClientInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CRouter {

    private static RouterClient mRouterClient;

    private RouterConfig.Builder mConfigBuilder;

    private CRouter() {
        mConfigBuilder = new RouterConfig.Builder();

        if (mRouterClient == null) {
            mRouterClient = new RouterClient(
                    new RouterConnection(Bus.getInstance()),
                    new Dispatcher());
        }

        injectGlobalInterceptors();
    }

    private void injectGlobalInterceptors() {
        List<RouterClientInterceptor> globalBeforeRouterClientInterceptors = RouterGlobal.getGlobalBeforeRouterClientInterceptors();
        for (RouterClientInterceptor globalBeforeRouterClientInterceptor : globalBeforeRouterClientInterceptors) {
            mRouterClient.addBeforeRouterClientInterceptor(globalBeforeRouterClientInterceptor);
        }

        List<RouterClientInterceptor> globalAfterRouterClientInterceptors = RouterGlobal.getGlobalAfterRouterClientInterceptors();
        for (RouterClientInterceptor globalAfterRouterClientInterceptor : globalAfterRouterClientInterceptors) {
            mRouterClient.addBeforeRouterClientInterceptor(globalAfterRouterClientInterceptor);
        }
    }

    public static CRouter newInstance() {
        return new CRouter();
    }

    public CRouter uri(Uri uri) {
        mConfigBuilder.uri(uri);
        return this;
    }

    public CRouter with(Context context) {
        mConfigBuilder.setContext(context);
        return this;
    }

    public CRouter scheme(String scheme) {
        mConfigBuilder.setScheme(scheme);
        return this;
    }

    public CRouter host(String host) {
        mConfigBuilder.setHost(host);
        return this;
    }

    public CRouter path(String path) {
        mConfigBuilder.setPath(path);
        return this;
    }

    public CRouter param(String name, Object object) {
        mConfigBuilder.addParam(name, object);
        return this;
    }

    public CRouter params(Map<String, Object> params) {
        mConfigBuilder.addParams(params);
        return this;
    }

    public CRouter timeout(long timeout) {
        mConfigBuilder.setTimeout(timeout);
        return this;
    }

    public CRouter addBeforeInterceptor(RouterClientInterceptor interceptor) {
        mRouterClient.addBeforeRouterClientInterceptor(interceptor);
        return this;
    }

    public CRouter addAfterInterceptor(RouterClientInterceptor interceptor) {
        mRouterClient.addAfterRouterClientInterceptor(interceptor);
        return this;
    }

    public RouterResult routeSync() {
        return mRouterClient.routeSync(mConfigBuilder.build());
    }

    public Call routeAsync(RouterCallback routerCallback) {
        return mRouterClient.routeAsync(mConfigBuilder.build(), routerCallback);
    }

    public static void init(Application application) {
        RouterGlobal.init(application);
        ServerRegister.init(application);
        CRouterActivityBinder.init();
    }

    public static void addGlobalAfterRouterClientInterceptor(RouterClientInterceptor interceptor) {
        RouterGlobal.addGlobalAfterRouterClientInterceptor(interceptor);
    }

    public static void addGlobalBeforeRouterClientInterceptor(RouterClientInterceptor interceptor) {
        RouterGlobal.addGlobalBeforeRouterClientInterceptor(interceptor);
    }

    public static <T> T api(Class<T> apiClass) {
        return RouterClientExecutor.create(apiClass);
    }
}
