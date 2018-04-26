package org.lwl.netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwl.netty.codec.ProtocolDataDecoder;
import org.lwl.netty.codec.ProtocolDataEncoder;
import org.lwl.netty.constant.ProtocolConfig;
import org.lwl.netty.server.HeartBeatRespHandler;
import org.lwl.netty.server.LoginRespHandler;

/**
 * @author thinking_fioa
 * @createTime 2018/4/21
 * @description Netty Server端
 */


public class NettyServer {
    private static final Logger LOGGER = LogManager.getLogger(NettyServer.class);

    public void bind(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildChannelHandler());

            ChannelFuture future = bootstrap.bind(port).sync();
            LOGGER.info("Netty Server started at port:{}", port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new ProtocolDataDecoder());
            socketChannel.pipeline().addLast(new ProtocolDataEncoder());
            socketChannel.pipeline().addLast(new ReadTimeoutHandler());
            socketChannel.pipeline().addLast(new LoginRespHandler());
            socketChannel.pipeline().addLast(new HeartBeatRespHandler());
        }
    }

    public static void main(String [] args) {
        try {
            new NettyServer().bind(ProtocolConfig.getPort());
        } catch (InterruptedException e) {
            LOGGER.error(e);
        }
    }
}
