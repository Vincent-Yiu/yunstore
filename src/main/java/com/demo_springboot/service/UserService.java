package com.demo_springboot.service;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;  

import com.demo_springboot.entity.Result;
import com.demo_springboot.mapper.UserMapper;
import com.demo_springboot.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	
	@Autowired 
	private UserMapper userMapper;

	public  String login(HttpSession session, String username,String password)throws IOException
	{
		// User user=userMapper.getOne(username,password);
		Result result=new Result();
		// if(user==null)
		// {
		// 	result.setCode("200");
		// 	result.setMsg("error");
		// 	ObjectMapper mapper = new ObjectMapper();  
		// 	String json = mapper.writeValueAsString(result);  
		// 	return json;
		// }
		session.setAttribute("user",username);
		result.setCode("200");
		result.setMsg("success");
		ObjectMapper mapper = new ObjectMapper();  
		String json = mapper.writeValueAsString(result);  
		return json;
	}
	
	
}
