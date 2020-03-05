package com.demo_springboot.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.demo_springboot.entity.Resource;
import com.demo_springboot.mapper.ResourceMapper;
import com.demo_springboot.service.UserService;

import javax.servlet.http.HttpSession;




@CrossOrigin(allowCredentials="true")

@RestController
public class UserController {
    static String test;
    @Autowired 
    private UserService userService;
    
    
    @RequestMapping("/login")
    public  String login(HttpSession session,@RequestParam("username")String username,@RequestParam("password")String password) throws Exception
    {
		System.out.println("username:"+username);
        
        return userService.login(session,username,password);
    }
    
    
    @Autowired
    ResourceMapper ResourceMapper;
    @RequestMapping("/test")
	@Transactional(propagation = Propagation.REQUIRED,readOnly = false,rollbackFor = {Exception.class} )
    
    public String getUsers()
    {
        Resource Resource=new Resource();
		Resource.setFilename("test");
		Resource.setSize("test");
		Resource.setUrl("test");
		Resource.setType("test");
		Resource.setOwner("test");
		ResourceMapper.insert(Resource);
        try
        {
            // testthrow(2);
            return "ok";
        }
        catch(Exception ex)
        {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "pok";
        }
        
        
    }
    public String testthrow(int i)
    {
        if(i==2)
            throw new RuntimeException();
        return "true" ;
    }
}                