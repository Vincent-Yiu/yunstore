package com.demo_springboot.mapper;

import java.util.List;
import com.demo_springboot.entity.User;

public interface UserMapper
{
	List<User> getAll();
	User getOne(String username,String password);
}