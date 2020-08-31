import React from 'react';
import { WorldBuilder } from './services/WorldBuilder';
import GlobalStyles from './styles/global';
import DefaultLayout from './pages/layouts/default';

export function Canvas(socket) {

    let world;
    const canvasRef = React.createRef();
    const resized = () => {
        canvasRef.current.width = window.innerWidth;
        canvasRef.current.height = window.innerHeight;
        drawWorld(canvasRef.current, world);
    };
    const handleMessage = (message) => {
        let parsed = JSON.parse(message.data);
        if (parsed.id === 'world') {
            world = WorldBuilder(parsed);
            drawWorld(canvasRef.current, world);
        }
    };
    const handleKeyPress = (event) => {
        console.log(event.key);
        switch (event.key) {
            case 'w':
                socket.send('{"id": "move","message":"UP"}');
                break;
            case 'a':
                socket.send('{"id": "move","message":"LEFT"}');
                break;
            case 's':
                socket.send('{"id": "move","message":"DOWN"}');
                break;
            case 'd':
                socket.send('{"id": "move","message":"RIGHT"}');
                break;
        }
    };

    React.useEffect(() => {
        resized();
        window.addEventListener('resize', resized);
        document.addEventListener('keydown', handleKeyPress);
        // socket.onopen = () => {
        //     socket.send('{"id": "move","message":"RIGHT"}');
        // };
        socket.onmessage = (message) => {
            handleMessage(message);
        };
    });

    return (
        <DefaultLayout>
        <GlobalStyles />
            <div id="wrapper">
                <canvas ref={canvasRef}></canvas>
            </div>
        </DefaultLayout>
    );
}

function drawWorld(canvas, world) {
    clearWorld(canvas);
    drawGrid(canvas);
    // only draw if initialized
    if (world) {
        drawUsers(canvas, world);
    }
}

function clearWorld(canvas) {
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function drawGrid(canvas) {
    const cellRows = 10;
    const ctx = canvas.getContext('2d');

    ctx.beginPath();
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
    ctx.closePath();
    console.log('done');
}

function drawUsers(canvas, world) {
    const ctx = canvas.getContext('2d');
    for (var user in world.users) {
        drawUser(ctx, world, user);
    }
    console.log('finished drawing users');
}

function drawUser(ctx, world, user) {
    // todo: move this to a better location
    // left this as a variable so it could
    // easily be changed in the future
    const playerSize = 20;

    ctx.beginPath();
    let playerXPosition = (world.users[user].position.x / world.maxX) * window.innerWidth;
    let playerYPosition = (world.users[user].position.y / world.maxY) * window.innerHeight;
    ctx.fillStyle = world.users[user].color;
    console.log(playerYPosition);
    ctx.fillRect(
        playerXPosition - playerSize / 2,
        playerYPosition - playerSize / 2,
        playerSize,
        playerSize
    );
    ctx.stroke();
    ctx.closePath();
}
