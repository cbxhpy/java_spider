/**
 * 
 */
/**
 * @author xuexiang
 *
 */
package servlet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONObject;

import dao.SqlAPI;;

/**
 * 
 */
/**
 * @author xuexiang
 * 
 */

public class Test {

	static String file_path = "/opt/data/pccontrail/";
	static Set<String> ipSet = new HashSet<String>();
	static Set<String> netExceptIpSet = new HashSet<String>();
	
	// 主函数
	public static void main(String[] args) throws Exception {
		get_ip_data();
		get_file_data();
	}

	// 从数据库读取ip
	public static void get_ip_data() throws Exception{
		// 开始时间
		long start = System.currentTimeMillis();
		String sql = "select ip from bas_ip_address_distinct_ip";
		try {
			SqlAPI sqlapi = new SqlAPI();
			PreparedStatement ps = sqlapi.getPreparedStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				ipSet.add(rs.getString("ip"));
			}
			rs.close();
			sqlapi.returnConnect();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// 结束时间
		long end = System.currentTimeMillis();
		System.out.println("ip读入内存成功，耗时：" + (end - start) / (double) 1000 + "秒");
	}
	// 遍历文件夹并加载文件
	public static void get_file_data(){
		File file = new File(file_path);
		File[] files = file.listFiles();
		for (File file2 : files){
			String fileName = file2.getName();
			// 文件包含日期小时数据，如：20170216_09 并且包含文件后缀
			if (fileName.indexOf("20170408") != -1 || fileName.indexOf("20170409") != -1 
				|| fileName.indexOf("20170410") != -1 || fileName.indexOf("20170411") != -1){
				System.out.println("正在处理文件：" + fileName);
				try{
					clear_data(file2);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	// 数据清洗
	public static void clear_data(File file) throws Exception{
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			// 切割字符串
			String[] field_list = line.split("\\$\\$\\$");

			String param = field_list[6];
			String url = field_list[4];

			// 过滤mo端资讯和问答页
			if(param.indexOf("first_class") != -1 || url.indexOf("ask/question") != -1){
				String IP = field_list[7];
				processIP(IP);
			}
		}
		
		// 对所有因网络异常等原因导致未获取到IP地址的ip, 重新访问网络获取地址
		processExceptIP();
	}

	//  入库前准备,格式化字符串
	public static void processIP(String IP) throws Exception{
		String[] strArr = null;
		if(!ipSet.contains(IP)){
			strArr = queryIpAddressByNetWork(IP); // 通过访问网络获取ip地址
			
			// 网络出现问题，获取不到ip对应地址
			if(strArr == null){
				// 当前线程休眠一分钟，因为百度地图api有每分钟访问并发限制。
				Thread.sleep(1000 * 60);
				netExceptIpSet.add(IP);
				return;
			}
			updateRepos("bas_ip_address_distinct_ip", IP, strArr); // 更新入库
			
			// 更新本地ip集合
			ipSet.add(IP);
		}		
		else{
		}
	}
	// 通过网络查询ip,返回国家、省、市字符串数组，如果全为空则为国外ip
	public static String[] queryIpAddressByNetWork(String ip){
		// 调用百度接口
		String[] strArr = null;
		try{
			strArr = getIpByBaidu(ip);
		}
		catch(Exception e){
			// 调用函数接受到null返回值,再对其处理
		}
		return strArr;
	}
	
	// 异常处理
	public static void processExceptIP() throws Exception{
		for(Iterator<String> it = netExceptIpSet.iterator(); it.hasNext();)
		{
			String IP = it.next();
			String[] strArr = queryIpAddressByNetWork(IP);
			if(strArr != null){
				updateRepos("bas_ip_address_distinct_ip", IP, strArr);
				
			}
			else{
				Thread.sleep(1000 * 60);
				strArr = queryIpAddressByNetWork(IP);
				
				// 三次访问网络获取不到IP对应的地址,放弃
				if(strArr != null){
					updateRepos("bas_ip_address_distinct_ip", IP, strArr);
				}
				else{
					Thread.sleep(1000 * 60);
					System.out.println("三次访问网络未获取到" + IP + "对应地址");
				}	
			}
				
		}
	}
	// 调用百度接口
	public static String[] getIpByBaidu(String ipAddress) throws Exception{
		String url = "http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip="
			+ ipAddress;
		URL urlStr = new URL(url);
		BufferedReader br = new BufferedReader(new InputStreamReader(urlStr.openStream()));
		String line = null;
		String[] strArr = new String[3];

		while((line = br.readLine()) != null){
		
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
			catch(Exception e){
				strArr[0] = "国外";
			}
		}
		return strArr;
	}
	// 入库
	public static void updateRepos(String table, String ip, String[] addressArr) 
	{
		// ip字段添加了唯一索引,因此可以直接插入,不必判断库中是否存在对应的ip
		String sql = "INSERT INTO " + table + "(ip,country,province,city) VALUES(?, ?, ?, ?)";
		SqlAPI sqlapi = new SqlAPI();
		try{
			PreparedStatement ps = sqlapi.getPreparedStatement(sql);
			ps.setString(1, ip);
			ps.setString(2, addressArr[0]);
			ps.setString(3, addressArr[1]);
			ps.setString(4, addressArr[2]);
			ps.executeUpdate();
			
			sqlapi.returnConnect();
		}
		catch(Exception e){
			System.out.println("updateRepos入库异常：" + e.getMessage());
		}
		
	}
}