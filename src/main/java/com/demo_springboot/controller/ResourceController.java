package com.demo_springboot.controller;
import com.demo_springboot.service.ResourceService;
import com.demo_springboot.entity.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.demo_springboot.entity.FileInfo;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletRequest;


@CrossOrigin(allowCredentials="true")

@RestController
public class ResourceController {
    
    @Autowired
    private ResourceService ResourceService;

    @RequestMapping("/data")
    public  List<Resource> getData(HttpSession session,@RequestParam("type")String type) throws Exception
    {
        List<Resource> res=ResourceService.getData(session,type);
        return res;
    }
    @RequestMapping("/download")
    public ResponseEntity<Object> downloadFile(@RequestParam("filename")String filename) throws IOException 
    {  
        return ResourceService.downloadFile(filename);
    }  

    
    @RequestMapping(value="upload",method=RequestMethod.POST)
    public String upload2(HttpSession session,FileInfo file) throws Exception
    {
        // return ResourceService.uploadAll(session,file);
        ResourceService.uploadFileByMappedByteBuffer(file);
        return "success";
    }

    @RequestMapping(value = "/upload",method =RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getInfo(HttpServletRequest request)throws Exception
    {
        String md5=request.getParameter("identifier");
        // return ResponseEntity.ok().body("ehllo");
        return ResponseEntity.ok().body(ResourceService.checkFileMd5(md5));
    }
    @RequestMapping("delete")
    public String delete(@RequestParam("id")Integer id)throws Exception
    {
        return ResourceService.delete(id);
    }
    
    @RequestMapping("deleteselect")
    public String deleteSelect(@RequestParam(name = "ids",required=false)ArrayList<Integer> ids)throws Exception
    {
        if(ids!=null)
            return ResourceService.deleteSelect(ids);
        return null;
    }

    @RequestMapping("rename")
    public String rename(@RequestParam("id")Integer id,@RequestParam("filename")String newfilename)throws Exception
    {
        return ResourceService.rename(newfilename,id);
    }

    @RequestMapping("recover")
    public String recover(@RequestParam("id")Integer id)throws Exception
    {
        return ResourceService.recover(id);
    }
}