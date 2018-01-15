'use strict';

const CommonUtils = require('./utils/CommonUtils'),
    SmsUtils = require('./utils/SmsUtils'),
    AffixUtils = require('./utils/AffixUtils'),
    AjaxUtils = require('./utils/AjaxUtils'),
    RelativeUtils = require('./utils/RelativeUtils');

const Utils = {
    affix: AffixUtils,
    sms: SmsUtils,
    ajax: AjaxUtils,
    relative: RelativeUtils,
};

module.exports = Object.assign(Utils, CommonUtils);
