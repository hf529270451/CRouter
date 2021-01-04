package org.hfyd.component.crouter.core.bus;

import android.content.Context;
import android.text.TextUtils;

import org.hfyd.component.crouter.core.server.ServerEntry;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Bus {

    private static Bus mBus;

    public static class Result {
        private int code;
        private String msg;
        private Object result;

        public Result(int code, String msg, Object result) {
            this.code = code;
            this.msg = msg;
            this.result = result;
        }

        public static Result error(int code) {
            return new Result(code, "", null);
        }

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }

        public Object getResult() {
            return result;
        }
    }

    public static class Call implements Monitor.Timeout {
        private String callId;
        private String scheme;
        private String host;
        private String path;
        private Context context;
        private Map<String, Object> params;
        private boolean isAsync;
        private Callback callback;
        private long timeout;
        private long timeoutTime;
        private volatile boolean isFinished;

        public Call(String scheme, String host, String path, Map<String, Object> params, boolean isAsync) {
            this.scheme = scheme;
            this.host = host;
            this.path = path;
            this.params = params;
            this.callId = createCallId();
            this.isAsync = isAsync;
        }


        public void setFinished(boolean isFinished) {
            this.isFinished = isFinished;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
            this.timeoutTime = System.currentTimeMillis() + timeout;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }

        private static String prefix;
        private static AtomicInteger index = new AtomicInteger(1);

        private String createCallId() {
            if (TextUtils.isEmpty(prefix)) {
                Context context = getContext();
                if (context != null) {
                    prefix = context.getPackageName() + ":";
                } else {
                    return ":::" + index.getAndIncrement();
                }
            }
            return prefix + index.getAndIncrement();
        }

        public boolean isAsync() {
            return isAsync;
        }

        public Callback getCallback() {
            return callback;
        }

        public String getCallId() {
            return callId;
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


        @Override
        public void timeout() {
            if (isAsync && callback != null) {
                this.callback.onComplete(new Result(ProtocolCode.TIMEOUT.getCode(), ProtocolCode.TIMEOUT.getMsg(), null));
                this.callback = null;
            }
        }

        @Override
        public long timeoutTime() {
            return this.timeoutTime;
        }

        @Override
        public boolean isFinished() {
            return isFinished;
        }

        @Override
        public String id() {
            return callId;
        }
    }

    public interface Callback {
        void onComplete(Result result);
    }

    private Bus() {
    }

    public static Bus getInstance() {
        if (mBus == null) {
            mBus = new Bus();
        }

        return mBus;
    }

    public Result connect(final Call call) throws Exception {
        if (call == null) {
            throw new ConnectException("参数异常");
        }

        ServerEntry serverEntry = ServerRegister.findServerEntry(call.getScheme());
        if (serverEntry == null) {
            Result errorResult = new Result(ProtocolCode.PATH_NOT_FIND.getCode(),
                    ProtocolCode.PATH_NOT_FIND.getMsg(), null);
            if (call.isAsync()) {
                call.getCallback().onComplete(errorResult);
            }

            return errorResult;
        }

        if (call.isAsync()) {
            final Callback callback = call.getCallback();
            if (callback != null) {
                call.setCallback(new MonitorCallback(call, call.getCallback()));
            }

            return serverEntry.execute(call);
        } else {
            Result result = serverEntry.execute(call);
            call.setFinished(true);
            return result;
        }
    }
}
