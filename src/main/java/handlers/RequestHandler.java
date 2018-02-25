package handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.UUID;


public class RequestHandler extends ChannelInboundHandlerAdapter {

    String requestId;

    private String parseToJson(final FullHttpRequest req) {
        JSONObject resultJson = new JSONObject();

        String type = req.method().toString();
        String body = req.content().toString(CharsetUtil.UTF_8);
        HashMap<String, String> params = uriDecode(req.uri());

        //TYPE
        resultJson.put("type", type);

        //BODY
        if(body != "") {
            //parsing body to JSONObject
            JSONParser parser = new JSONParser();
            Object obj = null;
            try {
                obj = parser.parse(body);
            } catch (ParseException e) {
                //should return a 400 Bad Request.
            }
            if(obj != null){
                JSONObject bodyJson = (JSONObject) obj;
                resultJson.put("body", bodyJson);
            }
        }

        //PARAMETERS
        if(params != null){
            JSONObject paramsJson = new JSONObject();
            for(String key : params.keySet()){
                String value = params.get(key);
                paramsJson.put(key, value);
            }
            resultJson.put("parameters", paramsJson);
        }

        return resultJson.toString();
    }

    private static HashMap<String, String> uriDecode(String uri){
        HashMap<String, String> result = new HashMap<String, String>();

        //check if it's a query
        if(!uri.contains("?")) return null;

        String query = uri.substring(uri.indexOf("?")+1);
        String [] pairs = query.split("&");

        for(String pair : pairs){
            //validate uri correctness
            if(!pair.contains("=")) return null;

            String [] kv = pair.split("=");
            result.put(kv[0],kv[1]);
        }
        return result;
    }

    private String getServiceName(final FullHttpRequest req){
        String result = req.uri().split("/")[1];
        if(result.contains("?")){
            result = result.substring(0,result.indexOf("?"));
        }
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

        ctx.fireChannelRead(data);
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


