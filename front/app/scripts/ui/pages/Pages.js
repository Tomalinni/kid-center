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
    AuthService = require('../services/AuthService'),
    Utils = require('../Utils');

function pageFactory(component, permission) {
    return rememberScroll(securedPage(component, permission))
}

function rememberScroll(component) {
    let scrollY = 0; //specific for every route

    return React.createClass({
        componentDidMount(){
            window.scrollTo(0, scrollY)
        },

        componentWillUnmount(){
            scrollY = window.scrollY
        },

        render() {
            return React.createElement(component, this.props)
        }
    })
}

function securedPage(component, permission) {
    return React.createClass({
        render() {
            if (AuthService.hasPermission(permission)) {
                return React.createElement(component, this.props)
            } else {
                return <div className="single-page-message">{Utils.message('common.pages.no.access')}</div>
            }
        }
    })
}

function securePageEnterCb(store) {
    return AuthService.onSecurePageEnterFactory(store)
}

module.exports = {pageFactory, securePageEnterCb};
