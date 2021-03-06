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
    Validators = require('../Validators'),
    Actions = require('../actions/Actions'),
    EntityLifeCycleReducerFactory = require('./EntityLifeCycleReducerFactory'),
    ReducersMap = require('./ReducersMap');

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'schools', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {school: action.response});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'schools', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                school: Utils.extend(state.school, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'schools'},
        (state, action) => {
            return Utils.extend(state, {school: {}});
        })
    .add({type: 'setEntityValue', entity: 'schools'},
        (state, action) => {
            let school = Utils.extend(state.school, {[action.fieldId]: action.newValue});
            return Utils.merge(state, {
                school: school,
                validationMessages: {schools: Validators.schools(school, action.fieldId)}
            });
        })
    .add({type: 'setValidationMessages', entity: 'schools'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {schools: action.messages}});
        });

function getInitialState() {
    return {
        school: {},
        validationMessages: {
            schools: {},
        }
    };
}

function SchoolReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('schools'), SchoolReducer);
