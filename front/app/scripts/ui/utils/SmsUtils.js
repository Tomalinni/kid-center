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


const SmsUtils = {

    mapSendSmsStatus(status) {
        return SmsUtils.smsStatusMessages[status] || SmsUtils.smsStatusMessages.unknownError;
    },

    smsStatusMessages: {
        incorrectNumberFormat: 'Incorrect phone number format. 11 digits number is expected.',
        invalidMessage: 'Sms provider doesn`t accept sent message.',
        noMoney: 'Insufficient money to send message.',
        invalidNumber: 'Sms provider doesn`t accept phone number.',
        accountNotExist: 'Sms provider can not find our account.',
        notAuthorized: 'Sms provider can not authorize our account.',
        accountBlocked: 'Sms provider blocked our account.',
        malformedRequest: 'Sms provider doesn`t accept request.',
        providerConnectionError: 'Can not connect to sms provider server.',

        unknownError: 'Unknown delivery error. Look at server log.'
    }
};

module.exports = SmsUtils;
