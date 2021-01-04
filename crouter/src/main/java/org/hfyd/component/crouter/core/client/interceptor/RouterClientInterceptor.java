package org.hfyd.component.crouter.core.client.interceptor;

import org.hfyd.component.crouter.core.client.RouterResult;
import org.hfyd.component.crouter.core.client.Scheme;

public interface RouterClientInterceptor {

    RouterResult intercept(Chain chain) throws Exception;

    interface Chain {

        RouterContext context();

        RouterResult proceed(Scheme scheme) throws Exception;
    }
}
