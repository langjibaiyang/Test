package com.yushen.java;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

public class RecivePut {
	public static void main(String[] args) {
		new RecivePut();
	}

	public RecivePut() {
		ServerSocket ss = null;
		Socket request = null;
		Thread receiveThread = null;

		try {
			ss = new ServerSocket(5050);
			System.out.println("The server is ready!");
			while (true) {
				if ((request = ss.accept()) != null) {
					receiveThread = new ServerThread(request);
					receiveThread.start();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (request != null) {
				try {
					request.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (ss != null) {
				try {
					ss.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

class ServerThread extends Thread {
	Socket clientSocket = null;

	public ServerThread(Socket s) {
		this.clientSocket = s;

	}

	public void run() {
		operation();
	}

	public synchronized void operation() {
		InputStream is = null;
		String result = "";
		Connection conn = null;
		Statement statement = null;

		try {
			is = clientSocket.getInputStream();
			byte[] b = new byte[20];
			int len;
			while ((len = is.read(b)) != -1) {
				String str = new String(b, 0, len);
				result += str;
			}
			String results[] = result.split("-");

			conn = getConnection2();

			statement = conn.createStatement();

			String sql = "insert into trashrecord(messagedate,id,status)"
					+ "values(sysdate,'" + results[0] + "','" + results[1]
					+ "')";

			statement.executeUpdate(sql);

			sql = "update trashstatus set status = '" + results[1]
					+ "' where id = '" + results[0] + "'";

			statement.executeUpdate(sql);

			for (int i = 0; i < results.length; i++) {
				System.out.print("--" + results[i]);
			}
			Date nowtime = new Date();
			System.out.println(nowtime + "收到来自于" + clientSocket.getRemoteSocketAddress()
					+ "的连接");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public Connection getConnection2() throws Exception {
		String driverClass = null;
		String jdbcUrl = null;
		String user = null;
		String password = null;

		// 读取类路径下的 jdbc.properties 文件
		InputStream in = getClass().getClassLoader().getResourceAsStream(
				"jdbc.properties");
		Properties properties = new Properties();
		properties.load(in);
		driverClass = properties.getProperty("driver");
		jdbcUrl = properties.getProperty("jdbcUrl");
		user = properties.getProperty("user");
		password = properties.getProperty("password");

		// 加载 Driver驱动
		Class.forName(driverClass);

		// 通过 DriverManager的 connect 方法获取数据库连接.
		Connection connection = DriverManager.getConnection(jdbcUrl, user,
				password);

		return connection;
	}
}
