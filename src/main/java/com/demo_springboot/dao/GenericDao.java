package com.demo_springboot.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
public class GenericDao<T> implements IGenericDao<T> {

    public static String DBusername = "root";
    public static String DBpassword = "letmein";
        // public static String DBaddress = "127.0.0.1";
//    public static String DBaddress = "192.168.31.66";
   public static String DBaddress = "192.168.1.103";
    public static String DBport = "3307";
    public static String DBname = "yunstore";
    public static String cmd = "mysql -u" + DBusername + " -p" + DBpassword + " -h" + DBaddress + " -P" + DBport + " " + DBname;

    public GenericDao(String clazzName) {
        this.clazzName = clazzName;
    }


    @Override
    public List<T> list() {
        String tableName = this.clazzName.toLowerCase().substring(this.clazzName.toLowerCase().lastIndexOf(".") + 1);
        String sql = "select * from " + tableName + ";";
        return list(sql);
    }

    @Override
    public List<T> list(String sql) {
        List<T> results = new ArrayList<>();
        List<String> res = connectByCmd(sql);
        if (res.size() == 0)
            return results;
        String[] columnName = res.get(0).split("\\s+");
        Class<T> clazz = null;
        try {
            clazz = (Class<T>) Class.forName(clazzName);

            for (int i = 1; i < res.size(); i++) {
                T s = (T) clazz.newInstance();
                String[] values = res.get(i).split("\\s+");
                for (int j = 0; j < columnName.length; j++) {
                    Field field = clazz.getDeclaredField(columnName[j]);
                    field.setAccessible(true);
                    String type = field.getType().getName();
                    String methodName = "set" + captureName(columnName[j]);

                    Method m;
                    Object value = null;

                    if (type.equals("int")) {
                        m = clazz.getMethod(methodName, int.class);
                        value = Integer.parseInt(values[j]);
                    } else if (type.equals("long")) {
                        m = clazz.getMethod(methodName, long.class);
                        value = Long.parseLong(values[j]);
                    } else if (type.equals("double")) {
                        m = clazz.getMethod(methodName, double.class);
                        value = Double.parseDouble(values[j]);
                    } else if (type.equals("float")) {
                        m = clazz.getMethod(methodName, float.class);
                        value = Float.parseFloat(values[j]);
                    } else if (!type.equals("java.lang.String")) {
                        m = clazz.getMethod(methodName, Class.forName(type));
                        value = field.getType().getMethod("valueOf", String.class).invoke(null, values[j]);
                    } else {
                        m = clazz.getMethod(methodName, Class.forName(type));
                        value = values[j];
                    }
                    m.invoke(s, value);
                }
                results.add(s);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException
                | NoSuchMethodException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return results;
    }

    @Override
    public T find(int id) {
        String tableName = this.clazzName.toLowerCase().substring(this.clazzName.toLowerCase().lastIndexOf(".") + 1);
        String sql = "select * from " + tableName + " where id = " + id + ";";
        List<T> res = list(sql);
        if (res.size() > 0)
            return list(sql).get(0);
        return null;
    }
    @Override
    public boolean insert(T o) {
        String tableName = this.clazzName.toLowerCase().substring(this.clazzName.toLowerCase().lastIndexOf(".") + 1);

        String sql = "insert into " + tableName + " (";
        Field[] fields;
        ArrayList<String> getterMethods = new ArrayList<>();
        try {
            fields = Class.forName(clazzName).getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                getterMethods.add("get" + captureName(fields[i].getName()));
                sql += fields[i].getName();
                if (i < fields.length - 1)
                    sql += ",";
            }
            sql += ") values (";
            for (int i = 0; i < getterMethods.size(); i++) {
                if (Class.forName(clazzName).getMethod(getterMethods.get(i), null).invoke(o).getClass() == Class.forName("java.lang.String"))
                    sql += "'" + Class.forName(clazzName).getMethod(getterMethods.get(i), null).invoke(o) + "'";
                else
                    sql += Class.forName(clazzName).getMethod(getterMethods.get(i), null).invoke(o);
                if (i < fields.length - 1)
                    sql += ",";
            }
            sql += ");";


        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        System.out.println(sql);
        connectByCmd(sql);
        return true;
    }

    @Override
    public boolean update(T o) {
        int id = 0;
        String sql = "update " + getTableName() + " set ";
        Field[] fields;

        Method[] methods;
        ArrayList<String> getterMethods = new ArrayList<>();
        try {
            fields = Class.forName(clazzName).getDeclaredFields();
            id = Integer.parseInt(Class.forName(clazzName).getMethod("getId").invoke(o).toString());
            for (int i = 0; i < fields.length; i++) {
                getterMethods.add("get" + captureName(fields[i].getName()));
            }
            for (int i = 0; i < getterMethods.size(); i++) {
                if (Class.forName(clazzName).getMethod(getterMethods.get(i), null).invoke(o).getClass() == Class.forName("java.lang.String")) {
                    sql += fields[i].getName() + " = ";
                    sql += "'" + Class.forName(clazzName).getMethod(getterMethods.get(i), null).invoke(o) + "'";
                } else {
                    sql += fields[i].getName() + " = ";
                    sql += Class.forName(clazzName).getMethod(getterMethods.get(i), null).invoke(o);
                }
                if (i < fields.length - 1)
                    sql += ",";
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        sql += " where id = " + id + " ; ";
        System.out.println(sql);
        connectByCmd(sql);
        return true;
    }

    @Override
    public boolean delete(int id) {
        String tableName = this.clazzName.toLowerCase().substring(this.clazzName.toLowerCase().lastIndexOf(".") + 1);
        String sql = "delete from " + tableName + " where id = " + id;
        connectByCmd(sql);
        return true;
    }

    /**
     * 通过命令行调用sql
     *
     * @param sql
     * @return 按行返回每一行数据，第一行为列名,每一行数据由空格隔开
     */
    public static List<String> connectByCmd(String sql) {
        List<String> results = new ArrayList<>();
//        sql = "insert into user (id,username,password,mail,create_time) values(3,'mary',13,123,'Tues')";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            OutputStream os = p.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(os);
            writer.write(sql);
            writer.flush();
            writer.close();
            os.close();
            InputStream is = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                results.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    private String clazzName;

    private String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }

    private String getTableName() {
        return this.clazzName.toLowerCase().substring(this.clazzName.toLowerCase().lastIndexOf(".") + 1);
    }
}
