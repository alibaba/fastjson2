# 1. 测试环境
## 1.1 服务器环境
测试的服务器基于阿里云最新代ECS，包括x64架构的Intel和AMD处理器，ARM架构的AltraMax和阿里云平头哥的倚天处理器。
* ecs.c7.xlarge
这个是阿里云当前代标准型ECS，处理器型号 Intel Xeon(Ice Lake) Platinum 8369B，4核，8G内存
  ![](ecs.c7.xlarge.png)


* ecs.c7a.xlarge
  这个是阿里云当前代标准型ECS，处理器型号 AMD EPYC™ Milan 7T83，4核，8G内存
  ![](ecs.c7a.xlarge.png)
* 
* ecs.c6r.xlarge
  这个是阿里云上售卖ARM处理器ECS，处理器型号 Ampere Altra / AltraMax，4核，8G内存
![](ecs.c6r.xlarge.png)

* ecs.g8m.xlarge
  这个是阿里云上售卖ARM处理器，处理器型号 Yitian 710，4核，16G内存。这个是阿里云平头哥的倚天710处理器，需要联系客服才能购买。
  ![](ecs.g8m.xlarge.png)

## 1.2 JDK版本
基于Oracle最新版本的Linux x64/aarch64的JDK版本，下载地址 https://www.oracle.com/java/technologies/
* oracle-jdk1.8.0_341
* oracle-jdk-11.0.16
* oracle-jdk-17.0.4
* oracle-jdk-18.0.2

## 1.3 测试代码以及运行方式
* 代码路径
https://github.com/alibaba/fastjson2/tree/2.0.12/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay

* 运行方式测试代码方式
```shell
git clone https://github.com/alibaba/fastjson2
cd fastjson2
git checkout 2.0.12
mvn clean install -Dmaven.test.skip
~/Install/jdk-1.8.0_341/bin/java -cp ~/git/fastjson2/benchmark/target/fastjson2-benchmarks.jar com.alibaba.fastjson2.benchmark.eishay.Eishay
~/Install/jdk-11.0.16/bin/java -cp ~/git/fastjson2/benchmark/target/fastjson2-benchmarks.jar com.alibaba.fastjson2.benchmark.eishay.Eishay
~/Install/jdk-17.0.4/bin/java -cp ~/git/fastjson2/benchmark/target/fastjson2-benchmarks.jar com.alibaba.fastjson2.benchmark.eishay.Eishay
~/Install/jdk-18.0.2/bin/java -cp ~/git/fastjson2/benchmark/target/fastjson2-benchmarks.jar com.alibaba.fastjson2.benchmark.eishay.Eishay
```

## EishayParseString
这个是最常用的场景，将JSON格式字符串反序列化为Java对象，这个场景在fastjson中的代码如下：
```java
String str = "...";
Bean bean = JSON.parseJSONObject(str, Bean.class);
```

这个场景是fastjson1最强的场景；

