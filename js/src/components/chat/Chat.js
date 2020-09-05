import React, { Component } from 'react';
import { Launcher } from 'react-chat-window';
import { ChatWrapper } from './styles';
export default class Chat extends Component {
    constructor() {
        super();
        this.state = {
            messageList: [
                {
                    author: 'Zump',
                    type: 'text',
                    data: {
                      text: 'Welcome to GatherinG'
                    }
                  }
            ],
        };
    }

    _onMessageWasSent(message) {
        this.setState({
            messageList: [...this.state.messageList, message],
        });
    }

    _sendMessage(text) {
        if (text.length > 0) {
            this.setState({
                messageList: [
                    ...this.state.messageList,
                    {
                        author: 'them',
                        type: 'text',
                        data: { text },
                    },
                ],
            });
        }
    }

    render() {
        return (
            <div>
                <ChatWrapper>
                    <Launcher
                        agentProfile={{
                            teamName: 'GatherinG',
                            imageUrl:
                                'https://i.imgur.com/2rUfCk5.png',
                        }}
                        onMessageWasSent={this._onMessageWasSent.bind(this)}
                        messageList={this.state.messageList}
                        showEmoji
                    />
                </ChatWrapper>
            </div>
        );
    }
}
