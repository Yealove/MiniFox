package com.yealove.listener;

import com.yealove.common.Config;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 测试桩启动器
 * Created by Yealove on 2016-09-24.
 */
public class MiniFoxProcess {
    public static void main(String[] args) throws IOException {
        int port = 5678;
        if(args.length>0) {
            port = Integer.parseInt(args[0]);
        }

        Config.init();

        ServerSocket server = new ServerSocket(port);
        while (true) {
            Listener listener = new Listener(server.accept());
            listener.start();
        }
    }
}
