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
        entitiesMap: {},
        rootIds: [],
        selectedObj: null
    };
}

function CategoriesReducer(state = getInitialState(), action) {
    if (action.type === 'toggleSelectedObject' && action.entity === 'categories') {
        return Utils.extend(state, {
            selectedObj: action.obj
        });
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'categories' && action.operation === 'save') {
        const category = action.response,
            categoryParent = category.parent,
            rootIds = categoryParent ? state.rootIds : Utils.arr.put(state.rootIds, category.id),
            selectedObj = state.selectedObj && state.selectedObj.id === category.id ? category : state.selectedObj,
            entitiesMapDiff = {[category.id]: category},
            stateCategoryParent = categoryParent ? state.entitiesMap[categoryParent.id] : null;

        if (stateCategoryParent) {
            entitiesMapDiff[categoryParent.id] = {children: Utils.arr.put(stateCategoryParent.children, category, Utils.obj.id)}
        }

        return Utils.merge(state, {
            entitiesMap: entitiesMapDiff,
            rootIds,
            selectedObj
        });
    } else if (action.type === 'ajaxStarted' && action.entity === 'categories' && action.operation === 'findAll') {
        return Utils.extend(state, {_loading: true})
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'categories' && action.operation === 'findAll') {
        const entitiesMap = action.response;

        const nextState = {
            _loading: false,
            entitiesMap: entitiesMap,
            rootIds: Utils.objectValues(entitiesMap).filter(o=>o.parent === null).map(Utils.obj.id)
        };

        return Utils.extend(state, nextState);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'categories' && action.operation === 'delete') {
        const categoryId = action.response.id,
            category = state.entitiesMap[categoryId],
            categoryParent = category.parent,
            entitiesMapDiff = {[categoryId]: undefined},
            stateCategoryParent = categoryParent ? state.entitiesMap[categoryParent.id] : null;

        if (stateCategoryParent) {
            entitiesMapDiff[categoryParent.id] = {children: Utils.arr.remove(stateCategoryParent.children, category, Utils.obj.id)}
        }

        const nextState = {
            entitiesMap: entitiesMapDiff,
            rootIds: Utils.arr.remove(state.rootIds, categoryId),
            selectedObj: null
        };
        return Utils.merge(state, nextState);
    }

    return state;
}

module.exports = CategoriesReducer;
