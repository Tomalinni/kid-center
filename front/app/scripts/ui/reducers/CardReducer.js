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

const triggerRevalidationFields = ['visitType', 'price', 'maxDiscount', 'durationDays', 'durationDaysMax'];

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'cards', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {
                card: action.response
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'cards', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                card: Utils.extend(state.card, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'cards'},
        (state, action) => {
            const nextState = {
                card: {
                    allowedSubjectsMask: 63,
                }
            };
            return Utils.extend(state, nextState);
        })
    .add({type: 'setEntityValue', entity: 'cards'},
        (state, action) => {
            let card = Utils.extend(state.card, {[action.fieldId]: action.newValue});

            if (action.fieldId === 'visitType') {
                const visitTypeOpt = Dictionaries.visitType.byId(action.newValue) || {};
                if (visitTypeOpt.chargeless) {
                    card.price = 0;
                    card.maxDiscount = 0;
                }
                if (Utils.isTransferCard(card)) {
                    card.lessonsLimit = 0
                }
            } else if (action.fieldId === 'ageRange') {
                const subjects = action.newValue && Dictionaries.lessonSubject.filter(o => o.ageRange === action.newValue) || [];
                card.allowedSubjectsMask = Utils.arrayItemsToBitmask(subjects, Dictionaries.lessonSubject);
            }
            const nextState = {
                card: card,
                validationMessages: {cards: Validators.cards(card, Utils.select(!Utils.arr.contains(triggerRevalidationFields, action.fieldId), action.fieldId))}
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'setValidationMessages', entity: 'cards'},
        (state, action) => {
            return Utils.merge(state, {validationMessages: {cards: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'cards'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {cards: {}}});
        });

function getInitialState() {
    return {
        card: {},
        validationMessages: {
            cards: {}
        }
    };
}

function CardReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('cards'), CardReducer);
