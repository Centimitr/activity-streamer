import React, {Component} from 'react';

import './style/ChatSendBox.css';
import ChatToolBar from "./ChatToolBar";
import AceEditor from 'react-ace';
import 'brace/mode/json';
import 'brace/theme/github';
import {observer} from "mobx-react/index";

const ChatSendBox = observer(class ChatSendBox extends Component {

    stream = this.props.stream;

    onChange = value => {
        this.stream.draft = value;
        this.stream.check();
    };

    render() {
        const stream = this.stream;
        return (
            <div className="ChatSendBox">
                <ChatToolBar stream={stream}/>
                <AceEditor
                    focus
                    mode="json"
                    theme="github"
                    showGutter={false}
                    width="100%"
                    height="170px"
                    fontSize={12}
                    name="ChatSendBox-Ace"
                    value={stream.draft}
                    onChange={this.onChange}
                    editorProps={{$blockScrolling: Infinity}}
                    // onChange={this.onChange}
                    // setOptions={{s
                    //     enableBasicAutocompletion: false,
                    //     enableLiveAutocompletion: false,
                    //     enableSnippets: false,
                    //     showLineNumbers: false,
                    //     tabSize: 2,
                />
            </div>
        );
    }
});

export default ChatSendBox;