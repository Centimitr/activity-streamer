import React, {Component} from 'react';
import './style/App.css';
import Chat from "./Chat/Chat";
import Cover from "./Cover";
import {observer} from "mobx-react";

const App = observer(class App extends Component {

    render() {
        const stream = this.props.stream;
        return (
            <div className="App">
                {stream.showLoading ?
                    (<div className="App-Startup-Background"/>) : null}
                {stream.showLoading ?
                    (<Cover title="Loading..." description=""/>) : null}
                {stream.showRegister ? <Cover title="Registering..."
                                              description={`Registering an account using username: ${stream.username}`}/> : null}
                {stream.showLogIn && !stream.reconnectFlag ? <Cover title="Logging in..."
                                                                    description={`Logging in with username: ${stream.username}, secret: ${stream.secret}`}/> : null}
                {stream.showLogOut ? <Cover title="Logged out." description=""/> : null}
                <Chat stream={stream}/>
            </div>
        );
    }
});

export default App;
