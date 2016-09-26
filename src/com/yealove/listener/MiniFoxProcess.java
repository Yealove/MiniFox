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
        Config.init();

        ServerSocket server = new ServerSocket(5678);
        while (true) {
            Listener listener = new Listener(server.accept());
            listener.start();
        }
    }
}
