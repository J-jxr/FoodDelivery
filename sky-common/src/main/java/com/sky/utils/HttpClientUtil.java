package com.sky.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类，封装了常用的GET和POST请求操作。
 * 技术选型：
 * - 使用 Apache HttpClient 作为HTTP请求的客户端。
 * - 使用 FastJSON 库来处理JSON数据（如在doPost4Json方法中）。
 */
public class HttpClientUtil {

    // 设置连接超时时间、请求超时时间和响应超时时间（单位：毫秒）
    static final int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 发送GET方式请求
     *
     * @param url      请求的URL
     * @param paramMap 请求的参数（如果有的话），键值对形式
     * @return 返回请求的响应内容（字符串形式）
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        // 创建HttpClient对象，用于执行HTTP请求
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String result = "";
        CloseableHttpResponse response = null;

        try {
            // 构建URI对象并添加参数（如果有的话）
            URIBuilder builder = new URIBuilder(url);
            if (paramMap != null) {
                // 将所有参数添加到URL的查询字符串中
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key, paramMap.get(key));
                }
            }
            URI uri = builder.build();  // 生成最终的URI

            // 创建GET请求对象
            HttpGet httpGet = new HttpGet(uri);

            // 执行请求并获取响应
            response = httpClient.execute(httpGet);

            // 判断响应状态码，200表示请求成功
            if (response.getStatusLine().getStatusCode() == 200) {
                // 获取并返回响应体内容
                result = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭响应和HttpClient，释放资源
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 发送POST方式请求（表单提交）
     *
     * @param url      请求的URL
     * @param paramMap 请求的参数，键值对形式
     * @return 返回请求的响应内容（字符串形式）
     * @throws IOException 如果请求过程发生IO异常
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建HttpClient对象，用于执行HTTP请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http POST请求对象
            HttpPost httpPost = new HttpPost(url);

            // 如果有参数，将它们封装为表单数据
            if (paramMap != null) {
                // 使用List封装表单参数
                List<NameValuePair> paramList = new ArrayList<>();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                // 将参数设置到POST请求的实体中
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            // 设置请求的配置（超时时间等）
            httpPost.setConfig(builderRequestConfig());

            // 执行HTTP请求，获取响应
            response = httpClient.execute(httpPost);

            // 获取响应内容
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                // 关闭响应对象，释放资源
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求（JSON数据提交）
     *
     * @param url      请求的URL
     * @param paramMap 请求的参数，键值对形式
     * @return 返回请求的响应内容（字符串形式）
     * @throws IOException 如果请求过程发生IO异常
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建HttpClient对象，用于执行HTTP请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http POST请求对象
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                // 将参数封装成JSON格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(), param.getValue());
                }
                // 将JSON数据封装为请求体
                StringEntity entity = new StringEntity(jsonObject.toString(), "utf-8");
                entity.setContentEncoding("utf-8");
                entity.setContentType("application/json"); // 设置请求体数据类型为JSON
                httpPost.setEntity(entity);
            }

            // 设置请求的配置（超时时间等）
            httpPost.setConfig(builderRequestConfig());

            // 执行HTTP请求，获取响应
            response = httpClient.execute(httpPost);

            // 获取响应内容
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                // 关闭响应对象，释放资源
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 构建HTTP请求的配置，包括超时时间等设置
     *
     * @return 返回配置对象
     */
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)           // 设置连接超时时间
                .setConnectionRequestTimeout(TIMEOUT_MSEC) // 设置请求超时时间
                .setSocketTimeout(TIMEOUT_MSEC)            // 设置读取超时时间
                .build();
    }
}
