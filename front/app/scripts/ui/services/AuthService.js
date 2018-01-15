/*
 * (C) Copyright ${YEAR} Legohuman (https://github.com/Legohuman).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use AuthService file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

const Config = require('../Config'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    DataService = require('./DataService'),
    LocalStorageService = require('./LocalStorageService'),
    Permissions = require('../Permissions'),
    Navigator = require('../Navigator');

const AuthService = {
    subject: 'anonymous',
    permissions: [],

    applyToken(token){
        if (token) {
            LocalStorageService.setToken(token);
            AuthService.applyPermissions(token);
        }
    },

    applyPermissions(token){
        if (token) {
            let payload = token.split('.')[1];
            console.log('Token payload', payload);
            let payloadObj = JSON.parse(atob(payload));
            console.log('Token payload obj', payloadObj);
            AuthService.permissions = Utils.hexMaskToArrayItems(payloadObj.perms, Object.keys(Permissions));
            AuthService.subject = payloadObj.sub;
            console.log('Token permissions', AuthService.permissions);
        }
    },

    logOut(){
        if (LocalStorageService.getToken()) {
            LocalStorageService.removeToken();
            Navigator.navigate(Navigator.routes.login)
        }
    },

    onSecurePageEnterFactory(store){
        return (nextState, replace) => {
            if (!LocalStorageService.getToken()) {
                replace(Navigator.routes.login)
            } else {
                if (!store.getState().common._dictsLoaded) {
                    store.dispatch(Actions.ajax.dictionaries.load())
                }
            }
        }
    },

    hasPermission(perm){
        return Utils.arr.contains(AuthService.permissions, perm)
    },
};
AuthService.applyPermissions(LocalStorageService.getToken());

DataService.addResponseProcessor(response => {
    let authHeadVal = response.headers.get('Authorization') || '';
    if (authHeadVal.startsWith(Config.authSchemePrefix)) {
        const curToken = LocalStorageService.getToken(),
            nextToken = authHeadVal.substring(Config.authSchemePrefix.length);

        if (nextToken !== curToken) {
            console.log('Changing token from ' + curToken + ' to ' + nextToken);
            AuthService.applyToken(nextToken);
        }
    }
});

DataService.addResponseProcessor(response => {
    let needsAuthentication = response.status === 401;
    if (needsAuthentication) {
        AuthService.logOut()
    }
});

module.exports = AuthService;
