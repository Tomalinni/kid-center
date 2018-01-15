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
    ReducersMap = require('./ReducersMap');

function getInitialState() {
    return {
        entities: [],
        searchRequest: {
            firstRecord: 1
        },
        totalRecords: 0,
        selectedObj: null,
        tableMessages: []
    };
}


const reducers = new ReducersMap(getInitialState())
    .add({type: 'setSearchRequest', entity: 'lessonTemplates'},
        (state, action) => {
            return Utils.merge(state, {searchRequest: action.request});
        })
    .add({type: 'ajaxStarted', entity: 'lessonTemplates', operation: 'findAll'},
        (state, action) => {
            return Utils.merge(state, {_loading: true});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'lessonTemplates', operation: 'findAll'},
        (state, action) => {
            const entities = action.request.appendResults ?
                Utils.mergeArraysByObjectKey(state.entities, action.response.results, 'id') :
                action.response.results;

            return Utils.merge(state, {
                _loading: false,
                entities: entities,
                totalRecords: action.response.total
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'lessonTemplates', operation: 'delete'},
        (state, action) => {
            return Utils.merge(state, {
                entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
                totalRecords: state.totalRecords - 1
            });
        })
    .add({type: 'toggleSelectedObject', entity: 'lessonTemplates'},
        (state, action) => {
            return Utils.merge(state, {
                selectedObj: action.obj
            });
        });

function LessonTemplatesReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = LessonTemplatesReducer;
