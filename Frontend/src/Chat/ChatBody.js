import React, {Component} from 'react';

import './style/ChatBody.css';
import ChatMessage from "./ChatMessage";
import {observer} from "mobx-react/index";

const ChatBody = observer(class ChatBody extends Component {
    render() {
        const stream = this.props.stream;
        return (
            <div className="ChatBody">
                <div className="ChatBody-Leading"/>
                {stream.messages.map(
                    (message, idx) => <ChatMessage message={message} key={idx}/>
                )}
            </div>
        );
    }
});

export default ChatBody;