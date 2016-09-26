package com.yealove.listener;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <简单描述>
 * Created by Yealove on 2016-09-24.
 */
public class Tester {


    public static void main(String[] args) {
        try {
            String url = "http://192.168.1.105:5678/order/test";
            // 使用默认配置创建httpclient的实例
            CloseableHttpClient client = HttpClients.createMinimal();

            HttpPost post = new HttpPost(url);

            /**
             * 设置参数，常用的有StringEntity,UrlEncodedFormEntity,MultipartEntity
             * 具体看org.apache.http.entity包
             */
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", "张三"));
            params.add(new BasicNameValuePair("password", "123456"));
            UrlEncodedFormEntity e = new UrlEncodedFormEntity(params, "UTF-8");
            post.setEntity(e);

            CloseableHttpResponse response = client.execute(post);

            // 服务器返回码
            int status_code = response.getStatusLine().getStatusCode();
            System.out.println(status_code);

            // 服务器返回内容
            String respStr = null;
            HttpEntity entity = response.getEntity();
            if(entity != null) {
                respStr = EntityUtils.toString(entity, "UTF-8");
            }
            System.out.println();
            System.out.println(respStr);
            // 释放资源
            EntityUtils.consume(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
