package com.twisleton.gathering.server;

import com.twisleton.gathering.dtos.User;
import com.twisleton.gathering.serveractions.ServerAction;
import com.twisleton.gathering.serveractions.ServerActions;
import com.twisleton.gathering.servermessages.ServerMessage;
import com.twisleton.gathering.servermessages.ServerMessages;
import com.twisleton.gathering.services.GameService;
import com.twisleton.gathering.services.UserService;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class GatheringServer extends WebSocketServer {

    private final Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private final GameService gameService;
    private final UserService userService;

    public GatheringServer(
            @Value("${port.number:42069}") int port,
            @Autowired GameService gameService,
            @Autowired UserService userService
    ) {
        super(new InetSocketAddress(port));
        this.gameService = gameService;
        this.userService = userService;
        this.start();
    }

    @PreDestroy
    public void stopServerOnShutdown() throws IOException, InterruptedException {
        userService.saveUsers();
        this.stop();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.info("Connection opened from {}", webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket connection, int i, String s, boolean b) {
        var userAddress = connection.getRemoteSocketAddress();
        var user = connectedUsers.get(userAddress);
        if (user != null) {
            logger.info("disconnected user {}", userAddress);
        } else {
            logger.warn("Tried to disconnect missing user with address {}", userAddress);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String messageBody) {
        var from = webSocket.getRemoteSocketAddress();
        logger.info("Message received from {}:  {}", from, messageBody);
        var action = gameService.interpretClientMessage(from, messageBody);
        var response = responseToAction(webSocket, action);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.info(
                "Oopsie Woopsie you did a real fucky wucky, now you have to get in the f o r e v e r box: ",
                e);
    }
    @Override
    public void onStart() {
        logger.info("Gathering server started on port {}", this.getPort());
    }

    private Optional<ServerMessage> responseToAction(WebSocket connection, ServerAction action) {
        if (action instanceof ServerActions.UpdateWorld userConnected) {
            connectedUsers.put(
                    connection.getRemoteSocketAddress(),
                    userConnected.user()
            );
            return Optional.of(
                    new ServerMessages.UpdateWholeWord(Set.copyOf(connectedUsers.values()))
            );
        } else if (action instanceof ServerActions.UpdateWorld updateWorld) {

        }

    }


}
