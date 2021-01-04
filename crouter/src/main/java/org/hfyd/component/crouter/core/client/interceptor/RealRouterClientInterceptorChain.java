package org.hfyd.component.crouter.core.client.interceptor;

import org.hfyd.component.crouter.core.client.RouterResult;
import org.hfyd.component.crouter.core.client.Scheme;

import java.util.List;

public class RealRouterClientInterceptorChain implements RouterClientInterceptor.Chain {

    private List<RouterClientInterceptor> interceptors;
    private RouterContext context;
    private int index;

    public RealRouterClientInterceptorChain(List<RouterClientInterceptor> interceptors, RouterContext context, int index) {
        this.interceptors = interceptors;
        this.context = context;
        this.index = index;
    }

    @Override
    public RouterContext context() {
        return context;
    }

    @Override
    public RouterResult proceed(Scheme scheme) throws Exception {
        if (index >= interceptors.size()) {
            throw new AssertionError();
        }

        RealRouterClientInterceptorChain next = new RealRouterClientInterceptorChain(interceptors, context, index + 1);
        RouterClientInterceptor interceptor = interceptors.get(index);
        return interceptor.intercept(next);
    }
}
