import { Canvas } from './Canvas';
function App() {
  let socket = new WebSocket('ws://localhost:42069');
  return Canvas(socket);
}

export default App;
