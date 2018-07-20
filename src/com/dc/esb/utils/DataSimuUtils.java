package com.dc.esb.utils;

public class DataSimuUtils {

	/**
	 * 从一段数值中随机获取一个数值
	 * @param range
	 * @return
	 */
	public static float getRandomFromRange(String range){
		String params[] = range.split("-");
		float min = Float.parseFloat(params[0]);
		float max = Float.parseFloat(params[1]);		
		float random=(float) (Math.random()*(max-min)+min);
		return random;
	}
	
	/**
	 * float型数据保留两位小数
	 * @param num
	 * @return
	 */
	public static float getTwoBitFloat(float num){
		float  newNum   =  (float)(Math.round(num*100))/100;
		return newNum;
	}
	
	/**
	 * 随机获取一个数到0间的任意值
	 * @param num
	 * @return
	 */
	public static float getRandomHalfValue(float num){
		float random=(float) (Math.random()*(num));
		String res = String.format("%.0f", random/2);
		return Float.parseFloat(res);
	}
	
	public static void main(String[] args) {
		for(int i=0;i<10000;i++){
			float aa = getRandomFromRange("3.07-3.27");
			System.out.println(aa);
		}
	}
}
