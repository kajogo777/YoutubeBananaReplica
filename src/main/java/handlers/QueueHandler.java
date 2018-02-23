package handlers;

import com.rabbitmq.client.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class QueueHandler extends ChannelInboundHandlerAdapter {

    ConnectionFactory factory;
    Connection connection;
    Channel mqChannel;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        final ChannelHandlerContext clientCtx = ctx;

        final String service = (String) ctx.channel().attr(AttributeKey.valueOf("SERVICE")).get();
        final String requestId = (String) ctx.channel().attr(AttributeKey.valueOf("REQUESTID")).get();
        final String data = (String) msg;

        System.out.println("DATA: "+ data);
        System.out.println("SERVICE: "+ service);
        initializeQueue();

        sendMessage(service, requestId, data);

        mqChannel.queueDeclare(service + "-response", false, false, false, null);
        Consumer consumer = new DefaultConsumer(mqChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if(properties.getCorrelationId().equals(requestId)){
                    String data = new String(body, "UTF-8");

                    FullHttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            copiedBuffer(data.getBytes())
                    );
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

                    clientCtx.write(response);
                    clientCtx.flush();

                    try {
                        this.getChannel().close();
                        this.getChannel().getConnection().close();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mqChannel.basicConsume(service + "-response", true, consumer);
    }

    private void initializeQueue(){
        try {
            //sharing connection between threads
            factory = new ConnectionFactory();
            factory.setUri("amqp://rabbitmq:rabbitmq@172.17.0.1:5672");
            connection = factory.newConnection();
            mqChannel = connection.createChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String service, String requestId, String message){
        try {
            mqChannel.queueDeclare(service + "-request", false, false, false, null);
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(requestId)
                    .replyTo(service + "-response")
                    .build();
            mqChannel.basicPublish("", service + "-request", props, message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
