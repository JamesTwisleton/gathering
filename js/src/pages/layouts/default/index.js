import React from 'react';
import Container from 'react-bootstrap/Container';
import 'bootstrap/dist/css/bootstrap.min.css';
// import Row from 'react-bootstrap/Row';
// import PropTypes from 'prop-types';
import { Wrapper } from './styles';
import { NavigationBar } from '../../../components/NavigationBar';

export default function DefaultLayout({ children }) {
    return (
        <>
            <NavigationBar />
            <Wrapper>{children}</Wrapper>
        </>
    );
}
DefaultLayout.propTypes = {
    // children: PropTypes.element.isRequired,
};
