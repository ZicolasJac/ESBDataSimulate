package com.dc.esb.bean;

import java.util.List;

public class Service {

	// 服务名
	private String serviceName;
	// 服务tps范围
	private List<String[]> tpsRangeList;
	// 系统成功率范围
	private String ssrRange;
	// 业务成功率范围
	private String bsrRange;
	// esb内部耗时范围
	private String esbTimeRange;
	// 交易耗时范围
	private String bussTimeRange;
	// 所属渠道列表
	private List<String> channels;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<String[]> getTpsRangeList() {
		return tpsRangeList;
	}

	public void setTpsRangeList(List<String[]> tpsRangeList) {
		this.tpsRangeList = tpsRangeList;
	}

	public String getSsrRange() {
		return ssrRange;
	}

	public void setSsrRange(String ssrRange) {
		this.ssrRange = ssrRange;
	}

	public String getBsrRange() {
		return bsrRange;
	}

	public void setBsrRange(String bsrRange) {
		this.bsrRange = bsrRange;
	}

	public String getEsbTimeRange() {
		return esbTimeRange;
	}

	public void setEsbTimeRange(String esbTimeRange) {
		this.esbTimeRange = esbTimeRange;
	}

	public String getBussTimeRange() {
		return bussTimeRange;
	}

	public void setBussTimeRange(String bussTimeRange) {
		this.bussTimeRange = bussTimeRange;
	}

	public List<String> getChannels() {
		return channels;
	}

	public void setChannels(List<String> channels) {
		this.channels = channels;
	}
}
