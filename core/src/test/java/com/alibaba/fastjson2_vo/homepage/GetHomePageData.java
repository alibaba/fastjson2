package com.alibaba.fastjson2_vo.homepage;

import java.util.List;

import com.alibaba.fastjson2_vo.homepage.puti.model.Section;

public class GetHomePageData  {

	//private static final long serialVersionUID = -684313709852285893L;

	public List<Section> section;

	public long interval;

	public String timestamp;


	public String digest;   //摘要信息

	public int way;//0：全量，1：增量


	public long WriteCacheTimestamp;
	
	public String userId;
	
	

}
