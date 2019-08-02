package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.FileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class FileController {

//    @Value("${file.upload.path}")
//    private String uploadImagePath;

    @RequestMapping("/file/upload")
    @ResponseBody
    public FileDTO upload(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        try {
            String fileName = saveFile(multipartRequest.getFile("editormd-image-file"), request.getServletContext().getRealPath("/images/upload/"));

            FileDTO fileDTO = new FileDTO();
            fileDTO.setMessage("success");
            fileDTO.setSuccess(1);
            fileDTO.setUrl(fileName);

            return fileDTO;
        } catch (IOException e) {
            e.printStackTrace();

            FileDTO fileDTO = new FileDTO();
            fileDTO.setSuccess(0);
            fileDTO.setMessage("上传失败");

            return fileDTO;
        }
    }

    /**
     * 保存上传文件到本地
     *
     * @param image 上传的文件
     * @return 本地文件引用
     */
    private String saveFile(MultipartFile image, String path) throws IOException {
        //文件后缀名
        String suffix = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));
        //上传文件名
        String filename = UUID.randomUUID() + suffix;
        //服务器端保存的文件对象
        File serverFile = new File(path + File.separator + filename);

        if(!serverFile.exists()) {
            //先得到文件的上级目录，并创建上级目录，在创建文件
            serverFile.getParentFile().mkdirs();
            try {
                //创建文件
                serverFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //将上传的文件写入到服务器端文件内
        image.transferTo(serverFile);

        return serverFile.getAbsolutePath();
    }
}
