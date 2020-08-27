package com.twisleton.gathering.server;

import com.google.gson.Gson;
import com.twisleton.gathering.records.Message;
import com.twisleton.gathering.services.GameService;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
public class GatheringServer extends WebSocketServer {

    private Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private GameService gameService;
    private Gson gson = new Gson();

    public GatheringServer(@Value("${port.number:42069}") int port,
                           @Autowired GameService gameService) {
        super(new InetSocketAddress(port));
        this.gameService = gameService;
        this.start();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.info("Connection opened from {}", webSocket.getRemoteSocketAddress());
        gameService.createUser(webSocket.getRemoteSocketAddress().getHostString());
        webSocket.send(gson.toJson(new Message("world", gameService.getWorld())));
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        logger.info("Connection {} closed", i);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        logger.info("Message received:  {}", s);
        Message message = new Message("0", "Message received, my ID is " + webSocket.getLocalSocketAddress().toString());
        webSocket.send(gson.toJson(message));
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.info("There was a bloody error wasnt there {}", e);
    }

    @Override
    public void onStart() {
        logger.info("Gathering server started on port {}", this.getPort());
    }
}
