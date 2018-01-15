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

'use strict';

const CommonUtils = require('./CommonUtils');

const RelativeUtils = {

    idOrEqualFieldsAccessor(r){
        return CommonUtils.joinDefined([r.role, r.name, r.mail, r.driverLicense, r.passport, r.mobile], ';');
    },

    mergeSimilar(r1, r2){
        return CommonUtils.extend(r1, r2, {
            id: r1.id || r2.id,
            mobileConfirmed: r1.mobileConfirmed || r2.mobileConfirmed,
            mobileNotifications: CommonUtils.arr.merge(r1.mobileNotifications, r2.mobileNotifications),
            emailNotifications: CommonUtils.arr.merge(r1.emailNotifications, r2.emailNotifications)
        });
    }
};

module.exports = RelativeUtils;
