import { Canvas } from './Canvas';
import config from './config';
function App() {
  let socketAddress = 'ws://localhost:42069';
  if(config.mode === 'dev') {
    socketAddress = `ws://${config.devServerAddress}:42069`;
  }
  let socket = new WebSocket(socketAddress);
  return Canvas(socket);
}

export default App;
