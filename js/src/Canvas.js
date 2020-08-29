import React from 'react';
import { WorldBuilder } from './services/WorldBuilder';

export function Canvas(socket) {
  let world;

  const canvasRef = React.createRef();
  const playerCanvasRef = React.createRef();

  const resized = () => {
    canvasRef.current.width = window.innerWidth;
    canvasRef.current.height = window.innerHeight;
    drawGrid(canvasRef.current);
  };

  const handleMessage = (message) =>  {
    let parsed = JSON.parse(message.data);
    if(parsed.id === 'world') {
      world = WorldBuilder(parsed);
      drawUsers(playerCanvasRef.current, world);
    }
  }

  React.useEffect(() => {
    resized();
    window.addEventListener("resize", resized);
    socket.onopen = () => {
      socket.send('{"id": "move","message":"RIGHT"}');
    }
    socket.onmessage = (message) => {
      handleMessage(message);
    }
  });

  

  return <div id="wrapper">
    <canvas ref={canvasRef}></canvas>
    <canvas ref={playerCanvasRef}></canvas>
    </div>
}

function drawGrid(canvas) {
  const cellRows = 10;
  const ctx = canvas.getContext('2d');

  ctx.strokeStyle = '#FF00FF';
  const incX = canvas.width / cellRows;
  for (let i = 0; i <= canvas.width; i += incX) {
    ctx.moveTo(i, 0);
    ctx.lineTo(i, canvas.height);
    ctx.stroke();
  }
  const incY = canvas.height / cellRows;
  for (let i = 0; i <= canvas.height; i += incY) {
    ctx.moveTo(0, i);
    ctx.lineTo(canvas.width, i);
    ctx.stroke();
  }
  console.log("done");
}

function drawUsers(canvas, world) {
  const ctx = canvas.getContext('2d');
  ctx.strokeStyle = '#00FF00';
  // ctx.moveTo(100, 0);
  //   ctx.lineTo(100, canvas.height);
  //   ctx.stroke();
  for(var user in world.users) {
    let playerXPosition = world.users[user].position.x;
    let playerYPosition = world.users[user].position.y;
    console.log(playerYPosition);
    ctx.moveTo(playerXPosition, playerYPosition);
    ctx.lineTo(playerXPosition+10, playerYPosition+10);
    ctx.stroke();
  }
  // console.log(world.users.[0]);

  // const cellRows = 10;
  

  
  // const incX = canvas.width / cellRows;
  // for (let i = 0; i <= canvas.width; i += incX) {
  //   ctx.moveTo(i, 0);
  //   ctx.lineTo(i, canvas.height);
  //   ctx.stroke();
  // }
  // const incY = canvas.height / cellRows;
  // for (let i = 0; i <= canvas.height; i += incY) {
  //   ctx.moveTo(0, i);
  //   ctx.lineTo(canvas.width, i);
  //   ctx.stroke();
  // }
  console.log("finished drawing users");
}
