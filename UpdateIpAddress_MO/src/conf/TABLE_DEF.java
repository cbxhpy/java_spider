package conf;

public enum TABLE_DEF {
	e_AppId,       // 应用标识
	e_EventType,   // 事件类型，值来源于“采集事件类型枚举表”     
	e_UserId,      // 用户标识，在业务平台上可唯一标识一个用户。
	e_UserFlag,    // 用户类型，枚举如下：1，注册用户 2，非注册用户
	e_PageId,      // 页面标识，唯一标识一个页面 {http://www.jianke.com/ask/question/16300980}
	e_Referrer,    // 来源页面标识与必备参数。 {http://www.baidu.com}
	e_Params,      // 页面扩展参数，其它个性化数据字典 { 'first_class':'外科'..}
	e_HostIp,      // 用户IP
	e_UserAgent,   // 用户代理信息
	e_TimeStamp,   // 时间戳，毫秒。
	e_HumanDate    // 年月日 时分秒	{2016-12-17 10:53:17}
};
