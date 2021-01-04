# CRouter
CRouter为Android的组件化路由框架，融合了scheme字符串协议以及api register两种调用方式，是做了多年组件化后融合多个组件化方案后的框架。

### CRouter的优点
#### 支持Client端多种调用方式
- api方法调用：通过直接调用组件暴露出来的接口方法进行调用，Client组件与Server组件只依赖接口，不依赖实现，调用方式简单易懂，隐藏了scheme字符串复杂的调用协议。

例如：
``` java
// 获取ModuleB对外暴露的接口
final ModuleAServerControllerApi api = CRouter.api(ModuleAServerControllerApi.class);

// 同步调用
RouterResult result = api.getModuleAInfo(new ModuleAParam("ModuleAParam1"), 123).routeSync();
```

- scheme字符串协议调用：通过scheme协议调用对应组件暴露的方法，更加灵活，在做混合开发路由打通的时候有意想不到的效果。

例如：
``` java
RouterResult result = CRouter.newInstance()
                .with(MainActivity.this)
                .scheme("myScheme")
                .host("moduleB")
                .path("/getModuleAInfo")
                .param("p1", 123")
                .routeSync();

```

- 无缝支持scheme uri，当后端下发scheme uri字符串的时候可以自动转换调用

例如：
``` java
// 调用moduleC的方法
RouterResult result = CRouter.newInstance()
                        .with(MainActivity.this)
                        .uri(Uri.parse("myScheme://moduleC/moduleCSchemeUriActivity?p1=fromMainActivity&p2=2003&p3=true"))
                        .routeSync();
```

#### Server端的灵活配置
- 类似于spring的参数注入，参数自动拆装箱，更直观。

例如：
``` java
@Host("moduleA")
@Controller("org.hfyd.component.a.ModuleAServerControllerImpl")
public interface ModuleAServerController {

    @Path("/getModuleAInfoSync")
    void getModuleAInfo(@Id String callId, @Param("p1") ModuleAParam p1, @Param("p2") long p2);
}
```


### demo目录结构
```
 - crouter              核心库
 - crouter-anno         配置所需要的注解
 - crouter-processor    api代码自动生成的库
 - crouter-plugin       插件库，用于上传api接口以及接口自动装配
 - app                  壳工程
 - base                 项目基础服务
 - component_a          组件A
 - component_b          组建B
 - component_c          组建C
 - repo                 本地maven仓库
```

## 开始使用

#### 初始化
Application中添加：
``` java
// 初始化以及自动注册组建
CRouter.init(getApplication());

// 开启debug
CRouterLogger.debugger(BuildConfig.DEBUG);
```

base组建的build.gradle中添加CRouter核心库依赖

最外层的build.gradle中添加CRouter-plugin的依赖

每个子组件中添加CRouter-processor的依赖以及插件

manifest里添加全局默认d的cheme：
``` xml
<meta-data android:name="org.hfyd.component.crouter.SCHEME" android:value="czb365"/>
```

#### Server组建注册：
1. Server组建的build.gradle中添加，添加后同步gradle：
``` groovy
apply plugin: 'crouter'

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                        // CROUTER_SCHEME：非必填，为设置单个组建的scheme前缀，默认manifest里全局的scheme
                        CROUTER_SCHEME     : "",
                        // CROUTER_API_PACKAGE：必填，src/main/${package}.api路径，这个路径下所有对外
                        // 暴露的接口会被打到jar包里。
                        CROUTER_API_PACKAGE: "org.hfyd.component.a.api"
                ]
            }
        }
    }
}

crouter {
    // 内部为maven-publish插件 用来上传api接口
    publishConfig = publishing {
        publications {
            maven(MavenPublication) {
                groupId "org.hfyd.component.a"
                artifactId "api"
                version "1.0.2-SNAPSHOT"
            }
        }

        repositories {
            maven {
                url = rootProject.file('./repo')
            }
        }
    } 
}

dependencies {
     annotationProcessor "$CRouter_processor"
}
```

2. 同步后会在src/main下发现一个api文件，手动创建名字为 ${package}.api 的包，例如org.component.a.api,创建完成后开始注册组建api。

``` java
// @host为本组件的host路径，建议每个组建用一个host
// @Controller中填入实现类的全路径，用来自动映射
// @Path 方法的path
// @Id 用来标示此次调用，用来进行结果返回
// @Param 用来表示参数，参数中对外暴露的对象也需要声明在api文件夹下
// Context 每个方法会自动注入一个Context，即调用方with(Context)传过来的

// 在${package}.api下创建要向外暴露的接口,
// rebuild后会自动生成ModuleAServerControllerApi和ModuleAServerController_ServerController
// ModuleAServerControllerApi为client端调用的api，ModuleAServerController_ServerController为事件分发的类
@Host("moduleA")
@Controller("org.hfyd.component.a.ModuleAServerControllerImpl")
public interface ModuleAServerController {
    
    @Path("/getModuleAInfoSync")
    void getModuleAInfo(@Id String callId, @Param("p1") ModuleAParam p1, @Param("p2") long p2);

    @Path("/startModuleAActivity")
    void startModuleAActivity(Context context, @Id String callId, @Param("p1") ModuleAParam p1, @Param("p2") boolean p2);
}

// 在package下创建实现类
public class ModuleAServerControllerImpl implements ModuleAServerController {

    private static Handler sMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void getModuleAInfo(String callId, ModuleAParam p1, long p2) {
        Log.e("CRouter", "module:ModuleA method:getModuleAInfo params:{p1:" + p1 + ",p2:" + p2 + "}");
        // ResponseDispatch返回结果
        ResponseDispatch.send(callId, ResponseResult.success(new ModuleAResult(p1.getName(), p2)));
    }

    @Override
    public void startModuleAActivity(final Context context, final String callId, final ModuleAParam p1, final boolean p2) {
        Intent intent = new Intent(context, ModuleAActivity.class);
        intent.putExtra("p1", p1);
        intent.putExtra("p2", p2);
        context.startActivity(intent);
        // ResponseDispatch返回结果
        ResponseDispatch.send(callId, ResponseResult.success(new ModuleAResult("startModuleAActivityResult", 100)));
    }
}

```

3. 如果需要方法调用，执行crouter下的publishApiToMaven 将api接口上传到maven仓库，如果是scheme字符串调用，不需要调用task。

#### Client组建调用：
1. scheme直接调用：
``` java
CRouter.newInstance()
                // contxt
                .with(MainActivity.this)
                // scheme可不填，默认为全局在manifest里面注册的
                .scheme("myScheme")
                // host
                .host("moduleB")
                // path
                .path("/getModuleAInfo")
                // 参数
                .param("p1", 123")
                // 同步调用，也可以routerAsync异步调用
                .routeSync();

```

2. api调用
1) 在client端的build.gradle中添加server端组件api的依赖

``` groovy
dependencies {
    compileApi 'org.hfyd.component.b:api:1.0.1-SNAPSHOT'
    compileApi 'org.hfyd.component.c:api:1.0.3-SNAPSHOT'
}
```

2) api调用
``` java
    
// 获取ModuleB对外暴露的接口
final ModuleAServerControllerApi api = CRouter.api(ModuleAServerControllerApi.class);

// 同步调用
RouterResult result = api.getModuleAInfo(new ModuleAParam("ModuleAParam1"), 123).routeSync();
```







