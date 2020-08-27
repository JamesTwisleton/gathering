package com.twisleton.gathering.server;

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

    private final Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private final GameService gameService;

    public GatheringServer(@Value("${port.number:42069}") int port,
                           @Autowired GameService gameService) {
        super(new InetSocketAddress(port));
        this.gameService = gameService;
        this.start();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.info("Connection opened from {}", webSocket.getRemoteSocketAddress());
        gameService.handleUserConnection(webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        logger.info("Connection {} closed", i);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        logger.info("Message received:  {}", s);
        gameService.handleMessage(webSocket);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.info("Oopsie Woopsie you did a real fucky wucky, now you have to get in the f o r e v e r box: ", e);
    }

    @Override
    public void onStart() {
        logger.info("Gathering server started on port {}", this.getPort());
    }
}
