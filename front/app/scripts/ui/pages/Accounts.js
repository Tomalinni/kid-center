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
    Renderers = require('../Renderers'),
    Navigator = require('../Navigator'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    ListScreen = require('../components/ListScreen');

const Accounts = React.createClass({
    render(){
        const self = this, p = self.props;

        return <ListScreen entity='accounts'
                           ajaxResource={Actions.ajax.accounts}
                           entityModifyPermssion={Permissions.paymentsModify}
                           entityRouteFn={Navigator.routes.account}
                           pageTitle={Utils.message('common.pages.accounts')}
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
                width: '30px',
                headerCell: {
                    children: Utils.message('common.accounts.search.table.type.abbr')
                },
                bodyCell: {
                    childrenFn: Renderers.account.typeAbbr
                }
            },
            {
                width: '60px',
                headerCell: {
                    children: Utils.message('common.accounts.search.table.school')
                },
                bodyCell: {
                    childrenFn: (obj)=> Renderers.arr(obj.schools, ', ', Renderers.school.name)
                }
            },
            {
                width: '40px',
                headerCell: {
                    children: Utils.message('common.accounts.search.table.city')
                },
                bodyCell: {
                    childrenFn: (obj)=> Renderers.city(obj.city)
                }
            },
            {
                width: '40px',
                headerCell: {
                    children: Utils.message('common.accounts.search.table.bank')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.bank
                }
            },
            {
                width: '60px',
                headerCell: {
                    children: Utils.message('common.accounts.search.table.department')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.department
                }
            },
            {
                width: '60px',
                headerCell: {
                    children: Utils.message('common.accounts.search.table.owner')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.ownerAbbr
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.accounts.search.table.number')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.type === 'cashless' ? obj.number : obj.login
                }
            }
        ]
    }
});

function mapStateToProps(state) {
    return state.pages.Accounts;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Accounts);
