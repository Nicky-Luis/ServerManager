package com.dbumama.market.service.api.area;

import com.dbumama.market.model.Area;

import java.util.List;


public interface AreaService {
	public List<Area> findRoots();     //顶级地区
	public List<Area> getChildren(Long AreaId);   //下级地区
	
	public String getAreaName(String[] id);//通过数组Id得到地区名称
	
	public List<AreaResultDto> getAreaResultDto(String[] id);
}
