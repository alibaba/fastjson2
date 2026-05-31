
# 1. SerDe Benchmark
The time required to execute 10000 times, the smaller the number, the better.

| 	 手机型号            | 	library	 | 	eishay-ser	 | 	eishay-deser	 | 		
|-------------------|-----------|--------------|----------------|
| Huawei Mate40 Pro | fastjson2 | 		104        | 	115	          |
| 	                 | fastjson1 | 	    69      | 	109	          |
| 	                 | jackson   | 	   154      | 		196          |
| 	                 | gson      | 	   411      | 		183          |


# 2. Parse Benchmark
The time required to execute 10000 times, the smaller the number, the better.

| 	 手机型号    ˚       | 	library	 | 	eishay	 | cart | 	homepage	 | 		 	h5api	 |
|-------------------|-----------|----------|------|------------|------------|
| Huawei Mate40 Pro | fastjson2 | 		34     | 3556 | 1411       | 3252       |
| 	                 | fastjson1 | 	     79 | 3699 | 1621       | 3330       |
| 	                 | orgjson   | 	   53   | 5238 | 1924       | 4400       |
