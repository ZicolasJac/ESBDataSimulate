package com.dc.esb.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.dc.esb.conf.DataSimuConfig;

public class JDBCTool {

	private static Logger log = Logger.getLogger(JDBCTool.class);
	
	private static String driver;
	private static String url;
	private static String username;
	private static String password ;
	
	private Connection conn = null;
	private PreparedStatement pstmt = null;
	
	public JDBCTool(){
		driver = DataSimuConfig.getStringProperty("db.driver", "oracle.jdbc.driver.OracleDriver");
		url = DataSimuConfig.getStringProperty("db.url", "jdbc:oracle:thin:@127.0.0.1:1521:esbimon");
		username = DataSimuConfig.getStringProperty("db.username", "esbimon");
		password = DataSimuConfig.getStringProperty("db.password", "abcd1234");		
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			log.error("找不到数据库驱动类！");
		}
	}
	
	public Connection getConn() {
		try {
			conn = DriverManager.getConnection(url, username, password);
			log.info("获取数据库连接成功！");
		} catch (SQLException e) {
			log.error("获取数据库连接失败！URL:"+url+";username:"+username+";password:"+password);
		}
		return conn;
	}

	public int insert(String sql, String[] params) {
		int result = -1;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			int loop = 1;
			if (params != null)
				for (String param : params) {
					pstmt.setString(loop, param);
					loop++;
				}
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public ResultSet query(String sql, String[] params) {
		Connection conn = getConn();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			int loop = 1;
			if (params != null)
				for (String param : params) {
					pstmt.setString(loop, param);
					loop++;
				}
			result = pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public void closeAll() {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConn() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void closePstmt() {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
