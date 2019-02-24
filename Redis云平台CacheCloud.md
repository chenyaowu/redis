# Redis云平台CacheCloud

## Redis规模化运维

- 遇到问题
  - 发布构建繁琐，私搭乱盖
  - 节点和机器等运维成本
  - 监控报警初级
- [CacheCloud](https://github.com/sohutv/cachecloud)
  1. 一键开启Redis。(Standalone、Sentinel、Cluster)
  2. 机器、应用、实例监控和报警
  3. 客户端：透明使用、性能上报
  4. 可视化运维：配置、扩容、Failover、机器/应用/实例上下线
  5. 已存在Redis直接接入和数据迁移
- 使用规模
  - 300亿+ commands/day
  - 3TB Memory Total
  - 1300+ Instances Total
  - 200+ Machines Total
- 使用场景
  - 全量视频缓存(视频播放API)：跨机房高可用
  - 消息队列同步(RedisMQ中间件)
  - 分布式布隆过滤器(百万QPS)
  - 计数统计：计数(播放量)
  - 其他：排行榜、社交（直播）

