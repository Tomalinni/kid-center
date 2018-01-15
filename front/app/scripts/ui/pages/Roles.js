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

const Roles = React.createClass({

    render(){
        const self = this, p = self.props;

        return <ListScreen entity='roles'
                           ajaxResource={Actions.ajax.roles}
                           entityModifyPermssion={Permissions.manageUsers}
                           entityRouteFn={Navigator.routes.role}
                           pageTitle={Utils.message('common.pages.roles')}
                           entitiesTblCols={self.columns()}
                           infoPanel={self.renderPermissions()}
                           {...p}
        />
    },

    renderPermissions() {
        const self = this, p = self.props;
        if (p.selectedObj) {
            return <div> {
                p.selectedObj.permissions.map(permissionId =>
                    <div key={permissionId}>{Dictionaries.permissions.byId(permissionId).name}</div>)}
            </div>
        }
        return <div>{Utils.message('common.roles.role.not.selected')}</div>;
    },

    columns(){
        return [
            {
                headerCell: {
                    children: Utils.message('common.roles.search.table.id')
                },
                bodyCell: {
                    childrenFn: (obj) => obj.id
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.roles.search.table.permissions')
                }
            }
        ]
    }
});

function mapStateToProps(state) {
    return state.pages.Roles
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Roles);
