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
    ReducersMap = require('./ReducersMap');

const formEntities = ['regRelatives', 'regStudents'];

const reducers = new ReducersMap(getInitialState())

    .add({type: 'initEntity', entity: 'regRelatives'},
        (state, action) => {
            return Utils.extend(state, {
                regRelative: {},
            });
        })
    .add({type: 'setEntityValue', entity: 'regRelatives'},
        (state, action) => {
            const regRelative = Utils.extend(state.regRelative, {[action.fieldId]: action.newValue}),
                nextState = {
                    regRelative: regRelative,
                    validationMessages: {regRelatives: Validators.regRelatives(regRelative, action.fieldId)}
                };

            return Utils.merge(state, nextState);
        })
    .add({type: 'setEntityValue', entity: 'regStudents'},
        (state, action) => {
            const index = state.selectedStudentIndex,
                students = state.regRelative.students,
                studentToChange = index >= 0 && students && students[index];

            if (studentToChange) {
                let newStudent = Utils.extend(studentToChange, {[action.fieldId]: action.newValue});
                let newStudents = [].concat(students);
                newStudents.splice(index, 1, newStudent);

                const studentsMessages = {},
                    studentMessage = Validators.regStudents(newStudent, action.fieldId);
                studentsMessages[index] = studentMessage;


                const newRelative = Utils.merge(state.student, {students: newStudents}),
                    nextState = {
                        regRelative: newRelative,
                        validationMessages: {regStudents: studentsMessages}
                    };

                return Utils.merge(state, nextState);
            }
            return state;
        })
    .add({type: 'setValidationMessages'},
        (state, action) => {
            if (Utils.arr.contains(formEntities, action.entity)) {
                return Utils.merge(state, {validationMessages: {[action.entity]: action.messages}});
            }
            return state;
        })
    .add({type: 'clearValidationMessages', entity: 'regRelatives'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {regRelatives: {}, regStudents: {}}});
        })
    .add({type: 'setPageMode', entity: 'regRelatives', page: 'register', fieldId: 'step'},
        (state, action) => {
            return Utils.extend(state, {pageMode: action.mode});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'regRelatives', operation: 'verifyMobileNumber'},
        (state, action) => {
            return Utils.merge(state, {
                regRelative: {
                    confirmationId: action.response.data
                }
            });
        })
    .add({type: 'addRegRelativeChild'},
        (state, action) => {
            return Utils.merge(state, {
                regRelative: {
                    students: arr => Utils.arr.push(arr, {})
                }
            });
        })
    .add({type: 'removeRegRelativeChild'},
        (state, action) => {
            let index = state.selectedStudentIndex;
            if (state.regRelative.students.length > 1 && index >= 0 && index < state.regRelative.students.length) {
                return Utils.merge(state, {
                    regRelative: {
                        students: arr => Utils.arr.removeAt(arr, index)
                    },
                    selectedStudentIndex: index - 1,
                    validationMessages: {
                        regStudents: {
                            [index]: null
                        }
                    }
                });
            }
            return state;
        })
    .add({type: 'selectRegRelativeChild'},
        (state, action) => {
            return Utils.merge(state, {
                selectedStudentIndex: action.index,
            });
        });

function getInitialState() {
    return {
        regRelative: {
            students: [
                {}
            ]
        },
        validationMessages: {
            regRelatives: {},
            regStudents: {}
        },
        selectedStudentIndex: 0
    };
}

function RegisterFormReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = RegisterFormReducer;
