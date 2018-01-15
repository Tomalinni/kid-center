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
    {connect} = require('react-redux'),
    {Link} = require('react-router'),
    Navigator = require('../Navigator'),
    Config = require('../Config'),
    Utils = require('../Utils'),
    Icons = require('./Icons'),
    Actions = require('../actions/Actions'),
    AuthService = require('../services/AuthService');

const NavSidebar = React.createClass({

    render() {
        const self = this, p = this.props;

        return (
            <div className={self.getSidebarClass()}
                 onClick={self.hide}>
                <div className="sidebar-nav-curtain"
                     onClick={self.hide}>&nbsp;</div>
                <div className="sidebar-nav-wrapper">
                    <ul className="sidebar-nav">
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
                        {self.renderMenuItem('preferences', 'studentCardPaymentPrefModify')}
                    </ul>
                </div>
            </div>
        )
    },

    renderMenuItem(pageId, pagePermission, inlineComponent){
        pagePermission = pagePermission === undefined ? pageId + 'Read' : pagePermission;
        if (pagePermission === null || AuthService.hasPermission(pagePermission)) {
            return <li key={pageId}>
                <Link to={Navigator.routes.byPageId(pageId)}
                      onClick={self.hide}>
                    {Utils.pageTitle(pageId)}
                    {Utils.select(inlineComponent, inlineComponent)}
                </Link>
            </li>
        }
    },

    renderLogOuBtn(){
        const self = this, p = this.props;
        return <button type="button"
                       className="btn btn-default btn-lg btn-sidebar-nav pull-right"
                       onClick={Utils.invokeAndPreventDefaultFactory(self.logOut)}>
            {Icons.glyph.logOut()}
        </button>
    },

    hide(){
        const self = this, p = this.props;
        p.dispatch(Actions.toggleNavSidebar(false))
    },

    logOut(){
        this.hide();
        AuthService.logOut();
    },

    getSidebarClass(){
        return Utils.joinDefined(['sidebar-nav-container', this.props.navSidebarOpened ? 'opened' : null]);
    }
});

function mapStateToProps(state) {
    return state.common;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(NavSidebar);
