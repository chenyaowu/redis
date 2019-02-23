# redis集群伸缩



## 集群伸缩原理

![](https://github.com/chenyaowu/redis/blob/master/image/RedisClusterPrincipleOfTelescopic.jpg)

集群伸缩 = 槽和数据在节点之间的移动

## 扩容集群

1. 准备节点

   - 集群模式

   - 配置和其他节点统一

   - 启动后是孤儿节点

   ![启动后效果图](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster1.jpg)

2. 加入集群

   - reids-cli方法

   ```bash
   127.0.0.1:6379>cluster meet 127.0.0.1 6385
   
   127.0.0.1:6379>cluster meet 127.0.0.1 6386
   ```

   ![加入后效果图](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster2.jpg)

   

   - redis-trib.rb方法

   ```bash
   redis-trib add-node new_host:new_port existing_host:existing_port --slave --master-id <arg>
   redis-trib add-node 127.0.0.1:6385 127.0.0.1:6379
   ```

   建议使用redis-trib.rb能够避免新节点已经加入了其他集群，造成故障。

   加入集群作用

   1. 为它迁移槽和数据实现扩容
   2. 作为从节点负责故障转移

3. 迁移槽和数据

   1. 槽迁移计划

      ![槽迁移计划](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster3.jpg)

   2. 迁移数据

      1. 对目标节点发送：cluster setloat {slot} importing {sourceNodeId}命令，让目标节点准备导入槽的数据。

      2. 对源节点发送：cluster setslot {slot} migrating {targetNodeId} 命令，让源节点准备迁出槽的数据。
      3. 源节点循环执行cluster getkeyinslost {slot} {count}命令,每次获取count个属于槽的键。
      4. 在源节点上执行migrate {targetIp} {targetPort} key 0 {timeout} 命令把指定key迁移。

      5. 重复执行步骤3~4知道槽下所有的键数据迁移到目标节点。

      6. 向集群内所有主节点发送cluster setloat {slot} node {targetNodeId}命令，通知槽分配给目标节点。

         ![迁移数据流程图](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster4.jpg)

   3. 添加从节点

      

## 收缩集群

![迁移数据流程图](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster5.jpg)

1. 下线迁移槽

   ![下线迁移槽](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster6.jpg)

2. 忘记节点

   ```ba
   redis-cli>cluster forget {downNodeId}
   ```

   ![忘记节点](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster7.jpg)

3. 关闭节点

   

## 客户端路由

### moved重定向

![move重定向](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster8.jpg)

![槽命中](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster9.jpg)

![槽不命中-move异常](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster10.jpg)

### ask重定向

![ask重定向](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster11.jpg)

![ask异常](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster12.jpg)

#### moved和ask

- 两者都是客户端重定向
- moved: 槽已经确定迁移
- ask:槽还在迁移中

### smart客户端

- smart客户端实现原理(追求性能)

  1. 从集群中选一个可运行节点，使用cluster slots初始化槽和节点映射.

  2. 将cluster slots的结果映射到本地，为每个节点创建JedisPool。

  3. 准备执行命令

     ![执行命令](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster13.jpg)

- smart客户端：JedisCluster

  - 基本使用

    ```java
    Set<HostAndPort> nodeList = new HashSet<HostAndPort>();
    nodeList.add(new HostAndPort(HOST1, PORT1));
    nodeList.add(new HostAndPort(HOST2, PORT2));
    nodeList.add(new HostAndPort(HOST3, PORT3));
    nodeList.add(new HostAndPort(HOST4, PORT4));
    nodeList.add(new HostAndPort(HOST5, PORT5));
    nodeList.add(new HostAndPort(HOST6, PORT6));
    JedisCluster redisCluster = new JedisCluster(nodeList, timeout, poolConfig);
    redisCluster.command...
    ```

    - 使用技巧
      1. 单例：内置了所有节点的连接池
      2. 无需手动借还连接池
      3. 合理设置commons-pool

  - 整合spring

    [demo](https://github.com/chenyaowu/redis/tree/master/project/JedisCluster)

  - 多节点命令实现

    ```java
         //获取所有节点的jedisPool
            Map<String,JedisPool> jedisPoolMap = jedisCluster.getClusterNodes();
            for (Map.Entry<String,JedisPool> entry: jedisPoolMap.entrySet()){
                //获取每个节点的Jedis连接
                Jedis jedis = entry.getValue().getResource();
                //只删除主节点数据
                String info = jedis.info("replication");
                String infoArr[] = info.split("\n");
                if(!"master".equals(infoArr[1].split(":")[1])){
                    continue;
                }
    
                //finally close
            }
        }
    
    ```

    

  - 批量命令实现

    - 串行mget

      ![串行mget](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster14.jpg)

    - 串行IO

      ![串行IO](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster15.jpg)

    - 并行IO

      ![并行IO](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster16.jpg)

    - hash_tag

      ![hash_tag](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster17.jpg)

    | 方案     | 优点                               | 缺点                                      | 网络IO            |
    | -------- | ---------------------------------- | ----------------------------------------- | ----------------- |
    | 串行mget | 编程简单，少量key满足需求          | 大量key请求延长严重                       | O(keys)           |
    | 串行IO   | 编程简单，少量节点满足需求         | 大量node延迟严重                          | O(nodes)          |
    | 并行IO   | 利用并行特性，延迟取决于最慢的节点 | 编程复杂，超时定位问题难                  | O(max_slow(node)) |
    | hash_tag | 性能最高                           | 读写增加tag维护成本,tag分布易出现数据倾斜 | O(1)              |

    

## 故障转移

### 故障发现

- 通过ping/pong消息实现故障发现：不需要sentinel
- 主观下线和客观下线

### 故障恢复

- 资格检查

  - 每个从节点检查与故障节点的断线时间
  - 超过cluster-node-timeout * cluster-slave-valudity-factor取消资格
  - cluster-slave-validity-factor:默认是10

- 准备选举时间

  ![准备选举时间](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster18.jpg)

- 选举投票

  ![选举投票](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster19.jpg)

- 替换主节点

  1. 当前从节点取消复制变为主节点。(sloveof no one)

  2. 执行clusterDelSlot撤销故障主节点负责的槽，并执行clusterAddSlot把这些槽分配给自己

  3. 向集群广播自己的pong消息，表明已经替换了故障从节点

     

## Redis Cluster开发运维常见问题

### 集群完整性

- cluster-require-full-coverage默认为yes
  - 集群中16384个槽全部可用：保证集群完整性
  - 节点故障或者正在故障迁移：(error)CLUSTERDOWN The cluster is down
- 大多数业务无法容忍，cluster-require-full-coverage建议设为no

### 带宽消耗

- 官方建议：1000个节点
- PING/PONG消息
- 不容忽视的带宽消耗
  - 消息发送频率：节点发现与其他节点最后通信时间超过cluster-node-timeout/2时会直接发送ping消息
  - 消息数据量：slots槽数组(2KB空间)和整个集群1/10的状态数据(10个节点状态数据约1KB)
  - 节点部署的机器规模：集群分布的机器数量越多且每台机器划分的节点数量约均匀，则集群内整体的可用带宽越高
- 优化
  - 避免“大”集群：避免多业务使用一个集群，大业务可以多集群
  - cluster-node-timeout:带宽和故障转移速度的均衡
  - 尽量均匀分配到多机器上：保证高可用和带宽

### Pub/Sub广播

​	![Pub/Sub广播](https://github.com/chenyaowu/redis/blob/master/image/RedisCluster20.jpg)

 - 问题：publish在集群每个节点广播：加重带宽
 - 解决：单独"走"一套Redis Sentinel

### 数据倾斜

- 数据倾斜：内存不均
  - 节点和槽分配不均
    - redis-trib info ip:port查看节点、槽、键值分布
    - redis-trib rebalance ip:port进行均衡（谨慎使用）
  - 不同槽对应键值数量差异较大
    - CRC16正常情况下比较均匀
    - 可以存在hash_tag
    - cluster countkeysinslot {slot}获取槽对应键值个数
  - 包含bigkey
    - bigkey: 例如大字符串、几百万的元素的hash、set
    - 从节点：redis-cli --bigkeys
    - 优化：优化数据结构
  - 内存相关配置不一致
    - hash-max-ziplist-value、set-max-intset-entries等
    - 优化：定期“检查”配置一致性
- 请求倾斜：热点
  - 热定key:重要的key或者bigkey
  - 优化：
    - 避免bigkey
    - 热键不要用hash_tag
    - 当一致性不高时，可以用本地缓存 + MQ

### 读写分离

- 只读连接：集群模式的从节点不接受任何读写请求

  - 重定向到负责槽的主节点

  - readonly命令可以读：连接级别命令

- 读写分离：更加复杂
  - 同样的问题：复制延迟、读取过期数据、从节点故障
  - 修改客户端：cluster slaves {nodeId}

### 数据迁移

- 在线/离线迁移：官方迁移工具:redis-trib import
  - 只能从单机迁移到集群
  - 不支持在线迁移：source需要停写
  - 不支持断点续传
  - 单线程迁移：影响速度

- 在线迁移：
  - 唯品会redis-migrate-tool
  - 豌豆荚：redis-port

### 集群VS单机

- 集群限制
  - key批量操作支持有限：例如mget、mset必须在一个slot
  - key事务和Lua支持有限：操作的key必须在一个节点
  - key是数据分区的最小粒度：不支持bigkey分区
  - 不支持多个数据库：集群模式下只有一个db0
  - 复制只支持一层：不支持树型复制结构

- 思考-分布式Redis不一定好
  - Redis Cluster:满足容量和性能的拓展性，很多业务“不需要”。
    - 大多数客户端性能会“降低”
    - 命令无法跨节点使用：mget、keys、scan、flush、sinter等
    - Lua和事务无法跨节点使用
    - 客户端维护更复杂：SDK和应用本身消耗（例如更多的连接池）
  - 很多场景Redis Sentinel已经足够好