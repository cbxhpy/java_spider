// 进入/var/spool/cron,修改root文件,每小时的10'开始运行
// 程序读取src文件,取出ip,获取地址,然后入库.
// 注意：线上代码需要修改lasthourFile = AppGlobal.lastHourFile();
import java.io.File;
import java.util.Date;

import work.UseMysqlCount;
import conf.AppGlobal;

// 最新修改，处理mo一段时间(几天)的ip 2017年2月01 - 2017年2月16
public class TrafficCountMain {

	static String fileName = AppGlobal.pc_SrcFilePath;
	static String lastHourFile = AppGlobal.lastHourFile();
	static String fileFlag = AppGlobal.fileSuffix;

	public static void main(String[] args)
	{
		runUpdateIpProcess();
	}

	public static void runUpdateIpProcess()
	{
		UseMysqlCount iniCnt = new UseMysqlCount();
		
		try
		{
			// 将ip地址从数据库装载到内存中。
			iniCnt.ipReaderFormMysqlToMemory(AppGlobal.ip_address_table);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


		// 遍历目标文件夹并加载数据
		File file = new File(fileName);
		File[] files = file.listFiles();

		System.out.println("文件处理开始时间：" + new Date().toString());
		for (File file2 : files)
		{
			String fileName = file2.getName();
			// 文件包含日期小时数据，如：20170216_09 并且包含文件后缀
			if (fileName.indexOf(lastHourFile) != -1 && fileName.indexOf(fileFlag) != -1) 
			{
				System.out.println("正在处理文件：" + fileName);
				try
				{
					iniCnt.firstCount(file2);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("文件处理结束时间 :" + new Date().toString());

	} 
}
