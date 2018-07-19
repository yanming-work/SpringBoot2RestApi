package com.test.web.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.test.core.service.impl.RedisServiceImpl;
import com.test.core.util.FastJsonUtils;
import com.test.web.dao.UserDao;
import com.test.web.model.User;
import com.test.web.service.UserService;

@Component("userService")
public class UserServiceImpl extends RedisServiceImpl implements UserService 
{
	@Autowired
	private UserDao userDao;
	

    @Autowired
    StringRedisTemplate stringRedisTemplate;

	
	public User login(User user)
	{
		return userDao.findByIdAndPwd(user);
	}

	public int register(User user)
	{
		return userDao.register(user);
	}

	@Override
	public User getByUserName(String userName) {
		User user=userDao.findByUserName(userName);
		String key = "user_"+userName;
		redisSet(key, FastJsonUtils.objectToJsonStr(user));
        //获取缓存
		String userJson1=redisGet(key);
		System.out.println("缓存userJson:"+userJson1);
		
		return user;
	}

}
