
# 1. SerDe Benchmark
执行10000次的耗时，数字越小越好。

| 	 手机型号            | 	libaray	 | 	eishay-ser	 | 	eishay-deser	 | 		
|-------------------|-----------|--------------|----------------|
| Huawei Mate40 Pro | fastjson2 | 		104        | 	115	          |
| 	                 | fastjson1 | 	    69      | 	109	          |
| 	                 | jackson   | 	   154      | 		196          |
| 	                 | gson      | 	   411      | 		183          |


# 2. Parse Benchmark
执行1000次的耗时，数字越小越好。

| 	 手机型号    ˚       | 	libaray	 | 	eishay	 | cart | 	homepage	 | 		 	h5api	 |
|-------------------|-----------|----------|------|------------|------------|
| Huawei Mate40 Pro | fastjson2 | 		34     | 3556 | 1411       | 3252       |
| 	                 | fastjson1 | 	     79 | 3699 | 1621       | 3330       |
| 	                 | orgjson   | 	   53   | 5238 | 1924       | 4400       |
