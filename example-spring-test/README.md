### 启动方式

运行SpringTestApplication.main方法即可

###  测试

访问[http://localhost:8080/test?username=123&password=456](http://localhost:8080/test?username=123&password=456)，测试HttpMessageConverter是否正常工作。
访问[http://localhost:8080/hello](http://localhost:8080/hello)进入一个聊天页面，测试websocket是否正常工作。
访问任意找不到的地址，即可测试FastJsonJsonView，错误页面已经使用FastJsonJsonView。
