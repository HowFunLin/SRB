package com.atguigu.srb.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.atguigu.srb.oss.config.OssConfig;
import com.atguigu.srb.oss.service.FileService;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Resource
    private OSS oss;

    @Override
    public String upload(InputStream inputStream, String module, String fileName) {
        // 判断 Bucket 是否存在，不存在则创建
        if (!oss.doesBucketExist(OssConfig.BUCKET_NAME)) {
            oss.createBucket(OssConfig.BUCKET_NAME);
            oss.setBucketAcl(OssConfig.BUCKET_NAME, CannedAccessControlList.PublicRead);
        }

        // 构建路径 /module/yyyy/MM/dd/uuid.*
        String folder = new DateTime().toString("/yyyy/MM/dd/");
        fileName = UUID.randomUUID().toString() + fileName.substring(fileName.lastIndexOf("."));
        String path = module + folder + fileName;

        // 文件上传到阿里云 OSS
        oss.putObject(OssConfig.BUCKET_NAME, path, inputStream);

        // 返回文件的URL: https://BUCKET_NAME.ENDPOINT/path
        return "https://" + OssConfig.BUCKET_NAME + "." + OssConfig.ENDPOINT + "/" + path;
    }

    @Override
    public void remove(String url) {
        oss.deleteObject(
                OssConfig.BUCKET_NAME,
                url.substring(("https://" + OssConfig.BUCKET_NAME + "." + OssConfig.ENDPOINT + "/").length())
        );
    }
}
