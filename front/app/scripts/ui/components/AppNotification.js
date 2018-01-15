/*
 * (C) Copyright ${YEAR} Legohuman (https://github.com/Legohuman).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

const React = require('react'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    DataService = require('../services/DataService');

const AppNotification = React.createClass({
    timeoutRef: null,

    getInitialState(){
        return {
            errorMessage: null
        }
    },

    componentDidMount(){
        DataService.addAfterRequestFailedListener(this.afterRequestFailed)
    },

    afterRequestFailed(error){
        const self = this;
        if (error) {
            self.setState({errorMessage: Utils.message(error.text, error.params)});
            self.clearFadeTimeout();
            self.timeoutRef = setTimeout(() => {
                self.setState({errorMessage: null})
            }, Config.notificationFadeTimeMs)
        }
    },

    componentWillUnmount(){
        DataService.removeAfterRequestFailedListener(this.afterRequestFailed);
        this.clearFadeTimeout()
    },

    render() {
        const self = this, p = self.props, s = self.state;

        if (s.errorMessage) {
            return <div className="app-notification alert-compact alert-warning" role="alert"> {s.errorMessage} </div>
        } else {
            return <div></div>
        }
    },

    clearFadeTimeout(){
        if (this.timeoutRef) {
            clearTimeout(this.timeoutRef);
            this.timeoutRef = null
        }
    }
});

module.exports = AppNotification;
