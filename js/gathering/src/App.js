import React from 'react';
import { Canvas } from './Canvas';
function App() {
  let socket = new WebSocket('ws://localhost:42069');
  socket.onopen = () => {
    socket.send('{"id": "move","message":"RIGHT"}');
  }
  socket.onmessage = (message) => {
    let parsed = JSON.parse(message.data);
  }
  return Canvas();
}

export default App;
