package com.demo_springboot.entity;

import java.util.List;

public class Result
{
    private String msg;
    private String code;
    private List<String> data;
    public void setMsg(String result){this.msg=result;}
    public String getMsg(){return this.msg;}
    public void setCode(String code){this.code=code;}
    public String getCode(){return this.code;}
    public void setData(List<String> data){this.data=data;}
    public List<String> getData(){return data;}

}