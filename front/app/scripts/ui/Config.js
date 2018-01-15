'use strict';

const Config = {
    registrationAvailable: false,
    currentTimeZone: 'Etc/GMT-8',
    baseServiceUrl: '/', //flag to retrieve data from client datastores, for ui development only
    dateFormat: 'DD.MM.YYYY',
    dateTimeFormat: 'DD.MM.YYYY-HHmm',
    humanDateTimeFormat: 'DD.MM.YYYY HH:mm',
    shortDateFormat: 'DD.MM.YY',
    yearMonthDateFormat: 'MM.YY',
    dayMonthDateFormat: 'DD.MM',
    dayMonthDateTimeFormat: 'DD.MM HH:mm',
    pxInMin: 1,
    minStudentAgeYears: 0.5,
    maxStudentAgeYears: 10,
    maxSlotsSoftLimit: 8,
    maxSlotsHardLimit: 10,
    lessonSlotsVisibleDaysCount: 7,
    lessonSlotsForwardLoadDaysCount: 21,
    lessonSlotsBackwardLoadDaysCount: 7,
    showVisitSummaryForUndefinedLessons: true,
    pageRecordsCount: 50,
    uploadPhotoMaxSizeBytes: 25 * 1024 * 1024,
    inputValuePropagationDebounceTimeoutMs: 800,
    maxCategoryLevel: 4,
    loggingEnabled: true,
    categoryFields: ['category1', 'category2', 'category3', 'category4', 'category5'],
    authSchemePrefix: 'Bearer ',
    backDatePlanningEnabled: false,
    notificationFadeTimeMs: 5000
};
Config.uploadPhotoMaxSizeMB = Math.floor(Config.uploadPhotoMaxSizeBytes * 100 / (1024 * 1024)) / 100;

//@exclude
// Config.baseServiceUrl='/';
//@endexclude

module.exports = Config;
