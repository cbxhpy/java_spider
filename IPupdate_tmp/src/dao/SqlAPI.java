package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlAPI {

	public static String driverClassName = "com.mysql.jdbc.Driver";
	public static String url = "jdbc:mysql://127.21.57.148:3306/java_stat?rewriteBatchedStatements=true";
	public static String username = "root";
	public static String password = "jianke@123";

//	public static Statement statement = null;
	public static PreparedStatement preparedStatement = null;
	public static Connection connection = null;
//	public static ResultSet resultSet = null;


	// in sql , out preparedStatement
	public PreparedStatement getPreparedStatement(String sql)
			throws SQLException {
		connection = DriverManager.getConnection(url, username, password);
		preparedStatement = connection.prepareStatement(sql);
		return preparedStatement;
	}
	public void returnConnect() throws SQLException{
		connection.close();
		preparedStatement.close();
	}

}
