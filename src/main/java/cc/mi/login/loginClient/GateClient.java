package cc.mi.login.loginClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class GateClient {
	public static void start(String ip, int port) {
        final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
        	final Bootstrap bootstrap = new Bootstrap();
	        bootstrap.channel(NioSocketChannel.class)
	        		 .option(ChannelOption.SO_KEEPALIVE,true)
	        		 .option(ChannelOption.TCP_NODELAY, true)
	        		 .group(eventLoopGroup);
	        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
	            @Override
	            protected void initChannel(SocketChannel ch) throws Exception {
	            	ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, -4, 4));
					ch.pipeline().addLast("decoder", new GateClientDecoder());
					
					ch.pipeline().addLast("clientHandler", new GateClientHandler());
					
					ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4, true));
					ch.pipeline().addLast("encoder", new GateClientEncoder());
	            }
	        });
	        ChannelFuture future = bootstrap.connect(ip, port).sync();
	        future.awaitUninterruptibly();
	        future.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					eventLoopGroup.shutdownGracefully();
				}
			});
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
