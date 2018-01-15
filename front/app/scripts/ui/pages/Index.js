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
    {Link} = require('react-router'),
    Navigator = require('../Navigator'),
    Utils = require('../Utils'),
    Icons = require('../components/Icons'),
    AuthService = require('../services/AuthService');

const Index = React.createClass({

    render() {
        const self = this, p = this.props;

        return (
            <div>
                <ul className="sidebar-nav nav-index">
                    {self.renderMenuItem('profile', null, self.renderLogOuBtn())}
                    {self.renderMenuItem('lessons')}
                    {self.renderMenuItem('children', 'hasChildren')}
                    {self.renderMenuItem('students')}
                    {self.renderMenuItem('studentCalls', 'studentsRead')}
                    {self.renderMenuItem('cards')}
                    {self.renderMenuItem('teachers')}
                    {self.renderMenuItem('payments')}
                    {self.renderMenuItem('cities', 'paymentsRead')}
                    {self.renderMenuItem('schools', 'paymentsRead')}
                    {self.renderMenuItem('accounts', 'paymentsRead')}
                    {self.renderMenuItem('categories', 'paymentsRead')}
                    {self.renderMenuItem('users', 'manageUsers')}
                    {self.renderMenuItem('roles', 'manageUsers')}
                    {self.renderMenuItem('homeworks', 'homeworkRead')}
                </ul>
            </div>
        )
    },

    renderMenuItem(pageId, pagePermission, inlineComponent){
        pagePermission = pagePermission === undefined ? pageId + 'Read' : pagePermission;
        if (pagePermission === null || AuthService.hasPermission(pagePermission)) {
            return <li key={pageId}>
                <Link to={Navigator.routes.byPageId(pageId)}>
                    {Utils.pageTitle(pageId)}
                    {Utils.select(inlineComponent, inlineComponent)}
                </Link>
            </li>
        }
    },

    renderLogOuBtn(){
        const self = this, p = this.props;
        return <button type="button"
                       className="btn btn-default btn-lg btn-sidebar-nav nav-index pull-right"
                       onClick={Utils.invokeAndPreventDefaultFactory(self.logOut)}>
            {Icons.glyph.logOut()}
        </button>
    },

    logOut(){
        AuthService.logOut();
    },
});

module.exports = Index;
