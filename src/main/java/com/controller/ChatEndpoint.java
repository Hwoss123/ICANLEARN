package com.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.FromMessage;
import com.pojo.Message;
import com.utils.GetHttpSessionConfigurator;
import com.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/instant/chat/{token}", configurator = GetHttpSessionConfigurator.class)
@Component
public class ChatEndpoint {

    //用来存储每一个客户端对象对应的ChatEndpoint对象,还有对应的id
    private static final Map<Integer, ChatEndpoint> onlineUsers = new ConcurrentHashMap<>();

    //和某个客户端连接对象，需要通过他来给客户端发送数据
    private Session session;

    @OnOpen
    //连接建立成功调用
    public void onOpen(Session session, EndpointConfig config, @PathParam("token") String token) {
        //需要通知其他的客户端，将所有的用户的用户名发送给客户端
        this.session = session;

        Claims claims = JwtUtils.parseJwt(token);
        Integer user_id = (Integer) claims.get("id");

        //存储该链接对象
        onlineUsers.put(user_id, this);
    }


    @OnMessage
    //接收到消息时调用
    public void onMessage(String message, Session session, @PathParam("token") String token) {
        try {
            //获取客户端发送来的数据  {"toId":"5","message":"你好"}String
            ObjectMapper mapper = new ObjectMapper();
            Message mess = mapper.readValue(message, Message.class);

            //获取用户id
            Claims claims = JwtUtils.parseJwt(token);
            Integer user_id = (Integer) claims.get("id");

//           获取返回的信息对象，就是发了什么给谁
            FromMessage fromMessage = new FromMessage();
            fromMessage.setFromId(user_id);
            fromMessage.setMessage(message);
            String FromMessageStr = mapper.writeValueAsString(fromMessage);

            //将数据推送给指定的客户端
            ChatEndpoint chatEndpoint = onlineUsers.get(mess.getToId());
            chatEndpoint.session.getBasicRemote().sendText(FromMessageStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    //连接关闭时调用,建议当用户退出聊天的时候进行主动关闭
    public void onClose(Session session, @PathParam("token") String token) {
        //获取用户id
        Claims claims = JwtUtils.parseJwt(token);
        Integer user_id = (Integer) claims.get("id");
        //移除连接对象
        System.out.println("关闭" + user_id);
        onlineUsers.remove(user_id);
    }

}
