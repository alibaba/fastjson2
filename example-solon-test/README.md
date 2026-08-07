### 启动方式

运行 SolonTestApp.main 方法（或者直接运行单测 DemoTest ）

###  测试

测试 Render 是否正常工作

* 访问：[http://localhost:8080/demo?username=world&password=1234](http://localhost:8080/demo?username=world&password=1234)

测试 找不到的地址异常（技术上和上面一样，Solon 所有的 Web 输出都会走：Render 接口）

* 访问：[http://localhost:8080/error](http://localhost:8080/error)

### 说明

此演示，会自动触发 fastjson2-extension-solon 的:

* Fastjson2RenderFactory 
* Fastjson2ActionExecutor
