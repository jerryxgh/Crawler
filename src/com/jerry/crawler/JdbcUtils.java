package com.jerry.crawler;

import java.sql.DriverManager;   
import java.sql.PreparedStatement;
import java.sql.ResultSet;   
import java.sql.SQLException;   
import java.sql.Statement;   

import java.sql.Connection;   
import java.util.List;

public class JdbcUtils {
	private static String url = "jdbc:mysql://192.168.1.144:3306/crawler?useUnicode=true&characterEncoding=utf-8"; //连接数据库连接   
	private static String use = "jerry"; // 登陆数据库用户名   
	private static String password = "123456"; //登陆数据库密码   

	private JdbcUtils() {   

	}   

	/**  
	 * 使用静态模块来进行注册驱动  
	 */  
	static {   
		try {   
			Class.forName("com.mysql.jdbc.Driver");   
		} catch (ClassNotFoundException e) {   
			throw new ExceptionInInitializerError(e);   
		}   
	}   

	/**  
	 * 建立数据库连接  
	 *   
	 * @return 返回数据库连接  
	 * @throws SQLException  
	 */  
	public static Connection getConnection() throws SQLException {
		Connection conn =  DriverManager.getConnection(url, use, password);
		return conn;
	}   

	/**  
	 * 释放数据资源  
	 *   
	 * @param rs  
	 * @param st  
	 * @param conn  
	 */  
	public static void free(ResultSet rs, Statement st, Connection conn) {   
		try {   
			if (rs != null)   
				rs.close();   
		} catch (SQLException e) {   
			e.printStackTrace();   
		} finally {   
			try {   
				if (st != null)   
					st.close();   
			} catch (SQLException e) {   
				e.printStackTrace();   
			} finally {   
				if (conn != null)   
					try {   
						conn.close();   
					} catch (SQLException e) {   
						e.printStackTrace();   
					}   
			}   
		}   
	}

	/**
	 * 将缓存的urls存入数据库
	 * @param bufferedUrl
	 */
	public static void savebufferedUrl(List<String> bufferedUrl) {
		Connection conn;
		try {
			conn = getConnection();

			conn.setAutoCommit(false);  
			String sql = "INSERT urls(url) VALUES(?)";  
			PreparedStatement prest = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			for (int i = 0; i < bufferedUrl.size(); ++i) {
				prest.setString(1, bufferedUrl.remove(i));
				prest.addBatch();
			}  
			bufferedUrl.clear();
			prest.executeBatch();
			conn.commit();
			free(null, prest, conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 从数据库中取出缓存的url
	 * @param bufferedUrl
	 */
	public static void getbufferedUrl(Queue<String> bufferedUrl, int bufferSize) {
		try {
			Connection conn = getConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT MIN(id) FROM urls");
			int minId = -1;
			if (rs.next()) {
				minId = rs.getInt(1);
			}
			rs.close();
			if (minId < 0) return;
			rs = st.executeQuery("SELECT url FROM urls where id < " + (minId + bufferSize));
			while(rs.next())
				bufferedUrl.enQueue(rs.getNString(1));
			st.executeUpdate("DELETE FROM urls where id < " + (minId + bufferSize));
			free(rs, st, conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Queue<String> bufferedUrl = new Queue<String>();
		getbufferedUrl(bufferedUrl, 5);
		while(!bufferedUrl.isEmpty()) {
			System.out.println(bufferedUrl.deQueue());
		}
//		ArrayList<String> bufferedUrl = new ArrayList<String>();
//		try {
//			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("un_visited.txt")));
//			String tmp = null;
//			while ((tmp = br.readLine()) != null) {
//				bufferedUrl.add(tmp);
//				System.err.println(tmp);
//			}
//			savebufferedUrl(bufferedUrl);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
