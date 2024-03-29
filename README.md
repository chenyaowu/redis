# Redis

## 目录

- [redis特性](#redis%E7%89%B9%E6%80%A7)
  - [典型应用场景](#典型应用场景)
  - [安装](#安装)
  - [常用配置](#常用配置)
  - [Redis单线程(为什么那么快)](#Redis单线程(为什么那么快))
- [Redis API](#redis-api)
  - [数据结构和内部编码](#数据结构和内部编码)
  - [通用命令](#通用命令)
  - [字符串](#字符串)
  - [Hash](#Hash)
  - [List](#List)
  - [Set](#Set)
  - [ZSet](#ZSet)
- [Jedis API](#jedisapi)
  - [创建方法](#创建方法)
  - [键相关](#键相关)
  - [字符串操作](#字符串操作)
  - [整数和浮点数操作](#整数和浮点数操作)
  - [列表操作](#列表操作)
  - [集合操作](#集合操作)
  - [哈希操作](#哈希操作)
  - [有序集合操作](#有序集合操作)
  - [排序](#排序)
- [功能](#功能)
  - [HyperLogLog](#HyperLogLog) 
  - [GEO](#GEO) 
  - [慢查询](#慢查询) 
  - [流水线pipeline](#流水线pipeline) 
  - [发布订阅](#发布订阅)
  - [位图bitmap](#位图bitmap)
- [Redis持久化](#Redis持久化)
  - [RDB](#RDB)
  - [AOF](#AOF)
  - [RDB与AOF](#RDB与AOF)
  - [持久化常见问题](#持久化常见问题)
- [Redis主从复制](#Redis主从复制)
  - [单机redis存在的问题](#单机redis存在的问题)
  - [Redis主从复制作用](#Redis主从复制作用)
  - [实现方式](#实现方式)
  - [全量复制](#全量复制)
  - [部分复制](#部分复制)
  - [故障处理](#故障处理)
  - [开发运维中的问题](#开发运维中的问题)
- [RedisSentinel](#RedisSentinel)
  - [主从复制问题](#主从复制问题)
  - [架构](#架构)
  - [安装与配置](#安装与配置)
  - [客户端实现基本原理](#客户端实现基本原理)
  - [客户端接入流程](#客户端接入流程)
  - [Jedis实现](#Jedis实现)
  - [内部三个定时任务](#内部三个定时任务)
  - [主观下线和客观下线](#主观下线和客观下线)
  - [领导者选举](#领导者选举)
  - [故障转移](#故障转移)
  - [选择“合适的”slave节点](#选择“合适的”slave节点)
- [RedisCluster](#RedisCluster)
  - [为什么需要redis集群](#为什么需要redis集群)
  - [分布方式](#分布方式)
  - [基本架构](#基本架构)
  - [安装](#安装)

  
## redis特性

1. **速度快**（10w ops）:数据存储在内存，C语言实现，单线程
2. **持久化** 所有数据保存在内存中，对数据的更新将异步地保存到磁盘上
3. **多种数据结构** String/Blobs/Bitmaps,Hash Table,Linked List,Sets,Sorted Sets,HyperLogLog,GEO
4. **支持多种编程语言** java php python ruby lua nodejs
5. **功能丰富** 发布订阅,Lua脚本,事务,pipeline
6. **简单** 23,000代码,不依赖外部库,单线程模型
7. **主从复制**
8. **高可用，分布式**

### 典型应用场景

- 缓存系统、计数器、消息队列系统、排行榜、社交网络、实时系统

### 安装

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

### 常用配置
- daemonize	是否是守护进程(no|yes)</br>
- port		    对外端口</br>
- logfile		 系统日志</br>
- dir		       工作目录</br>



### Redis单线程(为什么那么快)

1. **纯内存**
2. **非阻塞IO**
3. **避免线程切换和竞争消耗**
4. **注意**:一次只运行一条命令；拒绝长（慢）命令(keys、flushall、flushdb、slow lua script、mutil/exec、operate big value(collection))；其实不是单线程（fysnc file descriptor 、close file descritor）

## Redis API

### 数据结构和内部编码

![数据结构和内部编码](https://github.com/chenyaowu/redis/blob/master/image/dataStructure.jpg)

### 通用命令

| 命令               | 作用                                       | 时间复杂度 |
| ------------------ | ------------------------------------------ | ---------- |
| keys [pattern]     | 遍历所有的key(不建议在生产环境使用)        | O(n)       |
| dbsize             | 计算key总数                                | O(1)       |
| exists key         | 判断key是否存在                            | O(1)       |
| del key [key...]   | 删除key                                    | O(1)       |
| expire key seconds | key在seconds秒后过期                       | O(1)       |
| ttl key            | 查看key剩余时间                            | O(1)       |
| persis key         | 去掉key多余时间                            | O(1)       |
| type key           | 返回key类型(sting,hash,list,set,zset,none) | O(1)       |

### 字符串

- 使用场景：缓存、计数器、分布式锁

| 命令                                     | 作用                                        | 时间复杂度 |
| ---------------------------------------- | ------------------------------------------- | ---------- |
| get key                                  | 获取值                                      | O(1)       |
| set key                                  | 设置键值                                    | O(1)       |
| del key                                  | 删除key                                     | O(1)       |
| incr key                                 | key自增1，key不存在，自增后get(key) = 1     | O(1)       |
| decr key                                 | key自减1, key不存在，自减后get(key) = -1    | O(1)       |
| incrby key k                             | key自增k,如果key不存在，自增后get(key) = k  | O(1)       |
| decrby key k                             | key自减k,如果key不存在，自增后get(key) = -k | O(1)       |
|                                          |                                             |            |
| set   key value                          | 不管key是否存在，都设置                     | O(1)       |
| setnx key value                          | key不存在，才设置                           | O(1)       |
| set   key value xx                       | key存在，才设置                             | O(1)       |
|                                          |                                             |            |
| mget key1 key2 key3                      | 批量获取key,原子操作                        | O(n)       |
| mset key1 value1 key2 value2 key3 value3 | 批量设置key-value                           | O(n)       |
|                                          |                                             |            |
| getset key newvalue                      | set key newvalue并返回旧的value             | O(1)       |
| append key value                         | 将value追加到旧的value                      | O(1)       |
| strlen key                               | 返回字符串的长度                            | O(1)       |
|                                          |                                             |            |
| incrbyfloat key 3.5                      | 增加key对应的值3.5                          | O(1)       |
| getrange key start end                   | 获取指定字符串指定下标所有的值              | O(1)       |
| setrange key index value                 | 设置指定下标所有对应的值                    | O(1)       |

### Hash

- 结构：key   field value

| 命令                                      | 作用                                                    | 时间复杂度 |
| ----------------------------------------- | ------------------------------------------------------- | ---------- |
| hget key field                            | 获取hash key对应的field的value                          | O(1)       |
| hset key field value                      | 设置hash key对应field的value                            | O(1)       |
| hdel key field                            | 删除hash key对应field的value                            | O(1)       |
| hexists key field                         | 判断hash kye是否存在field                               | O(1)       |
| hlen key                                  | 获取hash key field的数量                                | O(1)       |
| hmget key field1 field2...                | 批量获取hash key的一批field对应的值                     | O(n)       |
| hmset key field1 value1 field2 value2 ... | 批量设置hash key的一批 field value                      | O(n)       |
|                                           |                                                         |            |
| hincrby key filed k                       | 增加hash key对应的field的值增加k                        | O(1)       |
|                                           |                                                         |            |
| hgetall key                               | 获取所有key value                                       | O(n)       |
| hvals key                                 | 获取所有filed的value                                    | O(n)       |
| hkeys key                                 | 获取hash key对应所有的field                             | O(n)       |
|                                           |                                                         |            |
| hsetnx key field value                    | 设置hash key对应field的value(如果field已经存在，则失败) | O(1)       |
| hincrby key field k                       | hash key对应的field的value自增k                         | O(1)       |
| hincrbyfloat key field k                  | hincrby浮点数版                                         | O(1)       |

- 对象转String 与 Hash对比

  | 格式                | 优点                         | 缺点                                     |
  | ------------------- | ---------------------------- | ---------------------------------------- |
  | 对象转json存        | 编程简单、可能节省内存       | 1.序列化开销。2.设置属性要操作整个数据。 |
  | 对象每个参数一个key | 直观、可以部分更新           | 1.内存占用较大。2.key较为分散            |
  | 对象每个参数做feild | 直观、节省空间、可以部分更新 | 1.编程稍微复杂。2.ttl不好控制            |

  

### List

| 命令                        | 作用                                                         | 时间复杂度 |
| --------------------------- | ------------------------------------------------------------ | ---------- |
| rpush key value1  value2... | 从列表右边插入值                                             | O(n)       |
| lpush key value1 value2...  | 从列表左边插入值                                             | O(n)       |
| linsert key before          | after value newValue                                         | O(n)       |
| lpop key                    | 从列表左侧弹出一个item                                       | O(1)       |
| rpop key                    | 从列表右侧弹出一个item                                       | O(1)       |
| ltrim key start end         | 按照索引范围修剪列表                                         | O(n)       |
| lrange key start end        | 获取列表指定索引范围所有的item                               | O(n)       |
| lindex key index            | 获取列表指定索引的item                                       | O(n)       |
| llen key                    | 获取列表的长度                                               | O(n)       |
| lset key index newValue     | 设置列表指定索引值为newValue                                 | O(n)       |
| lrem key count value        | 根据count值，从列表中删除所有value相等的项，count>0，从左到右，删除最多count个value相等的项；count<0，从右到左，删除最多-count个value相等的项，count=0，删除所有value相等的项 | O(n)       |
| blpop key timeout           | lpop阻塞版本，timeout是阻塞超时时间,timeout=0为永远不阻塞    | O(1)       |
| brpop key timeout           | rpop阻塞版本，timeout是阻塞超时时间，timeout=0为永远不阻塞   | O(1)       |

### Set

| 命令                    | 作用                                                | 时间复杂度 |
| ----------------------- | --------------------------------------------------- | ---------- |
| sadd key element        | 向集合key添加element(如果element已经存在，添加失败) | O(1)       |
| srem key element        | 将集合key中的element移除                            | O(1)       |
| scard key               | 计算集合大小                                        | O(1)       |
| sismenber key element   | 判断element是否存在                                 | O(1)       |
| srandmember key element | 从集合中随机挑count个元素                           | O(1)       |
| smembers key            | 获取集合所有元素                                    | O(1)       |
| spop key                | 从集合中随机弹出一个元素                            | O(1)       |
|                         |                                                     |            |
| sdiff key1 key2         | 差集                                                |            |
| sinter key1 key2        | 交集                                                |            |
| sunion key1 key2        | 并集                                                |            |

### ZSet

- 结构  key   score value

| 命令                                             | 作用                                 | 时间复杂度  |
| ------------------------------------------------ | ------------------------------------ | ----------- |
| zadd key score element(可以是多对)               | 添加score和element                   | O(logN)     |
| zrem key element(可以是多个)                     | 删除元素                             | O(1)        |
| zscore key element                               | 返回元素分数                         | O(1)        |
| zincrby key increScore element                   | 增加或减少元素分数                   | O(1)        |
| zcard key                                        | 返回元素的总个数                     | O(1)        |
| zrange key start end [withscores]                | 返回指定索引返回的升序元素[分值]     | O(log(n)+m) |
| zrangebyscore key minScore maxScore [withscores] | 返回指定分数范围的升序元素[分值]     | O(log(n)+m) |
| zcount key minScore maxScore                     | 返回有序集合内在指定分数范围内的个数 | O(log(n)+m) |
| zremrangebyrank key start end                    | 删除指定排名内的升序元素             | O(log(n)+m) |
| zremrangebyscore key minScore maxScore           | 删除指定分数内升序元素               | O(log(n)+m) |

## JedisAPI

### 创建方法

```JAVA
Jedis jedis = new Jedis(String ip, int port); 	Jedis直连
//Jedis连接池的使用
GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
JedisPool jedisPool = new JedisPool(poolConfig,String ip,int port);
Jedis jedis = jedisPool.getResource();
```

### 键相关

```java
//清空数据
public String jedis.flushDB()

//判断key是否存在
public Boolean exists(String key)

//新增(覆盖)键值对
public String set(String key, String value)

//获取匹配正则表达式的key
public Set<String> keys(String pattern)

//删除key
public Long del(String key)

//设置key过期时间
public Long expire(String key, int seconds)

//获取key剩余生存时间
public Long	ttl(String key)

//移除key的生存时间限制
public Long persist(String key)

//查看key对应value的数据类型
public String type(String key)
```

### 字符串操作

```java
//新增(存在则覆盖)键值对
public String set(String key, String value)

//新增(存在不插入)键值对
public Long setnx(String key, String value)

//新增数据并设置有效时间
public String setex(String key, int seconds, String value)

//删除key
public Long del(String key)

//获取key对应的值
public String get(String key)

//在key对应的值后面追加value
public Long append(String key, String value)

//增加多个键值对
public String mset(String... keysvalues)

//获取多个key对应的值
public List<String> mget(String... keys)

//删除多个key对应的value
public Long del(String... keys)

//获取key对应的value并更新value
public String getSet(String key, String value)

//获取key对应value第i到j个字符
public String getrange(String key, long startOffset, long endOffset)
```

### 整数和浮点数操作

```java
//新增(存在则覆盖)键值对
public String set(String key, String value)

//获取key对应的值
public String get(String key)

//将key对应的value自增1
public Long incr(String key)

//将key对应的value增加increment
public Long incrBy(String key, long increment)

//将key对应的value自减1
public Long decr(String key)

//将key对应的value减increment
public Long decrBy(String key, long increment)
```

### 列表操作

```java
//往key中左插入strings
public Long lpush(String key, String... strings)

//往key中右插入strings
public Long rpush(String key, String... strings)

//获取key对应List区间[i, j]元素
public List<String> lrange(String key, long start, long stop)

//根据count值，从列表中删除所有value相等的项，count>0，从左到右，删除最多count个value相等的项。count<0，从右到左，删除最多-count个value相等的项，count=0，删除所有value相等的项
public Long lrem(String key, long count, String value)

//删除list区间[i, j]之外的元素
public String ltrim(String key, long start, long stop)

//左弹出一个元素
public String lpop(String key)

//右弹出一个元素
public String rpop(String key)

//修改key对应list指定下标元素
public String lset(String key,long index,String value)

//获取list长度
public Long llen(String key)

//获取key对应下标的元素
public String lindex(String key, long index)

//将key中对应的List中的元素从小到大排序
public List<String> sort(String key)
```

### 集合操作

```java
//往key中添加集合
public Long sadd(String key, String... members)

//获取key对应的所有元素
public Set<String> smembers(String key)

//删除key中值为members的元素
public Long srem(String key,String... members)

//随机弹出一个元素
public String spop(String key)

//获取set中元素个数
public Long scard(String key)

//将元素member从srckey剪切到dstkey
public Long smove(String srckey, String dstkey, String member)

//获取集合交集
public Set<String> sinter(String... keys)

//获取集合并集
public Set<String> sunion(String... keys)

//获取集合差集
public Set<String> sdiff(String... keys)
```

### 哈希操作

```java
//添加一个hash
public String hmset(String key, Map<String,String> hash) 		

//往hash中插入一个元素（key-value)
public Long hset(String key,String field,String value) 	

//获取所有的元素
public Map<String,String> hgetAll(String key) 

//获取hash所有的元素key
public Set<String> hkeys(String key)

//获取hash所有的value
public List<String> hvals(String key)

//hash中对应的field自增1
public Long hincr(String key, String )

//hash中对应的field增加value
public Long hincrBy(String key, String field,long value) 

//删除hash中对应的field
public Long hdel(String key,String... fields) 

//获取hash中元素的个数
public Long hlen(String key) 		

//判断hash中是否存在field
public Boolean hexists(String key, String field) 

//获取hash中元素
public List<String> hmget(String key,String... fields)         		 
```

### 有序集合操作

```java
//向zset中添加一个集合
public Long zadd(String key,Map<String,Double> scoreMembers)

//向zset中添加一个元素
public Long zadd(String key,double score,String member)

//获取zset下标区间为[start,stop]元素（按分数排序）
public Set<String> zrange(String key,long start,long stop) 

//获取zset下标区间为[start,stop]元素（ 按分数排序 Val-Score ）
public Set<Tuple> zrangeWithScores(String key,long start,long stop)

//获取zset分数区间为[min,max]的元素
public Set<String> zrangeByScore(String key,double min,double max)

//获取zset分数区间为[min,max]的Val-Score元素
public Set<String> zrangeByScoreWithScores(String key,double min,double max)

//获取zset里值为member的score
public Double zscore(String key,String member) 

//获取zset里值为member的排名
public Long zrank(String key, String member)

//删除zset里值为member的元素
public Long zrem(String key,String... members) 

//获取zset个数
public Long zcard(String key)

// 获取zset中分数范围在[min,max]元素的个数
public Long zcount(String key,double min,double max)

//zset中值为member的分数增加increment
public Double zincrby(String key, double increment, String member)   
```

### 排序

```java
//生成排序对象
SortingParams sortingParams = new SortingParams(); 				
//获取排序后元素
public List<String> sort(String key, SortingParams sortingParameters)    
//按字母a-z排序
sortingParams.alpha();
//按数字升序排序
sortingParams.asc(); 
//按数字降序排序
sortingParams.desc();   
```



## 功能

### HyperLogLog

- 基于HyperLogLog算法：极小空间完成独立数量统计

- 本质还是字符串

- API

  | 命令                                     | 作用                     |
  | ---------------------------------------- | ------------------------ |
  | pfadd ley element [element...]           | 向hyperLogLog添加元素    |
  | pfcount key [key...]                     | 计算hperLogLog的独立总数 |
  | pfmerge destkey sourcekey [sourcekey...] | 合并多个hyperLogLog      |

- 内存消耗(百万独立用户)

  | 使用天数 | 内存消耗 |
  | -------- | -------- |
  | 1天      | 15KB     |
  | 1个月    | 450KB    |
  | 1年      | 5MB      |

- 使用经验：1.是否能容忍错误？（错误率：0.81%）。2.是否需要单条数据？

  

### GEO

- GEO(地理位置信息)：存储经纬度，计算两地巨鹿，范围计算等

- API

  | 命令                                                         | 作用                                                |
  | ------------------------------------------------------------ | --------------------------------------------------- |
  | geoadd key longitude latitude member [longitude latitude member... ] | 增加地理信息                                        |
  | geopos key member [member...]                                | 获取地理位置信息                                    |
  | eodist key member1 member2 [unit]                            | 获取两个地理位置的距离 unit:m、km、mi(英里)、ft(尺) |
  | georadius key longitude latitude radiusm\|km\|ft\|mi\|[withcoord] [withdist] [withhash] [COUNT count] [asc\|desc] [store key] [storedist key] | 获取指定位置范围内的地理位置信息集合                |
  | georadiusbymember key member radiusm\|km\|ft\|mi [withcoord] [withdist]   [withhash] [COUNT count] [asc\|desc] [store key] [storedist key] | 获取指定位置范围内的地理位置信息集合                |

  参数说明

  | 参数                                   | 作用 |
  | -------------------------------------- | ---- |
	|withcoord 		|返回结果中包含经纬度|
	|withdist 		|返回结果中包含距离中心节点位置|
	|withhash 		|返回结果中包含geohash|
	|COUNT count 	|指定返回结果的数量|
	|asc|desc 		|返回结果按照距离中心节点的距离做升序或降|序|
	|store key  		|将返回结果的地理位置信息保存到指定键|
	|storedist key 	|将返回结果距离中心节点的距离保存到指定的键|

  

### 慢查询

- Redis生命周期

  ![Redis生命周期](https://github.com/chenyaowu/redis/blob/master/image/life_cycle.jpg)

  - 慢查询发生在第三阶段执行命令期间
  - 客户端超时不一定是慢查询，但慢查询是客户端超时的一个因素

- 两个配置（记录慢查询）

  - slowlog-max-len
    1. 先进先出
    2. 固定长度
    3. 保存在内存内
  - slowlog-log-slower-than
    1. 慢查询阈值(ms)
    2. slowlog-log-slower-than=0，记录所有命令
    3. slowlog-log-slower-than<0，不记录任何命令

- 配置方法

  1. 默认值
     config get slowlog-max-len = 128
     config get slowlog-log-slower-than= 10000
  2. 修改配置文件后重启(不建议)
  3. 动态配置
     config set slowlog-max-len 1000
     config set slowlog-log-slower-than 1000

- 相关命令

  | 命令            | 作用               |
  | --------------- | ------------------ |
  | slowlog get [n] | 获取n条慢查询队列  |
  | slowlog len     | 获取慢查询队列长度 |
  | slowlog reset   | 清空慢查询队列     |

- 运维经验

  - slowlog-max-len不要设置过大，默认10ms，通常1ms
  - slowlog-log-slower-than不要设置过小，通常设置1000左右
  - 理解命令生命周期
  - 定期持久化慢查询

### 流水线pipeline

- 作用(为了解决减少n次网络通信时间)

  | 命令                 | 时间            | 数据量  |
  | -------------------- | --------------- | ------- |
  | n个命令操作          | n次网络+n次命令 | 1条命令 |
  | 1次pipeline(n个命令) | 1次网络+n次命令 | n条命令 |

- Jedis实现(pipeline操作不是原子操作)

  ```java
  //不使用pipeline:
  for(int i=0; i<10000; i++){
  	jedis.hset("hashkey:" + i, "field" + i, "value" + i);
  }
  
  
  //使用pipeline
  Pipeline pipeline = jedis.pipelined();
  for(int i=0; i<10000; i++){
  	pipeline.hset("hashkey:" + i, "field" + i, "value" + i);
  }
  pipeline.syncAndReturnAll();
  ```

- 使用建议

  1. 注意每次pipeline携带数据量

  2. pipeline每次只能作用在一个redis节点上

  3. M操作与pipeline的区别

     ![M操作](https://github.com/chenyaowu/redis/blob/master/image/pipeline1.jpg)

     ![pipeline](https://github.com/chenyaowu/redis/blob/master/image/pipeline2.jpg)

### 发布订阅

- 角色

  - 发布者(publisher)
  - 订阅者(subscriber)
  - 频道(channel)

- 模型

  ![模型1](https://github.com/chenyaowu/redis/blob/master/image/publish_subscrib_module.jpg)

  ![模型2](https://github.com/chenyaowu/redis/blob/master/image/publish_subscrib_module2.jpg)

- API

  | 命令 | 作用 |
  | ---- | ---- |
  |publish channel message 		|发布消息|
  |subscribe [channel...] 			|订阅消息|
  |subscribe [channel...] 			|取消订阅|
  |psubscribe [pattern...] 		|订阅符合条件的频道|
  |punsubscribe [pattern...] 		|取消订阅符合条件的频道|
  |pubsub channels 				|列出至少有一个订阅者的频道|
  |pubsub numpat 					|列出被订阅模式的数量|

- 消息队列

  ![消息队列](https://github.com/chenyaowu/redis/blob/master/image/message_queue.jpg)



### 位图bitmap

- 位图

  ![bitmap](https://github.com/chenyaowu/redis/blob/master/image/bitmap.jpg)

- API

  | 命令 | 作用 |
  | ---- | ---- |
  |setbit key offset value 		|给位图指定索引设置值|
  |getbit key offset  				|获取位图指定索引值|
  |bitcount key [start end] 		|获取位图指定范围(start到end，单位为字节m，如果不指定就是获取全部)位值为1的个数|
  |bitop op destkey key [key...] 	|做多个Bitmap的and(交集),or(并集),not(非),xor(异或)操作并将结果保存到destkey中|
  |bitpos key targeBit[start] [end] |计算位图指定范围(start到end,单位为字节,如果不指定就是获取全部)第一个偏移量对应的值等于targetBit的位置|

- 使用经验

  1. type=string，最大512MB
  2. 注意setbit时的偏移量，可能有较大耗时
  3. 位图不是绝对好。





## Redis持久化

- 作用：redis所有数据保持在内存中，对数据的更新将异步地保存到磁盘中redis所有数据保持在内存中，对数据的更新将异步地保存到磁盘中
- 持久化方式
  - 快照（redis RDB)
  - 写日志  (redis AOF)

  

### RDB

![bitmap](https://github.com/chenyaowu/redis/blob/master/image/RDB.jpg)

- 触发机制三种方式

  - save(同步)

    ![RDB_Save](https://github.com/chenyaowu/redis/blob/master/image/RDB_Save1.jpg)

    - save命令（redis>save）(阻塞，不消耗额外内存)

      ![RDB_Save](https://github.com/chenyaowu/redis/blob/master/image/RDB_Save2.jpg)

    - 文件策略：如存在老的RDB文件，替换 

    - 复杂度：O(n)

  - bgsave(异步)（redis>bgsave）

    ![bgsave](https://github.com/chenyaowu/redis/blob/master/image/RDB_Save3.jpg)

    - 文件策略：如存在老的RDB文件，替换 
    - 复杂度：O(n)

  - sava和bgsave对比

    | 命令     | save               | bgsave               |
    | -------- | ------------------ | -------------------- |
    | IO类型   | 同步               | 异步                 |
    | 是否阻塞 | 是                 | 是（阻塞发生在fork） |
    | 复杂度   | O(n)               | O(n)                 |
    | 优点     | 不会消耗额外的内存 | 不阻塞客户端命令     |
    | 缺点     | 阻塞客户端命令     | 需要fork，消耗内存   |

    

  - 自动(设置配置文件)

    ![自动](https://github.com/chenyaowu/redis/blob/master/image/RDB_Save4.jpg)

    ```bash
    #900秒中改变了1条数据则记录(不建议使用)
    save 900 1
    #300秒中改变了10条数据则记录(不建议使用)
    save 300 10
    #60秒中改变了10000条数据则记录(不建议使用)
    save 60 10000
    #rbd文件名
    dbfilename dump-${port}.rdb
    #rdb文件存放目录
    dir /bigdiskpath
    #rdb出现错误是否停止写入
    stop-writes-on-bgsave-error yes
    #rbd文件是否采用压缩格式
    rdbcompression yes
    #是否对rdb文件进行校验和检验
    rdbchecksum yes
    ```

    - 触发机制-自动生成rdb文件
      1. 全量复制
      2. debug reload
      3. shutdown

- 总结

  - RDB是Redis内存到硬盘的快照，用于持久化
  - save通常会阻塞Redis
  - bgsave不会阻塞Redis，但是会fork新进程
  - save自动配置满足任一就会被执行
  - 有些触发机制不容忽视



### AOF

- RDB存在的问题

  - 耗时耗性能

    ![耗时耗性能](https://github.com/chenyaowu/redis/blob/master/image/AOF1.jpg)

  - 不可控、丢失数据

- 运行原理

  - 创建

    ![创建](https://github.com/chenyaowu/redis/blob/master/image/AOF2.jpg)

  - 恢复

    ![恢复](https://github.com/chenyaowu/redis/blob/master/image/AOF3.jpg)

- 三种策略

  - always

    ![always](https://github.com/chenyaowu/redis/blob/master/image/AOF4.jpg)

  - everysec(默认值)

    ![everysec](https://github.com/chenyaowu/redis/blob/master/image/AOF5.jpg)

  - no

    ![no](https://github.com/chenyaowu/redis/blob/master/image/AOF6.jpg)

  - 对比

    | 三种策略 | 命令         | 缺点        | 优点          |
    | -------- | ------------ | ----------- | ------------- |
    | always   | 每一次       | IO开销大    | 不丢失数据    |
    | everysec | 每秒         | 丢失1秒数据 | 只丢失1秒数据 |
    | no       | 根据操作系统 | 不可控      | 不用管        |

      

  - AOF重写（优化AOF文件命令）

    ![AOF重写](https://github.com/chenyaowu/redis/blob/master/image/AOF7.jpg)

    - 作用：减少磁盘占用量、加速恢复速度

    - 两种实现方式：

      - bgrewiteaof命令

        ![bgrewiteaof命令](https://github.com/chenyaowu/redis/blob/master/image/AOF8.jpg)

      - AOF重写配置

        ```bas
        #使用aof基础
        appendonly yes
        ##文件名
        appendfilename "appendonly-${port}.aof"  
        ##策略
        appendfsync everysec
        ##目录
        dir /bigdiskpath 	
        ##aof重写的是否进行append操作
        no-appendfsync-no-rewtire yes 
        ##AOF文件增长率
        auto-aof-rewrite-percentage
        ##AOF文件重写需要的尺寸
        auto-aof-rewrite-min-size
        
        ##统计相关
        ##AOF当前尺寸(字节)
        aof_current_size 
        ##AOF上次启动和重写的尺寸(字节)
        aof_base_size
        ```



### RDB与AOF
|命令			|RDB  	|AOF|
|----|----|----|
|启动优先级 	|低 		|高|
|体积	 		|小 		|大|
|恢复速度 	|快 		|慢|
|数据安全性   |丢数据  |根据策略决定|
|操作轻重		|重 		|轻|

- RDB最佳策略

  - 关
  - 集中管理
  - 主从，从开

- AOF最佳策略

  - "开"缓存或者储存
  - AOF重写集中管理
  - everysec

- 最佳策略

  - 小分片
  - 缓存或者储存
  - 监控(硬盘、内存、负载、网络)
  - 足够内存

### 持久化常见问题

- fork操作(同步操作)，与内存量息息相关：内存越大，耗时越长(与机器类型有关)

  ```bash
  #查看上次执行fork的时间
  info:latest_fork_usec
  ```

  - 改善fork:
    - 优先使用物理机或者搞笑支持fork操作的虚拟化技术
    - 控制redis实例最大可用内存:maxmemory
    - 合理配置Linux内存分配策略:vm.overcommit_memory=1
    - 降低fork频率：例如放宽AOF重写自动触发时机，不必要的全量复制

- 进程外开销

  - CPU 
    - 开销：RDB和AOF文件生成，属于CPU密集型
    - 优化：不做CPU绑定，不和CPU密集型部署
  - 内存
    - 开销：fork内存开销,copy-on-write
    - 优化：echo never>/sys/kernel/mm/transparent_hugepage/enabled
  - 硬盘
    - 开销：AOF和RDB文件写入，可以结合iostat,iotop分析
    -  优化：不要和高硬盘负载服务部署在一起、no-appendfsync-on-rewrite = yes、根据写入量决定磁盘类型、单机多实例持久化文件目录可以考虑分盘

- AOF追加阻塞

  ![AOF追加阻塞](https://github.com/chenyaowu/redis/blob/master/image/AOF9.jpg)

  - AOF阻塞定位：

    - redis日志：Asynchronous AOF fsync is taking too long (disk is busy?). Writing the AOF buffer without waiting for fsync to complete, this may slow down Redis

    - info persistence

      ```bash
      #发生阻塞数量
      127.0.0.1:6379>info persistence 
      aof_delayed_fsync:100
      ```

    - 通过硬盘

      ![通过硬盘](https://github.com/chenyaowu/redis/blob/master/image/AOF10.jpg)

    ​	

## Redis主从复制

### 单机redis存在的问题

- 机器故障，数据丢失，连接失败
- 容量瓶颈
- 3.QPS瓶颈

### Redis主从复制作用

- 数据副本
- 扩展读性能

- 一主一从

![Redis——OneToOne](https://github.com/chenyaowu/redis/blob/master/image/master_slave1.jpg)

- 一主多从

![Redis——OneToOne](https://github.com/chenyaowu/redis/blob/master/image/master_slave2.jpg)



### 实现方式

- 命令方式(slave of)

  - 开启复制

    ![slave of](https://github.com/chenyaowu/redis/blob/master/image/master_slave3.jpg)

  - 取消复制

    ![slave of no one](https://github.com/chenyaowu/redis/blob/master/image/master_slave4.jpg)

- 配置方式(修改配置文件)

  ```bash
  slaveof ip port
  slave-read-only yes
  ```


### 全量复制

![全量复制](https://github.com/chenyaowu/redis/blob/master/image/master_slave5.jpg)

- 开销
  1. bgsave时间
  2. RDB文件网络传输时间
  3. 从节点清空数据时间
  4. 从节点加载RDB时间
  5. 可能的AOF重写时间

### 部分复制

- since redis2.8

![部分复制](https://github.com/chenyaowu/redis/blob/master/image/master_slave6.jpg)



### 故障处理

- Slave故障

  ![Slave故障](https://github.com/chenyaowu/redis/blob/master/image/master_slave7.jpg)

- master故障

  ![master故障](https://github.com/chenyaowu/redis/blob/master/image/master_slave8.jpg)

- 使用redis sentinel

  ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/master_slave9.jpg)



### 开发运维中的问题

- 读写分离:读流量分摊到从节点

  - 可能遇到的问题：
    - 复制数据延迟
    - 读到过期数据
    - 从节点故障

- 主从配置不一致

  - 例如maxmemory不一致：丢失数据
  - 例如数据结构优化参数(hash-max-ziplist-entries):内存不一致

- 规避全量复制

  -  第一次全量复制，第一次不可避免

    - 小主节点，低峰
  -  节点run id不匹配

    -   主节点重启(run id变化)
    -   故障转移，例如哨兵或集群
  -  复制积压缓冲区不足

    - 网络中断，部分复制无法满足
    - 增大复制缓冲区配置rel_backlog_size，网络"增强"
  -  规避复制风暴(一个主节点多个从节点，主节点宕机，重启，多个从节点复制数据)
     - 单主节点复制风暴
       - 问题：主节点重启，多从节点复制
       -  解决：更换复制拓扑
     - 单机器复制风暴
       -   问题：机器宕机后，大量全量复制
       -  解决：主节点分散多机器

## RedisSentinel

### 主从复制问题

- 手动故障转移
- 写能力和存储能力受限

### 架构

![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel1.jpg)

![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel5.jpg)

- 故障转移

  ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel2.jpg)

  ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel3.jpg)

  ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel4.jpg)

### 安装与配置

- redis-7000.conf

  ```bash
  port 7000
  daemonize yes
  dir "/opt/soft/redis/data"
  logfile "7000.log"
  dbfilename "dump-7000.rdb"
  ```

- redis-7001.conf

  ```bash
  port 7001
  daemonize yes
  dir "/opt/soft/redis/data"
  logfile "7001.log"
  dbfilename "dump-7001.rdb"
  slaveof 127.0.0.1 7000
  ```

- redis-7002.conf

  ```bash
  port 7002
  daemonize yes
  dir "/opt/soft/redis/data"
  logfile "7002.log"
  dbfilename "dump-7002.rdb"
  slaveof 127.0.0.1 7000
  ```

- redis-sentinel-26379.conf

  ```b
  port 26379
  daemonize yes
  dir "/opt/soft/redis/data"
  logfile "26379.log"
  sentinel monitor mymaster 127.0.0.1 7000 2
  sentinel down-after-milliseconds mymaster 30000
  sentinel parallel-syncs mymaster 1
  sentinel failover-timeout mymaster 180000
  ```

- redis-sentinel-26380.conf

  ```bash
  port 26380
  daemonize yes
  dir "/opt/soft/redis/data"
  logfile "26380.log"
  sentinel monitor mymaster 127.0.0.1 7000 2
  sentinel down-after-milliseconds mymaster 30000
  sentinel parallel-syncs mymaster 1
  sentinel failover-timeout mymaster 180000		
  
  ```

- redis-sentinel-26381.conf

  ```bash
  port 26381
  daemonize yes
  dir "/opt/soft/redis/data"
  logfile "26381.log"
  sentinel monitor mymaster 127.0.0.1 7000 2
  sentinel down-after-milliseconds mymaster 30000
  sentinel parallel-syncs mymaster 1
  sentinel failover-timeout mymaster 180000	
  ```



### 客户端实现基本原理

1. Step1

   ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel6.jpg)

2. Step2

   ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel7.jpg)

3. Step3

   ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel8.jpg)

4. Step4

   ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel9.jpg)

- 基本原理

  ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel10.jpg)

### 客户端接入流程

1. Sentinel地址集合
2. masterName
3. 不是代理模式

### Jedis实现

```java
String masterName = "mymaster";
Set<String> sentinelSet = new HashSet();
set.add("127.0.0.1:26379");
set.add("127.0.0.1:26380");
set.add("127.0.0.1:26381");

JedisSentinelPool sentinelPool = new JedisSentinePool(masterName, sentinelSet);
Jedis jedis = null;
try{
	jedis = sentinelPool.getResource();
}catch(Exception e){
	e.printStack();	
}finally{
	if(jedis != null){
		jedis.close();
	}
}

```

### 内部三个定时任务

1. 每10秒info

   - 发现slave节点
   - 确认主从关系

   ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel11.jpg)

2. 每2秒发布订阅（每2秒每个sentinel通过master节点的channel交换信息(pub/sub)）

   - 通过_sentinel_:hello频道交互
   - 交互对节点的“看法”和自身信息

   ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel12.jpg)

3. 每1秒ping(每1秒每个sentinel对其他sentinel和redis执行ping)

   - 心跳检测，失败判定依据

   ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel13.jpg)



### 主观下线和客观下线

- 主观下线：每个sentinel节点对redis节点失败的“偏见”
- 客观下线：所有sentinel节点对redis节点失败“达成共识”（超过quorum个统一）

### 领导者选举

- 原因：只有一个sentinel节点完成故障转移

- 选举：通过sentinel is-master-down-by-addr命令都希望成为领导者
  1. 每个做主观下线的Sentinel节点向其他Sentinel节点发送命令，要求将它设置为领导者
  2. 收到命令的Sentinel节点如果没有同意通过其他Sentinel节点发送的命令，那么将同意该请求，否则拒绝
  3. 如果该Sentinel节点发现自己的票数已经超过Sentinel集合半数且超过quorum,那么它将成为领导者
  4. 如果此过程有过个Sentinel节点成为领导者，那么将等待一段时间重新进行选举。

### 故障转移

- sentinel领导者节点完成
  1. 从slave节点中选出一个“合适的”节点作为新的master节点
  2. 对上面的slave节点执行slaveof no one命令让它成为master节点
  3. 向剩余的slave节点发送命令，让它们成为新的master节点的slave节点，复制规则和parallel-syncs参数有关
  4. 更新对原来的master节点配置成slave，并保持着对其“关注”，当其恢复后命令它去复制新的master节点。

### 选择“合适的”slave节点

1. 选择slave-priprity(slave节点优先级)最高的slave节点，如果存在则返回，不存在则继续
2. 选择复制偏移量最大的slave节点(复制最完整)，如果存在则返回，不存在则继续
3. 选择runId最小的slave节点



### 常见问题

- 节点运维

  原因：

  机器下线：例如过保等情况机器

  性能不足：例如CPU、内存、硬盘、网络等

  节点自身故障：例如服务不稳定等

  - 节点下线
    - 主节点：sentinel failover <masterName>
    - 从节点/Sentinel节点：临时下线还是永久下线，例如是否做一些清理工作，但是要考虑读写分离的情况
  - 节点上线
    - 主节点：sentinel failover进行替换
    - 从节点：slaveof即可，sentinel节点可以感知
    - sentinel节点：参考其他sentinel节点启动

- 高可用读写分离

  ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisSentinel14.jpg)

  - 从节点作用
    - 副本：高可用的基础
    - 扩展：读能力
  - 三个“消息”
    - +switch-master : 切换主节点（从节点晋升主节点）
    - +convert-to-slave：切换从节点（原主节点将为从节点）
    - +sdown：主观下线。

## RedisCluster

### 为什么需要redis集群

1. 并发量不足 10W OPS/s
2. 单机存储数据量不足
3. 网卡流量不足

### 分布方式 
| 分布方式 | 特点                                                       | 典型产品                         |
| -------- | ---------------------------------------------------------- | -------------------------------- |
| 哈希分区 | 数据分散度高，键值分布业务无关，无法顺序访问，支持批量操作 | 一致性哈希Memcache Redis Cluster |
| 顺序分区 | 数据分散度易倾斜，键值业务相关，可顺序访问，支持批量操作   | BigTable HBase                   |
- 哈希分区方式
  1. 节点取余分区
     - hash(key)%nodes
     - 客户端分片：哈希 + 取余
     - 节点伸缩：数据节点关系变化，导致数据迁移
     - 迁移数量和添加节点数量有关：建议翻倍扩容
  2. 一致性哈希分区
     - 客户端分片：哈希 + 顺时针（优化取余） 
     - 节点伸缩：只影响邻近节点，但是还是有数据迁移
     - 翻倍伸缩：保证最小迁移数据和负载均衡
  3. 虚拟槽分区
     - 预设虚拟槽：每个槽映射一个数据子集，一般比节点数大
     - 良好的哈希函数：例如CRC16
     - 服务端管理节点、槽、数据：例如Redis Cluster

### 基本架构

- 单机架构

  ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster21.jpg)

- 分布式架构

  ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster22.jpg)

- Redis Cluster架构

  - 节点

    ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster23.jpg)

  - meet

    ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster24.jpg)

    ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster25.jpg)

  - 指派槽

    ![redis sentinel](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster26.jpg)

  - 复制

  - Redis Cluster特性：复制、高可用、分片


### 安装

- 原生安装(命令)
  1. 配置开启Redis

     ```bash
     port ${port}
     daemonize yes
     dir "/opt/soft/redis/data"
     logfile "${port}.log"
     dbfilename "dump-${port}.rdb"
     cluster-enabled yes
     cluster-config-file node-${port}.conf
     
     redis-server reids-7000.conf
     redis-server reids-7001.conf
     redis-server reids-7002.conf
     redis-server reids-7003.conf
     redis-server reids-7004.conf
     redis-server reids-7005.conf
     ```

  2. meet

     ```bash
     cluster meet ip port
     
     redis-cli -p 7000 cluster meet 127.0.0.1 7001
     redis-cli -p 7000 cluster meet 127.0.0.1 7002
     redis-cli -p 7000 cluster meet 127.0.0.1 7003
     redis-cli -p 7000 cluster meet 127.0.0.1 7004
     redis-cli -p 7000 cluster meet 127.0.0.1 7005
     ```

  3. Cluster节点主要配置

     ```bash
     cluster-enabled yes
     cluster-node-timeout 15000
     cluster-config-file "node.conf"
     cluster-require-full-coverage yes
     ```

  4. 分配槽

     ```bash
     cluster addslots slot [slot...]
     
     redis-cli -h 127.0.0.1 -p 7000 cluster addslots{0...5461}
     redis-cli -h 127.0.0.1 -p 7001 cluster addslots{5462...10922}
     redis-cli -h 127.0.0.1 -p 7002 cluster addslots{10923...16383}
     ```

  5. 设置主从

     ```bash
     cluster replicate node-id
     
     redis-cli -h 127.0.0.1 -p 7003 cluster replicate ${node-id-7000}
     ```

- 原生安装(具体操作)

  1. 创建文件：redis-7000.conf、redis-7001.conf、redis-7002.conf、redis-7003.conf、redis-7004.conf、redis-7005.conf
  ```bash
  port ${port}
  daemonize yes
  dir "/opt/soft/redis/data"
  logfile "${port}.log"
  dbfilename "dump-${port}.rdb"
  cluster-enabled yes
  cluster-config-file node-${port}.conf
  cluster-require-full-coverage no
  ```
  2. 启动

     ```bash
     redis-server redis-7000.conf
     redis-server redis-7001.conf
     redis-server redis-7002.conf
     ...
     ```

  3. meet

     ```bash
     redis-cli -p 7000 cluster meet 127.0.0.1 7001
     redis-cli -p 7000 cluster meet 127.0.0.1 7002
     redis-cli -p 7000 cluster meet 127.0.0.1 7003
     redis-cli -p 7000 cluster meet 127.0.0.1 7004
     redis-cli -p 7000 cluster meet 127.0.0.1 7005
     ```

  4. 分配槽

     生成脚本:addslots.sh

     ```bash
     start=$1
     end=$2
     port=$3
     for slot in `seq ${start} ${end}`
     do
       echo "slot:${slot}"
       redis-cli -p ${port} cluster addslots ${slot}
     done
     ```

     执行

     ```bash
     sh addslots.sh 0 5461 7000
     sh addslots.sh 5462 10922 7001
     sh addslots.sh 10923 16383 7002
     ```

  5. 主从关系分配

     ```bash
     redis-cli -p 7003 cluster replicate 578cb9330ccdc1833f86646c749cfe46e08af563
     redis-cli -p 7004 cluster replicate 8d0b7c518e8a80199ee1e8b540ba591dbb629f7a
     redis-cli -p 7005 cluster replicate 8d0b7c518e8a80199ee1e8b540ba591dbb629f7a
     ```

- 官方工具安装

  1. 准备Ruby环境

     ```bash
     wget http://cache.ruby-lang.org/pub/ruby/2.3/ruby-2.3.1.tar.gz
     tar -xvf ruby-2.3.1.tar.gz
     cd ruby-2.3.1
     ./configure -prefix=/usr/local/ruby
     make
     make install
     ```

  2. 安装rubygem redis

     ```bash
     wget http://rubygems.org/downloads/redis-3.3.0.gem
     sudo gem install -l redis-3.3.0.gem
     ```

  3. 安装reids-trib.rb

     ```bash
     cp redis-3.0.0/src/redis-trib.rb /usr/local/bin/redis-trib 
     ```

  4. 启动

     ```ba
     redis-server redis-7000.conf
     redis-server redis-7001.conf
     redis-server redis-7002.conf
     ...
     
     redis-trib create --replicas 1 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005
     ```

     



   



