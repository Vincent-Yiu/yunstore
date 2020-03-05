package com.demo_springboot.dao;

import java.util.List;

public interface IGenericDao<T> {


    /**
     *
     * @return  返回对象集合
     */
    public List<T> list();

    /**
     * 根据sql 返回数据
     * @param sql
     * @return
     */
    public List<T> list(String sql);

    /**
     * 根据主键id 返回一条数据
     * @param id
     * @return T or null
     */
    public T find(int id);

    /**
     * 插入实体对象
     * @param o 对象实体
     * @return 插入成功
     */
    public boolean insert(T o);

    /**
     * 根据主键id 更新实体对象
     * @param o 实体
     * @return true
     */
    public boolean update(T o);

    /**
     * 根据主键id，删除数据
     * @param id
     * @return
     */
    public boolean delete(int id);

    /**
     * 通过命令行调用sql
     * @param sql
     * @return 按行返回每一行数据，第一行为列名,每一行数据由空格隔开
     */
//    public static List<String> connectByCmd(String sql);


}
