package com.demo_springboot.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpSession;

import com.demo_springboot.dao.GenericDao;
import com.demo_springboot.dto.ResultStatus;
import com.demo_springboot.dto.ResultVo;
import com.demo_springboot.entity.FileInfo;
import com.demo_springboot.entity.Resource;
import com.demo_springboot.entity.Result;
import com.demo_springboot.utils.Constants;
import com.demo_springboot.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class ResourceService {
	private String username;
	@Autowired
	private RedisUtil redisUtil;

	@Value("${upload.dir}")
	private String finalDirPath;

	private GenericDao<Resource> dao = new GenericDao<>("com.demo_springboot.entity.Resource");

	public List<Resource> getData(HttpSession session, String type) {
		session.setAttribute("user", "jim");
		username = session.getAttribute("user").toString();
		String sql = "select * from resource where owner = '" + username + "'";
		if (type.equals("trash")) {
			sql += " and isdelete = 1";
		} else {
			if (!type.equals("all")) {
				sql += " and " + getSqlByType(type);

			}
			sql += " and isdelete = 0 and complete = 1 ";
		}
		sql += " ; ";
		List<Resource> result = dao.list(sql);

		return result;
	}

	public ResponseEntity<Object> downloadFile(String filename) throws IOException {
		FileSystemResource resource = new FileSystemResource(finalDirPath + filename);

		System.out.println("filename:" + filename);
		return ResponseEntity.ok().header("Content-Disposition", "attachment;fileName=" + filename)
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
	}

	public String delete(Integer id) throws Exception {
		Resource item = dao.find(id);
		if (item.getIsdelete() == 1) {
			return this.trash_delete(item);
		} else {
			item.setIsdelete(1);
			dao.update(item);
			Result result = new Result();
			result.setCode("200");
			result.setMsg("success");
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(result);
			return json;
		}

	}

	public String trash_delete(Resource item) throws Exception {
		String filename = item.getFilename();
		String path = ResourceUtils.getURL("classpath:").getPath() + "/upload/";
		File file = new File(finalDirPath + filename);
		if (file.exists() && file.isFile())
			file.delete();

		dao.delete(item.getId());
		Result result = new Result();
		result.setCode("200");
		result.setMsg("success");
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(result);
		return json;
	}

	public String deleteSelect(ArrayList<Integer> fileids) throws Exception {
		Result result = new Result();
		for (int i = 0; i < fileids.size(); i++) {
			delete(fileids.get(i));
		}
		result.setCode("200");
		result.setMsg("success");
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(result);
		return json;
	}

	public String rename(String newfilename, Integer id) throws Exception {
		Resource item = dao.find(id);
		String oldfilename = item.getFilename();
		// String path = ResourceUtils.getURL("classpath:").getPath() + "/upload/";
		String path=finalDirPath;
		File oldfile = new File(path + oldfilename);
		File newfile = new File(path + newfilename);
		oldfile.renameTo(newfile);

		item.setFilename(newfilename);
		dao.update(item);
		Result result = new Result();
		result.setCode("200");
		result.setMsg("success");
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(result);
		return json;
	}

	public String recover(Integer id) throws Exception {
		Resource item = dao.find(id);
		item.setIsdelete(0);
		dao.update(item);
		Result result = new Result();
		result.setCode("200");
		result.setMsg("success");
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(result);
		return json;
	}

	/**
	 * 文件重命名
	 *
	 * @param toBeRenamed   将要修改名字的文件
	 * @param toFileNewName 新的名字
	 * @return
	 */
	public boolean renameFile(File toBeRenamed, String toFileNewName) {
		// 检查要重命名的文件是否存在，是否是文件
		if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {
			// logger.info("File does not exist: " + toBeRenamed.getName());
			return false;
		}
		String p = toBeRenamed.getParent();
		File newFile = new File(p + File.separatorChar + toFileNewName);
		// 修改文件名
		return toBeRenamed.renameTo(newFile);
	}

	public void uploadFileByMappedByteBuffer(FileInfo param) throws IOException {
		// System.out.println(finalDirPath+" and md5="+param.getMd5()+"
		// name="+param.getName()+" chunk"+CHUNK_SIZE);
		long CHUNK_SIZE = param.getChunkSize();
		String fileName = param.getFilename();
		String uploadDirPath = finalDirPath;
		String tempFileName = fileName + "_tmp";
		File tmpDir = new File(uploadDirPath);
		File tmpFile = new File(uploadDirPath, tempFileName);
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}

		RandomAccessFile accessTmpFile = new RandomAccessFile(tmpFile, "rw");
		long offset = CHUNK_SIZE * (param.getChunkNumber() - 1);
		// 定位到该分片的偏移量
		accessTmpFile.seek(offset);
		// 写入该分片数据
		// System.out.println(param.getChunkNumber() + " start write");
		accessTmpFile.write(param.getFile().getBytes());
		// System.out.println(param.getChunkNumber() + " end write");
		// 释放
		accessTmpFile.close();

		boolean isOk = checkAndSetUploadProgress(param, uploadDirPath);
		// boolean isOk=true;
		if (isOk) {
			boolean flag = renameFile(tmpFile, fileName);
			System.out.println("upload complete !!" + flag + " name=" + fileName);
		}
	}

	public Object checkFileMd5(String md5) {
		List<Resource> item = dao.list("select * from resource where md5= '" + md5 + "' ;");
		if (item.size() == 0) {
			return new ResultVo(ResultStatus.NO_HAVE);
		}
		int isComplete = item.get(0).getComplete();
		if (isComplete == 1) {
			return new ResultVo(ResultStatus.IS_HAVE);
		} else {
			File confFile = new File(finalDirPath + File.separator + item.get(0).getFilename() + ".conf");
			byte[] completeList;
			try {
				completeList = FileUtils.readFileToByteArray(confFile);
				List<Integer> missChunkList = new LinkedList<>();
				for (int i = 0; i < completeList.length; i++) {
					if (completeList[i] != Byte.MAX_VALUE) {
						missChunkList.add(i);
					}
				}
				return new ResultVo<>(ResultStatus.ING_HAVE, missChunkList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 检查并修改文件上传进度
	 *
	 * @param file
	 * @param uploadDirPath
	 * @return
	 * @throws IOException
	 */
	private boolean checkAndSetUploadProgress(FileInfo file, String uploadDirPath) throws IOException {
		String fileName = file.getFilename();

		File confFile = new File(uploadDirPath, fileName + ".conf");
		RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw");
		// 把该分段标记为 true 表示完成
		// System.out.println("set part " + file.getChunkNumber() + " complete");
		accessConfFile.setLength(file.getTotalChunks());
		accessConfFile.seek(file.getChunkNumber() - 1);
		accessConfFile.write(Byte.MAX_VALUE);

		// completeList 检查是否全部完成,如果数组里是否全部都是(全部分片都成功上传)
		// byte[] completeList = FileUtils.readFileToByteArray(confFile);
		InputStream in = new FileInputStream(confFile);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		byte[] completeList = out.toByteArray();
		in.close();
		out.close();
		byte isComplete = Byte.MAX_VALUE;
		for (int i = 0; i < completeList.length && isComplete == Byte.MAX_VALUE; i++) {
			// 与运算, 如果有部分没有完成则 isComplete 不是 Byte.MAX_VALUE
			isComplete = (byte) (isComplete & completeList[i]);
			// System.out.println("check part " + i + " complete?:" + completeList[i]);
		}

		accessConfFile.close();
		String md5 = file.getIdentifier();
		if (isComplete == Byte.MAX_VALUE) {
			if (file.getChunkNumber() == 1) {
				Resource newFile = new Resource();
				newFile.setFilename(file.getFilename());
				newFile.setOwner("jim");
				newFile.setMd5(file.getIdentifier());
				newFile.setType(file.getFilename().substring(file.getFilename().lastIndexOf(".") + 1).toLowerCase());
				newFile.setUploadtime(System.currentTimeMillis());
				newFile.setUrl("/upload");
				newFile.setSize(FormatFileSize(Long.parseLong(file.getTotalSize())));
				newFile.setComplete(1);
				newFile.setId(0);
				dao.insert(newFile);
			} else {
				GenericDao.connectByCmd("update resource set complete = 1 where md5 = '" + md5 + "';");
			}
			confFile.delete();
			return true;
		} else {
			int ifExist = 1;
			synchronized (this) {
				ifExist = GenericDao.connectByCmd("select * from resource where md5= '" + md5 + "' ;").size();

				if (ifExist == 0) {
					System.out.println("0");
					Resource newFile = new Resource();
					newFile.setFilename(file.getFilename());
					newFile.setOwner("jim");
					newFile.setMd5(file.getIdentifier());
					newFile.setType(
							file.getFilename().substring(file.getFilename().lastIndexOf(".") + 1).toLowerCase());
					newFile.setUploadtime(System.currentTimeMillis());
					newFile.setUrl("/upload");
					newFile.setSize(FormatFileSize(Long.parseLong(file.getTotalSize())));
					newFile.setId(0);
					dao.insert(newFile);
				}
			}
			return false;
		}
	}

	private String getSqlByType(String type) {
		StringBuffer sql = new StringBuffer("(");
		if (type.equals("img")) {
			for (int i = 0; i < Constants.img.length; i++) {
				sql.append(" type = '" + Constants.img[i] + "' ");
				if (i < Constants.img.length - 1)
					sql.append(" or ");
			}
		}
		if (type.equals("doc")) {
			for (int i = 0; i < Constants.doc.length; i++) {
				sql.append(" type = '" + Constants.doc[i] + "' ");
				if (i < Constants.doc.length - 1)
					sql.append(" or ");
			}
		}
		if (type.equals("video")) {
			for (int i = 0; i < Constants.video.length; i++) {
				sql.append(" type = '" + Constants.video[i] + "' ");
				if (i < Constants.video.length - 1)
					sql.append(" or ");
			}
		}

		sql.append(")");
		return sql.toString();
	}

	private String FormatFileSize(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}
}