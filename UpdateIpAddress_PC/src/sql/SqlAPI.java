package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import conf.AppGlobal;

public class SqlAPI {

	String driverClassName = AppGlobal.driverClassName;
	String url = AppGlobal.url;
	String username = AppGlobal.username;
	String password = AppGlobal.password;

	public static Statement statement = null;
	public static PreparedStatement preparedStatement = null;
	public static Connection connection = null;
	public static ResultSet resultSet = null;

	public SqlAPI() {
		try {
			Class.forName(driverClassName);
			connection = DriverManager.getConnection(url, username, password);
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// 数据库资源切换到20
//	public void switchDriver() throws Exception {
//		connection = DriverManager.getConnection(AppGlobal.changeUrl, 
//				AppGlobal.changeUsername, AppGlobal.changePassword);
//		statement = connection.createStatement();
//	}
	
	// 数据库资源切换到本地
	public void switchLocalDriver() throws Exception {
		connection = DriverManager.getConnection(url, username, password);
		statement = connection.createStatement();
	}
	
	
	// in sql, no out
	public static void update(String sql) throws SQLException
	{
		statement.executeUpdate(sql);
	}
	
	// in sql , out ResultSet
	public static ResultSet getResultSet(String sql) throws SQLException {
		resultSet = statement.executeQuery(sql);
		return resultSet;
	}

	// in sql , out handleNum
	public int getUpdate(String sql) throws SQLException {
		int i = statement.executeUpdate(sql);
		return i;

	}

	// in sql , out preparedStatement
	public static PreparedStatement getPreparedStatement(String sql)
			throws SQLException {
		preparedStatement = connection.prepareStatement(sql);
		return preparedStatement;
	}
	
	
}
