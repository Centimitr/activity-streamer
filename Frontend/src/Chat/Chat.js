import React, {Component} from 'react';
import ChatHeader from "./ChatHeader";
import ChatBody from "./ChatBody";
import ChatSendBox from "./ChatSendBox";

import './style/Chat.css';

export default class Chat extends Component {
    render() {
        const stream = this.props.stream;
        return (
            <div className="Chat">
                <ChatBody stream={stream}/>
                <ChatSendBox stream={stream}/>
                <ChatHeader stream={stream}/>
            </div>
        );
    }
}
