package com.chen.redis.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JedisClusterFactory {

    private JedisCluster jedisCluster;

    private List<String> hostPortList;

    private Logger logger = LoggerFactory.getLogger(JedisClusterFactory.class);

    /**
     * 单位毫秒
     */
    private int timeout;

    public void init() {
        //设置参数
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        Set<HostAndPort> nodeSet = new HashSet<HostAndPort>();
        for (String hostPort : hostPortList) {
            String arr[] = hostPort.split(":");
            if (arr.length != 2) {
                continue;
            }
            nodeSet.add(new HostAndPort(arr[0], Integer.parseInt(arr[1])));
        }

        try {
            jedisCluster = new JedisCluster(nodeSet, timeout, jedisPoolConfig);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    public void destory(){
        if(jedisCluster != null){
            try {
                jedisCluster.close();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setHostPortList(List<String> hostPortList) {
        this.hostPortList = hostPortList;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
