'use strict';

const DataService = require('../services/DataService'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Utils = require('../Utils');

const creators = _enhanceCreators({
    setDate: (date) => ({date}),
    setPlanLessonFilter: (fieldId, newValue) => ({fieldId, newValue}),
    setPaymentsFilter: (searchRequest) => ({searchRequest}),
    setPaymentsView: (view) => ({view}),
    setLessonsView: (view) => ({view}),
    toggleSelectedObject: (entity, obj) => ({entity, obj}),
    hidePaymentPhotos: () => ({}),
    showCarouselGallery: (entity, obj) => ({entity, obj}),
    hideCarouselGallery: () => ({}),
    setLessonProcedure: (lessonProcedure) => ({lessonProcedure}),
    selectLesson: (lesson) => ({lesson}),
    pickLesson: (lessonId) => ({lessonId}),
    setSearchRequest: (entity, request) => ({entity, request}),
    selectSidebar: (sidebarId) => ({sidebarId}),
    setPageMode: (entity, page, fieldId, mode, options) => ({entity, page, fieldId, mode, options}),
    setLessonAgeGroup: (lesson, ageGroup) => ({lesson, ageGroup}),

    /**
     * Action creator defining initialization of new entity, usually after move to new page
     * @param entity name of entity that is beigng initialized
     * @returns {{entity: *}}
     */
    initEntity: (entity) => ({entity}),

    /**
     * Action creator defining that ajax request is about to be initiated
     * @param entity name of entity that is fetched, modified or deleted by request
     * @param operation name of operation that is in progress
     * @param request payload of request
     * @param context context that holds request related data, but that is not send to server
     * @returns {{}} action declaration
     */
    ajaxStarted: (entity, operation, request, context) => ({entity, operation, request, context}),

    /**
     * Action creator defining that ajax response was completed
     * @param entity name of entity that is fetched, modified or deleted by request
     * @param operation name of operation that is in progress
     * @param response response data
     * @param request request data
     * @param context context that holds request related data, but that is not send to server
     * @returns {{}} action declaration
     */
    ajaxFinishedSuccess: (entity, operation, response, request, context) => ({
        entity,
        operation,
        response,
        request,
        context
    }),

    /**
     * Action creator defining that ajax response was completed
     * @param entity name of entity that is fetched, modified or deleted by request
     * @param operation name of operation that is in progress
     * @param error error data
     * @param request request data
     * @param context context that holds request related data, but that is not send to server
     * @returns {{}} action declaration
     */
    ajaxFinishedError: (entity, operation, error, request, context) => ({entity, operation, error, request, context}),


    setEntityValue: (entity, fieldId, newValue, options = defaultSetValueOptions()) => ({
        entity,
        fieldId,
        newValue,
        options
    }),
    setEntityValues: (entity, obj, options = defaultSetValueOptions()) => ({
        entity,
        obj,
        options
    }),
    setValidationMessages: (entity, messages) => ({entity, messages}),
    clearValidationMessages: (entity) => ({entity}),
    clearPhotos: (entity) => ({entity}),
    filesUploaded: (fileNames, entity, fieldId) => ({entity, fieldId, fileNames}),
    changeShownFile: (fileName, entity, fieldId) => ({entity, fieldId, fileName}),
    addStudentRelative: () => ({}),
    removeStudentRelative: () => ({}),
    selectEntity: (entity, id, ownerEntity, page) => ({entity, id, ownerEntity, page}),
    addRegRelativeChild: () => ({}),
    removeRegRelativeChild: () => ({}),
    selectRegRelativeChild: (index) => ({index}),
    studentRelativeFilesUploaded: (fileNames) => ({fileNames}),
    studentRelativeChangeShownFile: (fileName) => ({fileName}),
    startMoveStudentSlot: (lessonId, slotId) => ({lessonId, slotId}),
    setLessonTimeCategory: category => ({category}),
    setLessonVisitType: visitType => ({visitType}),
    toggleNavSidebar: (opened) => ({opened})
});
creators._dispatch = null;

function defaultSetValueOptions() {
    return {
        refreshDependentFields: true
    }
}

function _enhanceCreators(creators) {
    let resultCreators = {};
    Object.keys(creators).forEach(function (prop) {
        resultCreators[prop] = function () {
            const creator = creators[prop];
            let action = creator.apply(creator, arguments);
            action.type = prop;
            return action;
        }
    });
    return resultCreators;
}


function ajaxFn(entity, operation, requestFn, requestData, context) {
    const self = this;
    return function (dispatch) { //thunk function
        dispatch(creators.ajaxStarted(entity, operation, requestData, context));

        return requestFn.call(self, requestData).then(
            data => {
                dispatch(creators.ajaxFinishedSuccess(entity, operation, data, requestData, context));
                return Promise.resolve(data)
            },
            error => {
                if (Config.loggingEnabled) {
                    console.log("Error during ajax operation", entity, operation, error);
                }
                dispatch(creators.ajaxFinishedError(entity, operation, error, requestData, context));
                return Promise.reject(error)
            }
        );
    }
}

function ajaxResource(entity, resource = entity) {
    const operations = DataService.operations[resource];
    return {
        findAll(searchRequest){
            const request = Utils.extend(searchRequest, {pageRecordsCount: Config.pageRecordsCount});
            return ajaxFn(entity, 'findAll', operations.findAll, request)
        },
        findOne(id){
            return ajaxFn(entity, 'findOne', operations.findOne, id)
        },
        save(obj){
            return ajaxFn(entity, 'save', obj.id ? operations.modify : operations.create, obj)
        },
        delete(id){
            return ajaxFn(entity, 'delete', operations.delete, id)
        }
    }
}

creators.ajax = {
    dictionaries: {
        load(){
            return ajaxFn('dictionaries', 'load', DataService.operations.dictionaries.load)
        }
    },
    register: {
        verifyMobileNumber(mobile){
            return ajaxFn('regRelatives', 'verifyMobileNumber', DataService.operations.sms.verifyMobile, mobile)
        },
        register(user){
            return ajaxFn('regRelatives', 'register', DataService.operations.auth.register, user)
        }
    },
    profile: {
        save(obj){
            return ajaxFn('profile', 'save', DataService.operations.profile.save, obj)
        }
    },
    children: {
        load(){
            return ajaxFn('children', 'load', DataService.operations.children.load, {})
        },
        save(obj){
            return ajaxFn('children', 'save', DataService.operations.children.save, obj)
        },
    },
    lessons: {
        load(lessonsDataRequest){
            const defaultStartLoadMt = moment().subtract(Config.lessonSlotsBackwardLoadDaysCount, 'd'),
                defaultEndLoadMt = moment().add(Config.lessonSlotsForwardLoadDaysCount, 'd'),
                defaultRequest = Utils.getLessonsDataRequest(defaultStartLoadMt, defaultEndLoadMt);

            const request = Object.assign(defaultRequest, lessonsDataRequest);
            return ajaxFn('lessons', 'load', DataService.operations.lessons.load, request)
        },
        student(studentId){
            const request = {selection: studentId, columnsGroup: "withCards"};
            return ajaxFn('lessons', 'student', DataService.operations.students.findAll, request)
        },
        plan(studentId, cardId, cardVisitType, lessonIds, repeatWeekly, lessonProcedure){
            return ajaxFn('lessons', 'plan', DataService.operations.lessons.plan,
                {studentId, cardId, cardVisitType, lessonIds, repeatWeekly, lessonProcedure})
        },
        unplan(lessonId, slotId, visitType, status){
            return ajaxFn('lessons', 'unplan', DataService.operations.lessons.unplan,
                {lessonId, slotId, visitType, status})
        },
        transfer(studentId, cardId, targetStudentId, transferCardId, lessonIds, repeatWeekly){
            return ajaxFn('lessons', 'transfer', DataService.operations.lessons.transfer,
                {studentId, cardId, targetStudentId, transferCardId, lessonIds, repeatWeekly})
        },
        visit(lessonId, slotId){
            return ajaxFn('lessons', 'visit', DataService.operations.lessons.visit,
                {lessonId, slotId})
        },
        miss(lessonId, slotId){
            return ajaxFn('lessons', 'miss', DataService.operations.lessons.miss,
                {lessonId, slotId})
        },
        cancel(lessonId, slotId, visitType, repeatWeekly, confirmed = false){
            return ajaxFn('lessons', 'cancel', DataService.operations.lessons.cancel,
                {lessonId, slotId, visitType, repeatWeekly, confirmed})
        },
        revoke(lessonId){
            return ajaxFn('lessons', 'revoke', DataService.operations.lessons.revoke,
                {lessonId})
        },
        close(lessonId){
            return ajaxFn('lessons', 'close', DataService.operations.lessons.close,
                {lessonId})
        },
        suspend(cardId, fromDate, studentId, cardVisitType){
            return ajaxFn('lessons', 'suspend', DataService.operations.lessons.suspend,
                {cardId, fromDate, studentId, cardVisitType})
        },
        removeFile(lessonId, fileName){
            return ajaxFn('lessons', 'removeFile', DataService.operations.lessons.removeFile,
                {lessonId, fileName}
            )
        },
        findPhotos(lessonId){
            return ajaxFn('lessons', 'findPhotos', DataService.operations.lessons.findPhotos,
                {lessonId}
            )
        },
        setPresenceInSchool(studentId, presentInSchool){
            return ajaxFn('lessons', 'setPresenceInSchool', DataService.operations.lessons.setPresenceInSchool,
                {studentId, presentInSchool}
            )
        },
        findStudentPlannedLessons(studentId){
            return ajaxFn('lessons', 'findStudentPlannedLessons', DataService.operations.lessons.findStudentPlannedLessons,
                {studentId}
            )
        }
    },
    students: Utils.extend(
        ajaxResource('students'),
        {
            removeFile(studentId, fileName){
                return ajaxFn('students', 'removeFile', DataService.operations.students.removeFile,
                    {studentId, fileName}
                )
            },
            setPrimaryFile(studentId, fileName){
                return ajaxFn('students', 'setPrimaryFile', DataService.operations.students.setPrimaryFile,
                    {studentId, fileName}
                )
            },
            relatives: {
                verifyMobileNumber(mobile){
                    return ajaxFn('studentRelatives', 'verifyMobileNumber', DataService.operations.sms.verifyMobile, mobile)
                },
                checkConfirmation(relativeIndex, mobile, confirmationId, code){
                    return ajaxFn('studentRelatives', 'checkConfirmation', DataService.operations.confirmation.check,
                        {relativeIndex, mobile, confirmationId, code})
                },
                removeFile(studentId, relativeId, fileName){
                    return ajaxFn('studentRelatives', 'removeFile', DataService.operations.students.relatives.removeFile,
                        {studentId, relativeId, fileName}
                    )
                },
                setPrimaryFile(studentId, relativeId, fileName){
                    return ajaxFn('studentRelatives', 'setPrimaryFile', DataService.operations.students.relatives.setPrimaryFile,
                        {studentId, relativeId, fileName}
                    )
                },
                saveNotifications(studentId, relatives) {
                    return ajaxFn('studentRelatives', 'saveNotifications', DataService.operations.students.relatives.saveNotifications,
                        {studentId, relatives}
                    )
                }
            }
        }),
    siblings: Utils.extend(ajaxResource('siblings', 'students')),

    studentCards: Utils.extend(ajaxResource('studentCards'), {
        addPayment(request){
            return ajaxFn('studentCards', 'addPayment', DataService.operations.studentCards.addPayment, request)
        },
        removeFile(cardId, fileName){
            return ajaxFn('studentCards', 'removeFile', DataService.operations.studentCards.removeFile,
                {cardId, fileName}
            )
        },
        findPhotos(cardId){
            return ajaxFn('studentCards', 'findPhotos', DataService.operations.studentCards.findPhotos,
                {cardId}
            )
        }
    }),
    studentCalls: Utils.extend(ajaxResource('studentCalls'), {
        findAll(searchRequest){
            const request = Utils.extend(searchRequest, {
                pageRecordsCount: Config.pageRecordsCount,
                student: searchRequest.student ? {id: searchRequest.student.id} : null
            });
            return ajaxFn('studentCalls', 'findAll', DataService.operations.studentCalls.findAll, request)
        },
    }),
    studentDashboard: {
        findOne(id){
            return ajaxFn('studentDashboard', 'findOne', DataService.operations.studentDashboard.findOne, id)
        },
        findLessons(searchRequest){
            return ajaxFn('studentDashboard', 'findLessons', DataService.operations.studentDashboard.findLessons, searchRequest)
        },
    },
    promotionSources: ajaxResource('promotionSources'),
    promotionDetails: ajaxResource('promotionDetails'),
    cards: ajaxResource('cards'),
    teachers: ajaxResource('teachers'),
    lessonTemplates: Utils.extend(ajaxResource('lessonTemplates')),
    payments: Utils.extend(
        ajaxResource('payments'),
        {
            stat(searchRequest){
                return ajaxFn('payments', 'stat', DataService.operations.payments.stat, searchRequest)
            },
            balance(searchRequest){
                return ajaxFn('payments', 'balance', DataService.operations.payments.balance, searchRequest)
            },
            listPhotos(payment, fieldId){
                return ajaxFn('payments', 'listPhotos', DataService.operations.payments.listPhotos,
                    {paymentId: payment.id, fieldId},
                    payment)
            },
            removeFile(paymentId, fieldId, fileName){
                return ajaxFn('payments', 'removeFile', DataService.operations.payments.removeFile,
                    {paymentId, fieldId, fileName}
                )
            }
        }),
    cities: ajaxResource('cities'),
    schools: ajaxResource('schools'),
    accounts: ajaxResource('accounts'),
    categories: ajaxResource('categories'),
    users: ajaxResource('users'),
    roles: ajaxResource('roles'),
    homework: Utils.extend(ajaxResource('homework'),
        {
            removeFile(homeworkId, fileName){
                return ajaxFn('homework', 'removeFile', DataService.operations.homework.removeFile,
                    {homeworkId, fileName}
                )
            }
        }
    ),
    preferences: {
        get(id){
            return ajaxFn('preference', 'get', DataService.operations.preferences.get, id)
        },
        set(id, preference){
            return ajaxFn('preference', 'set', DataService.operations.preferences.set, {id, preference})
        },
    },
};


module.exports = creators;
