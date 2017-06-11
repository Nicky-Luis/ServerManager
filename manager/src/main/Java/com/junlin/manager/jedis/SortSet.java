package com.junlin.manager.jedis;

import java.util.Set;

import redis.clients.jedis.Jedis;

import static com.junlin.manager.jedis.RedisUtils.getJedis;

/**
 * Created by junlinhui eight on 2017/6/8.
 *
 */
public class SortSet {

    /**
     * 向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重
     *
     * @param key
     * @param score  权重
     * @param member 要加入的值，
     * @return 状态码 1成功，0已存在member的值
     */
    public long zadd(String key, double score, String member) {
        Jedis jedis = getJedis();
        long s = jedis.zadd(key, score, member);
        closeJedis(jedis);
        return s;
    }

    /**
     * 获取集合中元素的数量
     *
     * @param key
     * @return 如果返回0则集合不存在
     */
    public long zcard(String key) {
        Jedis sjedis = getJedis();
        long len = sjedis.zcard(key);
        closeJedis(sjedis);
        return len;
    }

    /**
     * 获取指定权重区间内集合的数量
     *
     * @param key
     * @param min 最小排序位置
     * @param max 最大排序位置
     */
    public long zcount(String key, double min, double max) {
        Jedis sjedis = getJedis();
        long len = sjedis.zcount(key, min, max);
        closeJedis(sjedis);
        return len;
    }

    /**
     * 获得set的长度
     *
     * @param key
     * @return
     */
    public long zlength(String key) {
        long len = 0;
        Set<String> set = zrange(key, 0, -1);
        len = set.size();
        return len;
    }

    /**
     * 权重增加给定值，如果给定的member已存在
     *
     * @param key
     * @param score  要增的权重
     * @param member 要插入的值
     * @return 增后的权重
     */
    public double zincrby(String key, double score, String member) {
        Jedis jedis = getJedis();
        double s = jedis.zincrby(key, score, member);
        closeJedis(jedis);
        return s;
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     *
     * @param key
     * @param start 开始位置(包含)
     * @param end   结束位置(包含)
     * @return Set<String>
     */
    public Set<String> zrange(String key, int start, int end) {
        //ShardedJedis sjedis = getShardedJedis();
        Jedis sjedis = getJedis();
        Set<String> set = sjedis.zrange(key, start, end);
        closeJedis(sjedis);
        return set;
    }

    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     * @param min 上限权重
     * @param max 下限权重
     * @return Set<String>
     */
    public Set<String> zrangeByScore(String key, double min, double max) {
        //ShardedJedis sjedis = getShardedJedis();
        Jedis sjedis = getJedis();
        Set<String> set = sjedis.zrangeByScore(key, min, max);
        closeJedis(sjedis);
        return set;
    }

    /**
     * 获取指定值在集合中的位置，集合排序从低到高
     *
     * @param key
     * @param member
     * @return long 位置
     */
    public long zrank(String key, String member) {
        //ShardedJedis sjedis = getShardedJedis();
        Jedis sjedis = getJedis();
        long index = sjedis.zrank(key, member);
        closeJedis(sjedis);
        return index;
    }

    /**
     * 获取指定值在集合中的位置，集合排序从高到低
     *
     * @param key
     * @param member
     * @return long 位置
     */
    public long zrevrank(String key, String member) {
        //ShardedJedis sjedis = getShardedJedis();
        Jedis sjedis = getJedis();
        long index = sjedis.zrevrank(key, member);
        closeJedis(sjedis);
        return index;
    }

    /**
     * 从集合中删除成员
     *
     * @param key
     * @param member
     * @return 返回1成功
     */
    public long zrem(String key, String member) {
        Jedis jedis = getJedis();
        long s = jedis.zrem(key, member);
        closeJedis(jedis);
        return s;
    }

    /**
     * 删除
     *
     * @param key
     * @return
     */
    public long zrem(String key) {
        Jedis jedis = getJedis();
        long s = jedis.del(key);
        closeJedis(jedis);
        return s;
    }

    /**
     * 删除给定位置区间的元素
     *
     * @param key
     * @param start 开始区间，从0开始(包含)
     * @param end   结束区间,-1为最后一个元素(包含)
     * @return 删除的数量
     */
    public long zremrangeByRank(String key, int start, int end) {
        Jedis jedis = getJedis();
        long s = jedis.zremrangeByRank(key, start, end);
        closeJedis(jedis);
        return s;
    }

    /**
     * 删除给定权重区间的元素
     *
     * @param key
     * @param min 下限权重(包含)
     * @param max 上限权重(包含)
     * @return 删除的数量
     */
    public long zremrangeByScore(String key, double min, double max) {
        Jedis jedis = getJedis();
        long s = jedis.zremrangeByScore(key, min, max);
        closeJedis(jedis);
        return s;
    }

    /**
     * 获取给定区间的元素，原始按照权重由高到低排序
     *
     * @param key
     * @param start
     * @param end
     * @return Set<String>
     */
    public Set<String> zrevrange(String key, int start, int end) {
        //ShardedJedis sjedis = getShardedJedis();
        Jedis sjedis = getJedis();
        Set<String> set = sjedis.zrevrange(key, start, end);
        closeJedis(sjedis);
        return set;
    }

    /**
     * 获取给定值在集合中的权重
     *
     * @param key
     * @param memebr
     * @return double 权重
     */
    public double zscore(String key, String memebr) {
        //ShardedJedis sjedis = getShardedJedis();
        Jedis sjedis = getJedis();
        Double score = sjedis.zscore(key, memebr);
        closeJedis(sjedis);
        if (score != null)
            return score;
        return 0;
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
