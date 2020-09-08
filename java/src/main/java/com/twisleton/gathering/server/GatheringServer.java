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
import java.util.*;

@Service
public class GatheringServer extends WebSocketServer {

    private final Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private final GameService gameService;
    private final UserService userService;
    private final HashMap<InetSocketAddress, WebSocket> connections;

    public GatheringServer(
            @Value("${port.number:42069}") int port,
            @Autowired GameService gameService,
            @Autowired UserService userService
    ) {
        super(new InetSocketAddress(port));
        this.gameService = gameService;
        this.userService = userService;
        this.start();
        connections = new HashMap<>();
    }

    @PreDestroy
    public void stopServerOnShutdown() throws IOException, InterruptedException {
        userService.saveUsers();
        this.stop();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.info("Connection opened from {}", webSocket.getRemoteSocketAddress());
        connections.put(webSocket.getRemoteSocketAddress(), webSocket);
        webSocket.send((new ServerMessages.UpdateWholeWord(userService.connectedUsers())).serialize());
    }

    @Override
    public void onClose(WebSocket connection, int i, String s, boolean b) {
        var userAddress = connection.getRemoteSocketAddress();
        // TODO: naff
        var disconnectedMessage = new ServerActions.UserDisconnected(userAddress);
        responseToAction(disconnectedMessage);
    }

    @Override
    public void onMessage(WebSocket webSocket, String messageBody) {
            var from = webSocket.getRemoteSocketAddress();
        logger.info("Message received from {}:  {}", from, messageBody);
        var action = gameService.interpretClientMessage(from, messageBody);
        var response = responseToAction(action);
        handleResponse(webSocket, response);
    }

    private void handleResponse(WebSocket socket, ServerMessage response) {
        switch (response.getResponseStrategy()) {
            case RESPOND_ALL -> connections.values()
                    .forEach(s -> s.send(response.serialize()));
            case RESPOND_DIRECTLY -> socket.send(response.serialize());
        }
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
        userService.loadUsers();
    }

    private ServerMessage responseToAction(ServerAction action) {
        if (action instanceof ServerActions.UserConnected userConnected) {
            userService.connectUser(userConnected.from(), userConnected.user());
            var connectedUsers = userService.connectedUsers();
            return new ServerMessages.UpdateWholeWord(connectedUsers);

        } else if (action instanceof ServerActions.UserDisconnected disconnectedMessage) {
            var address = disconnectedMessage.from();
            connections.remove(address);
            userService.disconnectUser(address);
            var userId = userService.findConnectedByAddress(address)
                    .orElseThrow(() -> new RuntimeException("can't find user for address " + address));
            return new ServerMessages.UserDisconnected(userId.id());
        }

        throw new RuntimeException("no handler for action " + action);
    }


}
