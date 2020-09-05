import { Gathering } from './components/Gathering';
function App() {
  let socket = new WebSocket(`ws://${process.env.REACT_APP_DEV_SERVER_ADDRESS}:42069`);
  return Gathering(socket);
}

export default App;
