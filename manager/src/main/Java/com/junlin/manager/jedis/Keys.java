package com.junlin.manager.jedis;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.SortingParams;
import redis.clients.util.SafeEncoder;

import static com.junlin.manager.jedis.RedisUtils.getJedis;

/**
 * Created by junlinhui eight on 2017/6/8.
 * key
 */
public class Keys {

    //默认缓存时间
    private static final int EXPIRE = 60000;

    /**
     * 设置过期时间
     *
     * @param key
     * @param seconds
     * @return 返回影响的记录数
     */
    public long expire(String key, int seconds) {
        if (seconds <= 0) {
            return -1L;
        }
        Jedis jedis = getJedis();
        long result = jedis.expire(key, seconds);
        closeJedis(jedis);
        return result;
    }

    /**
     * 设置过期时间，默认值为60000seconds
     *
     * @param key
     */
    public long expire(String key) {
        return expire(key, EXPIRE);
    }

    /**
     * 设置key的过期时间,它是距历元（即格林威治标准时间 1970 年 1 月 1 日的 00:00:00，格里高利历）的偏移量。
     *
     * @param key
     * @param timestamp 秒
     * @return 影响的记录数
     */
    public long expireAt(String key, long timestamp) {
        Jedis jedis = getJedis();
        long count = jedis.expireAt(key, timestamp);
        closeJedis(jedis);
        return count;
    }

    /**
     * 查询key的过期时间
     *
     * @param key
     * @return 以秒为单位的时间表示
     */
    public long ttl(String key) {
        //ShardedJedis sjedis = getShardedJedis();
        Jedis sjedis = getJedis();
        long len = sjedis.ttl(key);
        closeJedis(sjedis);
        return len;
    }

    /**
     * 取消对key过期时间的设置
     *
     * @param key
     * @return 影响的记录数
     */
    public long persist(String key) {
        Jedis jedis = getJedis();
        long count = jedis.persist(key);
        closeJedis(jedis);
        return count;
    }

    /**
     * 清空所有key
     *
     * @return
     */
    public String flushAll() {
        Jedis jedis = getJedis();
        String stata = jedis.flushAll();
        closeJedis(jedis);
        return stata;
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return boolean
     */
    public boolean exists(String key) {
        Jedis sjedis = getJedis();
        boolean exis = sjedis.exists(key);
        closeJedis(sjedis);
        return exis;
    }

    /**
     * 更改key
     */
    public String rename(String oldKey, String newKey) {
        return rename(SafeEncoder.encode(oldKey),
                SafeEncoder.encode(newKey));
    }

    /**
     * 更改key,仅当新key不存在时才执行
     *
     * @param oldKey
     * @param newKey
     * @return 状态码
     */
    public long renamenx(String oldKey, String newKey) {
        Jedis jedis = getJedis();
        long status = jedis.renamenx(oldKey, newKey);
        closeJedis(jedis);
        return status;
    }

    /**
     * 更改key
     */
    public String rename(byte[] oldKey, byte[] newKey) {
        Jedis jedis = getJedis();
        String status = jedis.rename(oldKey, newKey);
        closeJedis(jedis);
        return status;
    }


    /**
     * 删除keys对应的记录,可以是多个key
     *
     * @param keys
     * @return 删除的记录数
     */
    public long del(String... keys) {
        Jedis jedis = getJedis();
        long count = jedis.del(keys);
        closeJedis(jedis);
        return count;
    }

    /**
     * 删除keys对应的记录,可以是多个key
     *
     * @param keys
     * @return 删除的记录数
     */
    public long del(byte[]... keys) {
        Jedis jedis = getJedis();
        long count = jedis.del(keys);
        closeJedis(jedis);
        return count;
    }


    /**
     * 对List,Set,SortSet进行排序,如果集合数据较大应避免使用这个方法
     *
     * @param key
     * @return List<String> 集合的全部记录
     **/
    public List<String> sort(String key) {
        Jedis sjedis = getJedis();
        List<String> list = sjedis.sort(key);
        closeJedis(sjedis);
        return list;
    }

    /**
     * 对List,Set,SortSet进行排序或limit
     *
     * @param key
     * @param parame 定义排序类型或limit的起止位置.
     * @return List<String> 全部或部分记录
     **/
    public List<String> sort(String key, SortingParams parame) {
        Jedis jedis = getJedis();
        List<String> list = jedis.sort(key, parame);
        closeJedis(jedis);
        return list;
    }

    /**
     * 返回指定key存储的类型
     *
     * @param key
     * @return String string|list|set|zset|hash
     **/
    public String type(String key) {
        Jedis sjedis = getJedis();
        String type = sjedis.type(key);
        closeJedis(sjedis);
        return type;
    }

    /**
     * 查找所有匹配给定的模式的键
     *
     * @param pattern 的表达式,*表示多个，？表示一个
     */
    public Set<String> keys(String pattern) {
        Jedis jedis = getJedis();
        Set<String> set = jedis.keys(pattern);
        closeJedis(jedis);
        return set;
    }

    /**
     * 通用方法：释放Jedis
     *
     * @param jedis
     */
    private void closeJedis(Jedis jedis) {
        jedis.close();
    }
}
