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
        _loading: false,
        entities: [],
        selectedObj: null
    };
}

function CitiesReducer(state = getInitialState(), action) {
    if (action.type === 'toggleSelectedObject' && action.entity === 'cities') {
        return Utils.extend(state, {
            selectedObj: action.obj
        });
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'cities' && action.operation === 'save' && state.selectedObj) {
        return Utils.extend(state, {
            selectedObj: action.response

        });
    } else if (action.type === 'ajaxStarted' && action.entity === 'cities' && action.operation === 'findAll') {
        return Utils.extend(state, {_loading: true})
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'cities' && action.operation === 'findAll') {
        const entities = action.response.results;

        const nextState = {
                _loading: false,
                entities: entities
            };

        return Utils.extend(state, nextState);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'cities' && action.operation === 'delete') {
        const nextState = {
            entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
            totalRecords: state.totalRecords - 1,
            selectedObj: null
        };
        return Utils.extend(state, nextState);
    }

    return state;
}

module.exports = CitiesReducer;
