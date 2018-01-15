'use strict';

const React = require('react'),
    {connect} = require('react-redux'),
    AuthService = require('../services/AuthService'),
    Permissions = require('../Permissions'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const Users = React.createClass({

    render(){
        const self = this, p = self.props;

        return <ListScreen entity='users'
                           ajaxResource={Actions.ajax.users}
                           entityModifyPermssion={Permissions.manageUsers}
                           entityRouteFn={Navigator.routes.user}
                           pageTitle={Utils.message('common.pages.users')}
                           filterElementFn={self.renderFilterElement}
                           entitiesTblCols={self.columns()}
                           {...p}
        />
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;

        return <div className="row row-compact">
            <div className="col-compact col-xs-12">
                <div className="form-group form-group-compact">
                    <TextInput id="searchText"
                               name="searchText"
                               placeholder={Utils.message('common.search.text')}
                               defaultValue={p.searchRequest.text}
                               onChange={(text)=> {
                                   onSearchRequestChange({text})
                               }}/>
                </div>
            </div>
        </div>
    },

    columns(){
        return [
            {
                headerCell: {
                    children: Utils.message('common.users.search.table.id')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.id
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.users.search.table.name')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.name
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.users.search.table.roles')
                },
                bodyCell: {
                    childrenFn: (obj)=> Utils.joinDefined(obj.roles.map(function(obj) {return obj.id}), ', ')
                }
            }
        ]
    }
});

function mapStateToProps(state) {
    return state.pages.Users
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Users);
