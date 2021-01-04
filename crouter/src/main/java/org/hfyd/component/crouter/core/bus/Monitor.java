package org.hfyd.component.crouter.core.bus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

final class Monitor {

    public interface Timeout {

        void timeout();

        long timeoutTime();

        boolean isFinished();

        String id();
    }

    private static Map<String, Timeout> sTimeoutMonitorMap = new ConcurrentHashMap<>();
    private static final AtomicBoolean STOPPED = new AtomicBoolean(true);
    private static volatile long minTimeoutAt = Long.MAX_VALUE;
    private static final byte[] LOCK = new byte[0];

    static void monitorTimeout(Timeout call) {
        if (call != null) {
            sTimeoutMonitorMap.put(call.id(), call);
            long timeoutAt = call.timeoutTime();
            if (timeoutAt > 0) {
                if (minTimeoutAt > timeoutAt) {
                    minTimeoutAt = timeoutAt;
                    synchronized (LOCK) {
                        LOCK.notifyAll();
                    }
                }
                if (STOPPED.compareAndSet(true, false)) {
                    new TimeoutMonitorThread().start();
                }
            }
        }
    }

    static void unMonitorTimeout(Timeout call) {
        sTimeoutMonitorMap.remove(call.id());
    }

    private static class TimeoutMonitorThread extends Thread {

        @Override
        public void run() {
            if (STOPPED.get()) {
                return;
            }
            while(sTimeoutMonitorMap.size() > 0 || minTimeoutAt == Long.MAX_VALUE) {
                try {
                    long millis = minTimeoutAt - System.currentTimeMillis();
                    if (millis > 0) {
                        synchronized (LOCK) {
                            LOCK.wait(millis);
                        }
                    }
                    long min = Long.MAX_VALUE;
                    long now = System.currentTimeMillis();
                    for (Timeout timeout : sTimeoutMonitorMap.values()) {
                        if (!timeout.isFinished()) {
                            long timeoutAt = timeout.timeoutTime();
                            if (timeoutAt > 0) {
                                if (timeoutAt < now) {
                                    timeout.timeout();
                                } else if (timeoutAt < min) {
                                    min = timeoutAt;
                                }
                            }
                        }
                    }
                    minTimeoutAt = min;
                } catch (InterruptedException ignored) {
                }
            }
            STOPPED.set(true);
        }
    }
}
