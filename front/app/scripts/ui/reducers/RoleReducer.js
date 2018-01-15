const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    Validators = require('../Validators'),
    EntityLifeCycleReducerFactory = require('./EntityLifeCycleReducerFactory'),
    ReducersMap = require('./ReducersMap');

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'roles', operation: 'findOne'},
        (state, action) => {
            const role = Utils.extend(action.response, {_oldPass: action.response.pass});
            role.permissions = role.permissions.map(function (obj) {
                return Dictionaries.permissions.byId(obj)
            });

            return Utils.extend(state, {role: role});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'roles', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                role: Utils.extend(state.role, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'roles'},
        (state, action) => {
            return Utils.extend(state, {role: {}});
        })
    .add({type: 'setEntityValue', entity: 'roles'},
        (state, action) => {
            let role = Utils.extend(state.role);

            if (action.fieldId == "permissions") {
                role[action.fieldId] = action.newValue.map(function (obj) {
                    return obj.id
                });
            } else {
                role[action.fieldId] = action.newValue;
            }
            return Utils.merge(state, {
                role: role,
                validationMessages: {roles: Validators.roles(role, action.fieldId)}
            });
        })
    .add({type: 'setValidationMessages', entity: 'roles'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {roles: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'roles'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {roles: {}}});
        });

function getInitialState() {
    return {
        role: {},
        validationMessages: {
            roles: {}
        }
    };
}

function RoleReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('roles'), RoleReducer);
