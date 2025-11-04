import styled from 'styled-components';
import {flexAllCenter, typography } from './common.js';
import { NavLink } from 'react-router-dom';

const S = {};


S.Heading1 = styled.div`
    ${typography.h1}
`;
S.Heading2 = styled.div`
    ${typography.h2}
`;
S.Heading3 = styled.div`
    ${typography.h3}
`;
S.Body1 = styled.div`
    ${typography.b1}
`;
S.Body2 = styled.div`
    ${typography.b2}
`;
S.Caption = styled.div`
    ${typography.cp}
`;
S.Smalltext = styled.div`
    ${typography.st}
`;


export default S;