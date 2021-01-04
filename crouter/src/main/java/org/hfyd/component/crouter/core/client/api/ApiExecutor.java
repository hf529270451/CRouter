package org.hfyd.component.crouter.core.client.api;


import android.content.Context;

import org.hfyd.component.crouter.CRouter;
import org.hfyd.component.crouter.RouterConfig;
import org.hfyd.component.crouter.anno.Host;
import org.hfyd.component.crouter.anno.Id;
import org.hfyd.component.crouter.anno.Param;
import org.hfyd.component.crouter.anno.Path;
import org.hfyd.component.crouter.core.CRouterLogger;
import org.hfyd.component.crouter.core.client.RouterGlobal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ApiExecutor {

    private Map<Method, ApiMethodCall> mApiMap;

    private ApiExecutor(Map<Method, ApiMethodCall> apiMap) {
        this.mApiMap = apiMap;
    }

    static ApiExecutor parseClass(Class apiClass) {
        if (apiClass == null) {
            return null;
        }

        Method[] declaredMethods = apiClass.getDeclaredMethods();
        Map<Method, ApiMethodCall> apiMap = null;
        if (declaredMethods.length > 0) {
            apiMap = new ConcurrentHashMap<>();
            for (Method declaredMethod : declaredMethods) {
                Path pathAnno = declaredMethod.getAnnotation(Path.class);
                if (pathAnno != null) {
                    String schemeStr;
                    String host;

                    org.hfyd.component.crouter.anno.Scheme schemeAnno = declaredMethod.getAnnotation(org.hfyd.component.crouter.anno.Scheme.class);
                    Host hostAnno = declaredMethod.getAnnotation(Host.class);

                    if (schemeAnno != null) {
                        schemeStr = schemeAnno.value();
                    } else {
                        schemeStr = RouterGlobal.getDefaultScheme();
                    }

                    if (hostAnno == null) {
                        CRouterLogger.error("生成 ApiExecutor 失败，host为空");
                        return null;
                    }

                    host = hostAnno.value();

                    ApiMethodCall apiMethodCall = parseMethod(schemeStr, host, declaredMethod);
                    Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                    Annotation[][] parameterAnnotations = declaredMethod.getParameterAnnotations();
                    if (parameterTypes.length > 0 && apiMethodCall != null) {
                        apiMethodCall.params = new ArrayList<>();
                        ApiMethodCall.ParamAttr attr;
                        for (int i = 0; i < parameterTypes.length; i++) {
                            Class parameterType = parameterTypes[i];
                            Annotation[] annotations = parameterAnnotations[i];
                            attr = new ApiMethodCall.ParamAttr();
                            if (getParamsAnnotation(annotations, Id.class) != null) {
                                attr.type = ApiMethodCall.ParamAttr.Type.Id;
                            } else if (getParamsAnnotation(annotations, Param.class) == null && parameterType == Context.class) {
                                attr.type = ApiMethodCall.ParamAttr.Type.Context;
                            } else if (getParamsAnnotation(annotations, Param.class) != null) {
                                attr.type = ApiMethodCall.ParamAttr.Type.Param;
                                Param paramAnno = (Param) getParamsAnnotation(annotations, Param.class);
                                attr.paramName = paramAnno.value();
                            } else {
                                CRouterLogger.error("生成api call失败，不支持的类型");
                                return null;
                            }

                            apiMethodCall.params.add(attr);
                        }
                    }


                    if (apiMethodCall != null) {
                        apiMap.put(declaredMethod, apiMethodCall);
                    }
                }
            }
        }

        return new ApiExecutor(apiMap);
    }

    private static Annotation getParamsAnnotation(Annotation[] annotations, Class annotationClass) {
        if (annotations != null) {
            for (Annotation item : annotations) {
                if (item.annotationType() == annotationClass) {
                    return item;
                }
            }
        }

        return null;
    }

    CRouter invoke(Method method, Object[] args) {
        if (mApiMap == null || mApiMap.size() == 0) {
            CRouterLogger.error("api call失败，没有找个这个方法");
            return null;
        }

        ApiMethodCall apiMethodCall = mApiMap.get(method);
        if (apiMethodCall == null) {
            CRouterLogger.error("api call失败，没有找个这个方法");
            return null;
        }

        List<ApiMethodCall.ParamAttr> paramsAttrs = apiMethodCall.params;
        if (paramsAttrs.size() != args.length) {
            CRouterLogger.error("api call失败，参数数量错误");
            return null;
        }

        CRouter cRouter = CRouter.newInstance();
        cRouter.scheme(apiMethodCall.scheme);
        cRouter.host(apiMethodCall.host);
        cRouter.path(apiMethodCall.path);

        if (paramsAttrs.size() > 0) {
            for (int i = 0; i < paramsAttrs.size(); i++) {
                ApiMethodCall.ParamAttr paramAttr = paramsAttrs.get(i);
                Object arg = args[i];
                if (paramAttr.type == ApiMethodCall.ParamAttr.Type.Context) {
                    cRouter.with((Context) arg);
                } else if (paramAttr.type == ApiMethodCall.ParamAttr.Type.Param) {
                    cRouter.param(paramAttr.paramName, arg);
                }
            }
        }

        return cRouter;
    }

    private static ApiMethodCall parseMethod(String scheme, String host, Method declaredMethod) {
        Path pathAnno = declaredMethod.getAnnotation(Path.class);
        if (pathAnno == null) {
            return null;
        }

        ApiMethodCall apiMethodCall = new ApiMethodCall();
        apiMethodCall.scheme = scheme;
        apiMethodCall.host = host;
        apiMethodCall.path = pathAnno.value();

        return apiMethodCall;
    }
}
