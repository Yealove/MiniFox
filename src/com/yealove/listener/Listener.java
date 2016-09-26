package com.yealove.listener;

import com.yealove.common.Config;
import com.yealove.common.ConfigCheckedException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Date;

/**
 * 监听处理类
 * <p>
 * Created by Yealove on 2016-09-24.
 */
public class Listener extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(Listener.class);

    private Socket client;

    private boolean finishRead = false;

    public Listener(Socket client) {
        this.client = client;
    }

    /*
     * Http请求格式:
     * <request-line>
     * <headers>
     * <blank line>
     * [<request-body>]
     */
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream());

            //post请求时 <request-body> 的长度，默认0
            int dataSize = 0;

            String request = "";
            String url = "";

            while (!finishRead) {
                String line = in.readLine();

                LOG.debug(line);

                //post请求时 获取 <request-body> 的长度
                if (line.startsWith("Content-Length")) {
                    dataSize = Integer.parseInt(line.split(":")[1].trim());
                }

                if (line.startsWith("POST")) {
                    url = line.split(" ")[1];
                    LOG.debug("url: " + url);
                }

                //<blank line> 空行后读取请求体数据和返回响应
                if (line.equals("")) {

                    //<request-body>有数据时读取数据
                    if (dataSize > 0) {
                        char[] data = new char[dataSize];
                        in.read(data, 0, dataSize);
                        request = String.valueOf(data);

                        LOG.info(request);
                    }

                    finishRead = true;
                }
            }
            LOG.debug("------request end------");

            /* Http响应格式：
             * <status-line>
             * <headers>
             * <blank line>
             * [<response-body>]
             */
            writeHttpHead(out);
            writeHttpBody(out, url, request);

            out.flush();
            LOG.debug("------response end------");
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } catch (ConfigCheckedException e) {
            if(out != null) {
                out.write(e.getMessage());
                out.flush();
            }
        } finally {
            IOUtils.closeQuietly(client);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Http返回头信息
     * @param out
     */
    private void writeHttpHead(PrintWriter out) {
        //<status-line>
        out.println("HTTP/1.1 200 OK");
        //<headers>
        out.println("Date: " + new Date());
        out.println("Content-Type: text/html;charset=UTF-8");
        //<blank line>
        out.println();
    }

    /**
     * 将返回文件内容以Http Body输出
     * @param out
     * @param url
     * @param xml
     */
    private static void writeHttpBody(PrintWriter out, String url, String xml) {
        String fileName = Config.getResultFileName(url, xml);
        File file = new File(fileName);
        if (!file.isFile()) {
            throw new ConfigCheckedException("配置的返回文件[" + fileName + "]不存在");
        }

        FileReader fis = null;
        try {
            fis = new FileReader(file);
            char[] buf = new char[512];
            int temp;
            while ((temp = fis.read(buf)) != -1) {
                out.write(buf, 0, temp);
            }
            out.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}
