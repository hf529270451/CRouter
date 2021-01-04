package org.hfyd.component.crouter.core.server;

import android.content.Context;

import org.hfyd.component.crouter.core.bus.Bus;
import org.hfyd.component.crouter.core.bus.ServerRegister;
import org.hfyd.component.crouter.core.client.RouterGlobal;

import java.util.Map;

public abstract class ServerController {

    public static class Request {
        private String callId;
        private String path;
        private Context context;
        private Map<String, Object> params;

        private Request() {
        }

        public String getCallId() {
            return callId;
        }

        public String getPath() {
            return path;
        }

        public Map<String, Object> getParamsMap() {
            return params;
        }

        static Request from(Bus.Call call) {
            Request request = new Request();
            request.callId = call.getCallId();
            request.path = call.getPath();
            request.params = call.getParams();
            request.context = call.getContext();
            return request;
        }

        public Context getContext() {
            return context;
        }

        public Object getRequestParamByName(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                return paramsMap.get(paramName);
            }

            return null;
        }

        public byte getRequestParamByNameForByte(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                Object value = paramsMap.get(paramName);
                if (value == null) {
                    return 0;
                }

                return Byte.parseByte(value.toString());
            }

            return 0;
        }

        public short getRequestParamByNameForShort(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                Object value = paramsMap.get(paramName);
                if (value == null) {
                    return 0;
                }

                return Short.parseShort(value.toString());
            }

            return 0;
        }

        public int getRequestParamByNameForInt(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                Object value = paramsMap.get(paramName);
                if (value == null) {
                    return 0;
                }

                return Integer.parseInt(value.toString());
            }

            return 0;
        }

        public long getRequestParamByNameForLong(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                Object value = paramsMap.get(paramName);
                if (value == null) {
                    return 0;
                }

                return Long.parseLong(value.toString());
            }

            return 0;
        }

        public float getRequestParamByNameForFloat(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                Object value = paramsMap.get(paramName);
                if (value == null) {
                    return 0;
                }

                return Float.parseFloat(value.toString());
            }

            return 0;
        }

        public double getRequestParamByNameForDouble(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                Object value = paramsMap.get(paramName);
                if (value == null) {
                    return 0;
                }

                return Double.parseDouble(value.toString());
            }

            return 0;
        }

        public boolean getRequestParamByNameForBoolean(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                Object value = paramsMap.get(paramName);
                if (value == null) {
                    return false;
                }

                return Boolean.parseBoolean(value.toString());
            }

            return false;
        }

        public char getRequestParamByNameForChar(String paramName) {
            Map<String, Object> paramsMap = params;
            if (paramsMap != null && paramsMap.size() > 0) {
                Object value = paramsMap.get(paramName);
                if (value == null) {
                    return 0;
                }

                return (char) value;
            }

            return 0;
        }

    }

    public static class Response {
        private int code;
        private String msg;
        private Object body;
        private boolean isAsync;
        private Bus.Callback callback;

        public int getCode() {
            return code;
        }

        void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        void setMsg(String msg) {
            this.msg = msg;
        }

        Object getBody() {
            return body;
        }

        void setBody(Object body) {
            this.body = body;
        }

        boolean isAsync() {
            return isAsync;
        }

        void setAsync(boolean async) {
            isAsync = async;
        }

        Bus.Callback getCallback() {
            return callback;
        }

        void setCallback(Bus.Callback callback) {
            this.callback = callback;
        }
    }

    public abstract String getHost();

    public abstract boolean dispatchPath(Request request);

    public String getScheme() {
        return RouterGlobal.getDefaultScheme();
    }

}
