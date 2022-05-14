# `FASTJSON2`发布操作说明

`FASTJSON2`发布到`Maven`中央库的操作过程/CheckList。

## 0. 前置准备与配置

在`Maven`的`settting.xml`中配置`oss.sonatype.org`账号：

```xml
<servers>
    <server>
        <id>ossrh</id>
        <username>__YOUR_USERNAME__</username>
        <password>__YOUR_PASSWORD__</password>
    </server>
</servers>
```

更多发布操作说明（如用于`GPG`签名的`GPG`安装与配置），参见：

- OSSRH Guide  
  https://central.sonatype.org/pages/ossrh-guide.html
- Deploying to OSSRH with Apache Maven - Introduction  
  https://central.sonatype.org/pages/apache-maven.html

发布过程与发布文件的查看地址：

- sonatype的发布控制台  
  https://oss.sonatype.org/index.html
- Maven中央库的文件查看  
  https://repo1.maven.org/maven2/com/alibaba/fastjson2/


## 1. 发布操作

1. 如果发布正式版本，先确认版本号，去掉`SNAPSHOT`，如`2.x.y`。
1. 更新版本操作可以通过脚本[`scripts/bump_fastjson2_version`](../scripts/bump_fastjson2_version)来统一完成。

    ```bash
    scripts/bump_fastjson2_version 2.x.y
    ```
1. 在工程根目录，执行发布

    ```bash
    ./mvnw clean && ./mvnw deploy -DperformRelease
    ```
