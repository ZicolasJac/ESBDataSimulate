package com.dc.esb.bean;

import java.util.ArrayList;
import java.util.Random;

public class ProviderSystem {

	//提供方系统id
	private String prdId; 
	//提供方系统ip集
	private String[] prdIps;
	//旗下所有服务
	private ArrayList<Service> serviceList;
	
	public String getPrdId() {
		return prdId;
	}
	public void setPrdId(String prdId) {
		this.prdId = prdId;
	}
	public ArrayList<Service> getServiceList() {
		return serviceList;
	}
	public void setServiceList(
			ArrayList<Service> serviceList) {
		this.serviceList = serviceList;
	}
	public String getRandomPrdIps() {
		return this.prdIps[new Random().nextInt(prdIps.length)];
	}
	public void setPrdIps(String prdIps) {
		this.prdIps = prdIps.split(",");
	}
}
