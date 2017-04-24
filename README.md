# java_spider
java爬虫

| 程序名 | 作用 | 部署 |
|----|----|----|
| UpdateIpAddress_MO | 更新142上的ip地址库 | 每小时执行一次，从ashman汇聚的文件中取出访问资讯和问答页的ip，通过网络访问api.map.baidu.com获取ip地址，并将ip地址入库。此外，每一个开发者账号每分钟访问百度地图API服务的次数是有限制的。每分钟并发量限制参见：http://lbsyun.baidu.com/index.php?title=webapi/ip-api。因此程序对超量并发导致获取不到ip地址的情况，做了延迟处理 |
| UpdateIpAddress_PC | | |
