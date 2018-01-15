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
        common: {
            _dictsLoaded: false,
            studentRelativeRoles: []
        },
        pages: {
            Cities: {entities: []},
            Schools: {
                entities: [],
                entitiesMap: {}
            },
            Accounts: {
                entities: [],
                entitiesMap: {}
            },
            Categories: {
                entitiesMap: {},
                rootIds: []
            }
        }
    }
}


function GlobalReducer(state = getInitialState(), action) {
    if (action.type === 'ajaxFinishedSuccess' && action.entity === 'dictionaries' && action.operation === 'load') {
        const response = action.response,
            entitiesMap = response.categories;
        return Utils.merge(state, {
            common: {
                _dictsLoaded: true,
                relativeRoles: response.relativeRoles
            },
            pages: {
                Cities: {entities: response.cities},
                Schools: {
                    entities: response.schools,
                    entitiesMap: Utils.getSchoolsMap(response.schools)
                },
                Accounts: {
                    entities: response.accounts,
                    entitiesMap: Utils.getAccountsMap(response.accounts)
                },
                Categories: {
                    entitiesMap: entitiesMap,
                    rootIds: Utils.objectValues(entitiesMap).filter(o => o.parent === null).map(Utils.obj.id)
                },
                Roles: {entities: response.roles},
                Teachers: {entities: response.employees},
                PromotionSources: {entities: response.promotionSources},
                PromotionDetails: {entities: response.promotionDetails}
            }
        })
    }

    return state;
}

module.exports = GlobalReducer;
