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

const Utils = require('./Utils'),
    Config = require('./Config'),
    Dictionaries = require('./Dictionaries');

const nameMaxLength = 50;
const idMaxLength = 50;
const mobileLength = 11;
const literalNumericRegex = /^[a-z0-9]+$/i;
const stringIdRegex = /^[a-z0-9_]+$/i;

const Validators = {

    /**
     * @param entityId id of the specified validator
     * @param entity object to validate
     * @returns error messages for specified entity or null if no validators found
     */
    getByEntityId(entityId, entity)
    {
        const validator = Validators[entityId];
        return validator && validator(entity);
    },

    students (obj, fieldId){
        return Validators._studentsBase(obj, fieldId);
    },

    _studentsBase(obj, fieldId){
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        const isNamesEmpty = function () {
            messages._ = Utils.select(!Utils.isNotBlankString(obj.nameCn) && !Utils.isNotBlankString(obj.nameEn), Utils.message('common.students.validation.names.not.empty'));
        };

        validateFn("nameEn", val => validateByChain(val, [isNamesEmpty, validateMaxLengthFactory(nameMaxLength)]));
        validateFn("nameCn", val => validateByChain(val, [isNamesEmpty, validateMaxLengthFactory(nameMaxLength)]));

        validateFn("birthDate", val => {
            return validateByChain(val, [
                validateDefined,
                validateGeFactory(Config.minStudentAgeYears, Utils.message('common.students.validation.young.age', Config.minStudentAgeYears)),
                validateLeFactory(Config.maxStudentAgeYears, Utils.message('common.students.validation.old.age', Config.maxStudentAgeYears))
            ]);
        }, Utils.studentAge(obj.birthDate));

        validateFn("gender", validateDefined);

        return messages
    },

    relatives (obj, fieldId){
        const messages = {};
        const driverLicenseLength = 25;
        const passportLength = 20;
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("role", validateDefined);
        validateFn("name", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
        validateFn("mail", validateEmail);

        validateFn("driverLicense", val => validateByChain(val, [
            validateRegexpFactory(literalNumericRegex, Utils.message('common.relatives.validation.driverLicense.not.valid')),
            validateMaxLengthFactory(driverLicenseLength)
        ]));

        validateFn("passport", val => validateByChain(val, [
            validateRegexpFactory(literalNumericRegex, Utils.message('common.relatives.validation.passport.not.valid')),
            validateMaxLengthFactory(passportLength)
        ]));

        validateFn("mobile", val => validateByChain(val, [
            validateDigits,
            validateSpecificLengthFactory(mobileLength, Utils.message('common.validation.mobile.wrong.format', mobileLength))
        ]));

        return messages;
    },

    family (obj, fieldId){
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("students", validateNotEmptyArr);
        validateFn("relatives", validateNotEmptyArr);

        return messages;
    },

    studentCards(obj, fieldId){
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("price", val => validateByChain(val, [validateDefined, validateNotNegativeNumber]));
        validateFn("durationDays", val => validateByChain(val, [validateDefined, validateNotNegativeNumber]));
        validateFn("lessonsLimit", (val, obj) => validateByChain(val, [validateDefined, validateNotNegativeNumber]));
        validateFn("cancelsLimit", (val, obj) => validateByChain(val, [validateDefined, validateNotNegativeNumber]));
        validateFn("lateCancelsLimit", (val, obj) => validateByChain(val, [validateDefined, validateNotNegativeNumber]));
        validateFn("lastMomentCancelsLimit", (val, obj) => validateByChain(val, [validateDefined, validateNotNegativeNumber]));
        validateFn("undueCancelsLimit", (val, obj) => validateByChain(val, [validateDefined, validateNotNegativeNumber]));
        validateFn("suspendsLimit", (val, obj) => validateByChain(val, [validateDefined, validateNotNegativeNumber]));

        return messages;
    },

    studentCalls(obj, fieldId){
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("date", validateDefined);
        validateFn("employee", validateDefined);
        validateFn("method", validateDefined);
        validateFn("result", (val, obj) => validateCallResult(obj));

        return messages;
    },

    cards (obj, fieldId){
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("ageRange", validateDefined);
        validateFn("price", (val, obj) => validateByChain(val, [validateDefined, validateNotNegativeNumber, validateObjByFnFactory(obj, validateCardPrice)]));
        validateFn("maxDiscount", (val, obj) => validateByChain(val, [validateNotNegativeNumber, validateObjByFnFactory(obj, validateMaxDiscount)]));
        validateFn("maxSalesCount", validateNotNegativeNumber);
        validateFn("visitType", validateDefined);
        validateFn("durationDays", (val, obj) => validateByChain(val, [validateDefined, validateNotNegativeNumber, validateObjByFnFactory(obj, validateCardDuration)]));
        validateFn("durationDaysMax", validateNotNegativeNumber);
        validateFn("lessonsLimit", validateNotNegativeNumber);
        validateFn("cancelsLimit", validateNotNegativeNumber);
        validateFn("lateCancelsLimit", validateNotNegativeNumber);
        validateFn("lastMomentCancelsLimit", validateNotNegativeNumber);
        validateFn("undueCancelsLimit", validateNotNegativeNumber);
        validateFn("missLimit", validateNotNegativeNumber);
        validateFn("suspendsLimit", validateNotNegativeNumber);

        return messages;
    },

    teachers (obj, fieldId){
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("name", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
        return messages;
    },

    payments (obj, fieldId) {
        const messages = {},
            accountDirections = ['outgoing', 'transfer'],
            targetAccountDirections = ['incoming', 'transfer'],
            validateFn = receiveValidateFunction(obj, fieldId, messages),
            objDirection = obj.direction;

        validateFn("school", val => Utils.select(Utils.arr.contains(accountDirections, objDirection) && !val, Utils.message('common.validation.field.not.empty')));
        validateFn("account", val => Utils.select(Utils.arr.contains(accountDirections, objDirection) && !val, Utils.message('common.validation.field.not.empty')));
        validateFn("targetSchool", val => Utils.select(Utils.arr.contains(targetAccountDirections, objDirection) && !val, Utils.message('common.validation.field.not.empty')));
        validateFn("targetAccount", val => Utils.select(Utils.arr.contains(targetAccountDirections, objDirection) && !val, Utils.message('common.validation.field.not.empty')));
        validateFn("date", validateDefined);
        validateFn("category", validateDefined);
        validateFn("price", val => validateByChain(val, [validateDefined, validateNotNegativeNumber]));

        return messages;
    },

    accounts(obj, fieldId){
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("schools", val => validateByChain(val, [validateNotEmptyArr, validateOnlyOneOriginAccount]));
        validateFn("type", validateDefined);
        if (obj.type === 'cashless') {
            validateFn("city", validateDefined);
            validateFn("bank", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
            validateFn("department", validateMaxLengthFactory(nameMaxLength));
            validateFn("owner", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
            validateFn("number", val => validateByChain(val, [validateMinLengthFactory(4), validateMaxLengthFactory(19)]), Utils.trimDefined(obj.number));
        } else {
            validateFn("login", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
        }

        return messages;
    },

    categories (obj, fieldId) {
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("name", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
        return messages;
    },

    schools (obj, fieldId) {
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("city", validateDefined);
        validateFn("name", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
        return messages;
    },

    cities (obj, fieldId) {
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("name", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
        return messages;
    },

    profile (obj, fieldId) {
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        if (!fieldId) {
            messages._ = Utils.select(obj.newPass != obj.newPassRepeat, Utils.message('common.validation.profile.wrong.password.confirmation'));
        }

        validateFn("oldPass", validateNotBlank);
        validateFn("newPass", validateNotBlank);
        validateFn("newPassRepeat", validateNotBlank);
        return messages;
    },

    regRelatives (obj, fieldId) {
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("login", validateNotBlank);
        validateFn("mobile", val => validateByChain(val, [
            validateDigits,
            validateSpecificLengthFactory(mobileLength, Utils.message('common.validation.mobile.wrong.format', mobileLength))
        ]));
        return messages;
    },

    regStudents(obj, fieldId){
        return Validators._studentsBase(obj, fieldId)
    },

    users(obj, fieldId) {
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("id", val => validateByChain(val, [
            validateRegexpFactory(stringIdRegex, Utils.message('common.users.validation.user.id.not.valid')),
            validateMaxLengthFactory(idMaxLength)
        ]));

        validateFn("newId", val => validateByChain(val, [
            val => Utils.select(!Utils.isNotBlankString(obj.id) && !Utils.isNotBlankString(val), Utils.message('common.validation.field.not.empty')),
            validateRegexpFactory(stringIdRegex, Utils.message('common.users.validation.user.id.not.valid')),
            validateMaxLengthFactory(idMaxLength),
            val => Utils.select(val == "new", Utils.messages('common.users.validation.user.already.exists'))
        ]));

        validateFn("pass", val => Utils.select(Utils.isNotBlankString(obj.newId) && !Utils.isNotBlankString(val), Utils.message('common.validation.field.not.empty')));
        return messages;
    },

    roles(obj, fieldId) {
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("id", val => validateByChain(val, [
            validateRegexpFactory(stringIdRegex, Utils.message('common.users.validation.user.id.not.valid')),
            validateMaxLengthFactory(idMaxLength)
        ]));

        validateFn("newId", val => validateByChain(val, [
            val => Utils.select(!Utils.isNotBlankString(obj.id) && !Utils.isNotBlankString(val), Utils.message('common.validation.field.not.empty')),
            validateRegexpFactory(stringIdRegex, Utils.message('common.users.validation.user.id.not.valid')),
            validateMaxLengthFactory(idMaxLength),
            val => Utils.select(val == "new", Utils.messages('common.users.validation.user.already.exists'))
        ]));
        return messages;
    },

    homework(obj, fieldId) {
        const messages = {_: null};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("subject", val => validateByChain(val, [
            validateDefined,
            val => validateIdInArray(Dictionaries.lessonSubject, val)
        ]));

        validateFn("ageGroup", val => validateByChain(val, [
            validateDefined,
            val => validateIdInArray(Dictionaries.studentAge, val)
        ]));

        validateFn("startDate", validateDefined);

        validateFn("endDate", validateDefined);

        if (obj.startDate && obj.endDate && !Utils.areDatesInOrder(obj.startDate, obj.endDate)) {
            messages._ = Utils.message('common.homework.validation.dates.format')
        }

        return messages;
    },

    lessonTemplates (obj, fieldId) {
        const messages = {};
        const validateFn = receiveValidateFunction(obj, fieldId, messages);

        validateFn("name", val => validateByChain(val, [validateNotBlank, validateMaxLengthFactory(nameMaxLength)]));
        validateFn("startDate", validateDefined);

        messages.endDate = null;
        if (obj.endDate && !Utils.areMomentsInOrder(Utils.currentMoment(), Utils.momentFromString(obj.endDate))) {
            messages.endDate = Utils.message('common.error.template.end.date.before.now')
        }
        if (obj.startDate && obj.endDate && !Utils.areDatesInOrder(obj.startDate, obj.endDate)) {
            messages.endDate = Utils.message('common.error.template.end.date.before.start.date')
        }
        return messages;
    },
};

function validateSubjectsMask(val) {
    if (Utils.isDefined(val)) {
        if (val === 0) {
            return Utils.message('common.validation.field.not.empty')
        } else if (val < 0 || val > 63) {
            return Utils.message('common.validation.card.invalid.subjects.mask')
        }
    }
    return null;
}

function validateOnlyOneOriginAccount(schools) {
    if (Utils.isNotEmptyArray(schools)) {
        let isInternal, isExternal;
        for (let i = 0; i < schools.length; i++) {
            if (schools[i].external) {
                isExternal = true;
            } else {
                isInternal = true;
            }
        }
        if (isInternal && isExternal) {
            return Utils.message('common.validation.account.only.one.origin');
        }
    }
    return null;
}

function validateCallResult(call) {
    const methodOpt = Dictionaries.studentCallMethod.byId(call.method),
        results = methodOpt && methodOpt.results || [];
    if (!results.some(r => r === call.result)) {
        return Utils.message('common.validation.student.call.result.not.match.method');
    }
}

function validateCardPrice(card) {
    const visitTypeOpt = Dictionaries.visitType.byId(card.visitType) || {};
    if (visitTypeOpt.chargeless && card.price > 0) {
        return Utils.message('common.validation.card.positive.price.for.chargeless.card');
    }
}

function validateMaxDiscount(card) {
    if (+card.maxDiscount > +card.price) {
        return Utils.message('common.validation.card.discount.exceeds.price');
    }
}

function validateCardDuration(card) {
    if (+card.durationDays > +card.durationDaysMax) {
        return Utils.message('common.validation.card.min.duration.exceeds.max.duration');
    }
}

/**
 * Checks if val is defined in array as id of one of the element.
 * @param array objects with ids array
 * @param val value to check
 */
function validateIdInArray(array, val) {
    return Utils.select(!array.byId(val), Utils.message('common.validation.no.object.found.for.value'));
}

/**
 * Checks if val is number and greater than 0.
 * @param val value to check
 */
function validateNotNegativeNumber(val) {
    return validateByChain(val, [
        objValue => Utils.select(Utils.isDefined(objValue) && isNaN(objValue), Utils.message('common.validation.field.only.digits')),
        objValue => Utils.select(Utils.isDefined(objValue) && objValue < 0, Utils.message('common.validation.not.negative'))
    ]);
}

/**
 * Checks if val is defined.
 * @param val value to check
 */
function validateDefined(val) {
    return Utils.select(!Utils.isDefined(val), Utils.message('common.validation.field.not.empty'));
}

/**
 * Checks if val is not blank string.
 * @param val value to check
 */
function validateNotBlank(val) {
    return Utils.select(!Utils.isNotBlankString(val), Utils.message('common.validation.field.not.empty'));
}

function validateEmail(val) {
    return Utils.select(val && !Utils.validateEmail(val), Utils.message('common.validation.mail.not.valid'));
}

function validateDigits(val) {
    return Utils.select(val && !Utils.validateDigits(val), Utils.message('common.validation.field.only.digits'));
}

function validateRegexpFactory(regexp, message) {
    return val => Utils.select(val && !Utils.validateRegexp(val, regexp), message);
}

/**
 * Checks if val is not empty array.
 * @param val value to check
 * @param message custom validation message
 */
function validateNotEmptyArr(val, message = Utils.message('common.validation.field.not.empty')) {
    return Utils.select(!Utils.isNotEmptyArray(val), message);
}

/**
 * Creates functions that checks if val is greater than or equals to limit value
 * @param limitVal value to check against
 * @param message custom validation message
 */
function validateGeFactory(limitVal, message = Utils.message('common.validation.field.ge.val', limitVal)) {
    return val => Utils.select(!Utils.isDefined(val) || +val >= limitVal, null, message)
}

/**
 * Creates functions that checks if val is less than or equals to limit value
 * @param limitVal value to check against
 * @param message custom validation message
 */
function validateLeFactory(limitVal, message = Utils.message('common.validation.field.le.val', limitVal)) {
    return val => Utils.select(!Utils.isDefined(val) || +val <= limitVal, null, message)
}

/**
 * Creates functions that checks if string val has length not greater than maxLength
 * @param maxLength max length limit
 * @param message custom validation message
 */
function validateMaxLengthFactory(maxLength, message = Utils.message('common.validation.field.max.length', maxLength)) {
    return val => Utils.select(val && Utils.lengthMoreThan(val, maxLength), message)
}

/**
 * Creates functions that checks if string val has length not less than minLength
 * @param minLength min length limit
 * @param message custom validation message
 */
function validateMinLengthFactory(minLength, message = Utils.message('common.validation.field.min.length', minLength)) {
    return val => Utils.select(val && Utils.lengthLessThan(val, minLength), message)
}

/**
 * Creates functions that checks if string val has length not less than minLength
 * @param length min length limit
 * @param message custom validation message
 */
function validateSpecificLengthFactory(length, message = Utils.message('common.validation.field.specific.length', length)) {
    return val => Utils.select(val && Utils.lengthNotEqual(val, length), message)
}

/**
 * Creates functions that checks specified object by specified function
 * Suitable to include object based validations to chain
 * @param obj object that will be validated
 * @param fn function that will validate object
 */
function validateObjByFnFactory(obj, fn) {
    return val => fn(obj, val)
}

/**
 * Creates function that validates object specific fieldId.
 * If field focus is lost function is invoked with currentFieldId.
 * During save function is invoked with undefined currentFieldId, so every field is validated.
 * @param obj target object to check
 * @param currentFieldId field id to check
 * @param messages messages holder object
 * @return Function function which accepts (fieldId, validateFn, value) parameters.
 *
 * fieldId is field id to bind validation function to
 * validateFn is function that validates field. It accepts (val, obj) parameters
 * Val is obj value to be validated.
 * Obj is validated object.
 *
 * value is optional real value to pass to validation function
 */
function receiveValidateFunction(obj, currentFieldId, messages) {
    return function (fieldId, validateFn, value) {
        if (Utils.notDefinedOrEqual(currentFieldId, fieldId)) {
            messages[fieldId] = validateFn(Utils.isDefined(value) ? value : obj[fieldId], obj)
        }
    };
}

function validateByChain(obj, fns) {
    if (Utils.isNotEmptyArray(fns)) {
        for (let i = 0; i < fns.length; i++) {
            const fn = fns[i],
                message = fn(obj);
            if (message) {
                return message;
            }
        }
    }
    return null;
}

module.exports = Validators;
