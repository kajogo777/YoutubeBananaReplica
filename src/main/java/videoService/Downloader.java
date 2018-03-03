package videoService;

import io.netty.handler.codec.http.*;
import io.netty.channel.ChannelHandlerContext;
import static io.netty.buffer.Unpooled.copiedBuffer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;


public class Downloader{
    public String videoId;

    public Downloader(FullHttpRequest req ,  ChannelHandlerContext ctx) throws IOException{
        String uri = (req.uri());
        String[] parts = uri.split("/");

        // getting video id --> /videos/{videoId}

        for( int i=0; i< parts.length ; i++ ){
            if(parts[i].equals("videos") && parts.length >= (i+2) ){
                this.videoId = parts[i+1];
            }
        }
        this.start(ctx);
    }

    public static void start(ChannelHandlerContext ctx) throws IOException{

        File file = new File("/home/youssef/Desktop/small.mp4");

        FileInputStream fin = new FileInputStream(file);
        byte videoBytes[] = new byte[(int)file.length()];
        fin.read(videoBytes);


        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                copiedBuffer(videoBytes)
        );


        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION,"attachment; filename=small.mp4");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/video/mp4");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        ctx.write(response);
        ctx.flush();
    }
}
