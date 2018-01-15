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

const ReducersMap = require('./ReducersMap'),
    Utils = require('../Utils');

function getInitialState() {
    return {
        entities: []
    };
}

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'promotionDetails', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                entities: Utils.arr.put(state.entities, action.response, Utils.obj.id)
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'promotionDetails', operation: 'delete'},
        (state, action) => {
            return Utils.extend(state, {
                entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id)
            });
        });

function PromotionDetailsReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = PromotionDetailsReducer;
