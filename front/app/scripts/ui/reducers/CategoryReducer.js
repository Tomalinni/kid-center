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
    .add({type: 'ajaxFinishedSuccess', entity: 'categories', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {category: action.response});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'categories', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                category: Utils.extend(state.category, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'categories'},
        (state, action) => {
            return Utils.extend(state, {category: {}});
        })
    .add({type: 'setEntityValue', entity: 'categories'},
        (state, action) => {
            let category = Utils.extend(state.category, {[action.fieldId]: action.newValue});
            return Utils.merge(state, {
                category: category,
                validationMessages: {categories: Validators.categories(category, action.fieldId)}
            });
        })
    .add({type: 'setValidationMessages', entity: 'categories'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {categories: action.messages}});
        })
    .add({type: 'setPageMode', entity: 'categories', page: 'form', fieldId: 'parent'},
        (state, action) => {
            return Utils.extend(state, {pageMode: action.mode});
        });

function getInitialState() {
    return {
        category: {},
        pageMode: 'edit',
        validationMessages: {
            categories: {}
        }
    };
}

function categoryReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('categories'), categoryReducer);
