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
        searchRequest: {
            searchMethod: 'filters',
            direction: 'all',
            source: {
                schoolId: null,
                accountIds: null
            },
            target: {
                schoolId: null,
                accountIds: null
            },
            categoryIds: null,
            useInnerCategories: false,
            innerCategoryIds: [],
            period: 'month',
            groupBy: 'category3',
            firstRecord: 1,
            appendResults: false
        },
        view: 'list',
        directionRequestParts: {}, // {incoming: {source:{...}, target{...}}, outgoing:... ,...}
        outdatedChart: false,
        statChart: {},
        balanceItems: [], //{accountId, schoolId, startBalance, endBalance, income, expense}
        totalRecords: 0,
        selectedObj: null,
        viewPhotos: false,
        tableMessages: []
    };
}

function PaymentsReducer(state = getInitialState(), action) {
    if (action.type === 'setPaymentsFilter') {
        const searchRequest = action.searchRequest,
            tableMessages = [];
        if (searchRequest.direction === 'outgoing' && !searchRequest.source.schoolId) {
            tableMessages.push(Utils.message('common.payments.search.table.select.source.school'))
        }
        if (searchRequest.direction === 'incoming' && !searchRequest.target.schoolId) {
            tableMessages.push(Utils.message('common.payments.search.table.select.target.school'))
        }
        if (Utils.isEmptyArray(searchRequest.useInnerCategories ? searchRequest.innerCategoryIds : searchRequest.categoryIds)) {
            tableMessages.push(Utils.message('common.payments.search.table.select.categories'))
        }

        const nextState = {
            searchRequest: searchRequest,
            selectedObj: null,
            tableMessages: tableMessages,
            outdatedChart: true
        };
        if (!Utils.isEmptyArray(tableMessages)) {
            nextState.entities = [];
        }

        const mergedState = Utils.merge(state, nextState);

        if (searchRequest.direction !== state.searchRequest.direction) {
            mergedState.directionRequestParts = Utils.merge({}, state.directionRequestParts);
            mergedState.directionRequestParts[state.searchRequest.direction] = {
                source: state.searchRequest.source,
                target: state.searchRequest.target
            };

            const nextDirectionPart = state.directionRequestParts[searchRequest.direction];
            mergedState.searchRequest.source = nextDirectionPart && nextDirectionPart.source || {
                    schoolId: null,
                    accountIds: null
                };
            mergedState.searchRequest.target = nextDirectionPart && nextDirectionPart.target || {
                    schoolId: null,
                    accountIds: null
                };
        }
        return mergedState;
    } else if (action.type === 'setPaymentsView') {
        return Utils.extend(state, {view: action.view});
    } else if (action.type === 'toggleSelectedObject' && action.entity === 'payments') {
        return Utils.extend(state, {
            selectedObj: action.obj
        });
    } else if (action.type === 'ajaxStarted' && action.entity === 'payments' && action.operation === 'listPhotos') {
        return Utils.extend(state, {
            selectedObj: action.context,
            photosOwnerField: action.request.fieldId,
            photosLoading: true
        });
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'payments' && action.operation === 'listPhotos') {
        return Utils.extend(state, {
            photosLoading: false,
            shownPhotos: action.response.names
        });
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'payments' && action.operation === 'save' && state.selectedObj) {
        return Utils.extend(state, {
            selectedObj: action.response

        });
    } else if (action.type === 'hidePaymentPhotos') {
        return Utils.extend(state, {
            shownPhotos: null,
            photosOwnerField: null
        });
    } else if (action.type === 'ajaxStarted' && action.entity === 'payments' && action.operation === 'findAll') {
        return Utils.extend(state, {_loading: true})
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'payments' && action.operation === 'findAll') {
        const entities = action.request.appendResults ?
            Utils.mergeArraysByObjectKey(state.entities, action.response.results, 'id') :
            action.response.results;

        const tableMessages = Utils.isEmptyArray(action.response.results) ? [Utils.message('common.table.no.items')] : [],
            nextState = {
                _loading: false,
                entities: entities,
                searchRequest: action.request,
                totalRecords: action.response.total,
                tableMessages: tableMessages
            };

        return Utils.extend(state, nextState);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'payments' && action.operation === 'delete') {
        const nextState = {
            entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
            totalRecords: state.totalRecords - 1,
            selectedObj: null
        };
        return Utils.extend(state, nextState);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'payments' && action.operation === 'stat') {
        return Utils.extend(state, {statChart: action.response, outdatedChart: false});
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'payments' && action.operation === 'balance') {
        return Utils.extend(state, {balanceItems: action.response.items});
    }

    return state;
}

module.exports = PaymentsReducer;
