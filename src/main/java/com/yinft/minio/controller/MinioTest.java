package com.yinft.minio.controller;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/minio")
public class MinioTest {


    @Autowired
    private MinioClient minioClient;


    @GetMapping("/bucketList")
    public void getBucketList() throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        List<Bucket> bucketList = minioClient.listBuckets();
        for (Bucket bucket : bucketList) {
            log.info("=====" + bucket.name() + "========" + DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss").format(bucket.creationDate().withZoneSameInstant(ZoneId.of("Asia/Shanghai"))));
        }
    }

    @GetMapping("/objList")
    public void getObjList() {
        Iterable<Result<Item>> bucketList = minioClient.listObjects(
                ListObjectsArgs.builder().bucket("test").build()
        );
        bucketList.forEach(e -> {
            try {
                log.info(e.get().objectName());
            } catch (ErrorResponseException ex) {
                ex.printStackTrace();
            } catch (InsufficientDataException ex) {
                ex.printStackTrace();
            } catch (InternalException ex) {
                ex.printStackTrace();
            } catch (InvalidKeyException ex) {
                ex.printStackTrace();
            } catch (InvalidResponseException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            } catch (ServerException ex) {
                ex.printStackTrace();
            } catch (XmlParserException ex) {
                ex.printStackTrace();
            }
        });

    }


    @PostMapping("/upload")
    public Object upload(@RequestParam(name = "file", required = false) MultipartFile file) {
        if (file == null || file.getSize() == 0) {
            return "上传文件不能为空";
        }
        String orgfileName = file.getOriginalFilename();
        try {
            InputStream in = file.getInputStream();
            String contentType = file.getContentType();
            minioClient.putObject(
                    PutObjectArgs.builder().bucket("test").object(orgfileName).stream(in, -1, 10485760).contentType(contentType).build());
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", orgfileName);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "上传失败";
    }

    @GetMapping("/preView")
    public String preView() throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        Multimap<String, String> map = ArrayListMultimap.create();
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket("test").object("111.jpeg").method(Method.GET).build());
    }


    @GetMapping("/download/{fileName}")
    public void download(HttpServletResponse response, @PathVariable(name = "fileName") String fileName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        minioClient.downloadObject(DownloadObjectArgs.builder().bucket("test").object(fileName).filename(fileName).build());


//        InputStream in=null;
//        try {
//            //获取文件对象 stat原信息
//            ObjectStat stat =minioClient.statObject(MinioProp.MINIO_BUCKET, fileName);
//            response.setContentType(stat.contentType());
//            response.setContentType("application/octet-stream; charset=UTF-8");
//            in =   minioClient.getObject(MinioProp.MINIO_BUCKET, fileName);
//            IOUtils.copy(in,response.getOutputStream());
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            if(in!=null){
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

    }

    @GetMapping("/downloadObject/{fileName}")
    public void downloadObject(HttpServletResponse response, @PathVariable(name = "fileName") String fileName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {

        InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket("test").object(fileName).build());
//         下载设置
//        response.reset();
//        response.setContentType("application/octet-stream");
//        response.addHeader("Content-Disposition","attachment; filename=\""+fileName+"\"");
        byte[] b= new byte[1024];
        int len;
        while ((len=stream.read(b))>0){
            response.getOutputStream().write(b,0,len);
        }
        stream.close();
    }

}