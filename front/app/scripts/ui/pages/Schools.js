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
    Permissions = require('../Permissions'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    ListScreen = require('../components/ListScreen');

const Schools = React.createClass({
    render(){
        const self = this, p = self.props;

        return <ListScreen entity='schools'
                           ajaxResource={Actions.ajax.schools}
                           entityModifyPermssion={Permissions.paymentsModify}
                           entityRouteFn={Navigator.routes.school}
                           pageTitle={Utils.message('common.pages.schools')}
                           filterElementFn={self.renderFilterElement}
                           entitiesTblCols={self.columns()}
                           {...p}
        />
    },

    renderFilterElement(){
        return null
    },

    columns(){
        return [
            {
                headerCell: {
                    children: Utils.message('common.schools.search.table.name')
                },
                bodyCell: {
                    childrenFn: obj => obj.name
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.schools.search.table.external')
                },
                bodyCell: {
                    childrenFn: obj => Renderers.bool(obj.external, Utils.message('common.schools.search.table.external.true'), Utils.message('common.schools.search.table.external.false'))
                }
            }
        ]
    }
});

function mapStateToProps(state) {
    return state.pages.Schools;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Schools);
