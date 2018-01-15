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
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCalls', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {studentCall: action.response});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCalls', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                studentCall: Utils.extend(state.studentCall, action.response)
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'students', operation: 'findOne'},
        (state, action) => {
            const student = action.response;
            return Utils.merge(state, {studentCall: {student}});
        })
    .add({type: 'initEntity', entity: 'studentCalls'},
        (state, action) => {
            return Utils.extend(state, {
                studentCall: {
                    date: Utils.momentToDateTimeString(Utils.currentMoment().round(10, 'minutes'))
                },
            });
        })
    .add({type: 'setEntityValue', entity: 'studentCalls'},
        (state, action) => {
            const studentCall = Utils.extend(state.studentCall, {[action.fieldId]: action.newValue}),
                nextState = {
                    studentCall: studentCall,
                    validationMessages: {studentCalls: Validators.studentCalls(studentCall, action.fieldId)}
                };
            return Utils.merge(state, nextState);
        })
    .add({type: 'setValidationMessages', entity: 'studentCalls'},
        (state, action) => {
            return Utils.merge(state, {validationMessages: {studentCalls: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'studentCalls'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {studentCalls: {}}});
        });

function getInitialState() {
    return {
        studentCall: {},
        validationMessages: {
            studentCalls: {}
        }
    };
}

function StudentCallReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('studentCalls'), StudentCallReducer);
