package com.twisleton.gathering.services;

import com.twisleton.gathering.clientmessages.ClientMessage;
import com.twisleton.gathering.clientmessages.ClientMessages;
import com.twisleton.gathering.serveractions.ServerAction;
import com.twisleton.gathering.serveractions.ServerActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final UserService userService;

    public GameService(
            @Autowired UserService userService
    ) {
        this.userService = userService;
    }

    public ServerAction interpretClientMessage(String received) {
        var message = ClientMessage.parseMessage(received);
        if (message instanceof ClientMessages.UserConnect connectMessage) {
            userService.connectUser(connectMessage.userId());
            return new ServerActions.UpdateWorld(userService.connectedUsers);
        }
        if (message instanceof ClientMessages.Move moveMessage) {
            return handleMovement(moveMessage);
        }

        throw new RuntimeException("Missing action handler for " + message);
    }

    private ServerActions.UserMoved handleMovement(ClientMessages.Move moveMessage) {
        var newUser = userService.findById(moveMessage.userId())
                .map(u -> userService.movePlayer(u, moveMessage.direction()))
                .orElseThrow(() -> new RuntimeException("Missing player??"));

        return new ServerActions.UserMoved(newUser);
    }

}
