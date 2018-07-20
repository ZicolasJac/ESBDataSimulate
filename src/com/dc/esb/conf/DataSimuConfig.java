package com.dc.esb.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.dc.esb.DataSimulate;

/**
 * monitor.properties配置文件加载类
 * 
 * @author ZJC 2018-01-10 22:58:53
 */
public class DataSimuConfig {

	private static Logger log = Logger.getLogger(DataSimulate.class);

	// 文件编码
	public static final String ENCODING = "UTF-8";

	// 配置文件monitor.properties缓存对象
	private static final Properties properties = new Properties();

	/**
	 * 读取配置文件
	 * 
	 * @throws Exception
	 */
	public static void loadProperties() throws Exception {
		if(log.isInfoEnabled()){
			log.info("加载系统配置文件开始...");
		}

		// 配置文件monitor.properties的路径
		String filePath = System.getProperty("user.dir")  + "/conf/datasimu.properties";

		InputStreamReader reader = null;
		try {
			File file = new File(filePath);
			if (file.exists()) {
				if (file.isFile()) {
					reader = new InputStreamReader(new FileInputStream(file), ENCODING);
					properties.load(reader);
				} else {
					log.error("此抽象路径名表示的文件不是一个标准的文件");
				}
			} else {
				log.error("此抽象路径名表示的文件或目录不存在");
			}
			
			if(log.isInfoEnabled()){
				log.info("加载系统配置文件完成！");
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	public static String getStringProperty(String key) {
		return properties.getProperty(key).trim();
	}

	public static String getStringProperty(String key, String defaultValue) {
		if (properties.containsKey(key)) {
			return properties.getProperty(key);
		} else {
			return defaultValue;
		}
	}

	public static int getIntProperty(String key) {
		return Integer.parseInt(properties.getProperty(key).trim());
	}

	public static int getIntProperty(String key, int defaultValue) {
		if (properties.containsKey(key)) {
			return Integer.parseInt(properties.getProperty(key));
		} else {
			return defaultValue;
		}
	}

	public static double getDoubleProperty(String key) {
		return Double.parseDouble(properties.getProperty(key).trim());
	}

	public static double getDoubleProperty(String key, double defaultValue) {
		if (properties.containsKey(key)) {
			return Double.parseDouble(properties.getProperty(key));
		} else {
			return defaultValue;
		}
	}

	public static boolean getBooleanProperty(String key) {
		return "true".equalsIgnoreCase(properties.getProperty(key).trim());
	}

	public static boolean getBooleanProperty(String key, boolean defaultValue) {
		if (properties.containsKey(key)) {
			return "true".equalsIgnoreCase(properties.getProperty(key));
		} else {
			return defaultValue;
		}
	}
}
