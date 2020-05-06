package com.mugu.gene.test;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @Author : zp
 * @Description :
 * @Date : 2019/11/1
 */
public class TestMain {
    public static void main(String[] args) {
        String textFileName = "D:/my/bmi-2012.txt";
        // 创建HttpClient对象
        CloseableHttpClient client = HttpClients.createDefault();

        File file = new File(textFileName);
        HttpPost post = new HttpPost("https://hb.flatironinstitute.org/api/netwas/");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("gwas_file", file, ContentType.DEFAULT_BINARY, textFileName);
        builder.addTextBody("gwas_format", "vegas", ContentType.DEFAULT_BINARY);
        builder.addTextBody("tissue", "adipose_tissue", ContentType.DEFAULT_BINARY);
        builder.addTextBody("p_value", "0.01", ContentType.DEFAULT_BINARY);
        //
        HttpEntity entity = builder.build();
        post.setEntity(entity);
        HttpResponse response = null;
        try {
            response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!StringUtils.isEmpty(response)) {
            entity = response.getEntity();
        }
        String result = null;
        try {
            result = EntityUtils.toString(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }
}
