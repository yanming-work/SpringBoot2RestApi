package com.test.web.service;

import com.test.web.model.User;

public interface UserService {
	/**
	 * 登录
	 * @param user 用户名和密码的简单封装
	 * @return 登录成功返回完整信息
	 */
	public User login(User user);

	/**
	 * 注册
	 * @param user 
	 * @return
	 */
	public int register(User user);
	
	public User  getByUserName(String userName);
}