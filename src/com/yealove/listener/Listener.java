package com.yealove.listener;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * 监听类
 *
 * Created by Yealove on 2016-09-24.
 */
public class Listener extends Thread {

    Logger logger = LoggerFactory.getLogger(Listener.class);

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

            while (!finishRead) {
                String line = in.readLine();

                logger.debug(line);

                //post请求时 获取 <request-body> 的长度
                if (line.startsWith("Content-Length")) {
                    dataSize = Integer.parseInt(line.split(":")[1].trim());
                }

                //<blank line> 空行后读取请求体数据和返回响应
                if (line.equals("")) {

                    //<request-body>有数据时读取数据
                    if (dataSize > 0) {
                        char[] data = new char[dataSize];
                        in.read(data, 0, dataSize);

                        logger.debug(String.valueOf(data));
                    }

                    finishRead = true;
                }
            }
            logger.debug("------request end------");


            /* Http响应格式：
             * <status-line>
             * <headers>
             * <blank line>
             * [<response-body>]
             */
            //<status-line>
            out.println("HTTP/1.1 200 OK");
            //<headers>
            out.println("Date: " + new Date());
            out.println("Content-Type: text/html;charset=UTF-8");
            //<blank line>
            out.println();
            //[<response-body>]
            out.println("中文 English");

            out.flush();
            logger.debug("------response end------");
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            IOUtils.closeQuietly(client);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}
