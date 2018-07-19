package com.test.web.dao;

import com.test.web.model.User;

public interface UserDao
{
	User findByUserName(String userName);
	User findByIdAndPwd(User user);
	int register(User user);
}
