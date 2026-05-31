package com.dashboard.consumer.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty WebSocket 业务处理器
 *
 * 面试亮点：
 * 1. SimpleChannelInboundHandler 自动释放 ByteBuf
 * 2. 泛型指定处理的消息类型 (WebSocketFrame)
 * 3. 空闲事件处理 (心跳检测)
 * 4. 异常处理 & 连接生命周期
 */
public class NettyWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(NettyWebSocketHandler.class);

    private final WebSocketChannelManager channelManager;

    public NettyWebSocketHandler(WebSocketChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    /**
     * 收到 WebSocket 帧
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // 文本帧
        if (frame instanceof TextWebSocketFrame textFrame) {
            String text = textFrame.text();
            handleTextMessage(ctx, text);
        }
        // 二进制帧
        else if (frame instanceof BinaryWebSocketFrame) {
            log.debug("收到二进制帧: ctx={}", ctx.channel().id());
        }
        // Ping 帧 (Netty 自动回复 Pong)
        else if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        }
        // Pong 帧
        else if (frame instanceof PongWebSocketFrame) {
            log.debug("收到 Pong: ctx={}", ctx.channel().id());
        }
    }

    /**
     * 处理文本消息
     */
    private void handleTextMessage(ChannelHandlerContext ctx, String message) {
        String channelId = ctx.channel().id().asShortText();

        // 心跳
        if ("ping".equals(message)) {
            channelManager.updateHeartbeat(channelId);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(
                    "{\"type\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}"));
            return;
        }

        // 订阅频道
        if (message.startsWith("subscribe:")) {
            String channel = message.substring("subscribe:".length());
            channelManager.subscribe(channelId, channel);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(
                    "{\"type\":\"system\",\"data\":{\"message\":\"已订阅 " + channel + "\"}}"));
            return;
        }

        log.debug("收到消息: channelId={}, message={}", channelId, message);
    }

    /**
     * 连接建立
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        channelManager.addChannel(channelId, ctx.channel());

        int onlineCount = channelManager.getOnlineCount();
        log.info("WebSocket 连接建立: channelId={}, 在线人数={}", channelId, onlineCount);

        // 发送欢迎消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame(
                "{\"type\":\"system\",\"data\":{\"message\":\"连接成功\",\"onlineCount\":" + onlineCount + "}}"));
    }

    /**
     * 连接关闭
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        channelManager.removeChannel(channelId);

        int onlineCount = channelManager.getOnlineCount();
        log.info("WebSocket 连接关闭: channelId={}, 在线人数={}", channelId, onlineCount);
    }

    /**
     * 空闲事件处理 - 心跳检测
     *
     * 面试亮点：
     * IdleStateEvent 由 IdleStateHandler 触发
     * 当 30 秒无读写时，主动关闭连接
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            String channelId = ctx.channel().id().asShortText();
            log.warn("心跳超时，关闭连接: channelId={}", channelId);
            ctx.close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        log.error("WebSocket 异常: channelId={}", channelId, cause);
        ctx.close();
    }
}
