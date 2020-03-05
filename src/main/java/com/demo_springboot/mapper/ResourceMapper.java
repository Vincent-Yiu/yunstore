package com.demo_springboot.mapper;

import java.util.List;
import com.demo_springboot.entity.Resource;

public interface ResourceMapper
{
	List<Resource> getAll(String owner,String type);
	Resource getOne(int fileid);
	void insert(Resource Resource);
	void update(String filename,int fileid);
	void delete(int fileid);
	void trashdelete(int fileid);
	void recover(int fileid);
}