package work;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Test;

import sql.SqlAPI;
import conf.AppGlobal;
import conf.TABLE_DEF;
/**
 * 每一个开发者账号每分钟访问百度地图API服务的次数是有限制的。每分钟并发量限制参见：
 * http://lbsyun.baidu.com/index.php?title=webapi/ip-api
 * @author jiangxuepeng
 *
 */
public class UseMysqlCount {

	private Set<String> ipSet = new HashSet<String>();
	
	private Set<String> netExceptIpSet = new HashSet<String>();
	
	SqlAPI sqlAPI = new SqlAPI();

	// 将读取到的字符串清洗后放入临时数据库表
	public void firstCount(File file) throws Exception {
		
		FileReader fileReader = new FileReader(file);
		@SuppressWarnings("resource")
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			// 切割字符串
			String[] field_list = line.split("\\$\\$\\$");
			// 清洗非法字段
			if (clean(field_list)) {
				continue;
			}
			
			String param = field_list[TABLE_DEF.e_Params.ordinal()];
			
			// 过滤pc端资讯和问答页
			if(param.indexOf("first_class") != -1)
			{
				String IP = field_list[TABLE_DEF.e_HostIp.ordinal()]; 
				processIP(IP);
			}

		}
		
		// 对所有因网络异常等原因导致未获取到IP地址的ip, 重新访问网络获取地址
		processExceptIP();

	}
	
	//  清洗原始数据
	public boolean clean(String[] field_list) {

		if (field_list[0].matches("^(?:[0-]+)$")) {
			return true;
		}
		int nAppId = TABLE_DEF.e_AppId.ordinal();
		String strChannelApp = field_list[nAppId];
		if (!strChannelApp.equals("1") && !strChannelApp.equals("2")
				&& !strChannelApp.equals("3") && !strChannelApp.equals("4")
				&& !strChannelApp.equals("5") && !strChannelApp.equals("6")) {
			return true;
		}
		// 原：清洗规则4：action_type必须为数据，且为指定的集合
		// 调整: 必须为页面加载事件 action_type=0
		int nEventType = TABLE_DEF.e_EventType.ordinal();
		String strActionType = field_list[nEventType];
		if (!strActionType.equals("0")) {
			return true;
		}
		// 日期字段必须合法  -- 由服务器端生成，不再判断
		return false;
	}

	public void processExceptIP() throws Exception
	{
		for(Iterator<String> it = netExceptIpSet.iterator(); it.hasNext();)
		{
			String IP = it.next();
			String[] strArr = queryIpAddressByNetWork(IP);
			if(strArr != null)
			{
				updateRepos(AppGlobal.ip_address_table, IP, strArr);
			}
			else
			{
				Thread.sleep(1000 * 60);
				strArr = queryIpAddressByNetWork(IP);
				// 三次访问网络获取不到IP对应的地址,放弃
				if(strArr != null)
				{
					updateRepos(AppGlobal.ip_address_table, IP, strArr);
				}
				else
				{
					Thread.sleep(1000 * 60);
					System.out.println("三次访问网络未获取到" + IP + "对应地址");
				}
					
			}
				
		}
	}
	
	//  入库前准备,格式化字符串
	public void processIP(String IP) throws Exception{
		
		String[] strArr = null;
		
		if(!ipSet.contains(IP))
		{
			strArr = queryIpAddressByNetWork(IP); // 通过访问网络获取ip地址
			
			// 网络出现问题，获取不到ip对应地址
			if(strArr == null)
			{
				// 因百度api定位服务每分钟并发量限制,当前线程休眠一分钟
				Thread.sleep(1000 * 60);
				netExceptIpSet.add(IP);
				return;
			}	

			updateRepos(AppGlobal.ip_address_table, IP, strArr); // 更新入库
			// 更新本地ip集合
			ipSet.add(IP);
		}		
		else 
		{
		}
		
	}
	
	// 通过网络查询ip,返回国家、省、市字符串数组，如果全为空则为国外ip
	public String[] queryIpAddressByNetWork(String ip) 
	{
		// 调用百度接口
		String[] strArr = null;
		try
		{
			strArr = getIpByBaidu(ip);
		}
		catch(Exception e)
		{
			// 调用函数接受到null返回值,再对其处理
		}
		return strArr;
	}
	
	

	// 单条查询,返回省、市字符串数组，如果全为空则为国外ip
	public String[] queryIpAddress(String table, String ip) throws Exception
	{
		String sql = "SELECT province,city FROM " + table + " WHERE ip='" + ip + "'";
		
		PreparedStatement ps = SqlAPI.getPreparedStatement(sql);
		ResultSet rs = ps.executeQuery();
	
		String[] strArr = new String[2];
		if(rs.next())
		{
			strArr[0] = rs.getString("province");
			strArr[1] = rs.getString("city");
		}
		else
		{
			// 调用百度接口，更新库
			String[] tempArr = new String[3];
			tempArr = getIpByBaidu(ip);
			strArr[0] = tempArr[1];
			strArr[1] = tempArr[2];
			updateRepos(table, ip, tempArr); // 执行更新
		}
		
		return strArr;
	}
	
	public void updateRepos(String table, String ip, String[] addressArr) 
	{
		// ip字段添加了唯一索引,因此可以直接插入,不必判断库中是否存在对应的ip
		String sql = "INSERT INTO " + table + "(ip,country,province,city) VALUES(?, ?, ?, ?)";
		try
		{
			PreparedStatement ps = SqlAPI.getPreparedStatement(sql);
			ps.setString(1, ip);
			ps.setString(2, addressArr[0]);
			ps.setString(3, addressArr[1]);
			ps.setString(4, addressArr[2]);
			ps.executeUpdate();
		}
		catch(Exception e)
		{
			System.out.println("updateRepos抛出异常：" + e.getMessage());
		}
		
	}
	
	@Test
	public void test() throws Exception{
		String[] strArr = getIpByBaidu("");
		System.out.println(Arrays.toString(strArr));
	}
	
	public static String[] getIpByBaidu(String ipAddress) throws Exception
	{
		String url = "http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip="
			+ ipAddress;
		URL urlStr = new URL(url);
		BufferedReader br = new BufferedReader(new InputStreamReader(urlStr.openStream()
				));
		String line = null;
		String[] strArr = new String[3];
		
		while((line = br.readLine()) != null)
		{
		
			JSONObject job = new JSONObject(line);
			strArr[0] = "中国";
			strArr[1] = "";
			strArr[2] = "";
			try {
				JSONObject str = job.getJSONObject("content").getJSONObject("address_detail");
				String province = str.getString("province");
				String city = str.getString("city");
				strArr[1] = province;
				strArr[2] = city;
			}
			catch(Exception e)
			{
				strArr[0] = "国外";
			}
	
		}
		
		return strArr;
	}
	

	/*
	 * 将ip地址从数据库装载到内存中
	 */
	public void ipReaderFormMysqlToMemory(String tableName) throws Exception
	{
		System.out.println("从数据库中读取ip到内存(Set集合)");

		long start = System.currentTimeMillis();
		
		String sql = "SELECT ip FROM " + tableName;
		PreparedStatement ps = SqlAPI.getPreparedStatement(sql);
		ResultSet rs = ps.executeQuery();

		while(rs.next())
		{
			ipSet.add(rs.getString("ip"));
		}

		rs.close();

		long end = System.currentTimeMillis();

		System.out.println("ip读入内存成功，耗时：" + (end - start) / (double) 1000 + "秒");		
	}
	
}
