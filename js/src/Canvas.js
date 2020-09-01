import React from 'react';
import { WorldBuilder } from './services/WorldBuilder';
import GlobalStyles from './styles/global';
import DefaultLayout from './pages/layouts/default';
import { InputBuffer } from './objects/InputBuffer';

export function Canvas(socket) {

    let world;
    // buffer for keypress until sent by server
    const inputBuffer = new InputBuffer();
    const canvasRef = React.createRef();
    const resized = () => {
        canvasRef.current.width = window.innerWidth;
        canvasRef.current.height = window.innerHeight;
    };
    const gameLoop = () => {
        sendInputToServer(socket, inputBuffer);
        drawWorld(canvasRef.current, world);
        window.requestAnimationFrame(gameLoop);
    };
    const handleMessage = (message) => {
        let parsed = JSON.parse(message.data);
        if (parsed.id === 'world') {
            world = WorldBuilder(parsed);
        }
    };
    const handleKeyDown = (event) => {
        switch (event.key) {
            case 'w':
                inputBuffer.keyW = true;
                break;
            case 'a':
                inputBuffer.keyA = true;
                break;
            case 's':
                inputBuffer.keyS = true;
                break;
            case 'd':
                inputBuffer.keyD = true;
                break;
            default:
                break;
        }
    };
    const handleKeyUp = (event) => {
        switch (event.key) {
            case 'w':
                inputBuffer.keyW = false;
                break;
            case 'a':
                inputBuffer.keyA = false;
                break;
            case 's':
                inputBuffer.keyS = false;
                break;
            case 'd':
                inputBuffer.keyD = false;
                break;
            default:
                break;
        }
    };

    React.useEffect(() => {
        resized();
        window.addEventListener('resize', resized);
        document.addEventListener('keydown', handleKeyDown);
        document.addEventListener('keyup', handleKeyUp);
        socket.onmessage = (message) => {
            handleMessage(message);
        };
        // supposedly the below hook is not the best method of doing things
        // https://css-tricks.com/using-requestanimationframe-with-react-hooks/
        socket.onopen = () => {
            gameLoop();
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

/* region handle user input */
function sendInputToServer(socket, inputBuffer) {
    const direction = inputBuffer.getCardinalDirection();
    if (direction) {
        socket.send(`{"id": "move","message":"${direction}"}`);
    }
}

/* region canvas drawing */
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
}

function drawUsers(canvas, world) {
    const ctx = canvas.getContext('2d');
    for (var user in world.users) {
        drawUser(ctx, world, user);
    }
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
    ctx.fillRect(
        playerXPosition - playerSize / 2,
        playerYPosition - playerSize / 2,
        playerSize,
        playerSize
    );
    ctx.stroke();
    ctx.closePath();
}
/* endregion */
