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
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    Validators = require('../Validators'),
    AuthService = require('../services/AuthService'),
    ReducersMap = require('./ReducersMap');

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'preference', operation: 'get'},
        (state, action) => {
            return Utils.extend(state, {
                preference: Utils.extend(state.preference, action.response)
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'preference', operation: 'set'},
        (state, action) => {
            return Utils.extend(state, {
                preference: Utils.extend(state.preference, action.response)
            });
        })
    .add({type: 'setEntityValue', entity: 'preference'},
        (state, action) => {
            return Utils.merge(state, {
                preference: Utils.extend(state.preference, {[action.fieldId]: action.newValue})
            });
        });


function getInitialState() {
    return {
        preference: {}
    };
}

function ProfileReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = ProfileReducer;