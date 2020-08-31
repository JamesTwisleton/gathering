import { Canvas } from './Canvas';
function App() {
  let socket = new WebSocket(`ws://${process.env.REACT_APP_DEV_SERVER_ADDRESS}:42069`);
  return Canvas(socket);
}

export default App;
