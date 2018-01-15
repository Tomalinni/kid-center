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

const AjaxUtils = {

    operationStatus(ajaxStatuses, operation, entity, status) {
        if (!ajaxStatuses) throw 'Ajax statuses is not defined. Check object in common reducer.';
        if (!operation) throw 'Ajax operation is not defined. Check created action.';
        if (!entity) throw 'Ajax operation entity is not defined. Check created action.';

        if (status) { //set
            ajaxStatuses[operation + '_' + entity] = status;
            return ajaxStatuses;
        } else {
            return ajaxStatuses[operation + '_' + entity]
        }
    },

    isInProgress(ajaxStatuses, operation, entity){
        return AjaxUtils.operationStatus(ajaxStatuses, operation, entity) === 'started'
    }
};

module.exports = AjaxUtils;