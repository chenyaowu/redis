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

  - 多节点命令实现

  - 批量命令实现