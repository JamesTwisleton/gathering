import React from 'react';
import { Canvas } from './Canvas';

function App() {
  let socket = new WebSocket('ws://localhost:42069');
  let payload = {user: "me", message: "hello sockets"};
  socket.onopen = () => {
    // socket.send('Client is here!');
    socket.send('{"id": "move","message":"RIGHT"}');
    // socket.send('{"id": "move","message":"UP"}');
  }
  socket.onmessage = (message) => {
    let parsed = JSON.parse(message.data);
    // handle message from backend
  }
  return Canvas();
}

export default App;
