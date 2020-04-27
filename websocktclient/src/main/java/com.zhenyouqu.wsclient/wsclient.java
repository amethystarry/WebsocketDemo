package com.zhenyouqu.wsclient;

import org.java_websocket.drafts.Draft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.enums.ReadyState;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class wsclient {

    static String cookie = null;
    private static Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
    public static WebSocketClient client;
    static CountDownLatch countDownLatch;
    public static void main(String[] args) throws InterruptedException {
        tryConnect();
        //因为WebSocketClient请求是异步返回调用，所以需要等待上一次返回设置Cookie后，再设置Cookie进行请求
        countDownLatch.await();
        tryConnect();
        countDownLatch.await();
        tryConnect();
        tryConnect();
        tryConnect();
    }

    public static void tryConnect() throws InterruptedException {
        try {
            countDownLatch = new CountDownLatch(1);
            Draft draft = new Draft_6455();
            HashMap<String, String> headers = new HashMap<>();
            if(cookie != null) {
                headers.put("Cookie", cookie);
            }
            client = new WebSocketClient(new URI("ws://101.133.195.232:8000"),draft, headers) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    cookie = serverHandshake.getFieldValue("Set-Cookie");
                    System.out.println("收到Cookie："+cookie);
                    countDownLatch.countDown();
                }

                @Override
                public void onMessage(String msg) {
                    System.out.println("收到消息==========\n"+msg);
                    if(msg.equals("over")){
                        client.close();
                    }
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    logger.info("链接已关闭");
                }

                @Override
                public void onError(Exception e){
                    e.printStackTrace();
                    logger.info("发生错误已关闭");
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        client.connect();
        //logger.info(client.getDraft());
        while(!client.getReadyState().equals(ReadyState.OPEN)){
            logger.info("正在连接...");
        }
        //连接成功,发送信息
        client.send("哈喽,连接一下啊");

        //等待三秒 接受数据
        Thread.sleep(1000);
        client.close();
    }
}
