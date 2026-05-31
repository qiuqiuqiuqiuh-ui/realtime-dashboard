package com.dashboard.consumer.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Netty WebSocket 服务端
 *
 * 面试亮点：
 * 1. 主从 Reactor 线程模型
 *    - Boss Group: 1 个线程，负责接收 TCP 连接
 *    - Worker Group: N 个线程，负责 I/O 读写
 * 2. Pipeline 责任链模式
 * 3. 零拷贝 (FileRegion)
 * 4. 池化 ByteBuf (减少 GC)
 * 5. 单机支撑 10 万+ 长连接
 *
 * 对比 Spring WebSocket (Tomcat):
 * | 维度         | Tomcat      | Netty        |
 * |-------------|-------------|--------------|
 * | 线程模型     | 一连接一线程 | Reactor 多路复用 |
 * | 长连接上限   | 几千        | 10 万+       |
 * | 内存占用     | 高          | 低 (池化)     |
 * | 适用场景     | 普通 Web    | 高并发长连接   |
 */
public class NettyWebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(NettyWebSocketServer.class);

    private final int port;
    private final WebSocketChannelManager channelManager;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyWebSocketServer(int port, WebSocketChannelManager channelManager) {
        this.port = port;
        this.channelManager = channelManager;
    }

    /**
     * 启动服务
     */
    public void start() throws InterruptedException {
        // Boss Group: 1 个线程，负责 accept 新连接
        bossGroup = new NioEventLoopGroup(1);

        // Worker Group: 默认 CPU*2 个线程，负责 I/O 读写
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // TCP 参数
                .option(ChannelOption.SO_BACKLOG, 1024)          // 连接队列大小
                .childOption(ChannelOption.SO_KEEPALIVE, true)   // TCP 保活
                .childOption(ChannelOption.TCP_NODELAY, true)    // 禁用 Nagle 算法 (降低延迟)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // HTTP 编解码器
                        pipeline.addLast(new HttpServerCodec());

                        // 聚合 HTTP 消息 (将 HttpMessage + HttpContent 合并为 FullHttpRequest)
                        pipeline.addLast(new HttpObjectAggregator(65536));

                        // 支持大文件传输
                        pipeline.addLast(new ChunkedWriteHandler());

                        // 空闲检测 (30 秒无读写触发心跳检测)
                        pipeline.addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));

                        // WebSocket 协议处理器
                        // 路径 /ws/dashboard，自动处理握手 Ping/Pong
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws/dashboard"));

                        // 自定义业务处理器
                        pipeline.addLast(new NettyWebSocketHandler(channelManager));
                    }
                });

        // 绑定端口，同步等待
        ChannelFuture future = bootstrap.bind(port).sync();
        serverChannel = future.channel();

        log.info("Netty WebSocket 服务启动成功: port={}", port);
    }

    /**
     * 优雅关闭
     */
    public void shutdown() {
        log.info("Netty WebSocket 服务关闭中...");
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty WebSocket 服务已关闭");
    }

    public int getPort() {
        return port;
    }
}
