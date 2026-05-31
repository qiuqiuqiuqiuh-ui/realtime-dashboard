package com.dashboard.consumer.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Configuration
public class NettyServerConfig {

    private static final Logger log = LoggerFactory.getLogger(NettyServerConfig.class);

    @Value("${netty.websocket.port:9090}")
    private int nettyPort;

    @Autowired
    private WebSocketChannelManager channelManager;

    private NettyWebSocketServer server;

    @PostConstruct
    public void start() {
        server = new NettyWebSocketServer(nettyPort, channelManager);
        Thread nettyThread = new Thread(() -> {
            try {
                server.start();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Netty interrupted", e);
            }
        }, "netty-server");
        nettyThread.setDaemon(true);
        nettyThread.start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
