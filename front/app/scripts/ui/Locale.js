'use strict';

const rcCalendar_zh_CN = require('rc-calendar/lib/locale/zh_CN'),
    rcCalendar_en_US = require('rc-calendar/lib/locale/en_US'),
    LocalStorageService = require('./services/LocalStorageService');

const Locale = {
    curLocale: 'cn',
    existingLocales: ['en', 'cn'],

    rcCalendarLocales: {en: rcCalendar_en_US, cn: rcCalendar_zh_CN},
    shortMonthsLabels: {
        en: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
        cn: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    },

    current(){
        let newLocale = arguments[0];
        if (newLocale) {
            if (Locale.existingLocales.indexOf(newLocale) >= 0) {
                Locale.curLocale = newLocale;
                LocalStorageService.setLocale(newLocale);
            } else {
                throw Error('Unable to set locale ' + newLocale + '. Valid locales are ' + this.existingLocales.join(', '))
            }
        } else {
            return Locale.curLocale;
        }
    },

    shortMonths(){
        return Locale.shortMonthsLabels[Locale.curLocale]
    },

    rcCalendarLocale(){
        return Locale.rcCalendarLocales[Locale.curLocale]
    },

    pluralize(count, word, pluralForm){
        const pluralFn = Locale.curLocale === 'en' ? pluralizeEn : pluralizeCn;
        return pluralFn(count, word, pluralForm)
    },

    printCount(count, word, pluralForm) {
        return count + ' ' + Locale.pluralize(count, word, pluralForm)
    }
};

function pluralizeEn(count, word, pluralForm) {
    return count === 1 ? word : (pluralForm || word + 's')
}

function pluralizeCn(count, word, pluralForm) {
    return count === 1 ? word : (pluralForm || word)
}

// @if ENV='dev'
Locale.curLocale = 'en';
// @endif

const savedLocale = LocalStorageService.getLocale();
if (savedLocale) {
    Locale.curLocale = savedLocale;
}

module.exports = Locale;
