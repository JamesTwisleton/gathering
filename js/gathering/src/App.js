function App() {
  let socket = new WebSocket('ws://localhost:42069');
  let payload = {user: "me", message: "hello sockets"};
  socket.onopen = () => {
    // socket.send('Client is here!');
    socket.send('{"id": "move","message":"LEFT"}');
  }
  socket.onmessage = (message) => {
    console.log(message);
    let parsed = JSON.parse(message.data);
    console.log('message received', parsed);
    // handle message from backend
}
  return 'client is running';
  // return (
  //   <div className="App">
  //     <header className="App-header">
  //       <img src={logo} className="App-logo" alt="logo" />
  //       <p>
  //         Edit <code>src/App.js</code> and save to reload.
  //       </p>
  //       <a
  //         className="App-link"
  //         href="https://reactjs.org"
  //         target="_blank"
  //         rel="noopener noreferrer"
  //       >
  //         Learn React
  //       </a>
  //     </header>
  //   </div>
  // );
}

export default App;
