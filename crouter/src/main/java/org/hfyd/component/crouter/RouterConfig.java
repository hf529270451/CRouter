package org.hfyd.component.crouter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.hfyd.component.crouter.core.CRouterLogger;
import org.hfyd.component.crouter.core.bus.ServerRegister;
import org.hfyd.component.crouter.core.client.RouterGlobal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RouterConfig {

    private Context context;

    private String scheme;

    private String host;

    private String path;

    private Map<String, Object> params;

    private long timeout;

    private RouterConfig(Context context, String scheme, String host, String path, Map<String, Object> params, long timeout) {
        this.context = context;
        this.scheme = scheme;
        this.host = host;
        this.path = path;
        this.params = params;
        this.timeout = timeout;
    }

    public Context getContext() {
        return context;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public long getTimeout() {
        return timeout;
    }

    public static class Builder {

        private Context context;

        private String scheme;

        private String host;

        private String path;

        private long timeout = -1;

        private Map<String, Object> params;

        public Builder() {
            params = new HashMap<>();
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder addParam(String name, Object value) {
            this.params.put(name, value);
            return this;
        }

        public Builder addParams(Map<String, Object> params) {
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    this.params.put(param.getKey(), param.getValue());
                }
            }
            return this;
        }


        public Builder uri(Uri uri) {
            if (uri == null || TextUtils.isEmpty(uri.getScheme()) || TextUtils.isEmpty(uri.getHost())
                    || TextUtils.isEmpty(uri.getPath())) {
                CRouterLogger.error("scheme uri 不合法");
                return this;
            }

            setScheme(uri.getScheme());
            setHost(uri.getHost());
            setPath(uri.getPath());
            Set<String> queryParameterNames = uri.getQueryParameterNames();
            if (queryParameterNames != null && queryParameterNames.size() > 0) {
                for (String queryParameterName : queryParameterNames) {
                    addParam(queryParameterName, uri.getQueryParameter(queryParameterName));
                }
            }
            return this;
        }

        public RouterConfig build() {
            if (context == null) {
                context = RouterGlobal.getApplication();
            }

            if (scheme == null) {
                scheme = RouterGlobal.getDefaultScheme();
            }

            return new RouterConfig(context, scheme, host, path, params, timeout);
        }

    }
}
