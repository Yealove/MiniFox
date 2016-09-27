package com.yealove.serve;

import com.yealove.common.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 测试桩启动器
 * Created by Yealove on 2016-09-24.
 */
public class MiniFoxServe {
    private static final Logger LOG = LoggerFactory.getLogger(MiniFoxServe.class);

    public static void main(String[] args) throws IOException {
        int port = 5678;
        if(args.length>0) {
            port = Integer.parseInt(args[0]);
        }

        LOG.info("监听的端口号：" + port);

        Config.init();

        ServerSocket server = new ServerSocket(port);
        while (true) {
            Fox fox = new Fox(server.accept());
            fox.start();
        }
    }
}
