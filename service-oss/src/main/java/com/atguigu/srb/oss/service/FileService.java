package com.atguigu.srb.oss.service;

import java.io.InputStream;

/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/5/14 14:38
 */
public interface FileService {

    /**
     * 文件上传至阿里云
     */
    String upload(InputStream inputStream, String module, String fileName);

    void removeFile(String url);
}
