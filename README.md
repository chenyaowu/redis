# redis
redis特性
1.速度快（10w ops）
数据存储在内存，C语言实现，单线程，
2.持久化
所有数据保存在内存中，对数据的更新将异步地保存到磁盘上
3.多种数据结构
String/Blobs/Bitmaps,Hash Table,Linked List,Sets,Sorted Sets,HyperLogLog,GEO
4.支持多种编程语言
java php python ruby lua nodejs
5.功能丰富
发布订阅,Lua脚本,事务,pipeline
6.简单
23,000代码,不依赖外部库,单线程模型
7.主从复制
8.高可用，分布式

典型应用场景：缓存系统、计数器、消息队列系统、排行榜、社交网络、实时系统

常用配置
daemonize	是否是守护进程(no|yes)
port		redis对外端口
logfile		redis系统日志
dir			redis工作目录
