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
            activeState: 'active',
            visitType: 'all',
            firstRecord: 1
        },
        detailsCollapsed: true,
        totalRecords: 0,
        selectedObj: null,
        tableMessages: []
    };
}

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxStarted', entity: 'cards', operation: 'findAll'}, (state, action) => {
        return Utils.merge(state, {searchRequest: action.request, _loading: true});
    })
    .add({type: 'ajaxFinishedSuccess', entity: 'cards', operation: 'findAll'}, (state, action) => {
        const nextState = {
            _loading: false,
            entities: action.response.results,
            searchRequest: action.request,
            totalRecords: action.response.total
        };
        return Utils.merge(state, nextState);
    })
    .add({type: 'ajaxStarted', entity: 'cards', operation: 'delete'}, (state, action) => {
        const nextState = {
            entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
            totalRecords: state.totalRecords - 1
        };
        return Utils.merge(state, nextState);
    })
    .add({type: 'toggleSelectedObject', entity: 'cards'}, (state, action) => {
        const nextState = {
            selectedObj: action.obj
        };
        return Utils.merge(state, nextState);
    })
    .add({type: 'setPageMode', entity: 'cards', page: 'list', fieldId: 'card'},
        (state, action) => {
            return Utils.merge(state, {detailsCollapsed: !state.detailsCollapsed});
        });

function CardsReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = CardsReducer;
