package com.twisleton.gathering.server;

import com.google.gson.Gson;
import com.twisleton.gathering.records.Message;
import com.twisleton.gathering.records.User;
import com.twisleton.gathering.records.World;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class GatheringServer extends WebSocketServer {

    private Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private World world;
    private int maxX = 100;
    private int maxY = 100;

    public GatheringServer(@Value("${port.number:42069}") int port) {
        super(new InetSocketAddress(port));
        world = new World("0", new HashMap<String, User>());
        this.start();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.info("Connection opened from {}", webSocket.getRemoteSocketAddress().getAddress().toString());
        int newPlayerX = (int) ((Math.random() * (maxX - 0)) + 0);
        int newPlayerY = (int) ((Math.random() * (maxY - 0)) + 0);
        Point newPlayerPoint = new Point(newPlayerX, newPlayerY);
        Gson gson = new Gson();
        webSocket.send(gson.toJson(newPlayerPoint));
        world.users().put(webSocket.getRemoteSocketAddress().getAddress().toString(),
                new User("0", new Point(0, 0)));
        logger.info("user added!");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        logger.info("Connection {} closed", i);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        logger.info("Message received:  {}", s);
        Message message = new Message("0", "Message received, my ID is " + webSocket.getLocalSocketAddress().toString());
        Gson gson = new Gson();
        webSocket.send(gson.toJson(message));
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.info("There was a bloomin error >_< {}", e);
    }

    @Override
    public void onStart() {
        logger.info("Chat server started on port {}", this.getPort());
    }

    public static void main(String[] args) {
        new GatheringServer(42069).start();
    }
}
