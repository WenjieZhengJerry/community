package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.FileDTO;
import cn.lngfun.community.community.provider.UcloudProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@Slf4j
public class FileController {

    @Autowired
    private UcloudProvider ucloudProvider;

    @RequestMapping("/file/upload")
    @ResponseBody
    public FileDTO upload(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("editormd-image-file");
        try {
            String fileName = ucloudProvider.upload(file.getInputStream(), file.getContentType(), file.getOriginalFilename());
            FileDTO fileDTO = new FileDTO();
            fileDTO.setSuccess(1);
            fileDTO.setUrl(fileName);
            return fileDTO;
        } catch (Exception e) {
            log.error("upload error", e);
            FileDTO fileDTO = new FileDTO();
            fileDTO.setSuccess(0);
            fileDTO.setMessage("上传失败");
            return fileDTO;
        }
    }

}
