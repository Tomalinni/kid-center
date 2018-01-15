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
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCards', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {
                studentCard: action.response
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCards', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                studentCard: Utils.extend(state.studentCard, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'studentCards'},
        (state, action) => {
            return Utils.extend(state, {
                studentCard: {
                    purchaseDate: Utils.momentToString(Utils.currentMoment())
                },
            });
        })
    .add({type: 'setEntityValue', entity: 'studentCards'},
        (state, action) => {
            const studentCard = Utils.extend(state.studentCard, {[action.fieldId]: action.newValue}),
                nextState = {
                    studentCard: studentCard,
                    validationMessages: {studentCards: Validators.studentCards(studentCard, action.fieldId)}
                };
            return Utils.merge(state, nextState);
        })
    .add({type: 'setValidationMessages', entity: 'studentCards'},
        (state, action) => {
            return Utils.merge(state, {validationMessages: {studentCards: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'studentCards'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {studentCards: {}}});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCards', operation: 'findPhotos'},
        (state, action) => {
            const photos = action.response.names;
            return Utils.merge(state, {cardPhotos: photos, shownPhoto: photos[0] || ""});
        })
    .add({type: 'filesUploaded', entity: 'studentCards'},
        (state, action) => {
            const originalPhotos = state.cardPhotos,
                nextPhotos = Utils.pushNew(originalPhotos, action.fileNames);
            return Utils.merge(state, {cardPhotos: nextPhotos, shownPhoto: action.fileNames[0]});
        })
    .add({type: 'changeShownFile', entity: 'studentCards'},
        (state, action) => {
            return Utils.merge(state, {shownPhoto: action.fileName});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCards', operation: 'removeFile'},
        (state, action) => {
            const nextPhotos = Utils.removeAndCopy(state.cardPhotos, action.request.fileName);
            return Utils.merge(state, {cardPhotos: nextPhotos, shownPhoto: nextPhotos[0] || ""});
        });
function getInitialState() {
    return {
        studentCard: {},
        validationMessages: {
            studentCards: {}
        },
        cardPhotos: []
    };
}

function StudentCardReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('studentCards'), StudentCardReducer);
