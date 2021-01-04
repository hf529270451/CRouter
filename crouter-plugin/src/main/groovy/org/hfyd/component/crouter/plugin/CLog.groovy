package org.hfyd.component.crouter.plugin


class CLog {

    private static boolean debug = true
    public static String TAG = "CRouterPlugin"

    /**
     * 设置是否打印日志
     */
    static void setDebug(boolean isDebug) {
        debug = isDebug
    }

    /**
     * 打印日志
     */
    static e(Object msg) {
        if (debug)
            try {
                println "${LogUI.C_ERROR.value}[$TAG]: ${msg}${LogUI.E_NORMAL.value}"
            } catch (Exception e) {
                e.printStackTrace()
            }
    }

    /**
     * 打印日志
     */
    static i(Object msg) {
        if (debug)
            try {
                println "[$TAG]: $msg"
            } catch (Exception e) {
                e.printStackTrace()
            }
    }

    enum LogUI {
        //color
        C_ERROR("\033[40;31m"),
        //end
        E_NORMAL("\033[0m");

        private final String value

        LogUI(String value) {
            this.value = value
        }

        String getValue() {
            return this.value
        }
    }
}