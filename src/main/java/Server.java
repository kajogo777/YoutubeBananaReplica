import handlers.QueueHandler;
import handlers.RequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;

public class Server {

    private int port;

    public Server(int port){
        this.port = port;
    }

    public void run() throws Exception{
        EventLoopGroup listeners = new NioEventLoopGroup();
        EventLoopGroup workers = new NioEventLoopGroup();
        final EventLoopGroup handlers = new NioEventLoopGroup(5);

        try{
            ServerBootstrap sb = new ServerBootstrap();

            sb.group(listeners, workers) //parents , children
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>(){
                @Override
                public void initChannel(SocketChannel ch) throws Exception{
                    ChannelPipeline p = ch.pipeline();
                    EventExecutor e1 = new DefaultEventExecutor(handlers);

                    p.addLast(new HttpServerCodec());
                    p.addLast(new HttpObjectAggregator(10*1024*1024));
                    //p.addLast(new WebSocketServerProtocolHandler("/ws"));
                    //p.addLast(new Dispatcher());
                    //p.addLast(e1, new CommandHandler());
                    p.addLast(new RequestHandler());
                    //p.addLast(new CacheHandler());
                    p.addLast(new QueueHandler());
                }
            });

            ChannelFuture f = sb.bind(port).sync();

            System.out.println("Listening on 0.0.0.0:" + port);
            f.channel().closeFuture().sync();
        }finally{
            listeners.shutdownGracefully();
            workers.shutdownGracefully();
            handlers.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 12345;
        }
        new Server(port).run();
    }
}
