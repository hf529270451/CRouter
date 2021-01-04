# CRouter
CRouter为Android的组件化路由框架，融合了scheme字符串协议以及api register两种调用方式，是做了多年组件化后融合多个组件化方案后的框架。

### CRouter的优点
##### 支持Client端多种调用方式
- api方法调用：通过直接调用组件暴露出来的接口方法进行调用，Client组件与Server组件只依赖接口，不依赖实现。

     优点：调用方式简单易懂，隐藏了scheme字符串复杂的调用协议。

 例如：
```
// 获取ModuleB对外暴露的接口
final ModuleAServerControllerApi api = CRouter.api(ModuleAServerControllerApi.class);

// 同步调用
RouterResult result = api.getModuleAInfo(new ModuleAParam("ModuleAParam1"), 200).routeSync();
```

- scheme字符串协议调用：通过scheme协议调用对应组件暴露的方法，更加灵活，在做混合开发路由打通的时候有意想不到的效果。

例如：
```
RouterResult result = CRouter.newInstance()
                .with(MainActivity.this)
                .scheme("myScheme")
                .host("moduleB")
                .path("/getModuleAInfo")
                .routeSync();

```


