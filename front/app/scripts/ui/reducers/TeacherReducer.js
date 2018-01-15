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
    .add({type: 'ajaxFinishedSuccess', entity: 'teachers', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {teacher: action.response});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'teachers', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                teacher: Utils.extend(state.teacher, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'teachers'},
        (state, action) => {
            return Utils.extend(state, {teacher: {}});
        })
    .add({type: 'setEntityValue', entity: 'teachers'},
        (state, action) => {
            const teacher = Utils.extend(state.teacher, {[action.fieldId]: action.newValue});
            return Utils.merge(state, {
                teacher: teacher,
                validationMessages: {teachers: Validators.teachers(teacher, action.fieldId)}
            });
        })
    .add({type: 'setValidationMessages', entity: 'teachers'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {teachers: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'teachers'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {teachers: {}}});
        });

function getInitialState() {
    return {
        teacher: {},
        validationMessages: {
            teachers: {}
        }
    };
}

function TeacherReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('teachers'), TeacherReducer);
