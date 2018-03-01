package userApp.commands.get;

import userApp.commands.Command;
import java.io.IOException;
import java.util.HashMap;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import userApp.model.User;

public class GetUser extends Command {

    public void execute() {
        HashMap<String, Object> props = parameters;

        Channel channel = (Channel) props.get("channel");
        AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
        AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
        Envelope envelope = (Envelope) props.get("envelope");
//        String response = User.getUserById(1);
        String response = (String)props.get("body");
        try {
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
            channel.basicAck(envelope.getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
