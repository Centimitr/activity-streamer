import React, {Component} from 'react';

import './style/ChatToolBar.css';
import {observer} from "mobx-react/index";

const ChatToolBar = observer(class ChatToolBar extends Component {

    render() {
        const stream = this.props.stream;
        return (
            <div className="ChatToolBar">
                <div className="ChatToolBar-Row">
                    <div>
                        <button onClick={stream.clearMessages}>Clear Screen</button>
                        <button onClick={stream.formatDraft}>Format</button>
                    </div>
                    <div>
                        <button className={stream.valid ? 'accept' : 'refuse'} onClick={stream.send}>Send</button>
                    </div>
                </div>
            </div>
        );
    }
});

export default ChatToolBar;