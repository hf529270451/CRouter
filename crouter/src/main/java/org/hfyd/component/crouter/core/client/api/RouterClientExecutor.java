package org.hfyd.component.crouter.core.client.api;

import android.text.TextUtils;

import org.hfyd.component.crouter.anno.Path;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RouterClientExecutor {

    private static final Map<Class, ApiExecutor> sApiCache = new ConcurrentHashMap<>();

    public static <T> T create(final Class<T> apiClass) {
        validateInterface(apiClass);
        ApiExecutor apiExecutor = sApiCache.get(apiClass);
        if (apiExecutor == null) {
            sApiCache.put(apiClass, apiExecutor = ApiExecutor.parseClass(apiClass));
        }

        final ApiExecutor finalApiExecutor = apiExecutor;

        return (T) Proxy.newProxyInstance(apiClass.getClassLoader(),
                new Class<?>[]{apiClass},
                new InvocationHandler() {
                    private final Object[] emptyArgs = new Object[0];

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        args = args != null ? args : emptyArgs;
                        return finalApiExecutor.invoke(method, args);
                    }
                }
        );

    }

    private static <T extends Class> void validateInterface(T apiClass) {
        if (!apiClass.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
    }
}
