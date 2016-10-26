# DnsLB4J
 A high performance load balancing dns proxy , writing in java.  
# Requirement
Linux  
JDK1.8+  
Maven 3.3+  
unzip  
# Usage:
git clone https://github.com/snail007/DnsLB4J.git  

cd DnsLB4J  

mvn install  

cp target/DnsLB4J-1.0-SNAPSHOT-package.zip /root/  

cd /root/  

unzip DnsLB4J-1.0-SNAPSHOT-package.zip  

cd DnsLB4J-1.0-SNAPSHOT  

java -jar DnsLB4J-1.0-SNAPSHOT-jar-with-dependencies.jar development  

# Notice

"development" is config subfolder's name,local it in config/development  

you can use "production" or "testing" or "development" for different environment.  

#Configuration

;UDP监听IP,所有IP使用：0.0.0.0  
listen_ip=0.0.0.0  
;UDP监听端口  
listen_port=10053  
;后端dns设置，格式ip：port，多个英文半角逗号分隔  
backend_dns=127.0.0.1:1053,127.0.0.1:1054,127.0.0.1:1055  
;备份dns设置，格式ip：port，多个英文半角逗号分隔  
backup_dns=192.168.1.1  
;备份dns文件设置，比如：/etc/resolv.conf，如果是其它文件，内容格式需要和/etc/resolv.conf一样  
backup_dns_file=  
;后端dns健康检查使用的域名  
check_domain=www.baidu.com  
;每次检测后端dns一个节点服务的次数  
check_count=3  
;每次检测一个节点check_count次，服务不可用的次数达到error_count次，节点被认为不可用  
error_count=3  
;每次检测后端dns一个节点服务一次的超时时间,单位毫秒  
check_timeout=3000  
;两次检测后端dns之间的时间间隔,单位毫秒  
check_interval=1000  
;域名解析结果是否缓存  
cache=true  
;域名解析结果最小缓存时间，单位秒  
ttl_min_seconds=1800  

#End
邮件功能暂时没有开发。  

