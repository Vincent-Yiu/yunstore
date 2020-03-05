package com.demo_springboot.entity;

public class User
{
    private String username;
    private String password;
    private String mail;
    private String create_time;
    public String getUsername(){return username;}
    public void setUsername(String name){this.username=name;}
    public String getPassword(){return password;}
    public void setPassword(String password){this.password=password;}
    public String getMail(){return mail;}
    public void setMail(String mail){this.mail=mail;}
    public String getCreateTime(){return create_time;}
    @Override
    public String toString()
    {
        return "username:"+username+
                ",password:"+password+
                ",mail:"+mail+
                ",create_time:"+create_time;
    }
}