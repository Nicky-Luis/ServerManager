package com.dbumama.market.service.api.delivery;

import com.dbumama.market.model.DeliverySet;
import com.dbumama.market.service.common.AbstractResultDto;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class DeliveryTemplateResultDto extends AbstractResultDto{
     private Long id;
     private String name;
     private Long valuationType;//计费方式
     private String startTime;
     private List<DeliverySet> deliverySetList=new ArrayList<DeliverySet>();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Long getValuationType() {
		return valuationType;
	}
	public void setValuationType(Long valuationType) {
		this.valuationType = valuationType;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public List<DeliverySet> getDeliverySetList() {
		return deliverySetList;
	}
	public void setDeliverySetList(List<DeliverySet> deliverySetList) {
		this.deliverySetList = deliverySetList;
	}
     
}
