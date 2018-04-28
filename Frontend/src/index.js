import React from 'react';
import ReactDOM from 'react-dom';
import './style/index.css';
import App from './App';
import registerServiceWorker from './registerServiceWorker';
import stream from "./Stream/Stream";

ReactDOM.render(<App stream={stream}/>, document.getElementById('root'));
registerServiceWorker();
