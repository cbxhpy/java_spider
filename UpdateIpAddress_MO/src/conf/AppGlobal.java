package conf;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;

public class AppGlobal {

	static public String mo_SrcFilePath ="/opt/data/mcontrail/";
	static public String pc_SrcFilePath = "/opt/data/pccontrail/";
	static public String fileSuffix = ".src";
	// 数据保存到生产环境(20)
//	public static String changeUrl = "jdbc:mysql://120.86.64.41:3308/big_data_platform?rewriteBatchedStatements=true";
//	public static String changeUsername = "javacol";
//	public static String changePassword = "javacol@2016";
	
	
	public static String SEPERATOR = ",";

	public static String driverClassName = "com.mysql.jdbc.Driver";
	public static String url = "jdbc:mysql://localhost:3306/java_stat?rewriteBatchedStatements=true";
	public static String username = "root";
	public static String password = "jianke@123"; //jianke@123
	
	public static String ip_address_table = "bas_ip_address_distinct_ip"; // ip<->区域对应表
	
	// today
	public static String lastHourFile() {
		Calendar c = Calendar.getInstance();  
		c.add(Calendar.HOUR, -1);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HH");
		String time = format.format(c.getTime());
		return time;
	}


	public void test() {
		String time = lastHourFile();
		System.out.println(time);
	}
}
