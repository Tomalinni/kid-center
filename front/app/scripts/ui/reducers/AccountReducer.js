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
    .add({type: 'ajaxFinishedSuccess', entity: 'accounts', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {account: action.response});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'accounts', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                account: Utils.extend(state.account, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'accounts'},
        (state, action) => {
            return Utils.extend(state, {account: {}});
        })
    .add({type: 'setEntityValue', entity: 'accounts'},
        (state, action) => {
            let account = Utils.extend(state.account, {[action.fieldId]: action.newValue});
            return Utils.merge(state, {
                account: account,
                validationMessages: {accounts: Validators.accounts(account, action.fieldId)}
            });
        })
    .add({type: 'setValidationMessages', entity: 'accounts'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {accounts: action.messages}});
        });

function getInitialState() {
    return {
        account: {},
        validationMessages: {
            accounts: {},
        }
    };
}

function accountReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('accounts'), accountReducer);
