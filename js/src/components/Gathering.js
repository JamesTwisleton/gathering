import React from 'react';
import GlobalStyles from '../styles/global';
import DefaultLayout from '../pages/layouts/default';
import { Container, Row, Col } from 'react-bootstrap/';
import Canvas from './Canvas';
import Chat from './chat/Chat';
export function Gathering(socket) {
    return (
        <DefaultLayout>
            <GlobalStyles />
            <Container fluid>
                <Row>
                    <Col xs={9}>
                        <Canvas socket={socket} />
                    </Col>
                    <Col>
                        <Chat />
                    </Col>
                </Row>
            </Container>
        </DefaultLayout>
    );
}
