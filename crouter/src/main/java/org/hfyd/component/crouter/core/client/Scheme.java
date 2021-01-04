package org.hfyd.component.crouter.core.client;

import java.util.Map;

public class Scheme {

    private String scheme;

    private String host;

    private String path;

    private Map<String, Object> params;

    public Scheme(String scheme, String host, String path, Map<String, Object> params) {
        this.scheme = scheme;
        this.host = host;
        this.path = path;
        this.params = params;
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
}
