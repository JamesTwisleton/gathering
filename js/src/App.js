import { Canvas } from './Canvas';
import { WorldBuilder } from './services/WorldBuilder';
function App() {
  let socket = new WebSocket('ws://localhost:42069');
  let world;
  socket.onopen = () => {
    //socket.send('{"id": "move","message":"RIGHT"}');
  }
  socket.onmessage = (message) => {
    let parsed = JSON.parse(message.data);
    if(parsed.id === 'world') {
      world = WorldBuilder(parsed);
    }
  }
  return Canvas();
}

export default App;
