const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    Validators = require('../Validators'),
    AuthService = require('../services/AuthService'),
    ReducersMap = require('./ReducersMap');

const reducers = new ReducersMap(getInitialState())
    .add({type: 'initEntity', entity: 'profile'},
        (state, action) => {
            return Utils.extend(state, {
                profile: {name: AuthService.subject},
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'profile', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                profile: Utils.extend(state.profile, action.response, {newPassRepeat: ''})
            });
        })
    .add({type: 'setEntityValue', entity: 'profile'},
        (state, action) => {
            let profile = Utils.extend(state.profile, {[action.fieldId]: action.newValue});
            const nextState = {
                profile: profile,
                screen: profile,
                validationMessages: {profile: Validators.profile(profile, action.fieldId)}
            };

            return Utils.merge(state, nextState);
        })
    .add({type: 'setValidationMessages', entity: 'profile'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {profile: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'profile'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {profile: {}}});
        });

function getInitialState() {
    return {
        profile: {},
        validationMessages: {
            profile: {}
        }
    };
}

function ProfileReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = ProfileReducer;