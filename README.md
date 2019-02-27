# Redis

## redis特性

1. **速度快**（10w ops）:数据存储在内存，C语言实现，单线程
2. **持久化** 所有数据保存在内存中，对数据的更新将异步地保存到磁盘上
3. **多种数据结构** String/Blobs/Bitmaps,Hash Table,Linked List,Sets,Sorted Sets,HyperLogLog,GEO
4. **支持多种编程语言** java php python ruby lua nodejs
5. **功能丰富** 发布订阅,Lua脚本,事务,pipeline
6. **简单** 23,000代码,不依赖外部库,单线程模型
7. **主从复制**
8. **高可用，分布式**

- 典型应用场景：缓存系统、计数器、消息队列系统、排行榜、社交网络、实时系统

- 安装

  ```bash
  [root@localhost soft]# wget http://download.redis.io/releases/redis-5.0.3.tar.gz
  [root@localhost soft]# tar -xvf redis-5.0.3.tar.gz
  [root@localhost redis-3.0.7]# make all
  [root@localhost redis-3.0.7]# make install
  ```

  | 可执行文件名     | 作用              |
  | ---------------- | ----------------- |
  | redis-server     | redis服务器       |
  | redis-cli        | redis命令行客户端 |
  | redis-benchmark  | redis性能测试工具 |
  | redis-check-aof  | AOF文件修复工具   |
  | redis-check-dump | RDB文件检查工具   |
  | redis-sentinel   | Sentinel服务器    |

- 常用配置
  daemonize	是否是守护进程(no|yes)
  port		    对外端口
  logfile		 系统日志
  dir		       工作目录