## EishayFuryCompatibleParse
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fury |
|-----|-----|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1549.704	|	1715.273 (110.68%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1988.507	|	2025.196 (101.85%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	2163.821	|	1524.853 (70.47%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	1332.641	|	1631.669 (122.44%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	1739.845	|	1685.845 (96.9%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1919.997	|	1569.346 (81.74%) |

## EishayFuryCompatibleWrite
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fury |
|-----|-----|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1347.998	|	1776.865 (131.82%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1406.225	|	2105.532 (149.73%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	1735.242	|	2234.08 (128.75%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	1118.336	|	1580.647 (141.34%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	1365.135	|	1726.397 (126.46%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1307.887	|	1553.29 (118.76%) |

## EishayFuryParse
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fury |
|-----|-----|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	2338.034	|	1964.204 (84.01%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	3209.894	|	2350.656 (73.23%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	2837.918	|	1708.815 (60.21%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	2181.861	|	1826.994 (83.74%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	2620.709	|	2010.321 (76.71%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	2959.312	|	1833.215 (61.95%) |

## EishayFuryWrite
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fury |
|-----|-----|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	2507.536	|	2472.099 (98.59%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	3294.566	|	2416.049 (73.33%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	3854.421	|	3178.027 (82.45%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	2125.683	|	2194.696 (103.25%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	2649.598	|	2148.93 (81.1%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	2683.5	|	2875.445 (107.15%) |

## EishayFuryWriteNoneCache
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fury |
|-----|-----|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	192.71	|	0.055 (0.03%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	188.224	|	0.057 (0.03%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	193.888	|	0.072 (0.04%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	173.623	|	0.049 (0.03%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	142.663	|	0.058 (0.04%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	183.086	|	0.065 (0.04%) |

## EishayParseBinary
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fastjson2UTF8Bytes	|	hessian	|	javaSerialize |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1660.423	|	1141.994 (68.78%)	|	279.311 (16.82%)	|	47.273 (2.85%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	2647.482	|	1151.486 (43.49%)	|	250.357 (9.46%)	|	47.857 (1.81%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	2943.172	|	1222.094 (41.52%)	|	252.913 (8.59%)	|	53.048 (1.8%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	1438.72	|	877.661 (61%)	|	203.413 (14.14%)	|	42.645 (2.96%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	2143.038	|	1058.215 (49.38%)	|	199.905 (9.33%)	|	39.802 (1.86%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	2388.01	|	1055.204 (44.19%)	|	231.178 (9.68%)	|	39.658 (1.66%) |

## EishayParseBinaryArrayMapping
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	kryo	|	fastjson2UTF8Bytes	|	fastjson1UTF8Bytes |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	2703.758	|	1732.707 (64.09%)	|	1638.097 (60.59%)	|	1471.94 (54.44%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	3960.123	|	1676.106 (42.32%)	|	1818.433 (45.92%)	|	1493.219 (37.71%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	4913.057	|	1543.063 (31.41%)	|	1982.583 (40.35%)	|	1659.456 (33.78%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	2446.561	|	1236.427 (50.54%)	|	1220.728 (49.9%)	|	1110.453 (45.39%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	3224.434	|	1427.62 (44.28%)	|	1547.74 (48%)	|	1259.955 (39.08%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	3760.639	|	1313.947 (34.94%)	|	1631.844 (43.39%)	|	1381.287 (36.73%) |

## EishayParseBinaryAutoType
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fastjson2JSONB_autoTypeFilter	|	hessian	|	javaSerialize |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1465.064	|	1344.982 (91.8%)	|	276.347 (18.86%)	|	46.605 (3.18%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1934.273	|	1699.826 (87.88%)	|	248.329 (12.84%)	|	49.952 (2.58%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	2203.809	|	1799.166 (81.64%)	|	254.277 (11.54%)	|	54.478 (2.47%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	1195.67	|	1218.091 (101.88%)	|	201.379 (16.84%)	|	41.935 (3.51%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	1635.114	|	1563.888 (95.64%)	|	199.401 (12.19%)	|	41.647 (2.55%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1739.155	|	1667.104 (95.86%)	|	223.101 (12.83%)	|	38.027 (2.19%) |

## EishayParseString
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1275.871	|	1010.469 (79.2%)	|	530.346 (41.57%)	|	444.651 (34.85%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1202.176	|	911.642 (75.83%)	|	485.487 (40.38%)	|	437.692 (36.41%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	1312.981	|	1262.468 (96.15%)	|	495.322 (37.72%)	|	443.381 (33.77%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	942.674	|	773.479 (82.05%)	|	365.171 (38.74%)	|	351.804 (37.32%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	1085.677	|	794.433 (73.17%)	|	366.876 (33.79%)	|	378.586 (34.87%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1122.387	|	953.357 (84.94%)	|	402.824 (35.89%)	|	308.227 (27.46%) |

## EishayParseStringPretty
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	961.87	|	270.518 (28.12%)	|	480.018 (49.9%)	|	411.525 (42.78%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	898.242	|	239.463 (26.66%)	|	461.084 (51.33%)	|	412.271 (45.9%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	932.74	|	298.344 (31.99%)	|	450.717 (48.32%)	|	415.897 (44.59%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	734.284	|	216.957 (29.55%)	|	338.943 (46.16%)	|	329.225 (44.84%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	794.282	|	229.299 (28.87%)	|	369.814 (46.56%)	|	339.661 (42.76%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	802.017	|	259.882 (32.4%)	|	369.999 (46.13%)	|	321.127 (40.04%) |

## EishayParseTreeString
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	956.892	|	498.179 (52.06%)	|	529.472 (55.33%)	|	338.301 (35.35%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	808.401	|	396.58 (49.06%)	|	454.495 (56.22%)	|	330.643 (40.9%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	1056.311	|	544.237 (51.52%)	|	491.147 (46.5%)	|	332.527 (31.48%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	591.943	|	292.989 (49.5%)	|	303.108 (51.21%)	|	263.734 (44.55%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	671.926	|	365.499 (54.4%)	|	362.208 (53.91%)	|	277.324 (41.27%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	807.176	|	411.438 (50.97%)	|	379.262 (46.99%)	|	304.906 (37.77%) |

## EishayParseTreeStringPretty
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	800.399	|	444.821 (55.57%)	|	510.735 (63.81%)	|	319.276 (39.89%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	641.862	|	352.409 (54.9%)	|	437.82 (68.21%)	|	306.942 (47.82%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	802.343	|	476.751 (59.42%)	|	472.527 (58.89%)	|	302.387 (37.69%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	501.522	|	284.497 (56.73%)	|	282.325 (56.29%)	|	249.78 (49.8%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	541.073	|	286.395 (52.93%)	|	349.386 (64.57%)	|	286.747 (53%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	625.863	|	373.481 (59.67%)	|	350.201 (55.95%)	|	282.158 (45.08%) |

## EishayParseTreeUTF8Bytes
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	857.645	|	436.804 (50.93%)	|	605.634 (70.62%)	|	316.8 (36.94%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	761.365	|	371.783 (48.83%)	|	543.089 (71.33%)	|	316.464 (41.57%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	978.827	|	480.489 (49.09%)	|	581.322 (59.39%)	|	320.797 (32.77%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	561.88	|	258.888 (46.08%)	|	321.24 (57.17%)	|	237.873 (42.34%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	740.639	|	334.083 (45.11%)	|	412.591 (55.71%)	|	297.906 (40.22%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	791.374	|	307.855 (38.9%)	|	475.388 (60.07%)	|	272.636 (34.45%) |

## EishayParseTreeUTF8BytesPretty
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	738.0	|	389.812 (52.82%)	|	515.598 (69.86%)	|	295.418 (40.03%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	621.14	|	311.775 (50.19%)	|	478.493 (77.03%)	|	296.654 (47.76%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	769.633	|	407.937 (53%)	|	491.749 (63.89%)	|	296.788 (38.56%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	500.283	|	232.157 (46.41%)	|	309.249 (61.81%)	|	225.119 (45%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	615.701	|	201.093 (32.66%)	|	385.631 (62.63%)	|	281.195 (45.67%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	632.412	|	300.044 (47.44%)	|	439.426 (69.48%)	|	265.218 (41.94%) |

## EishayParseUTF8Bytes
| aliyun ecs spec | jdk version 	|	fastjson2	|	dsljson	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1104.366	|	915.673 (82.91%)	|	810.916 (73.43%)	|	581.269 (52.63%)	|	314.249 (28.46%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1080.849	|	877.517 (81.19%)	|	758.093 (70.14%)	|	523.885 (48.47%)	|	316.922 (29.32%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	1168.599	|	821.319 (70.28%)	|	938.288 (80.29%)	|	552.087 (47.24%)	|	313.607 (26.84%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	825.317	|	641.105 (77.68%)	|	655.671 (79.44%)	|	420.071 (50.9%)	|	240.27 (29.11%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	979.417	|	684.397 (69.88%)	|	680.132 (69.44%)	|	435.674 (44.48%)	|	292.508 (29.87%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1033.856	|	634.987 (61.42%)	|	589.84 (57.05%)	|	469.69 (45.43%)	|	297.249 (28.75%) |

## EishayParseUTF8BytesPretty
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	852.964	|	241.721 (28.34%)	|	533.423 (62.54%)	|	290.091 (34.01%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	838.821	|	225.822 (26.92%)	|	498.236 (59.4%)	|	303.282 (36.16%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	877.163	|	271.704 (30.98%)	|	494.71 (56.4%)	|	298.617 (34.04%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	678.956	|	206.809 (30.46%)	|	379.199 (55.85%)	|	225.95 (33.28%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	746.043	|	176.505 (23.66%)	|	415.7 (55.72%)	|	278.922 (37.39%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	732.446	|	246.029 (33.59%)	|	421.32 (57.52%)	|	278.864 (38.07%) |

## EishayWriteBinary
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fastjson2UTF8Bytes	|	hessian	|	javaSerialize |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1911.243	|	1542.492 (80.71%)	|	340.982 (17.84%)	|	237.135 (12.41%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	2461.338	|	1616.116 (65.66%)	|	333.605 (13.55%)	|	216.814 (8.81%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	3316.205	|	1913.799 (57.71%)	|	320.612 (9.67%)	|	227.078 (6.85%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	1680.185	|	1371.384 (81.62%)	|	351.608 (20.93%)	|	206.748 (12.31%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	2209.008	|	1438.47 (65.12%)	|	347.247 (15.72%)	|	220.992 (10%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	2062.278	|	1374.031 (66.63%)	|	340.402 (16.51%)	|	209.463 (10.16%) |

## EishayWriteBinaryArrayMapping
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	kryo	|	fastjson2UTF8Bytes	|	fastjson1UTF8Bytes |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	3123.138	|	1871.064 (59.91%)	|	1867.812 (59.81%)	|	745.167 (23.86%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	4788.932	|	1956.713 (40.86%)	|	2055.379 (42.92%)	|	695.014 (14.51%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	5388.55	|	1925.885 (35.74%)	|	2158.264 (40.05%)	|	687.381 (12.76%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	2607.069	|	1439.817 (55.23%)	|	1567.615 (60.13%)	|	461.514 (17.7%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	4273.239	|	1583.189 (37.05%)	|	1738.057 (40.67%)	|	534.975 (12.52%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	4017.431	|	1432.471 (35.66%)	|	1676.242 (41.72%)	|	456.793 (11.37%) |

## EishayWriteBinaryAutoType
| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	fastjson2UTF8Bytes	|	hessian	|	javaSerialize |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1298.667	|	1483.758 (114.25%)	|	340.421 (26.21%)	|	233.819 (18%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1427.133	|	1560.933 (109.38%)	|	336.218 (23.56%)	|	212.007 (14.86%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	1640.957	|	1846.889 (112.55%)	|	321.359 (19.58%)	|	224.801 (13.7%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	1131.335	|	1302.537 (115.13%)	|	355.748 (31.44%)	|	203.045 (17.95%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	1338.89	|	1344.06 (100.39%)	|	353.567 (26.41%)	|	205.192 (15.33%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1329.225	|	1301.567 (97.92%)	|	345.749 (26.01%)	|	209.549 (15.76%) |

## EishayWriteString
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1623.409	|	605.647 (37.31%)	|	1007.513 (62.06%)	|	443.382 (27.31%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1444.739	|	584.045 (40.43%)	|	918.445 (63.57%)	|	352.322 (24.39%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	1625.319	|	593.371 (36.51%)	|	1047.563 (64.45%)	|	242.664 (14.93%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	1241.744	|	509.353 (41.02%)	|	648.176 (52.2%)	|	352.058 (28.35%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	1266.263	|	456.129 (36.02%)	|	541.704 (42.78%)	|	325.474 (25.7%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1281.246	|	474.074 (37%)	|	697.9 (54.47%)	|	216.392 (16.89%) |

## EishayWriteStringTree
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1237.506	|	845.347 (68.31%)	|	960.554 (77.62%)	|	483.008 (39.03%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1231.114	|	757.162 (61.5%)	|	964.796 (78.37%)	|	387.288 (31.46%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	1191.534	|	813.528 (68.28%)	|	884.214 (74.21%)	|	257.604 (21.62%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	947.751	|	590.546 (62.31%)	|	680.715 (71.82%)	|	414.062 (43.69%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	1025.389	|	570.45 (55.63%)	|	733.46 (71.53%)	|	330.864 (32.27%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1025.752	|	626.336 (61.06%)	|	677.506 (66.05%)	|	217.029 (21.16%) |

## EishayWriteStringTree1x
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1160.511	|	788.422 (67.94%)	|	877.638 (75.63%)	|	475.314 (40.96%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1090.647	|	741.539 (67.99%)	|	919.036 (84.27%)	|	387.598 (35.54%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	916.565	|	708.11 (77.26%)	|	772.276 (84.26%)	|	249.043 (27.17%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	828.813	|	553.519 (66.78%)	|	618.164 (74.58%)	|	386.792 (46.67%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	891.281	|	579.627 (65.03%)	|	698.045 (78.32%)	|	348.353 (39.08%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	742.761	|	566.08 (76.21%)	|	642.018 (86.44%)	|	216.316 (29.12%) |

## EishayWriteUTF8Bytes
| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| ecs.c7.xlarge | oracle-jdk1.8.0_341_x64	|	1544.227	|	580.055 (37.56%)	|	933.927 (60.48%)	|	380.462 (24.64%) |
| ecs.c7.xlarge | oracle-jdk-11.0.16_x64	|	1621.33	|	535.689 (33.04%)	|	854.714 (52.72%)	|	348.193 (21.48%) |
| ecs.c7.xlarge | oracle-jdk-17.0.4_x64	|	1867.968	|	564.361 (30.21%)	|	976.904 (52.3%)	|	241.76 (12.94%) |
| ecs.g8m.xlarge | oracle-jdk1.8.0_341_aarch64	|	1345.053	|	442.152 (32.87%)	|	680.865 (50.62%)	|	306.423 (22.78%) |
| ecs.g8m.xlarge | oracle-jdk-11.0.16_aarch64	|	1453.007	|	408.277 (28.1%)	|	549.755 (37.84%)	|	314.612 (21.65%) |
| ecs.g8m.xlarge | oracle-jdk-17.0.4_aarch64	|	1396.771	|	476.833 (34.14%)	|	724.421 (51.86%)	|	214.758 (15.38%) |

