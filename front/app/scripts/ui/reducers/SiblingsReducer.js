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
    ReducersMap = require('./ReducersMap');

const reducers = new ReducersMap(getInitialState())
    .add({type: 'setSearchRequest', entity: 'siblings'},
        (state, action) => {
            return Utils.merge(state, {searchRequest: action.request});
        })
    .add({type: 'ajaxStarted', entity: 'siblings', operation: 'findAll'},
        (state, action) => {
            return Utils.merge(state, {_loading: true});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'siblings', operation: 'findAll'},
        (state, action) => {
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
        });

function getInitialState() {
    return {
        entities: [],
        searchRequest: {
            text: ''
        },
        totalRecords: 0,
        tableMessages: [],
        selection: {
            student: null
        }
    };
}

function SiblingsReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = SiblingsReducer;
