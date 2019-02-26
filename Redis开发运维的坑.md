# Redis开发运维的坑

## Linux内核优化

- vm.overcommit_momory

  | 值   | 含义                                                         |
  | ---- | ------------------------------------------------------------ |
  | 0    | 表示内核将检查是否有足够的可用内容。如果有足够的可用内存，内存申请通过，否则内存申请失败，并把错误返回给应用程序 |
  | 1    | 表示内核允许超量使用内存直到用完为止                         |
  | 2    | 表示内核绝不过量的使用内存，即系统整个内存地址空间不能超过swap+50%的RAM值，50%是overcommit_ratio默认值，此参数同样支持修改 |

  ```bash
  #获取
  cat /proc/sys/vm/overcommit_memory
  0
  #设置
  echo "vm.overcommit_memory=1" >> /etc/sysctl.conf
  sysctl vm.overcommit_memory=1
  ```

  1. Redis设置合理的maxmemory，保证机器有20%~30%的闲置内存
  2. 集中化管理AOF重写和RDB的bgsave。
  3. 设置vm.overcommit_memory = 1,防止极端情况下会造成fork失败

- swappiness

  | 值   | 策略                                                         |
  | ---- | ------------------------------------------------------------ |
  | 0    | Linux3.5以及以上：宁愿用OOM killer也不用swap;Linux3.4以及更早：宁愿用swap也不用OOM killer |
  | 1    | Linux3.5以及以上：宁愿用swap也不用OOM killer                 |
  | 60   | 默认值                                                       |
  | 100  | 操作系统会主动地使用swap                                     |

  ```bash
  #立即生效
  echo {bestvalue} > /proc/sys/vm/swappiness
  #永久生效
  echo vm.swappiness = {bestvalue} >> /etc/sysctl.conf
  ```

  - 如果Linux>3.5，vm.swapniess = 1,否则vm.swapniess=0,从而实现：
    - 物理内存充足时，使Redis足够快
    - 物理内存不足时，避免Redis宕机(如果当前Redis为高可用，宕机比阻塞更好)

- THP(Transparent huge page)

  1. 作用：加速fork
  2. 建议：禁用，可能产生更大的内存开销
  3. 设置方法：echo never>/sys/kernel/mm/transparent_hugepage/enabled

- OOM killer

  1. 作用：内存使用超出，操作系统按照规则kill某些进程
  2. 配置方法：/proc/{progress_id}/oom_adj越小，被杀掉概率越小
  3. 运维经验：不要过度依赖次特性，应该合理管理内存

- NTP(Net Time Protocol)

  ![NTP](https://github.com/chenyaowu/redis/blob/master/image/NTP.jpg)

- ulimit

- TPC backlog

  Redis默认的tcp-backlog值为511，可以通过修改配置tcp-backlog进行调整，如果Linux的tcp-packlog小于Redis设置的tcp-backlog，那么Redis启动时会出现警告信息。

## 安全的Redis

1. 服务端配置：require和masterauth
2. 客户端连接：auth命令和-a参数
3. 伪装命令
4. bind
5. 防火墙
6. 定期备份
7. 不使用默认端口
8. 使用非root用户启动

## 热点key发现

- 客户端

- 服务端

  ![server](https://github.com/chenyaowu/redis/blob/master/image/server.jpg)

- 机器端

  ![machine](https://github.com/chenyaowu/redis/blob/master/image/machine.jpg)

- 代理