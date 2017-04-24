package bean;

import java.util.HashSet;

import conf.AppGlobal;

public class Traffic
{
	public int pv;
	public HashSet<String> uvSet = new HashSet<String>();
	public HashSet<String> ipSet = new HashSet<String>();
	
	public Traffic(String uv, String ip)
	{
		pv = 1;
		uvSet.add(uv);
		ipSet.add(ip);
	}
	
	public String toString()
	{
		return AppGlobal.SEPERATOR + pv + AppGlobal.SEPERATOR + uvSet.size() + AppGlobal.SEPERATOR + ipSet.size();
	}
}
