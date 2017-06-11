package com.junlin.manager.jedis;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis工具类
 * Created by caicf on 2017/1/1.
 */
public class RedisUtils {

    //日志
    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);
    //属性
    private static Prop properties;
    //实例
    private static RedisUtils instance;
    //jedisPool
    private static JedisPool jedisPool;
    //lock
    private static ReentrantLock lock = new ReentrantLock();
    //对Keys,以及存储结构为String、List、Set、HashMap类型的操作
    public final Keys KEYS = new Keys();
    public final Strings STRINGS = new Strings();
    public final Lists LISTS = new Lists();
    public final Sets SETS = new Sets();
    public final Hash HASH = new Hash();
    public final SortSet SORTSET = new SortSet();

    private RedisUtils() {
    }

    /**
     * 获取实例
     *
     * @return
     */
    private static RedisUtils getInstance() {
        if (instance == null) {
            lock.lock();
            if (instance == null) {
                instance = new RedisUtils();
            }
            lock.unlock();
        }
        return instance;
    }


    /**
     * 初始化JedisPool
     */
    private static void initJedisPool() {
        properties = PropKit.use("redis.properties");
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxIdle(properties.getInt("redis.pool.maxIdle"));
        config.setMinIdle(properties.getInt("redis.pool.minIdle"));
        config.setTestOnBorrow(properties.getBoolean("redis.pool.testOnBorrow", false));
        config.setTestOnReturn(properties.getBoolean("redis.pool.testOnReturn", false));
        config.setMaxWaitMillis(properties.getInt("redis.pool.maxWait"));
        config.setMaxTotal(properties.getInt("redis.pool.maxActive"));
        //连接池
        jedisPool = new JedisPool(config,
                properties.get("redis.host"),
                properties.getInt("redis.port"),
                properties.getInt("redis.timeout"),
                properties.get("redis.password"));

        log.info("密码：" + properties.get("redis.password") +
                "\nhost：" + properties.get("redis.host") +
                "\nport：" + properties.get("redis.port"));

    }

    /**
     * 通用方法：从JedisPool中获取Jedis
     *
     * @return
     */
    public static Jedis getJedis() {
        if (jedisPool == null) {
            lock.lock();    //防止吃初始化时多线程竞争问题
            initJedisPool();
            lock.unlock();
            log.info("JedisPool init success！");
        }
        return jedisPool.getResource();
    }

    /**
     * 通用方法：释放Jedis
     *
     * @param jedis
     */
    private void closeJedis(Jedis jedis) {
        jedis.close();
    }

    public static void main(String[] args) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("1", 1);
        map.put("2", "sfasdfa");
        map.put("3", "sfawrwere3fa");

        RedisUtils instance = RedisUtils.getInstance();
        String werwe = instance.STRINGS.set("werwe", JSON.toJSONString(map));
        System.out.println(werwe);
        String s = instance.STRINGS.get("werwe");
        System.out.println(s);
    }
}

