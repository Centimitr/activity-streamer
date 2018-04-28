import React, {Component} from 'react';

import './style/ChatMessage.css';

export default class ChatMessage extends Component {

    render() {
        const msg = this.props.message;
        switch (msg.type) {
            case 'notification':
                return (<div className="ChatMessage">
                    <div className="ChatMessage-Notification">
                        <div className="ChatMessage-Notification-Content">
                            {this.props.message.content}
                        </div>
                    </div>
                </div>);
            case 'message':
            default:
                return (<div className="ChatMessage">
                    <div className="ChatMessage-Message">
                        <div className="ChatMessage-Header">
                            <span className="ChatMessage-Username">{this.props.message.username}</span>
                            {/*<span>{this.props.time}</span>*/}
                        </div>
                        <div className="ChatMessage-Body">
                            {this.props.message.content}
                        </div>
                    </div>
                </div>);
        }
    }
}
