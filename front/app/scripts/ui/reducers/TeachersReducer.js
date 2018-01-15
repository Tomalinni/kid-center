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
    Utils = require('../Utils');

function getInitialState() {
    return {
        entities: [],
        searchRequest: {
            text: '',
            firstRecord: 1
        },
        totalRecords: 0,
        selectedObj: null,
        tableMessages: []
    };
}

function TeachersReducer(state = getInitialState(), action) {
    if (action.type === 'ajaxStarted' && action.entity === 'teachers' && action.operation === 'findAll') {
        return Utils.merge(state, {searchRequest: action.request, _loading: true});
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'teachers' && action.operation === 'findAll') {
        const entities = action.request.appendResults ?
            Utils.mergeArraysByObjectKey(state.entities, action.response.results, 'id') :
            action.response.results;

        const nextState = {
            _loading: false,
            entities: entities,
            searchRequest: action.request,
            totalRecords: action.response.total
        };
        return Utils.merge(state, nextState);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'teachers' && action.operation === 'delete') {
        const nextState = {
            entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
            totalRecords: state.totalRecords - 1
        };
        return Utils.merge(state, nextState);
    } else if (action.type === 'toggleSelectedObject' && action.entity === 'teachers') {
        const nextState = {
            selectedObj: action.obj
        };
        return Utils.merge(state, nextState);
    }


    return state;
}

module.exports = TeachersReducer;
