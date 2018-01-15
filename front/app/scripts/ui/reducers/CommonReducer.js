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

const ajaxStatusMappings = {
    ajaxStarted: 'started',
    ajaxFinishedSuccess: 'finishedSuccess',
    ajaxFinishedError: 'finishedError'
};

function getInitialState() {
    return {
        accounts: [],
        categories: [],
        rootCategories: [],
        ajaxStatuses: {} //statuses of ajax operations
    }
}

function CommonReducer(state = getInitialState(), action) {
    if (action.type === 'toggleNavSidebar') {
        return Utils.extend(state, {navSidebarOpened: action.opened})
    }
    const ajaxOperationStatus = ajaxStatusMappings[action.type];
    if (ajaxOperationStatus) {
        const nextAjaxStatuses = Utils.ajax.operationStatus(Utils.extend(state.ajaxStatuses), action.operation, action.entity, ajaxOperationStatus);
        return Utils.extend(state, {ajaxStatuses: nextAjaxStatuses})
    }

    return state;
}

module.exports = CommonReducer;