在ecs.c7.xlarge-oracle-jdk1.8.0_341_x64环境中，只相当于只相当于fastjson2的76.16%;jackson则只相当于fastjson2的41.62%; gson是fastjson2的34.44%.

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > fastjson1 > jackson > gson
100%        76.16%      41.62%    34.44%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	1280.79	|	975.417 (76.16%)	|	533.05 (41.62%)	|	441.098 (34.44%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	1156.746	|	923.166 (79.81%)	|	484.755 (41.91%)	|	432.261 (37.37%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	1271.058	|	1245.138 (97.96%)	|	488.057 (38.4%)	|	458.173 (36.05%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	1285.745	|	1162.7 (90.43%)	|	497.201 (38.67%)	|	416.679 (32.41%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	1232.166	|	997.019 (80.92%)	|	516.422 (41.91%)	|	454.289 (36.87%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	1074.775	|	928.247 (86.37%)	|	496.175 (46.17%)	|	468.162 (43.56%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	1237.042	|	1323.952 (107.03%)	|	529.334 (42.79%)	|	536.419 (43.36%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	1226.619	|	1192.121 (97.19%)	|	481.629 (39.26%)	|	467.084 (38.08%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	843.629	|	719.256 (85.26%)	|	383.128 (45.41%)	|	313.66 (37.18%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	828.013	|	734.647 (88.72%)	|	377.617 (45.61%)	|	318.57 (38.47%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	853.772	|	1020.291 (119.5%)	|	379.463 (44.45%)	|	318.711 (37.33%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	849.58	|	995.946 (117.23%)	|	362.179 (42.63%)	|	303.018 (35.67%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	917.802	|	737.61 (80.37%)	|	370.102 (40.32%)	|	353.573 (38.52%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	1026.274	|	802.58 (78.2%)	|	408.008 (39.76%)	|	376.903 (36.73%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	1030.558	|	1026.0 (99.56%)	|	392.415 (38.08%)	|	244.263 (23.7%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	1089.054	|	999.211 (91.75%)	|	396.401 (36.4%)	|	268.559 (24.66%) |


## EishayParseStringPretty
这个场景是将格式化过的JSON格式字符串反序列化为Java对象，这个场景fastjson1的parse算法不擅长的。这个场景fastjson2表现仍然非常好。
这个场景在ecs.c7.xlarge-oracle-jdk1.8.0_341_x64环境下，fastjson1的性能是fastjson2的27.29%，jackson的性能是fastjson2的50.56%，gson是fastjson2的43.01%.

这个场景在fastjson中的代码如下：
```java
// 这里输入的是格式化过后的json字符串
String str = "{\n" +
        "\t\"id\":123\n" +
        "}";
Bean bean = JSON.parseJSONObject(str, Bean.class);
```

性能排序分别如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > jackson > gson  > fastjson1
100%        50.56%    43.01%  27.29%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。


| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	963.195	|	262.851 (27.29%)	|	486.976 (50.56%)	|	414.293 (43.01%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	872.125	|	241.835 (27.73%)	|	477.263 (54.72%)	|	404.389 (46.37%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	955.065	|	299.972 (31.41%)	|	480.246 (50.28%)	|	420.784 (44.06%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	955.217	|	289.455 (30.3%)	|	462.899 (48.46%)	|	391.052 (40.94%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	915.908	|	280.182 (30.59%)	|	459.843 (50.21%)	|	426.466 (46.56%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	828.309	|	250.989 (30.3%)	|	461.404 (55.7%)	|	407.729 (49.22%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	849.51	|	301.561 (35.5%)	|	462.203 (54.41%)	|	485.299 (57.13%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	831.064	|	296.826 (35.72%)	|	468.978 (56.43%)	|	432.39 (52.03%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	669.478	|	208.992 (31.22%)	|	348.335 (52.03%)	|	277.598 (41.46%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	644.651	|	212.15 (32.91%)	|	341.676 (53%)	|	296.356 (45.97%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	655.857	|	241.67 (36.85%)	|	350.738 (53.48%)	|	293.984 (44.82%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	652.962	|	229.365 (35.13%)	|	332.02 (50.85%)	|	278.116 (42.59%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	714.792	|	214.501 (30.01%)	|	349.959 (48.96%)	|	323.449 (45.25%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	752.608	|	231.712 (30.79%)	|	383.271 (50.93%)	|	347.639 (46.19%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	784.183	|	259.68 (33.11%)	|	387.016 (49.35%)	|	317.655 (40.51%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	782.402	|	247.16 (31.59%)	|	359.573 (45.96%)	|	323.368 (41.33%) |

## EishayParseTreeString


这个场景是将JSON格式字符串反序列化为JSONObject，这也是一个最常见的场景之一，这个场景在fastjson中的代码如下：
```java
String str = "...";
JSONObject jsonObject = JSON.parseJSONObject(str);
```

fastjson2在这个场景也表现了远超fastjson1/jackson/gson的性能。
在不同的环境中，fastjson1和jackson都只能相当于fastjson1性能的40%~60%，gson则更差一些。

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > fastjson1 > jackson > gson
100%        55.39%      54.29%    36.53% 
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	933.434	|	517.022 (55.39%)	|	506.806 (54.29%)	|	340.962 (36.53%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	774.516	|	394.826 (50.98%)	|	440.933 (56.93%)	|	322.93 (41.69%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	1024.532	|	583.808 (56.98%)	|	505.817 (49.37%)	|	320.666 (31.3%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	1067.726	|	529.394 (49.58%)	|	471.898 (44.2%)	|	324.342 (30.38%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	880.183	|	474.715 (53.93%)	|	519.486 (59.02%)	|	362.992 (41.24%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	797.78	|	403.234 (50.54%)	|	484.84 (60.77%)	|	374.36 (46.93%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	979.102	|	536.398 (54.78%)	|	564.281 (57.63%)	|	397.783 (40.63%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	1010.951	|	521.952 (51.63%)	|	543.735 (53.78%)	|	396.25 (39.2%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	576.868	|	305.566 (52.97%)	|	356.642 (61.82%)	|	256.349 (44.44%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	533.525	|	319.366 (59.86%)	|	355.745 (66.68%)	|	260.384 (48.8%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	611.576	|	357.592 (58.47%)	|	385.607 (63.05%)	|	257.244 (42.06%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	612.693	|	340.3 (55.54%)	|	375.034 (61.21%)	|	255.753 (41.74%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	551.643	|	308.559 (55.93%)	|	320.324 (58.07%)	|	260.041 (47.14%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	655.95	|	361.855 (55.17%)	|	382.319 (58.28%)	|	306.29 (46.69%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	787.272	|	412.435 (52.39%)	|	426.889 (54.22%)	|	245.45 (31.18%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	793.481	|	368.496 (46.44%)	|	414.609 (52.25%)	|	279.315 (35.2%) |

## EishayParseTreeStringPretty

这个场景在fastjson中的代码如下：
```java
// 这里输入的是格式化过后的json字符串
String str = "{\n" +
        "\t\"id\":123\n" +
        "}"; 
JSONObject jsonObject = JSON.parseJSONObject(str);
```

fastjson2在这个场景也表现了远超fastjson1/jackson/gson的性能。

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > jackson > fastjson1 > gson
100%        63.68%    60.43%      43.28% 
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	744.185	|	449.72 (60.43%)	|	473.933 (63.68%)	|	322.106 (43.28%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	613.163	|	344.72 (56.22%)	|	417.245 (68.05%)	|	309.322 (50.45%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	814.548	|	483.549 (59.36%)	|	454.505 (55.8%)	|	301.853 (37.06%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	835.176	|	479.556 (57.42%)	|	459.29 (54.99%)	|	301.324 (36.08%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	705.798	|	423.443 (59.99%)	|	475.629 (67.39%)	|	333.679 (47.28%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	611.819	|	356.926 (58.34%)	|	441.875 (72.22%)	|	344.588 (56.32%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	752.479	|	472.299 (62.77%)	|	483.349 (64.23%)	|	371.936 (49.43%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	781.802	|	465.518 (59.54%)	|	504.176 (64.49%)	|	345.81 (44.23%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	493.539	|	267.924 (54.29%)	|	330.741 (67.01%)	|	239.599 (48.55%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	457.78	|	274.412 (59.94%)	|	326.581 (71.34%)	|	246.084 (53.76%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	501.855	|	311.258 (62.02%)	|	350.552 (69.85%)	|	245.072 (48.83%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	500.836	|	292.854 (58.47%)	|	337.513 (67.39%)	|	240.535 (48.03%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	465.583	|	286.793 (61.6%)	|	308.676 (66.3%)	|	248.297 (53.33%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	542.036	|	283.319 (52.27%)	|	355.39 (65.57%)	|	288.205 (53.17%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	618.605	|	367.727 (59.44%)	|	375.298 (60.67%)	|	265.321 (42.89%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	606.432	|	337.089 (55.59%)	|	355.232 (58.58%)	|	250.433 (41.3%) |


## EishayParseUTF8Bytes


这个是将UTF8格式的byte数组反序列化为Java对象，这个场景在缓存和RPC场景中常用。

这个场景在fastjson中的代码如下：
```java
byte[] utf8Bytes = ...;
Bean bean = JSON.parseJSONObject(utf8Bytes, Bean.class);
```

这个场景fastjson2同样表现出了卓越的性能；在JDK8下，fastjson1和jackson的性能分别之后fastjson2的50.49%和63.22%;
gson不直接支持输入utf8Bytes，需要先构造字符串，性能只有fastjson2的35%。

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > fastjson1 > jackson > gson
100%        71.36%      57.27%    29.08%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	1083.498	|	773.228 (71.36%)	|	620.496 (57.27%)	|	315.12 (29.08%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	1110.444	|	755.773 (68.06%)	|	548.41 (49.39%)	|	306.689 (27.62%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	1207.359	|	951.476 (78.81%)	|	567.635 (47.01%)	|	316.653 (26.23%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	1121.7	|	893.757 (79.68%)	|	563.522 (50.24%)	|	319.072 (28.45%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	916.079	|	807.567 (88.15%)	|	605.357 (66.08%)	|	328.311 (35.84%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	1083.919	|	721.734 (66.59%)	|	587.903 (54.24%)	|	357.015 (32.94%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	1135.129	|	906.3 (79.84%)	|	619.861 (54.61%)	|	385.474 (33.96%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	1091.062	|	902.03 (82.67%)	|	584.882 (53.61%)	|	383.314 (35.13%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	723.162	|	563.093 (77.87%)	|	455.927 (63.05%)	|	239.648 (33.14%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	743.157	|	592.675 (79.75%)	|	442.215 (59.5%)	|	254.261 (34.21%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	766.054	|	758.582 (99.02%)	|	429.762 (56.1%)	|	253.173 (33.05%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	715.554	|	742.419 (103.75%)	|	417.566 (58.36%)	|	252.146 (35.24%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	806.11	|	658.904 (81.74%)	|	423.583 (52.55%)	|	240.131 (29.79%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	924.035	|	683.072 (73.92%)	|	478.147 (51.75%)	|	284.194 (30.76%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	975.486	|	767.593 (78.69%)	|	440.031 (45.11%)	|	276.31 (28.33%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	922.588	|	789.418 (85.57%)	|	453.087 (49.11%)	|	283.509 (30.73%) |

## EishayParseUTF8BytesPretty
这个是将格式化过后的UTF8格式的byte数组反序列化为Java对象，这个场景在缓存和RPC场景中常用。

这个场景在fastjson中的代码如下：
```java
// 这里输入的是格式化过后的json字符串
String str = "{\n" +
        "\t\"id\":123\n" +
        "}";
byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
Bean bean = JSON.parseJSONObject(utf8Bytes, Bean.class);
```

这个场景fastjson2同样表现出了卓越的性能；在JDK8下，fastjson1和jackson的性能分别之后fastjson2的53.72%和70.22%，
gson不直接支持输入utf8Bytes，需要先构造字符串，性能只有fastjson2的41.25%。

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > jackson > gson > fastjson1
100%        67.78%    37.4%  31.93%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	786.524	|	251.142 (31.93%)	|	533.112 (67.78%)	|	294.143 (37.4%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	834.493	|	225.711 (27.05%)	|	498.434 (59.73%)	|	303.7 (36.39%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	905.466	|	271.031 (29.93%)	|	508.311 (56.14%)	|	303.584 (33.53%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	872.907	|	258.943 (29.66%)	|	490.634 (56.21%)	|	292.248 (33.48%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	755.152	|	248.451 (32.9%)	|	537.181 (71.14%)	|	302.875 (40.11%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	796.538	|	228.723 (28.71%)	|	491.373 (61.69%)	|	342.609 (43.01%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	828.639	|	274.785 (33.16%)	|	557.601 (67.29%)	|	341.393 (41.2%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	818.144	|	262.646 (32.1%)	|	524.572 (64.12%)	|	351.046 (42.91%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	599.255	|	191.374 (31.94%)	|	416.723 (69.54%)	|	214.748 (35.84%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	590.414	|	193.621 (32.79%)	|	406.777 (68.9%)	|	240.051 (40.66%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	606.763	|	218.096 (35.94%)	|	395.387 (65.16%)	|	240.114 (39.57%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	573.736	|	208.397 (36.32%)	|	380.389 (66.3%)	|	234.282 (40.83%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	654.577	|	204.519 (31.24%)	|	372.8 (56.95%)	|	227.904 (34.82%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	703.777	|	152.967 (21.74%)	|	431.272 (61.28%)	|	275.286 (39.12%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	739.801	|	237.956 (32.16%)	|	400.672 (54.16%)	|	278.539 (37.65%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	702.402	|	218.875 (31.16%)	|	410.706 (58.47%)	|	271.897 (38.71%) |


## EishayParseTreeUTF8Bytes

这个是将UTF8格式的byte数组反序列化为JSONObject. 这个场景在fastjson中的代码如下：
```java
byte[] utf8Bytes = ...;
JSONObject jsonObject = JSON.parseJSONObject(utf8Bytes);
```

这个场景fastjson2同样表现出了卓越的性能；在JDK8下，fastjson1和jackson的性能分别之后fastjson2的50.49%和63.22%;
gson不直接支持输入utf8Bytes，需要先构造字符串，性能只有fastjson2的35%。

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > jackson > fastjson1 > gson
100%        63.22%    50.49%      35.5%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	882.065	|	445.319 (50.49%)	|	557.669 (63.22%)	|	313.124 (35.5%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	654.899	|	360.26 (55.01%)	|	514.301 (78.53%)	|	315.332 (48.15%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	1044.966	|	478.063 (45.75%)	|	568.644 (54.42%)	|	321.917 (30.81%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	1019.586	|	475.741 (46.66%)	|	554.104 (54.35%)	|	313.421 (30.74%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	724.629	|	426.142 (58.81%)	|	572.952 (79.07%)	|	328.595 (45.35%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	721.44	|	360.332 (49.95%)	|	565.566 (78.39%)	|	360.34 (49.95%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	852.787	|	452.351 (53.04%)	|	606.124 (71.08%)	|	392.469 (46.02%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	880.7	|	472.351 (53.63%)	|	597.554 (67.85%)	|	374.988 (42.58%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	532.788	|	249.575 (46.84%)	|	441.72 (82.91%)	|	240.525 (45.14%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	446.808	|	287.303 (64.3%)	|	413.758 (92.6%)	|	254.144 (56.88%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	590.05	|	317.036 (53.73%)	|	456.912 (77.44%)	|	253.741 (43%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	558.045	|	301.757 (54.07%)	|	434.725 (77.9%)	|	252.7 (45.28%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	501.696	|	253.89 (50.61%)	|	372.582 (74.26%)	|	242.819 (48.4%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	530.747	|	337.552 (63.6%)	|	437.781 (82.48%)	|	297.201 (56%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	771.518	|	348.721 (45.2%)	|	507.337 (65.76%)	|	275.168 (35.67%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	714.17	|	325.797 (45.62%)	|	477.229 (66.82%)	|	241.458 (33.81%) |


## EishayParseTreeUTF8BytesPretty
这个是将格式化过后的UTF8格式的byte数组反序列化为JSONObject， 这个场景在fastjson中的代码如下：
```java
// 这里输入的是格式化过后的json字符串
String str = "{\n" +
        "\t\"id\":123\n" +
        "}";
byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
JSONObject jsonObject = JSON.parseJSONObject(utf8Bytes);
```

这个场景fastjson2同样表现出了卓越的性能；在JDK8下，fastjson1和jackson的性能分别之后fastjson2的53.72%和70.22%，
gson不直接支持输入utf8Bytes，需要先构造字符串，性能只有fastjson2的41.25%。

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > jackson > fastjson1 > gson
100%        70.22%    53.72%      41.25%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	716.744	|	385.002 (53.72%)	|	503.301 (70.22%)	|	295.637 (41.25%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	601.404	|	314.029 (52.22%)	|	490.292 (81.52%)	|	295.215 (49.09%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	851.286	|	411.4 (48.33%)	|	512.209 (60.17%)	|	293.605 (34.49%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	840.136	|	401.867 (47.83%)	|	506.222 (60.25%)	|	291.464 (34.69%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	635.907	|	366.566 (57.64%)	|	516.876 (81.28%)	|	301.97 (47.49%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	582.825	|	312.162 (53.56%)	|	488.51 (83.82%)	|	332.122 (56.98%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	682.302	|	381.257 (55.88%)	|	547.474 (80.24%)	|	364.789 (53.46%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	749.046	|	389.891 (52.05%)	|	541.245 (72.26%)	|	357.097 (47.67%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	466.952	|	220.237 (47.16%)	|	402.868 (86.28%)	|	220.594 (47.24%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	389.837	|	243.253 (62.4%)	|	391.982 (100.55%)	|	240.36 (61.66%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	499.026	|	273.415 (54.79%)	|	406.731 (81.5%)	|	236.56 (47.4%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	474.922	|	264.985 (55.8%)	|	396.4 (83.47%)	|	235.098 (49.5%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	471.287	|	234.086 (49.67%)	|	344.737 (73.15%)	|	230.781 (48.97%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	470.475	|	231.123 (49.13%)	|	421.706 (89.63%)	|	286.665 (60.93%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	622.918	|	250.837 (40.27%)	|	441.649 (70.9%)	|	279.948 (44.94%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	583.691	|	209.574 (35.9%)	|	440.884 (75.53%)	|	277.102 (47.47%) |

## EishayWriteString

这个是Java对象序列化成字符串， 这个是最常用的场景之一。这个场景在fastjson中的代码如下：
```java
Bean bean = ...;
String str = JSON.toJSONString(bean);
```

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > jackson > fastjson1 > gson
100%        54.03%    37.61%      20.69%
```
下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	1581.037	|	594.658 (37.61%)	|	854.22 (54.03%)	|	327.195 (20.69%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	1343.414	|	595.695 (44.34%)	|	906.331 (67.46%)	|	275.727 (20.52%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	1712.266	|	613.577 (35.83%)	|	1001.97 (58.52%)	|	205.201 (11.98%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	1699.625	|	593.921 (34.94%)	|	916.976 (53.95%)	|	205.046 (12.06%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	1624.636	|	659.362 (40.59%)	|	904.775 (55.69%)	|	364.295 (22.42%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	1442.604	|	593.853 (41.17%)	|	916.225 (63.51%)	|	287.247 (19.91%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	1841.055	|	643.602 (34.96%)	|	925.489 (50.27%)	|	338.389 (18.38%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	1688.356	|	629.806 (37.3%)	|	870.935 (51.58%)	|	325.734 (19.29%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	1033.834	|	446.877 (43.23%)	|	578.324 (55.94%)	|	278.484 (26.94%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	1046.255	|	424.717 (40.59%)	|	608.651 (58.17%)	|	248.206 (23.72%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	1163.12	|	433.589 (37.28%)	|	637.242 (54.79%)	|	188.469 (16.2%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	1136.576	|	381.31 (33.55%)	|	597.276 (52.55%)	|	188.559 (16.59%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	1221.452	|	502.207 (41.12%)	|	643.246 (52.66%)	|	328.688 (26.91%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	1167.871	|	424.902 (36.38%)	|	650.535 (55.7%)	|	255.595 (21.89%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	1372.859	|	417.568 (30.42%)	|	664.706 (48.42%)	|	204.356 (14.89%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	1314.683	|	441.703 (33.6%)	|	564.61 (42.95%)	|	200.301 (15.24%) |

## EishayWriteUTF8Bytes

这个是Java对象序列化成UTF格式的byte数组， 这个是最常用的场景之一，在缓存和RPC场景常用。gson不直接支持，需要先序列化为String再转换为UTF8格式的byte数组。

这个场景在fastjson中的代码如下：
```java
Bean bean = ...;
byte[] utf8Bytes = JSON.toJSONBytes(bean);
```


性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2 > jackson > fastjson1 > gson
100%        59.13%    37.99%      19.52%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。


| 	 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	1504.496	|	571.509 (37.99%)	|	889.578 (59.13%)	|	293.743 (19.52%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	1592.624	|	507.917 (31.89%)	|	854.436 (53.65%)	|	276.88 (17.39%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	1856.092	|	570.574 (30.74%)	|	969.387 (52.23%)	|	202.302 (10.9%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	1835.109	|	548.702 (29.9%)	|	938.845 (51.16%)	|	203.566 (11.09%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	1487.078	|	582.897 (39.2%)	|	836.831 (56.27%)	|	333.118 (22.4%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	1642.28	|	543.887 (33.12%)	|	830.435 (50.57%)	|	273.111 (16.63%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	1769.397	|	576.354 (32.57%)	|	895.627 (50.62%)	|	338.069 (19.11%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	1809.864	|	554.232 (30.62%)	|	865.073 (47.8%)	|	320.201 (17.69%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	1111.549	|	407.663 (36.68%)	|	564.422 (50.78%)	|	233.167 (20.98%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	1167.558	|	387.93 (33.23%)	|	563.532 (48.27%)	|	245.837 (21.06%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	1165.606	|	385.793 (33.1%)	|	602.722 (51.71%)	|	188.328 (16.16%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	1178.795	|	350.563 (29.74%)	|	581.995 (49.37%)	|	187.292 (15.89%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	1329.046	|	437.516 (32.92%)	|	658.158 (49.52%)	|	289.968 (21.82%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	1232.089	|	399.648 (32.44%)	|	634.515 (51.5%)	|	266.952 (21.67%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	1360.522	|	356.383 (26.19%)	|	678.117 (49.84%)	|	197.732 (14.53%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	1412.507	|	401.57 (28.43%)	|	602.449 (42.65%)	|	195.807 (13.86%) |



## EishayWriteBinary
这个场景是将Java对象序列化为二进制的byte数组，是缓存序列化和RPC场景使用序列化协议的典型场景。

fastson2内置支持二进制格式jsonb，jsonb是fastjson2为了RPC场景设计的序列化和反序列化设置的高性能序列化协议。

jsonb有两种映射方式，将Java对象映射为类似JSONObject的KV格式，也可以映射类似JSONArray的数组格式。

fastjson2JSONBArrayMapping的代码如下：
```java
Bean bean = ...;
byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
```

fastjson2JSONB的代码如下：
```java
Bean bean = ...;
byte[] jsonbBytes = JSONB.toBytes(bean);
```

fastjson2UTF8Bytes的代码如下：
```java
Bean bean = ...;
byte[] utf8Bytes = JSON.toJSONBytes(bean);
```

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2JSONBArrayMapping > fastjson2JSONB > kryo  >  fastjson2UTF8Bytes > hessian > javaSerialize
165.59%                      100%             90.73%   71.59%               16.05%    10.37%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2JSONB	|	fastjson2JSONBArrayMapping	|	fastjson2UTF8Bytes	|	hessian	|	javaSerialize	|	kryo |
|-----|----------|----------|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	2109.521	|	3492.913 (165.58%)	|	1510.194 (71.59%)	|	338.479 (16.05%)	|	218.692 (10.37%)	|	1913.902 (90.73%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	1832.584	|	3305.27 (180.36%)	|	1587.198 (86.61%)	|	327.303 (17.86%)	|	204.381 (11.15%)	|	1914.781 (104.49%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	2398.784	|	3445.758 (143.65%)	|	1870.477 (77.98%)	|	323.179 (13.47%)	|	223.274 (9.31%)	|	1875.871 (78.2%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	2631.992	|	4095.755 (155.61%)	|	1821.928 (69.22%)	|	317.838 (12.08%)	|	241.661 (9.18%)	|	1807.544 (68.68%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	1977.848	|	3205.618 (162.08%)	|	1491.867 (75.43%)	|	414.534 (20.96%)	|	228.309 (11.54%)	|	1761.061 (89.04%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	1986.387	|	3064.238 (154.26%)	|	1696.878 (85.43%)	|	389.741 (19.62%)	|	242.035 (12.18%)	|	1981.922 (99.78%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	1974.045	|	3129.304 (158.52%)	|	1716.659 (86.96%)	|	411.243 (20.83%)	|	264.826 (13.42%)	|	1790.338 (90.69%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	2385.79	|	4073.694 (170.75%)	|	1742.115 (73.02%)	|	409.094 (17.15%)	|	260.802 (10.93%)	|	1721.088 (72.14%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	1331.057	|	1885.63 (141.66%)	|	1108.585 (83.29%)	|	297.788 (22.37%)	|	176.367 (13.25%)	|	1107.134 (83.18%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	1476.657	|	2319.398 (157.07%)	|	1159.41 (78.52%)	|	322.343 (21.83%)	|	189.048 (12.8%)	|	1296.507 (87.8%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	1443.992	|	2228.329 (154.32%)	|	1173.612 (81.28%)	|	317.746 (22%)	|	181.799 (12.59%)	|	1230.239 (85.2%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	1608.314	|	2627.964 (163.4%)	|	1165.379 (72.46%)	|	312.327 (19.42%)	|	195.064 (12.13%)	|	1282.308 (79.73%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	1577.12	|	2362.358 (149.79%)	|	1330.904 (84.39%)	|	349.276 (22.15%)	|	204.709 (12.98%)	|	1431.816 (90.79%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	1774.645	|	2726.764 (153.65%)	|	1360.397 (76.66%)	|	340.869 (19.21%)	|	218.516 (12.31%)	|	1555.83 (87.67%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	1732.468	|	2463.208 (142.18%)	|	1381.276 (79.73%)	|	331.195 (19.12%)	|	207.308 (11.97%)	|	1439.65 (83.1%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	2004.688	|	3157.957 (157.53%)	|	1417.265 (70.7%)	|	332.99 (16.61%)	|	224.267 (11.19%)	|	1454.408 (72.55%) |


## EishayParseBinary
这个场景是将二进制的byte数组反序列化为Java对象，是缓存序列化和RPC场景使用序列化协议的典型场景。

fastjson2JSONBArrayMapping的代码如下：
```java
byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
Bean bean = JSONB.parseObject(jsonbBytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
```

fastjson2JSONB的代码如下：
```java
byte[] jsonbBytes = JSONB.toBytes(bean);
Bean bean = JSONB.parseObject(jsonbBytes, Bean.class);
```

fastjson2UTF8Bytes的代码如下：
```java
byte[] utf8Bytes = JSON.toJSONBytes(bean);
Bean bean = JSON.parseObject(utf8Bytes, Bean.class);
```

性能排序如下：
```java
// ecs.c7.xlarge-oracle-jdk1.8.0_341_x64

fastjson2JSONBArrayMapping > kryo   >  fastjson2JSONB > fastjson2UTF8Bytes > hessian > javaSerialize
153.96%                      100%.25%  100%             67.16%               16.83%    2.84%
```

下面是不同CPU不同JDK下的性能比较表格，第一列是阿里云机器规格和JDK的版本信息，第二列到第五列是JMH的Throughput数据，越大越好。
第三列到第五列中的百分比是Throughput相比fastjson2的比，比如第三列的百分比是fastjson1/fastjson2的百分数表示。

| 	 	|	fastjson2JSONB	|	fastjson2JSONBArrayMapping	|	fastjson2UTF8Bytes	|	hessian	|	javaSerialize	|	kryo |
|-----|----------|----------|----------|----------|----------|-----|
| ecs.c7.xlarge-oracle-jdk1.8.0_341_x64	|	1648.648	|	2538.226 (153.96%)	|	1107.184 (67.16%)	|	277.395 (16.83%)	|	46.794 (2.84%)	|	1652.73 (100.25%) |
| ecs.c7.xlarge-oracle-jdk-11.0.16_x64	|	2178.064	|	3546.334 (162.82%)	|	1141.561 (52.41%)	|	248.849 (11.43%)	|	48.114 (2.21%)	|	1706.163 (78.33%) |
| ecs.c7.xlarge-oracle-jdk-17.0.4_x64	|	2210.446	|	3581.414 (162.02%)	|	1240.231 (56.11%)	|	257.332 (11.64%)	|	52.042 (2.35%)	|	1499.663 (67.84%) |
| ecs.c7.xlarge-oracle-jdk-18.0.2_x6	|	2208.152	|	3733.627 (169.08%)	|	1201.981 (54.43%)	|	246.276 (11.15%)	|	51.45 (2.33%)	|	1633.479 (73.97%) |
| ecs.c7a.xlarge-oracle-jdk1.8.0_341_x64	|	1293.382	|	1745.828 (134.98%)	|	929.428 (71.86%)	|	255.801 (19.78%)	|	43.298 (3.35%)	|	1446.488 (111.84%) |
| ecs.c7a.xlarge-oracle-jdk-11.0.16_x64	|	2067.736	|	3332.012 (161.14%)	|	1058.228 (51.18%)	|	243.973 (11.8%)	|	47.149 (2.28%)	|	1554.486 (75.18%) |
| ecs.c7a.xlarge-oracle-jdk-17.0.4_x64	|	2223.355	|	3609.628 (162.35%)	|	1120.472 (50.4%)	|	250.796 (11.28%)	|	53.126 (2.39%)	|	1623.24 (73.01%) |
| ecs.c7a.xlarge-oracle-jdk-18.0.2_x64	|	2114.786	|	3522.422 (166.56%)	|	1126.199 (53.25%)	|	246.965 (11.68%)	|	51.824 (2.45%)	|	1644.455 (77.76%) |
| ecs.c6r.xlarge-oracle-jdk1.8.0_341_aarch64	|	1256.386	|	2110.098 (167.95%)	|	730.412 (58.14%)	|	190.884 (15.19%)	|	36.785 (2.93%)	|	1300.187 (103.49%) |
| ecs.c6r.xlarge-oracle-jdk-11.0.16_aarch64	|	1418.018	|	2542.738 (179.32%)	|	756.803 (53.37%)	|	179.716 (12.67%)	|	37.508 (2.65%)	|	1242.747 (87.64%) |
| ecs.c6r.xlarge-oracle-jdk-17.0.4_aarch64	|	1303.159	|	2187.211 (167.84%)	|	761.374 (58.43%)	|	208.406 (15.99%)	|	39.672 (3.04%)	|	1223.11 (93.86%) |
| ecs.c6r.xlarge-oracle-jdk-18.0.2_aarch64	|	1271.578	|	2163.511 (170.14%)	|	719.966 (56.62%)	|	187.4 (14.74%)	|	37.463 (2.95%)	|	1189.698 (93.56%) |
| ecs.g8m.xlarge-oracle-jdk1.8.0_341_aarch64	|	1270.914	|	2272.559 (178.81%)	|	816.239 (64.22%)	|	200.373 (15.77%)	|	42.276 (3.33%)	|	1191.146 (93.72%) |
| ecs.g8m.xlarge-oracle-jdk-11.0.16_aarch64	|	1811.452	|	3101.861 (171.24%)	|	982.786 (54.25%)	|	200.661 (11.08%)	|	40.634 (2.24%)	|	1413.636 (78.04%) |
| ecs.g8m.xlarge-oracle-jdk-17.0.4_aarch64	|	1644.118	|	2692.082 (163.74%)	|	989.303 (60.17%)	|	156.648 (9.53%)	|	40.901 (2.49%)	|	1090.731 (66.34%) |
| ecs.g8m.xlarge-oracle-jdk-18.0.2_aarch64	|	1630.171	|	2673.894 (164.03%)	|	938.366 (57.56%)	|	201.279 (12.35%)	|	42.244 (2.59%)	|	1222.099 (74.97%) |
