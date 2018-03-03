package handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import videoService.Downloader;
import java.util.UUID;


public class RequestHandler extends ChannelInboundHandlerAdapter {

    String requestId;

    private String parseToJson(final FullHttpRequest req){
        String result = "canary";
//        System.out.println(req.uri());
//        System.out.println(req.method());
//        System.out.println(req.content());
//        System.out.println(req.headers());

//        {
//            "type": //CREATE or READ or UPDATE or DELETE],
//            "parameters": {
//                url arguments (read) of json (create, update, delete)
//            }
//        }

        return result;
    }

    private String getServiceName(final FullHttpRequest req){
        String result = "APP";

        return result;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        final FullHttpRequest req = (FullHttpRequest) msg;

        String service = getServiceName(req);
        String data = parseToJson(req);

        requestId = UUID.randomUUID().toString();

        ctx.channel().attr(AttributeKey.valueOf("SERVICE")).set(service);
        ctx.channel().attr(AttributeKey.valueOf("REQUESTID")).set(requestId);
        //ctx.channel().attr(AttributeKey.newInstance("DATA")).set(data);


        Downloader downloader = new Downloader( req, ctx );

        ctx.fireChannelRead(data); //maybe send json
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}


