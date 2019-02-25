# Redis内存优化

## 内存消耗

- 内存使用统计

  | 属性名                      | 属性说明                                                  |
  | --------------------------- | --------------------------------------------------------- |
  | **used_memory**             | Redis分配器分配的内存量，也就是实际存储数据结构的内存总量 |
  | used_momory_human           | 以可读格式返回Redis使用的内存总量                         |
  | **used_rss**                | 从操作系统的角度，Redis进程占用的总物理内存               |
  | used_momory_peak            | 内存分配器分配的最大内存，代表used_momory的历史峰值       |
  | used_memory_peak_human      | 以可读的格式显示内存消耗峰值                              |
  | used_memory_lua             | Lua引擎所消耗的内存                                       |
  | **mem_fragmentation_ratio** | used_memory_rss/used_memory比值，表示内存碎片率           |
  | mem_allocator               | Redis所使用的内存分配器。默认jemalloc                     |

  

- 内存消耗划分

  ![内存划分](https://github.com/chenyaowu/redis/blob/master/image/RedisMemory1.jpg)

- 子进程内存消耗

  ![内存消耗](https://github.com/chenyaowu/redis/blob/master/image/RedisMemory2.jpg)

## 缓冲区

- 输入缓冲区

  ![输入缓冲区](https://github.com/chenyaowu/redis/blob/master/image/RedisMemory3.jpg)

- 输出缓冲区

  ![输出缓冲区配置](https://github.com/chenyaowu/redis/blob/master/image/RedisMemory4.jpg)

  - 普通客户端缓冲区
    1. 默认：client-output-buffer-limit normal 0 0 0
    2. 默认： 没有限制客户端缓冲
    3. 注意： 防止大的命令或者monitor
  - slave客户端
    1. 默认：client-output-buffer-limit slave 256mb 64mb 60
    2. 阻塞：主从延迟较高，或者从节点过多
    3. 注意：主从网络，从节点不要超过2个
  - pubsub客户端
    1. 默认： client-output-buffer-limit pubsub 32mb 8mb 60
    2. 阻塞：生产大于消费
    3. 注意：根据实际场景调试

- 复制缓冲区

  ![输出缓冲区配置](https://github.com/chenyaowu/redis/blob/master/image/RedisMemory5.jpg)

- AOF缓冲区

  ![AOF缓冲区](https://github.com/chenyaowu/redis/blob/master/image/RedisMemory6.jpg)

## 对象内存

1. key:不要过长，量大不容忽视(redis3:embstr 39字节)
2. value:ziplist、intsete等优化方式

## 内存碎片

1. 必然存在：jemalloc
2. 优化方式
   - 避免频繁更新操作：append、setrange等
   - 安全重启，例如redis sentinel和redis cluster等

## 子进程内存消耗

1. 必然存在：fork(bgsave和bgrewriteaof)
2. 优化方式：去掉THP:2.6.38增加的特性

## 内存管理

- 设置内存上限

  ![AOF缓冲区](https://github.com/chenyaowu/redis/blob/master/image/RedisMemory7.jpg)

- 动态调整内存上限

  ```bash
  redis>config set maxmemory 6GB
  redis>config set maxmemory 2GB
  redis>config rewrite
  ```

- 内存回收策略

  - 删除过期键值
    1. 惰性删除：访问key->expired dict ->del key
    2. 定时删除：每秒运行10次，采样删除。
  - 内存溢出控制策略
    - 超过maxmemory后触发相应策略，由maxmemory-policy控制
    - Noeviction：默认策略，不会删除任何数据，拒绝所有写入操作并返回端错误信息"(error)OOM command not allowed when used memory"此时Redis只响应读操作由maxmemory-policy控制
    - Volatile-lru：根据LRU算法删除设置了超时属性(expire)的键，直到腾出足够空间为止。如果没有可删除的键对象，回退到noeviction策略
    - Allkeys-lru：根据LRU算法删除键，不管数据有没有设置超时属性，直到腾出空间为止
    - Allkeys-random：随机删除所有的键，直到腾出空间为止
    - volatile-random：岁间删除过期键，直到腾出空间为止
    - volatile-ttl：根据键值对象的ttl属性，删除最近将要过期数据。如果没有，回退到noeviction策略。

## 内存优化

- 内存分布

  ![内存消耗](https://github.com/chenyaowu/redis/blob/master/image/RedisMemory8.jpg)

- 合理选择优化数据结构

  | 方案     | 优点     | 缺点                       |
  | -------- | -------- | -------------------------- |
  | 全string | 编程简单 | 浪费内存，全量获取较为复杂 |
  | 全hash   | 暂无     | 浪费内存，bigkey           |
  | 分段hash | 节省内存 | 编程复杂，超时问题         |

- 客户端缓冲区优化

- 其他方法

- 需不需要用Redis