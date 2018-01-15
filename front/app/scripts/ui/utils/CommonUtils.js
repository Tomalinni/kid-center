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

const moment = require('moment-timezone'),
    Locale = require('../Locale'),
    Messages = require('../Messages'),
    Config = require('../Config');

require('moment-round'); //used implicitly by moment().round()
moment.tz.setDefault(Config.currentTimeZone);
moment.locale('en', {
        week: {
            dow: 1
        }
    }
);

const Utils = {

    documentFullHeight(){
        const body = document.body,
            html = document.documentElement;

        return Math.max(body.scrollHeight, body.offsetHeight,
            html.clientHeight, html.scrollHeight, html.offsetHeight);
    },

    documentScrollBottom(){
        const body = document.body,
            bottomLine = body.scrollTop + body.clientHeight;

        return Utils.documentFullHeight() - bottomLine;
    },

    scroll: {
        top(topOffset){
            $(document.body).animate({scrollTop: topOffset}, 400)
        },
        offset(elem){
            return elem == null ? null : $(elem).offset()
        }
    },

    /**
     * Converts time in format hh:mm to number of minutes from midnight
     * @param timeString string in format hh:mm
     * @returns number of minutes from midnight
     */
    timeToMinutes(timeString) {
        let parts = timeString ? timeString.split(':') : [];
        if (parts.length == 2) {
            let hours = +parts[0], mins = +parts[1];
            return hours * 60 + mins;
        }
        return undefined;
    },

    /**
     * Converts number of minutes from midnight to time in format hh:mm
     * @param mins number of minutes from midnight
     * @returns string in format hh:mm
     */
    minutesToTime(mins) {
        if (!!mins) {
            let hours = Math.floor(mins / 60),
                minsInHour = mins % 60;
            return Utils._leftPadZeroes(hours) + ':' + Utils._leftPadZeroes(minsInHour);
        }
        return undefined;
    },

    _leftPadZeroes(num) {
        return ('00' + num).substr(-2, 2);
    },

    /**
     * Converts 4-digit string HHmm to readable time in format HH:mm
     * Used in conversion of time ids.
     * @param hoursMinutes string in format HHmm
     * @returns string in format hh:mm
     */
    hoursMinutesToTime(hoursMinutes) {
        if (!!hoursMinutes) {
            let hours = hoursMinutes.substring(0, 2),
                minsInHour = hoursMinutes.substring(2, 4);
            return hours + ':' + minsInHour;
        }
        return undefined;
    },

    /**
     * onverts 4-digit string HHmm to number of minutes from midnight
     * @param hoursMinutes string in format HHmm
     * @returns number of minutes from midnight
     */
    hoursMinutesToMinutes(hoursMinutes) {
        if (!!hoursMinutes) {
            let hours = hoursMinutes.substring(0, 2),
                mins = hoursMinutes.substring(2, 4);
            return +hours * 60 + (+mins);
        }
        return undefined;
    },

    /**
     * Converts duration in minutes from midnight to 4-digit string HHmm
     * @param mins duration in minutes from midnight
     * @returns {*} 4-digit string in format HHmm
     */
    minutesToHoursMinutes(mins) {
        if (!!mins) {
            let hours = Math.floor(mins / 60),
                minsInHour = mins % 60;
            return Utils._leftPadZeroes(hours) + Utils._leftPadZeroes(minsInHour);
        }
        return undefined;
    },

    /**
     * Converts moment to string in format HH:mm
     * @param mt moment.js object
     * @returns {*} string in format HHmm
     */
    momentToTime(mt){
        return mt && (Utils._leftPadZeroes(mt.hour()) + ':' + Utils._leftPadZeroes(mt.minute()))
    },

    /**
     * Converts moment to minutes from midnight
     * @param mt moment.js object
     * @returns {*} int count of minutes from midnight
     */
    momentToMinutes(mt){
        return mt && (mt.hour() * 60 + mt.minute())
    },

    /**
     * Return minutes from midnight for specified moment object
     * @param moment moment object
     */
    minutesFromMidnight(moment) {
        return moment && (moment.hour() * 60 + moment.minute());
    },

    /**
     * @param code message code in bundle. Code can reference message plain string or function returning parametrized message
     * Commented list of params is vararg of parameters for message function
     * If first param is array, only it will be used to interpolate message,
     * otherwise all the rest arguments are interpreted as array of parameters.
     * @returns {*} localized message from messages bundle, or message interpolating function if parameters are not defined
     */
    message(code /*, param, param2*, ...*/) {
        const messageOrFn = Messages[Locale.current()][code] || code;
        if (Utils.isFunction(messageOrFn)) {
            if (arguments.length === 1) {
                return messageOrFn;
            } else if (arguments.length === 2 && Utils.isArray(arguments[1]) && arguments[1].length > 0) {
                return messageOrFn.apply(Utils, [].concat(arguments[1]))
            } else {
                return messageOrFn.apply(Utils, Array.prototype.slice.call(arguments, 1))
            }
        }
        return messageOrFn;
    },

    /**
     * Copies object with message codes and replaces all codes with messages from message bundle
     * @param obj object to process
     * @return {*} processed object
     */
    messages(obj){
        if (obj.text && obj.params) {
            return Utils.message(obj.text, obj.params)
        }
        if ('string' === typeof obj) {
            return Utils.message(obj)
        }
        if ('object' === typeof obj) {
            const result = {};
            Object.keys(obj).forEach(k => {
                result[k] = Utils.messages(obj[k])
            });
            return result
        }
    },

    pageTitle(pageId){
        return Utils.message('common.pages.' + pageId)
    },

    addLocalizedProp(obj, prop, localizationPrefix){
        obj[prop] = Utils.message(localizationPrefix + obj['id']);
        return obj
    },

    /**
     * @param obj object to return values from
     * @returns {*} array of all key values in object
     */
    objectValues(obj) {
        return obj && Object.keys(obj).map((key) => {
                return obj[key];
            })
    },

    objectHasDefinedValues(obj){
        return obj && Object.keys(obj).some(k => obj[k]);
    },

    mergeArraysByObjectKey(arr, newArr, key){
        if (Utils.isNotEmptyArray(newArr)) {
            if (Utils.isNotEmptyArray(arr)) {
                const objMap = {};
                arr.forEach(o => {
                    objMap[o[key]] = true
                });
                const merged = [].concat(arr);
                newArr.forEach((o, i) => {
                    if (objMap[o[key]]) {
                        merged[i] = o
                    } else {
                        merged.push(o)
                    }
                });
                return merged;
            } else {
                return newArr;
            }
        }
        return arr;
    },

    arr: {
        /**
         * Finds first object and its index in array that strictly equals to obj
         * @param arr array to find in
         * @param obj object to find
         * @param elemAccessor function that gets object from array element that will be compared with searched object
         * Default elemAccessor returns array element itself
         * @returns {*} first array object that strictly equals to obj
         */
        find(arr, obj, elemAccessor = returnSelf) {
            if (arr && Array.isArray(arr)) {
                const index = Utils.arr._findIndex(arr, obj, elemAccessor);
                return {index: index, obj: index >= 0 ? arr[index] : undefined};
            }
            return {index: -1, obj: undefined};
        },

        _findIndex(arr, obj, elemAccessor = returnSelf){
            const objVal = elemAccessor(obj);
            return objVal === obj ?
                arr.indexOf(objVal) :
                arr.findIndex((arrObj) => elemAccessor(arrObj) === objVal);
        },

        /**
         * Determines if specified array contains specified objedt
         * @param arr array to find in
         * @param obj object to find
         * @param elemAccessor function that gets object from array element that will be compared with searched object
         * Default elemAccessor returns array element itself
         * @returns {*} true if array contains specified objec, false otherwise
         */
        contains(arr, obj, elemAccessor = returnSelf){
            if (arr && Array.isArray(arr)) {
                return Utils.arr._findIndex(arr, obj, elemAccessor) >= 0;
            }
            return false;
        },

        /**
         * Makes a copy of array with object added or replaced
         * @param arr array to process
         * @param obj object to find
         * @param elemAccessor function that gets object from array element that will be compared with searched object
         * Default elemAccessor returns array element itself
         * @returns {*} new array with object added or replaced
         */
        put(arr, obj, elemAccessor = returnSelf) {
            if (arr && Array.isArray(arr)) {
                const index = Utils.arr._findIndex(arr, obj, elemAccessor);
                let result = [].concat(arr);
                if (index >= 0) {
                    result.splice(index, 1, obj);
                } else {
                    result.push(obj)
                }
                return result;
            }
            return arr;
        },

        push(arr, obj){
            if (arr && Array.isArray(arr)) {
                const result = [].concat(arr);
                result.push(obj);
                return result;
            }
            return arr;
        },

        /**
         * Makes a copy of array with found object removed, or the same array if object was not found
         * @param arr array to process
         * @param obj object to find
         * @param elemAccessor function that gets object from array element that will be compared with searched object
         * Default elemAccessor returns array element itself
         * @returns {*} new array if object was found and removed, same array otherwise
         */
        remove(arr, obj, elemAccessor = returnSelf) {
            if (arr && Array.isArray(arr)) {
                const index = Utils.arr._findIndex(arr, obj, elemAccessor);
                if (index >= 0) {
                    let result = [].concat(arr);
                    result.splice(index, 1);
                    return result;
                }
                return arr;
            }
            return arr;
        },

        putAt(arr, index, obj){
            if (arr && Array.isArray(arr) && index >= 0) {
                const result = [].concat(arr);
                result.splice(index, 1, obj);
                return result;
            }
            return arr;
        },

        removeAt(arr, index){
            if (arr && Array.isArray(arr) && index >= 0) {
                const result = [].concat(arr);
                result.splice(index, 1);
                return result;
            }
            return arr;
        },

        /**
         * Makes a copy of array with found object removed if it was there, or copy of array with object added otherwise
         * @param arr array to process
         * @param obj object to find
         * @param elemAccessor function that gets object from array element that will be compared with searched object
         * Default elemAccessor returns array element itself
         * @returns {*} new array with object added or removed
         */
        toggle(arr, obj, elemAccessor = returnSelf) {
            if (arr && Array.isArray(arr)) {
                const index = Utils.arr._findIndex(arr, obj, elemAccessor);
                let result = [].concat(arr);
                if (index >= 0) {
                    result.splice(index, 1);
                } else {
                    result.push(obj)
                }
                return result;
            }
            return arr;
        },

        /**
         * Returns full or empty array depending on current array state
         * @param arr array to toggle. Has some options from full array.
         * It must use only options from full array and not from anywhere else.
         * @param fullArr full array
         */
        toggleAll(arr, fullArr){
            if (Utils.isArray(fullArr)) {
                return arr && arr.length === fullArr.length ? [] : fullArr
            }
            return arr
        },

        merge(arr, newArr, elemAccessor = returnSelf, mergeFn){
            if (Utils.isNotEmptyArray(newArr)) {
                if (Utils.isNotEmptyArray(arr)) {
                    const merged = [].concat(arr);
                    newArr.forEach(o => {
                        let firstItemIndex = Utils.arr._findIndex(merged, o, elemAccessor);
                        if (firstItemIndex < 0) {
                            merged.push(o);
                        } else if (Utils.isFunction(mergeFn)) {
                            const firstItem = merged[firstItemIndex];
                            merged.splice(firstItemIndex, 1, mergeFn(firstItem, o));
                        }
                    });
                    return merged;
                } else {
                    return newArr;
                }
            }
            return arr;
        },

        diff(arr, other){
            if (Utils.isNotEmptyArray(other) && Utils.isNotEmptyArray(arr)) {
                const diff = [].concat(arr);
                other.forEach(o => {
                    let i = diff.indexOf(o);
                    if (i >= 0) {
                        diff.splice(i, 1);
                    }
                });
                return diff;
            }
            return arr;
        },

        /**
         * Groups input array items from arr by string group name from groupsArr and outputs them in
         * array of arrays.
         * @param arr input array to group
         * @param groupsArr array of group names
         * @param groupAccessor function that gets group name from item, if accessor returns null or undefined item is skipped.
         * @return {Array} Array of grouped items arrays. Ex. [[a1, a2], [b1], undefined, [d1, d2, d3]]
         * index of group array corresponds to index of group name in groupsArr
         */
        groupByOrdered(arr, groupsArr, groupAccessor = returnSelf){
            if (Utils.isNotEmptyArray(arr) && Utils.isNotEmptyArray(groupsArr) && Utils.isFunction(groupAccessor)) {
                const grouped = [];
                arr.forEach(o => {
                    let group = groupAccessor(o);
                    if (group && Utils.isString(group)) {
                        let groupIndex = groupsArr.indexOf(group);
                        if (groupIndex >= 0) {
                            grouped[groupIndex] = (grouped[groupIndex] || []).concat(o);
                        }

                    }
                });
                return grouped
            }
            return []
        },

        /**
         * Concatenates values of object keys, that are Array object to single array
         * @param obj object that contains keys with arrays
         * @return concatenated array or null if object is not defined.
         * If object has no keys, array will be empty
         */
        concatArrayValues(obj){
            if (obj) {
                const arrays = Object.keys(obj).map(k => obj[k])
                    .filter(val => Utils.isArray(val));
                return Array.prototype.concat.apply([], arrays)
            }
            return null;
        },

        /**
         * Finds max element of array
         * @param arr source array
         * @param elemComparator (accumulator, current) function that compares current max value
         * with next element in array. Should return max value of them.
         * @return {*} max value in array, or null if array is empty, or parameter is not an array
         */
        max(arr, elemComparator){
            if (Utils.isArray(arr) && arr.length > 0) {
                return arr.reduce(elemComparator, -Infinity);
            }
            return null;
        },

        /**
         * Finds min element of array
         * @param arr source array
         * @param elemComparator (accumulator, current) function that compares current min value
         * with next element in array. Should return min value of them.
         * @return {*} min value in array, or null if array is empty, or parameter is not an array
         */
        min(arr, elemComparator){
            if (Utils.isArray(arr) && arr.length > 0) {
                return arr.reduce(elemComparator, Infinity);
            }
            return null;
        },

        /**
         * Finds last item of array
         * @param arr array to find in
         * @return {*} last item or null if array is empty
         */
        lastItem(arr){
            if (Utils.isArray(arr) && arr.length > 0) {
                return arr[arr.length - 1];
            }
            return null;
        }
    },
    select(flag, left, right){
        return flag ? left : right;
    },

    selectFn(flag, leftFn, rightFn){
        const fn = flag ? leftFn : rightFn;
        return Utils.isFunction(fn) && fn.apply(this) || undefined;
    },

    /**
     * General max implementation for objects that support comparison
     * @param a first object
     * @param b second object
     * @return {*} max object
     */
    max(a, b){
        return a > b ? a : b
    },

    when(key, fnsObj, defaultFn){
        const fn = fnsObj[key] || defaultFn;
        return Utils.isFunction(fn) && fn.apply(this) || undefined
    },

    lengthMoreThan(val, length){
        return Utils.isString(val) && val.length > length
    },

    lengthLessThan(val, length){
        return Utils.isString(val) && val.length < length
    },

    lengthNotEqual(val, length){
        return Utils.isString(val) && val.length !== length
    },

    /**
     * Removes object from array if it present there.
     * @param arr array to delete object from
     * @param obj object to delete from array
     * @returns Array copy if object was removed, orginal array if object was not found.
     */
    removeAndCopy(arr, obj) {
        let resultArr = arr;
        if (Utils.isNotEmptyArray(arr)) {
            const index = arr.indexOf(obj);
            if (index >= 0) {
                resultArr = [].concat(arr);
                resultArr.splice(index, 1);
            }
        }
        return resultArr;
    },

    /**
     * Finds object in array and returns it.
     * If object is not in array first element will be returned.
     * If array is empty or undefined undefined will be returned.
     * @param arr array to find object in
     * @param obj object to find
     */
    specificOrFirst(arr, obj) {
        if (Array.isArray(arr) && arr.length > 0) {
            const index = arr.indexOf(obj);
            return index >= 0 ? arr[index] : arr[0];
        }
        return undefined;
    },

    objToQueryString(obj, prefix){
        const query = Utils._internalObjToQueryString(obj, prefix);
        return query ? '?' + query : '';
    },

    _internalObjToQueryString(obj, prefix){
        return obj && Object.keys(obj)
                .map(key => {
                    const val = obj[key];
                    if (Utils.isDefined(val)) {
                        if (Utils.isObject(val)) {
                            return Utils._internalObjToQueryString(val, key + '.')
                        } else {
                            return (prefix || '') + key + '=' + val
                        }
                    }
                    return null;
                }).filter(val => !!val).join('&');
    },

    delegateValueChange(delegate) {
        const self = this;
        return function (event) {
            delegate.call(self, event.target.value)
        };
    },

    getLessonsDataRequest(startDateMt, endDateMt, studentId){
        return {
            studentId: studentId,
            startDate: Utils.momentToString(startDateMt),
            endDate: Utils.momentToString(endDateMt)
        }
    },

    /**
     * @returns {*} age of student with one fractional digit or undefined if object or birthDate are undefined
     * @param birthDate
     */
    studentAge(birthDate) {
        if (birthDate) {
            let age = Utils.momentDiffYearsWithFraction(moment(), moment(birthDate, Config.dateFormat));
            return Math.floor(age * 10) / 10;
        }
        return undefined;
    },

    /**
     * @param birthDate
     * @return {*} string that represents age in years and months
     */
    studentAgeYearMonths(birthDate) {
        if (birthDate) {
            const age = Utils.momentDiffYearsWithFraction(moment(), moment(birthDate, Config.dateFormat)),
                years = Math.floor(age),
                months = Math.floor((age - years) * 12);
            return years + '/' + months;
        }
        return undefined;
    },

    studentAgeGroup(birthDate, ageGroups){
        const age = Utils.studentAge(birthDate);
        if (age && Utils.isNotEmptyArray(ageGroups)) {
            const group = ageGroups.find(g => (age >= g.minAge && age < g.maxAge));
            return group && group.id
        }
    },

    /**
     * Creates student card create request by provided request
     * @param request provided request
     * @param studentId owner student id
     */
    studentCardCreateRequest(request, studentId){
        const studentCard = Utils.pick(request, ['lessonsLimit', 'cancelsLimit', 'lateCancelsLimit', 'lastMomentCancelsLimit', 'undueCancelsLimit', 'missLimit', 'suspendsLimit']);
        studentCard.ageRange = request.card.ageRange;
        studentCard.visitType = request.card.visitType;
        studentCard.allowedSubjectsMask = request.card.allowedSubjectsMask;
        studentCard.lessonsAvailable = studentCard.lessonsLimit;
        studentCard.cancelsAvailable = studentCard.cancelsLimit;
        studentCard.lateCancelsAvailable = studentCard.lateCancelsLimit;
        studentCard.lastMomentCancelsAvailable = studentCard.lastMomentCancelsLimit;
        studentCard.undueCancelsAvailable = studentCard.undueCancelsLimit;
        studentCard.missAvailable = studentCard.missLimit;
        studentCard.suspendsAvailable = studentCard.suspendsLimit;
        studentCard.price = request.finalPrice;
        studentCard.durationDays = request.finalDuration;
        studentCard.purchaseDate = request.purchaseDate;
        studentCard.student = {id: studentId};
        studentCard.cardId = request.card.id;
        const createRequest = Utils.pick(request, ['accountType']);
        createRequest.card = studentCard;
        createRequest.targetPartner = Utils.pick(request.targetPartner, ['id']);
        createRequest.targetAccount = Utils.pick(request.targetAccount, ['id']);
        return createRequest
    },

    /**
     * Creates student card add payment request by provided request
     * @param request provided request
     * @param cardId existing card id
     */
    studentCardAddPaymentRequest(request){
        return {
            card: Utils.pick(request.card, ['id']),
            targetPartner: Utils.pick(request.targetPartner, ['id']),
            targetAccount: Utils.pick(request.targetAccount, ['id']),
            accountType: request.accountType
        }
    },

    /**
     * Generates lesson id
     * @param subjectId lesson subject id
     * @param date string date in format dd.mm.yyyy
     * @param fromMins lesson start time represented as minutes from midnight
     */
    lessonId(subjectId, date, fromMins) {
        return subjectId.substr(0, 1) + date + '-' + Utils.minutesToHoursMinutes(fromMins);
    },

    lessonTemplateId(templateId, dayId, subjectId, time) {
        return 't' + templateId + '-' + dayId + '-' + subjectId + '-' + time;
    },

    subjectFromLessonTemplateId(templateId){
        return templateId && templateId.split('-')[2]
    },

    /**
     * Comparse lessons (templates) by from mins value. Used in array sorting
     * @param lesson1 first lesson
     * @param lesson2 second lesson
     * @return {number} comparison result
     */
    lessonFromMinsCompareFn(lesson1, lesson2){
        return (lesson1 && lesson1.fromMins || 0) - (lesson2 && lesson2.fromMins || 0)
    },

    /**
     * Generates moment object that denotes lesson start time
     * @param date string date of lesson in format dd.mm.yyyy
     * @param fromMins lesson start time represented as minutes from midnight
     */
    lessonMoment(date, fromMins) {
        let lessonMt = Utils.momentFromString(date);
        lessonMt.hour(fromMins / 60);
        lessonMt.minute(fromMins % 60);
        return lessonMt;
    },

    lessonMomentFromId(lessonId) {
        return lessonId && moment(lessonId.substring(1), 'DD.MM.YYYY-HHmm');
    },

    lessonPlanFromId(lessonId, duration){
        return {
            id: lessonId,
            status: Utils.select(Utils.isLessonFinished(lessonId, duration),
                'closed',
                'planned')
        }
    },

    /**
     * Is specified lesson moment started in current moment of time
     * @returns {boolean} true if lesson was started, false otherwise. Canceling or finishing lesson does not change result.
     */
    isLessonStarted(lessonMoment) {
        let curMoment = moment();
        return curMoment.isAfter(lessonMoment)
    },

    /**
     * Is specified lesson moment finished in current moment of time
     * @returns {boolean} true if lesson was finished, false otherwise. Canceling lesson does not change result.
     */
    isLessonFinished(lessonId, duration) {
        const lessonMoment = Utils.lessonMomentFromId(lessonId),
            curMoment = moment(),
            lessonTimeMoment = moment(lessonMoment).add(duration, 'minutes');

        return curMoment.isAfter(lessonTimeMoment)
    },

    /**
     * Is specified lesson moment will be today
     * @returns {boolean} true if lesson will be today, false otherwise.
     */
    lessonWillBeToday(lessonMoment){
        return moment().diff(lessonMoment, 'days') === 0
    },

    lessonDateTime(lessonId){
        return lessonId.substring(1);
    },

    lessonDate(lessonId){
        return lessonId.substring(1, 11); //it`s safe for strict format sdd.mm.yyyy-HHmm
    },

    lessonTime(lessonId){
        return Utils.hoursMinutesToTime(lessonId.substring(12));
    },

    getSchoolsMap(schools) {
        const map = {
            internal: [],
            external: [],
            all: schools || []
        };

        schools.forEach((school) => {
            if (school.external) {
                map.external.push(school)
            } else {
                map.internal.push(school)
            }
        });
        return map;
    },

    /**
     * Gets schools list depending on following conditions
     * <b>direction</b>  <b>origin</b>  <b>schools</b>
     * all        source  all
     * all        target  all
     * outgoing   source  internal
     * outgoing   target  external
     * incoming   source  external
     * incoming   target  internal
     * transfer   source  internal
     * transfer   target  internal
     * @param origin
     */
    getSchoolsCollection(schoolsMap, direction, origin){
        let result = [];
        if (direction === 'outgoing') {
            result = origin === 'source' ? schoolsMap.internal : schoolsMap.external
        } else if (direction === 'incoming') {
            result = origin === 'source' ? schoolsMap.external : schoolsMap.internal
        } else if (direction === 'transfer') {
            result = schoolsMap.internal
        } else {
            result = schoolsMap.all;
        }
        return result || [];
    },

    getAccountsMap(accounts) {
        const map = {
            internal: {},
            external: {},
            all: {}
        };
        accounts.forEach((acc) => {
            if (acc.type && acc.schools) {
                let isInternal, isExternal;
                for (let i = 0; i < acc.schools.length; i++) {
                    if (acc.schools[i].external) {
                        isExternal = true;
                    } else {
                        isInternal = true;
                    }
                }

                if (isInternal) {
                    Utils.pushToObjArr(map.internal, acc.type, acc);
                }
                if (isExternal) {
                    Utils.pushToObjArr(map.external, acc.type, acc);
                }

                Utils.pushToObjArr(map.all, acc.type, acc);

            }
        });
        return map;
    },

    pushToObjArr(obj, key, elem){
        let arr = obj[key];
        if (!arr) {
            obj[key] = [elem]
        } else {
            arr.push(elem)
        }
    },

    degradeArray(arr){
        if (Utils.isArray(arr)) {
            arr = arr.filter(o => !!o);

            if (arr.length === 1) {
                return arr[0]
            }
            if (arr.length === 0) {
                return undefined
            }
        }
        return arr
    },

    /**
     * Gets accounts map splitted by account type depending on following conditions
     * <b>direction</b>  <b>origin</b>  <b>accounts</b>
     * all        source  all
     * all        target  all
     * outgoing   source  internal
     * outgoing   target  external
     * incoming   source  external
     * incoming   target  internal
     * transfer   source  internal
     * transfer   target  internal
     *
     * @param accountsMap object with different collections set to fields {all: {}, internal:{}, external:{}}
     * @param direction payment direction: all, outgoing, incoming, transfer
     * @param origin account origin: source, target
     * @return {*}
     */
    getAccountsSubMap(accountsMap, direction, origin){
        if (accountsMap) {
            if (direction === 'outgoing') {
                return origin === 'source' ? accountsMap.internal : accountsMap.external
            } else if (direction === 'incoming') {
                return origin === 'source' ? accountsMap.external : accountsMap.internal
            } else if (direction === 'transfer') {
                return accountsMap.internal
            }
            return accountsMap.all
        }
        return {};
    },

    ui: {
        btn: {
            successClassForVal(successVal, btnVal){
                return successVal === btnVal ? 'btn-success' : 'btn-default';
            },
        }
    },

    objectsByIds(objects, ids){
        return objects.map(o => (ids.indexOf(o.id) >= 0))
    },

    wrapPromiseFn(fn, beforeCb, successCb, errorCb = successCb){
        return function () {
            const result = fn.apply(this, arguments);
            beforeCb.apply(this);

            if (Promise.resolve(result) == result) {
                return result.then(successCb, errorCb)
            } else {
                successCb.apply(this);
            }
            return result
        }
    },

    fn: {
        truth: returnTrue,
        nullify: () => null,
        nop: () => {
        },
        emptyObj: () => {
            return {}
        }
    },

    obj: {
        self: returnSelf,
        id(obj){
            return Utils.obj.key('id')(obj)
        },
        key(k){
            return (obj) => obj && obj[k]
        },
        name(obj){
            return Utils.obj.key('name')(obj)
        },
        keysDiff(left, right){
            const diff = {added: [], deleted: []};

            Object.keys(left).forEach(k => {
                if (!right.hasOwnProperty(k)) {
                    diff.deleted.push(k)
                }
            });

            Object.keys(right).forEach(k => {
                if (!left.hasOwnProperty(k)) {
                    diff.added.push(k)
                }
            });

            return diff;
        }
    },

    sort: {
        byKey(key){
            return (a, b) => {
                return (a && a[key] || 0) - (b && b[key] || 0)
            }
        },
        reverse(fn){
            return (a, b) => -fn(a, b)
        }
    },

    cols: {
        col(header, bodyCellFn, width, className, sortId){
            return {
                width: width,
                className: className,
                sortId: sortId,
                headerCell: {
                    children: header
                },
                bodyCell: {
                    childrenFn: bodyCellFn
                }
            }
        }
    },

    treeMap(rootNodes, map = {}){
        if (Utils.isArray(rootNodes)) {
            rootNodes.forEach((node) => {
                map[node.id] = node;
                if (Utils.isArray(node.children)) {
                    map = Utils.treeMap(node.children, map)
                }
            })
        }
        return map;
    },

    fillArray(length, fn){
        const arr = [];
        for (let i = 0; i < length; i++) {
            const val = fn(i);
            if (Utils.isDefined(val)) {
                arr.push(val)
            }
        }
        return arr
    },

    nullifyEvery(count){
        return i => i % count !== 0 ? i : null
    },

    /**
     * Concats array of arrays to single array
     * @param arrs array of arrays
     */
    concatArrays(arrs){
        return Array.prototype.concat.apply([], arrs)
    },

    collectArraysByProp(objects, prop){
        const arrs = [];
        (objects || []).forEach(city => {
            arrs.push(city[prop])
        });
        return Utils.concatArrays(arrs)
    },

    pick(obj, keys){
        if (Utils.isDefined(obj) && Utils.isNotEmptyArray(keys)) {
            const result = {};
            keys.forEach(k => {
                if (obj.hasOwnProperty(k)) {
                    result[k] = obj[k];
                }
            });
            return result;
        }
        return obj;
    },

    pickBy(obj, fn){
        if (Utils.isDefined(obj) && Utils.isFunction(fn)) {
            const result = {};
            Object.keys(obj).forEach(k => {
                if (fn(k, obj[k])) {
                    result[k] = obj[k];
                }
            });
            return result;
        }
        return obj;
    },

    omitSingle(obj, key){
        if (Utils.isDefined(obj) && Utils.isDefined(key)) {
            const result = {}, objKey = key + '';
            Object.keys(obj).forEach(k => {
                if (k !== objKey) {
                    result[k] = obj[k]
                }
            });
            return result;
        }
        return obj;
    },

    omit(obj, keys){
        if (Utils.isDefined(obj) && Utils.isNotEmptyArray(keys)) {
            const result = {};
            Object.keys(obj).forEach(k => {
                if (!keys.some(omitKey => omitKey === k)) {
                    result[k] = obj[k]
                }
            });
            return result;
        }
        return obj;
    },

    putKeys(obj, keys, valFn){
        if (Utils.isDefined(obj) && Utils.isNotEmptyArray(keys) && Utils.isFunction(valFn)) {
            keys.forEach(k => {
                obj[k] = valFn(k)
            });
        }
        return obj;
    },

    valueOrZero(val){
        return Utils.isDefined(val) ? val : 0;
    },

    /**
     * Determines last record number to be shown or requested
     * @param firstRecord number of first record in list
     * @param totalRecords total count of records
     * @returns {number} last record number
     */
    getLastRecord(firstRecord, totalRecords) {
        let lastRecord = firstRecord + Config.pageRecordsCount - 1;
        if (lastRecord > totalRecords) {
            lastRecord = totalRecords;
        }
        return lastRecord;
    },

    /**
     * Determines first record number to be shown
     * @param firstRecord number of first record in list
     * @param totalRecords total count of records
     * @returns {number} last record number
     */
    getFirstRecord(firstRecord, totalRecords) {
        return totalRecords === 0 ? 0 : firstRecord;
    },

    isTransferCard(card){
        return card && card.visitType === 'transfer'
    },

    query: {
        option(optionId, negate){
            return Utils.select(negate, '!', '') + optionId
        }
    },

    /**
     * Sets default value to object specified props
     * @param obj
     * @param props
     * @param value
     */
    defaultValues(obj, props, value){
        let fixedObj = obj || {};
        props.forEach(prop => {
            if (!fixedObj[prop]) {
                fixedObj[prop] = value;
            }
        });
        return fixedObj;
    },

    numberFromString(str, defaultVal){
        return +str || defaultVal
    },

    visitsTotal(s){
        return s ? s.total || (Utils.zeroIfNo(s.regular) + Utils.zeroIfNo(s.trial) + Utils.zeroIfNo(s.bonus)) : 0
    },

    /**
     * Return names of selected files in file input
     * @param files
     * @returns {*}
     */
    fileNames(files){
        if (typeof files == 'string') {
            return [files];
        } else if (files instanceof FileList) {
            let names = [];
            for (let i = 0; i < files.length; i++) {
                names.push(files[i].name);
            }
            return names;
        } else {
            return [];
        }
    },

    /**
     * Adds those new objects to array, which are not present in original array
     * @param original array of original objects
     * @param candidates array of objects to add to array
     * @return new array with new objects added.
     * If original array is not defined candidates array copy will be returned.
     * If candidates array is not defined copy of original array or empty array will be returned.
     */
    pushNew(original, candidates){
        let resultArr = Utils.isDefined(original) ? [].concat(original) : [];
        if (Utils.isDefined(candidates)) {
            if (Array.isArray(candidates)) {
                candidates.forEach((item => {
                    if (resultArr.indexOf(item) < 0) {
                        resultArr.push(item);
                    }
                }))
            } else {
                resultArr.push(candidates);
            }
        }
        return resultArr;
    },

    /**
     * Suitable to wrap onClick handlers for <a> elements
     * @param fn function to execute
     * @return {function(*)} function that invokes specified argumnet function and prevent default action on passed event.
     */
    invokeAndPreventDefaultFactory(fn){
        return e => {
            fn();
            e.preventDefault()
        }
    },

    filterMapObject(obj, predicate = Utils.fn.truth, map = Utils.obj.self, addUndefined, addNull) {
        if (!obj) return obj;
        let result = {}, key;

        for (key in obj) {
            if (obj.hasOwnProperty(key) && predicate(key, obj[key])) {
                let mapped = map(key, obj[key]);
                if (mapped || addUndefined && mapped === undefined || addNull && mapped === null) {
                    result[key] = mapped;
                }

            }
        }
        return result;
    },

    isNewId(id){
        return id === 'new';
    },

    isValidNumberId(id){
        return +id > 0;
    },

    isArray: Array.isArray,

    /**
     * @param arr object to check
     * @return {boolean} true if arr is array object and is not empty, false otherwise
     */
    isNotEmptyArray(arr) {
        return Array.isArray(arr) && arr.length > 0;
    },

    /**
     *
     * @param arr object to check
     * @returns {boolean} true if arr is array object and is empty, false otherwise
     */
    isEmptyArray(arr) {
        return Array.isArray(arr) && arr.length === 0;
    },

    /**
     * Checks that all obj values are empty (null)
     * @param obj object with values
     * @returns {*|boolean} true if object values are empty
     */
    isAllValuesEmpty(obj) {
        return !obj || !Object.keys(obj).some(k => Utils.isDefined(obj[k]))
    },


    isNotDefinedOrNotEmptyArray(arr) {
        return !arr || Array.isArray(arr) && arr.length > 0;
    },

    /**
     * @param val object to check
     * @return {boolean} true if val is string object and is not blank, false otherwise
     */
    isNotBlankString(val) {
        return Utils.isString(val) && val.trim().length > 0;
    },

    trimDefined(str){
        return Utils.isString(str) && str.trim() || str;
    },

    /**
     * Creates moment object from string in format Config.dateFormat
     * @param str string to convert
     * @param format momentjs format string
     */
    momentFromString(str, format = Config.dateFormat) {
        return str && moment(str, format);
    },

    /**
     * Creates string object from moment using format Config.dateFormat
     * @param mt moment object to convert
     * @param format momentjs format string
     */
    momentToString(mt, format = Config.dateFormat) {
        return mt && mt.format(format);
    },

    /**
     * Creates string object from moment using format Config.dateTimeFormat
     * @param mt moment object to convert
     */
    momentToDateTimeString(mt) {
        return Utils.momentToString(mt, Config.dateTimeFormat);
    },

    humanReadableDateTimeFromString(str){
        return Utils.mt.convert.format(str, Config.dateTimeFormat, Config.humanDateTimeFormat)
    },

    mt: {
        convert: {
            format(str, formatFrom, formatTo){
                return Utils.momentToString(Utils.momentFromString(str, formatFrom), formatTo)
            }
        }
    },

    /**
     * @param m1 first moment
     * @param m2 second moment
     * @returns {*} difference in to moments with fractional precision
     */
    momentDiffYearsWithFraction(m1, m2) {
        return m1.diff(moment(m2), 'years', true);
    },

    /**
     * @param m moment before which days count is calculated
     * @returns {*} difference between current day and specified moment in days. Integer value.
     * Positive if specified moment is not happened yet, negative value otherwise
     */
    daysLeftBeforeExpiration(m) {
        return m && m.diff(moment(), 'days') + 1;
    },

    daysLeftBeforeOrZero(m) {
        let days = Utils.daysLeftBeforeExpiration(m);
        return days < 0 ? 0 : days;
    },

    /**
     * @return number 0-based number of day in week starting from Monday
     */
    dayOfWeek(mt){
        let number = mt.day() - 1;
        if (number < 0) { //Sunday
            number = 6
        }
        return number
    },

    renderArray(arr, delimeter) {
        return arr && Array.isArray(arr) ? arr.filter((v => !!v)).join(delimeter) : '';
    },

    returnArg (arg) {
        return arg;
    },

    nop(){
    },

    joinFlitered(arr, filterFn, delimeter) {
        if (!Array.isArray(arr)) {
            return '';
        }
        return arr.filter(filterFn.bind(this)).join(delimeter || ' ');
    },

    joinDefined(arr, delimeter) {
        return Utils.joinFlitered(arr, Utils.isDefined, delimeter)
    },

    joinTruthy(arr, delimeter) {
        return Utils.joinFlitered(arr, Utils.isTruthy, delimeter)
    },

    objToKey(obj) {
        return Utils.isDefined(obj) ? Object.keys(obj).map(key => key + ':' + obj[key]).join(',') : '';
    },

    log() {
        if (Config.loggingEnabled && console) {
            console.log(arguments);
        }
    },

    /**
     * Deeply merges 'a' and 'b' and returns merged object, arguments are not modified.
     * During merge 'a' object values van be replaced, updated or merged with 'b' object values
     * Value is replaced if it`s not mergeable, ex. string, number, boolean, date, moment, array, function
     * It`s also will be replaced if 'b' object value is not mergeable.
     *
     * Value is updated if 'b' object value is updater function. Updater function will be called with 'a' object value or undefined if no value can be get.
     *
     * Value is merged if both 'a' and 'b' values are objects
     *
     * @param a initial object
     * @param b changes object that will be merged with initial
     */
    merge(a, b) {
        if (!a) {
            return b;
        }
        if (!b) {
            return a;
        }
        let r = Utils.extend(a);
        Object.getOwnPropertyNames(b).forEach(prop => {
            let av = a[prop], bv = b[prop];
            if (Utils.isFunction(bv)) {
                r[prop] = bv.call(this, av); //updates existing value in a by defined updater function
            } else if (!Utils._isMergeableValue(av) || !Utils._isMergeableValue(bv)) {
                r[prop] = bv;
                //bv can has some updaters, but at this step it`s just set to result, so updaters weren`t called. Call them.
                Utils._callUpdaters(bv);
            } else { //av and bv are objects that should be merged further
                r[prop] = Utils.merge(av, bv);
            }
        });
        return r;
    },

    extend(){
        return Object.assign.apply(Utils, [{}].concat(Array.prototype.slice.call(arguments)))
    },

    assign: Object.assign,

    /**
     * Recursively calls object updaters
     * @param obj object to call updaters in
     * @private
     */
    _callUpdaters(obj){
        if (Utils.isDefined(obj)) {
            Object.getOwnPropertyNames(obj).forEach(prop => {
                let v = obj[prop];
                if (Utils.isFunction(v)) {
                    obj[prop] = v.call(this);
                }
                //can become mergeable object after fn call
                v = obj[prop];
                if (Utils._isMergeableValue(v)) {
                    Utils._callUpdaters(v);
                }
            });
        }
    },

    /**
     * Should value be merged property by property or just replaced at once
     * @param v value to check
     * @returns {boolean} true if value is defined object that should be merged, false otherwise
     * @private
     */
    _isMergeableValue(v){
        return !(!v || Utils.isBoolean(v) || Utils.isNumber(v) || Utils.isString(v) ||
        Utils.isDate(v) || Utils.isMoment(v) || Array.isArray(v) || Utils.isFunction(v));
    },

    applyOrThen(calculatable, resolvedFn, rejectedFn){
        if (calculatable instanceof Promise) {
            calculatable.then(
                val => Utils.applyOrThen(val, resolvedFn, Utils.nop),
                val => Utils.applyOrThen(val, rejectedFn, Utils.nop))
        } else if (Utils.isFunction(calculatable)) {
            Utils.applyOrThen(calculatable(), resolvedFn, rejectedFn)
        } else {
            resolvedFn(calculatable)
        }
    },

    /**
     * Updater function that increments current value by 1
     * @param v value to update
     * @returns {*} new value
     */
    incrementor(v){
        return (v || 0) + 1
    },

    /**
     * Updater function that decrements current value by 1
     * @param v value to update
     * @returns {*} new value
     */
    decrementor(v){
        return (v || 0) - 1
    },

    /**
     * Returns zero if value is not defined
     * @param v value to check
     * @returns {*|number} value or 0 if it`s not defined
     */
    zeroIfNo(v){
        return v || 0;
    },

    /**
     * Converts bit mask to array of items picked from source array.
     * @param mask bit mask to process
     * @param arr source array of items to search in
     * @return {Array} array of picked items
     */
    bitmaskToArrayItems(mask, arr){
        if (!arr || !arr.length) return [];
        let pickedItems = [];
        for (let i = 0; i < arr.length; i++) {
            if ((mask >>> i) % 2 === 1) {
                pickedItems.push(arr[i])
            }
        }
        return pickedItems
    },

    /**
     * Converts array of picked items to bit mask according to items order in source array.
     * @param pickedItems array of picked items to process
     * @param arr array which defines corresponding bit number for each picked item
     * @return {number} bit mask
     */
    arrayItemsToBitmask(pickedItems, arr){
        if (!pickedItems || !arr || !pickedItems.length || !arr.length) return 0;
        let mask = 0;
        for (let i = arr.length - 1; i >= 0; i--) {
            mask <<= 1;
            if (pickedItems.indexOf(arr[i]) >= 0) {
                mask++;
            }
        }
        return mask
    },

    /**
     * Converts hexadecimal string mask to array of items picked from source array.
     * @param hex hexadecimal string that represents bitmask. Each hex digit stores 4 bits of mask.
     * @param arr source array of items to search in
     * @return {Array} array of picked items
     */
    hexMaskToArrayItems(hex, arr){
        let pickedItems = [];
        if (Utils.isNotEmptyArray(arr) && Utils.isString(hex)) {
            for (let i = 0; i < hex.length; i++) {
                const mask = parseInt(hex.charAt(i), 16);
                if (!isNaN(mask)) {
                    for (let j = 0; j < 4; j++) {
                        if ((mask >>> j) % 2 === 1) {
                            let pickedItem = arr[i * 4 + j];
                            if (pickedItem) {
                                pickedItems.push(pickedItem)
                            }
                        }
                    }
                }
            }
        }
        return pickedItems
    },

    loadSelectOptions(promise, callback){
        promise.then(response => {
            callback(null, {
                options: response.results
            });
        }, error => {
            callback(error.status, null);
        })
    },

    debounceInput(func){
        return Utils.debounce(func, Config.inputValuePropagationDebounceTimeoutMs);
    },

    debounce(func, wait, immediate) {
        let timeout, args, context, timestamp, result;

        let later = function () {
            let last = Utils.now() - timestamp;

            if (last < wait && last >= 0) {
                timeout = setTimeout(later, wait - last);
            } else {
                timeout = null;
                if (!immediate) {
                    result = func.apply(context, args);
                    if (!timeout) context = args = null;
                }
            }
        };

        return function () {
            context = this;
            args = arguments;
            timestamp = Utils.now();
            let callNow = immediate && !timeout;
            if (!timeout) timeout = setTimeout(later, wait);
            if (callNow) {
                result = func.apply(context, args);
                context = args = null;
            }

            return result;
        };
    },

    wrapIdentity(id){
        return {id}
    },

    now(){
        if (Date.now) {
            return Date.now();
        }
        return new Date().getTime();
    },

    isBoolean(obj) {
        return obj === true || obj === false || toString.call(obj) === '[object Boolean]';
    },

    isMoment(obj) {
        return moment.isMoment(obj);
    },

    isUndefined(obj) {
        return obj === void 0;
    },

    isDefined(obj){
        return !Utils.isUndefined(obj) && obj !== null;
    },

    /**
     * Returns value if defined or default value. Suitable for checking numbers and strings
     * @param val value that should be checked
     * @param defaultVal value to return if value is not defined
     * @return {*} val if object is defined, defaultVal otherwise
     */
    definedValueOrDefault(val, defaultVal){
        return Utils.isDefined(val) ? val : defaultVal;
    },

    setValueIfDefined(obj, prop, val){
        if (Utils.isDefined(obj) && Utils.isDefined(prop) && Utils.isDefined(val)) {
            obj[prop] = val;
        }
    },

    isTruthy(obj){
        return !!obj;
    },

    isTrue(val){
        return val === true || val === 'true'
    },

    isAbsoluteUrl(str){
        return str && (str.startsWith('http://') || str.startsWith('https://'))
    },

    convertUrlToAbsolute(str){
        return str ? ('http://' + str) : str
    },

    /**
     * @param str repeating string
     * @param count repeats count
     * @returns {string} string consisting from specified count of specified strings
     */
    repeatingString(str, count) {
        return new Array(count).fill(str).join('');
    },

    /**
     * @param actual value to check
     * @param expected value to compare with
     * @returns {boolean} true if actual is undefined or equals to expected value
     */
    notDefinedOrEqual(actual, expected) {
        return !actual || actual === expected;
    },

    emailRegexp: /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
    digitsRegexp: /^[0-9]+$/,

    /**
     * Validates email according to predefined regexp.
     * @param email string to validate
     * @returns {boolean} true if email is correct
     */
    validateEmail(email) {
        return Utils.validateRegexp(email, Utils.emailRegexp);
    },

    validateDigits(num) {
        return Utils.validateRegexp(num, Utils.digitsRegexp);
    },

    validateRegexp(val, re) {
        return re.test(val);
    },

    areDatesInOrder(beforeDate, afterDate) {
        return Utils.areMomentsInOrder(Utils.momentFromString(beforeDate), Utils.momentFromString(afterDate));
    },

    areMomentsInOrder(beforeMt, afterMt) {
        return beforeMt.isBefore(afterMt);
    },

    currentMoment(){
        return moment()
    },

    /**
     * Invokes specified reducers in order that they declared.
     * Each reducer works with intermediate state from previous reduce step.
     */
    chainReducers(/*reducers list*/) {
        const reducers = Array.prototype.slice.call(arguments);
        return function (state, action) {
            const mergeReducerStates = !Utils.isDefined(state); //useful to define initial state part in every reducer
            let nextState = state || {};
            reducers.forEach((reducer => {
                if (mergeReducerStates) {
                    nextState = Utils.merge(nextState, reducer(state, action))
                } else { //use result state from previous reducer as input for next reducer
                    nextState = reducer(nextState, action)
                }
            }));
            return nextState;
        }
    },

    lifeCycle: {
        fieldId(entityId){
            return '_' + entityId + 'LifeCycle'
        },

        entity(props, entityId){
            return props[Utils.lifeCycle.fieldId(entityId)];
        }
    },

    symbols: {
        nbsp: '\u00a0',
        mdash: '\u2014'
    }
};

function returnSelf(o) {
    return o
}

function returnTrue() {
    return true
}

['Function', 'String', 'Number', 'Date', 'Object'].forEach(function (name) {
    Utils['is' + name] = function (obj) {
        return toString.call(obj) === '[object ' + name + ']';
    };
});

// Optimize `isFunction` if appropriate. Work around some typeof bugs in old v8,
// IE 11 (#1621), and in Safari 8 (#1929).
if (typeof /./ != 'function' && typeof Int8Array != 'object') {
    Utils.isFunction = function (obj) {
        return typeof obj == 'function' || false;
    };
}

module.exports = Utils;
