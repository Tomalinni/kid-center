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
    Utils = require('../Utils'),
    Icons = require('./Icons'),
    Actions = require('../actions/Actions'),
    NavSidebar = require('./NavSidebar'),
    AuthService = require('../services/AuthService');

const PageToolbar = React.createClass({

    getDefaultProps: function () {
        return {
            hasOptionsBtn: true
        };
    },

    render() {
        const self = this, p = this.props;

        return (
            <div className="row row-compact nav-toolbar">
                <NavSidebar/>
                <div className="col-compact col-xs-10">
                    {self.renderBtnArray(p.leftButtons)}
                </div>
                <div className="col-compact col-xs-2">
                    {self.renderOptionsBtn()}
                    {self.renderBtnArray(p.rightButtons)}
                </div>
            </div>
        )
    },

    renderBtnArray(btns){
        return Utils.isArray(btns) ? btns.filter(Utils.obj.self) : null
    },

    renderOptionsBtn(){
        const self = this, p = self.props;

        if (p.hasOptionsBtn) {
            return <button type="button"
                           className="btn btn-default btn-lg nav-toolbar-btn pull-right"
                           onClick={() => p.dispatch(Actions.toggleNavSidebar(true))}>
                {Icons.glyph.optionVertical()}
            </button>
        }
    }
});

function mapStateToProps(state) {
    return state.common;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(PageToolbar);
