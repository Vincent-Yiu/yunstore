package com.demo_springboot.dao;
import java.util.List;

import com.demo_springboot.entity.User;
public interface UserMapper
{
    List<User> getAll();
}