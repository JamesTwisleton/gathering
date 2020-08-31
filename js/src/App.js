import { Canvas } from './Canvas';
function App() {
  let socket = new WebSocket('ws://64.227.45.141:42069');
  return Canvas(socket);
}

export default App;
