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
    .add({type: 'ajaxFinishedSuccess', entity: 'users', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {user: action.response});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'users', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                user: Utils.extend(state.user, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'users'},
        (state, action) => {
            return Utils.extend(state, {user: {}});
        })
    .add({type: 'setEntityValue', entity: 'users'},
        (state, action) => {
            let user = Utils.extend(state.user, {[action.fieldId]: action.newValue});
            if (action.fieldId == "pass") {
                user.passChanged = Utils.isNotBlankString(action.newValue) && action.newValue != user._oldPass;
            }
            return Utils.merge(state, {
                user: user,
                validationMessages: {users: Validators.users(user, action.fieldId)}
            });
        })
    .add({type: 'setValidationMessages', entity: 'users'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {users: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'users'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {users: {}}});
        });

function getInitialState() {
    return {
        user: {},
        validationMessages: {
            users: {}
        }
    };
}

function UserReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('users'), UserReducer);
