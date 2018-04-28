import React, {Component} from 'react';

import './style/ChatHeader.css';

export default class ChatHeader extends Component {
    render() {
        const stream = this.props.stream;
        return (
            <header className="ChatHeader">
                <div className="ChatHeader-Info">
                    <div className="ChatHeader-Username">{stream.username}</div>
                    <div className="ChatHeader-Secret">Secret: <span>{stream.secret}</span></div>
                </div>
                <div className="ChatHeader-Logout" onClick={stream.logout}>
                    Logout
                </div>
            </header>
        );
    }
}