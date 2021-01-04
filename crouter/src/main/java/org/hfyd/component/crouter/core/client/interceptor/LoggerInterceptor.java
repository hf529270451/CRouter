package org.hfyd.component.crouter.core.client.interceptor;

import org.hfyd.component.crouter.core.CRouterLogger;
import org.hfyd.component.crouter.core.client.RouterResult;
import org.hfyd.component.crouter.core.client.Scheme;

import java.util.Map;

public class LoggerInterceptor implements RouterClientInterceptor {

    @Override
    public RouterResult intercept(Chain chain) {
        RouterContext context = chain.context();
        Scheme schemeDetail = context.getScheme();

        String scheme = schemeDetail.getScheme();
        String host = schemeDetail.getHost();
        String path = schemeDetail.getPath();
        Map<String, Object> params = schemeDetail.getParams();
        CRouterLogger.info("发起请求： scheme:" + scheme + " host:" + host + " path:" + path + " params:" + params);
        RouterResult routerResult = context.getRouterResult();
        CRouterLogger.info("请求结果：" + routerResult);
        return routerResult;
    }
}
