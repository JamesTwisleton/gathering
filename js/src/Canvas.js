import React from 'react';

export function Canvas() {
  const canvasRef = React.createRef();

  const resized = () => {
    canvasRef.current.width = window.innerWidth;
    canvasRef.current.height = window.innerHeight;
    drawGrid(canvasRef.current);
  };

  React.useEffect(() => {
    resized();
    window.addEventListener("resize", resized);
  });

  return <div id="wrapper">
    <canvas ref={canvasRef}></canvas>
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
