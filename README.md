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

- Redis单线程(为什么那么快)
  1. **纯内存**
  2. **非阻塞IO**
  3. **避免线程切换和竞争消耗**
  4. **注意**:一次只运行一条命令；拒绝长（慢）命令(keys、flushall、flushdb、slow lua script、mutil/exec、operate big value(collection))；其实不是单线程（fysnc file descriptor 、close file descritor）

## Redis API

- 数据结构和内部编码

  ![数据结构和内部编码](https://github.com/chenyaowu/redis/blob/master/image/dataStructure.jpg)

- 通用命令

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

- 字符串(使用场景：缓存、计数器、分布式锁)

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

- Hash

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

- List

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

- Set

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

- ZSet

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

  

