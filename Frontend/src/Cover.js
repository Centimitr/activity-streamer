import React, {Component} from 'react';

import './style/Cover.css';

export default class Cover extends Component {
    render() {
        return (
            <div className="Cover">
                <div className="Cover-Title">{this.props.title}</div>
                <div className="Cover-Description">{this.props.description}</div>
            </div>
        );
    }
}