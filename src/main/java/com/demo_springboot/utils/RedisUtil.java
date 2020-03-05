package com.demo_springboot.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;

@Component
public final class RedisUtil
{
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    public boolean expire(String key,long time)
    {
        try
        {
            if(time>0)
                redisTemplate.expire(key,time,TimeUnit.SECONDS);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public long getExpire(String key)
    {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
    public boolean hasKey(String key)
    {
        return redisTemplate.hasKey(key);
    }

    @SuppressWarnings("unchecked")
    public void del(String... key)
    {
        if(key!=null&&key.length>0)
            if(key.length==1)
                redisTemplate.delete(key[0]);
            else
                redisTemplate.delete(CollectionUtils.arrayToList(key));
    }
    
    //String
    public Object get(String key)
    {
        return key==null?null:redisTemplate.opsForValue().get(key);
    }
    public boolean set(String key,Object value)
    {
        try
        {
            redisTemplate.opsForValue().set(key,value);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();;
            return false;
        }
    }


    //map
    public Object hget(String key,String item)
    {
        return redisTemplate.opsForHash().get(key,item);
    }
    public Map<Object,Object> hmget(String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }
    public boolean hmset(String key,Map<String,Object>map)
    {
        try
        {
            redisTemplate.opsForHash().putAll(key,map);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public boolean hset(String key,String item,Object value)
    {
        try
        {
            redisTemplate.opsForHash().put(key,item,value);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public boolean hExists(String key, String field) {
		return redisTemplate.opsForHash().hasKey(key, field);
    }
    
    //list
    public List<Object> lGet(String key,long start,long end)
    {
        try
        {
            return redisTemplate.opsForList().range(key,start,end);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean lSet(String key,Object value)
    {
        try
        {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public boolean lSet(String key,List<Object> value)
    {
        try
        {
            redisTemplate.opsForList().rightPushAll(key,value);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}