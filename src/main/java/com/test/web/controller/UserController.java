package com.test.web.controller;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.test.core.result.ApiReturnObj;
import com.test.web.model.User;
import com.test.web.service.UserService;

/**
 * 用户请求控制器
 * 
 */
@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;// 由Spring容器注入一个UserService实例

	
    
	/**
	 * 登录
	 * 
	 * @param user
	 *            用户
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/login", method = {  RequestMethod.GET,RequestMethod.POST })
	@ResponseBody
	public ApiReturnObj<User> login(User user) throws IOException {
		ApiReturnObj<User> apiReturnObj=new ApiReturnObj<User>(2, "失败");
		User result = userService.login(user);// 调用UserService的登录方法
		if(result!=null){
			apiReturnObj=new ApiReturnObj<User>(1, "成功",result);
		}
		return apiReturnObj;
	}

	/**
	 * 注册（只允许注册普通用户）
	 * 
	 * @param user
	 *            用户
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/register", method = {  RequestMethod.GET,RequestMethod.POST })
	@ResponseBody
	public ApiReturnObj<Object>  register(User user) throws IOException {
		ApiReturnObj<Object> apiReturnObj=new ApiReturnObj<Object>(2, "失败");
		int lines = userService.register(user);
		if(lines>0){
			apiReturnObj=new ApiReturnObj<Object>(1, "注册成功");
		}
		return apiReturnObj;
	}
	
	
	@RequestMapping(value = "/getByUserName", method = {  RequestMethod.GET,RequestMethod.POST })
	@ResponseBody
	public ApiReturnObj<User> getByUserName(String userName) throws IOException {
		ApiReturnObj<User> apiReturnObj=new ApiReturnObj<User>(2, "失败");
		User user = userService.getByUserName(userName);// 调用UserService的登录方法
		if(user!=null){
			apiReturnObj=new ApiReturnObj<User>(1, "成功",user);
		}
		return apiReturnObj;
	}

	
}