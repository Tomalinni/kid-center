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

const Utils = require('../Utils');

function EntityLifeCycleReducerFactory(entityId) {
    const stateFieldId = Utils.lifeCycle.fieldId(entityId);

    return function EntityLifeCycleReducer(state = getInitialState(), action) {
        if (action.entity === entityId) {
            if (action.operation === 'findOne') {
                switch (action.type) {
                    case 'ajaxStarted':
                        return Utils.merge(state, getInitialState());
                    case 'ajaxFinishedSuccess':
                        return Utils.merge(state, {[stateFieldId]: {loading: false, found: true, saved: true}});
                    case 'ajaxFinishedError':
                        return Utils.merge(state, {[stateFieldId]: {loading: false, found: false, saved: true}});
                }
            }

            if (action.type === 'setEntityValue') {
                return Utils.merge(state, {[stateFieldId]: {saved: false}});
            }

            if (action.operation === 'save' && action.type === 'ajaxFinishedSuccess') {
                return Utils.merge(state, {[stateFieldId]: {saved: true}});
            }
        }
        return state
    };

    function getInitialState() {
        return {
            [stateFieldId]: {
                loading: true,
                found: false,
                saved: true
            }
        }
    }
}

module.exports = EntityLifeCycleReducerFactory;