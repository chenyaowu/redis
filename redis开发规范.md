# redis开发规范

## key设计

- 可读性和可管理型：以业务名（或数据库名）为前缀（防止key冲突），用冒号分隔，比如业务名:表名:id

- 简洁性：保证语义的前提下，控制key的长度，当key较多时，内存占用也不容忽视(redis3:39字节embstr)，如：user:{uid}:friends:messages:{mid}简化为u:{uid}:fr​:m/:​{mid}​

- 不要包含特殊字符，空格，换行，单双引号以及其他转义字符

  ![Redis3 embstr测试](https://github.com/chenyaowu/redis/blob/master/image/RedisKey.jpg)

## value设计

- 拒绝bigkey
  - 强制：String类型控制在10KB以内、hash、list、set、zset元素个数不要超过5000
- 危害
  - 网络拥塞
  - Redis阻塞
  - 集群节点数据不均匀
  - 频繁序列化：应用服务器CPU消耗

- 发现bigkey的方法
  - 应用异常
  - redis-cli --bigkeys
  - scan + debug object
  - 主动报警：网络流量监控、客户端监控
  - 内核热点key问题优化

- bigkey的删除
  - 阻塞：注意隐形删除(过期、rename等)
  - redis 4.0 : lazy delete(unlink命令)
- bigkey预防
  - 优化数据结构：例如二级拆分
  - 物理隔离或者千兆网卡：不是治标之本
  - 命令优化：例如hgetall->hmget、hscan
  - 报警和定期优化

- 选择合理的数据结构

- 键值生命周期
  - 周期数据需要设置过期时间，object idle time可以找垃圾key-value
  - 过期时间不宜集中：缓存穿透和雪崩等问题

## 命令优化技巧

- O(n)以上命令关注N的数量
- 禁用命令：keys、flushall、flushdb等
- 合理使用select
  - redis的多数据库较弱，使用数字进行区分。
  - 很多客户端支持查
  - 同时多业务用多数据库实际还是单线程处理，会有干扰
- redis事务功能较弱，不建议过多使用
  - redis的事务功能较弱（不支持回滚）
  - 而且集群版本（自研和官方）要求一次事务操作的key必须在一个slot上(可以使用hashtag功能解决)
  - redis集群版本在使用Lua上有特殊要求
  - 必要情况下使用monitor命令时，要注意不要长时间使用

## java客户端优化

- 避免多个应用使用一个Redis实例
- 正例：不相干业务拆分，公共数据做服务化
- 使用连接池

## 连接池参数优化

| 参数名                       | 含义                                                         | 默认值                | 使用建议                                                     |
| ---------------------------- | :----------------------------------------------------------- | --------------------- | ------------------------------------------------------------ |
| maxTotal                     | 连接池最大连接数                                             | 8                     | 见下方                                                       |
| maxIdle                      | 资源池允许最大空闲的连接数                                   | 8                     | 见下方                                                       |
| minIdle                      | 资源池确保最少空闲的连接数                                   | 0                     | 见下方                                                       |
| blockWhenExhausted           | 当资源池用尽后，调用者是否要等待。只有当为true时，maxWaitMillis才会生效 | true                  | true                                                         |
| maxWaitMillis                | 当资源池连接用尽后，调用者的最大等待时间(单位毫秒)           | -1(表示永不超时)      | 不建议使用默认值                                             |
| testOnBorrow                 | 向资源池借用连接时，是否做连接有效性检测(ping)，无效连接会被移除 | false                 | 当业务量很大时建议设置为false(多一次ping开销)                |
| testOnReturn                 | 向资源池归还连接时，是否做连接有效性测试(ping)，无效连接会被移除 | false                 | 当业务量很大时建议设置为false(多一次ping开销)                |
| jmxEnabled                   | 是否开启jmx监控，可用于监控                                  | true                  | 建议开启，但应用本身也要开启                                 |
| testWhileIdle                | 是否开启空闲资源监测                                         | false                 | true                                                         |
| timeBetweenEvictionRunMillis | 空闲资源的监测周期(单位为毫秒)                               | -1(不监测)            | 周期自行选择                                                 |
| minEvicatableIdleTimeMillis  | 资源池中资源最小空闲时间(单位为毫秒)，达到此值后空闲资源将被移除 | 1000 *60 *30 = 30分钟 | 可根据自身业务决定，大部分默认值即可                         |
| numTestsPerEvictionRun       | 做空闲资源监测时，每次的采样数                               | 3                     | 可根据自身应用连接数进行微调，如果设置为-1，就是对所有连接做空闲监测 |

maxIdle接近maxTotal即可

- maxTotal考虑因素
  - 业务希望Redis并发量
  - 客户端执行时间
  - redis资源：例如node(应该个数)*maxTotal不能超过Redis最大连接数
  - 资源开销：例如虽然希望质控空闲连接数，但是不希望因为连接池的频繁释放创建连接造成不必要的开销

- 例子：

  一次平均时间(borrow|return resource + Jedis执行命令(含网络))的平均耗时约为1ms，一个连接的QPS大约是1000。业务期望的QPS是50000。理论的maxTotal = 50000 / 1000 = 50个，可适当伸缩