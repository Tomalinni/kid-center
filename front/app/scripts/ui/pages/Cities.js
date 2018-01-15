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
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    ListScreen = require('../components/ListScreen');

const Cities = React.createClass({
    render(){
        const self = this, p = self.props;

        return <ListScreen entity='cities'
                           ajaxResource={Actions.ajax.cities}
                           entityModifyPermssion={Permissions.paymentsModify}
                           entityRouteFn={Navigator.routes.city}
                           pageTitle={Utils.message('common.pages.cities')}
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
                    children: Utils.message('common.cities.search.table.name')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.name
                }
            }
        ]
    }
});

function mapStateToProps(state) {
    return state.pages.Cities;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Cities);
