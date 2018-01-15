'use strict';

//noinspection JSUnresolvedFunction
const Config = require('../Config'),
    Utils = require('../Utils'),
    Dictionaries = require('../Dictionaries'),
    LocalStorageService = require('./LocalStorageService');

const DataService = {
    tokenProp: 'token',
    postPutHeaders: {
        'Content-Type': 'application/json;charset=UTF-8'
    },
    deleteHeaders: {
        'Access-Control-Request-Headers': 'Origin',
        'x-fix-spring-cors': 'fix'
    },

    status: {
        badRequest: '400',
        conflict: '409',
    },

    /**
     *  Array of Functions to process each response
     */
    responseProcessors: [],

    /**
     *  Array of listener Functions that will be notified about failed request.
     *  They will be invoked with argument errorObj:{status, data}
     */
    afterRequestFailedListeners: [],

    /**
     * Makes GET request to server
     * @param url relative server url. Will be prefixed with Config.baseServiceUrl
     * @param options additional request options
     * @return {*} promise
     */
    get(url, options){
        //noinspection JSUnresolvedFunction
        return this.fetchData(new Request(Config.baseServiceUrl + url), options)
    },

    /**
     * Makes POST request to server
     * @param url relative server url. Will be prefixed with Config.baseServiceUrl
     * @param data object that will be serialized to JSON and sent to server
     * @param options additional request options
     * @return {*} promise
     */
    post(url, data, options){
        const self = this;
        //noinspection JSUnresolvedFunction
        return self.fetchData(new Request(Config.baseServiceUrl + url),
            Object.assign({
                method: 'POST',
                headers: self.postPutHeaders,
                body: JSON.stringify(data || {})
            }, options))
    },

    /**
     * Makes PUT request to server
     * @param url relative server url. Will be prefixed with Config.baseServiceUrl
     * @param data object that will be serialized to JSON and sent to server
     * @param options additional request options
     * @return {*} promise
     */
    put(url, data, options){
        const self = this;
        //noinspection JSUnresolvedFunction
        return self.fetchData(new Request(Config.baseServiceUrl + url),
            Object.assign({
                method: 'PUT',
                headers: self.postPutHeaders,
                body: JSON.stringify(data || {})
            }, options))
    },

    /**
     * Makes DELETE request to server
     * @param url relative server url. Will be prefixed with Config.baseServiceUrl
     * @param options additional request options
     * @return {*} promise
     */
    delete(url, options){
        const self = this;
        //noinspection JSUnresolvedFunction
        return self.fetchData(new Request(Config.baseServiceUrl + url),
            Object.assign({
                method: 'DELETE',
                headers: self.deleteHeaders
            }, options))
    },

    fetchData (request, init){
        init = this.appendJwtTokenHeader(init);

        const self = this;
        try {
            //noinspection JSUnresolvedFunction
            return fetch(request, init).then(
                response => {
                    self.responseProcessors.forEach(fn => fn(response));
                    if (response.ok) {
                        //noinspection JSUnresolvedFunction
                        return response.json().then(
                            data => data,
                            () => {
                                if (!init || init.method === 'GET') {
                                    return self.onAfterRequestFailed(self.error('404')); //no content returned from server
                                } else {
                                    return {};
                                }
                            }
                        )
                    } else {
                        //noinspection JSUnresolvedFunction
                        return response.json().then(
                            data => self.onAfterRequestFailed(Utils.assign(data, {status: response.status + ''}) || self.error('0')),
                            error => self.onAfterRequestFailed(self.error(response.status))
                        );
                    }
                }
            )
        } catch (e) {
            //noinspection JSUnresolvedVariable
            return self.onAfterRequestFailed({text: e.stack || e.toString()});
        }
    },

    onAfterRequestFailed(error){
        this.afterRequestFailedListeners.forEach(listener => listener(error));
        return Promise.reject(error);
    },

    appendJwtTokenHeader(init){
        let token = LocalStorageService.getToken();
        if (token) {
            init = Utils.merge(init, {
                headers: {
                    'Authorization': Config.authSchemePrefix + token
                }
            })
        }
        return init
    },

    /**
     * Append saved auth token to query part of secured resource like image, file, etc.
     * @param queryPart query part of url to be appended with token. Should be null, undefined, empty or in format: ?param1=value1&param2=value2
     * @return string modified query part in format ?param1=value1&param2=value2&token=secret
     */
    appendJwtTokenParam(queryPart){
        let token = LocalStorageService.getToken();
        if (token) {
            const tokenParam = "auth=" + token;
            if (queryPart && queryPart.startsWith("?")) {
                return queryPart + "&" + tokenParam
            } else {
                return "?" + tokenParam
            }
        }
        return queryPart
    },

    unknownErrorMessage: Dictionaries.responseStatus.byId('0').title,

    error (status){
        const statusStr = status + '',
            opt = Dictionaries.responseStatus.byId(statusStr);
        return {status: statusStr, text: opt && opt.title || this.unknownErrorMessage}
    },

    resourceUrl(url, queryPart){
        return Config.baseServiceUrl + url + this.appendJwtTokenParam(queryPart)
    },

    ajaxResource(entitiesId){
        const self = this;
        return {
            findAll(searchRequest){
                return self.get('data/' + entitiesId + '/' + Utils.objToQueryString(searchRequest))
            },
            findOne(id){
                return self.get('data/' + entitiesId + '/' + id)
            },
            create(obj){
                return self.post('data/' + entitiesId + '/', obj)
            },
            modify(obj){
                return self.put('data/' + entitiesId + '/' + obj.id, obj)
            },
            delete(id){
                return self.delete('data/' + entitiesId + '/' + id)
            }
        }
    },

    addResponseProcessor(fn){
        if (Utils.isFunction(fn)) {
            this.responseProcessors.push(fn)
        } else {
            console.error('Unable to register response processor. It should be function')
        }
    },

    removeResponseProcessor(fn){
        Utils.arr.remove(this.responseProcessors, fn)
    },

    addAfterRequestFailedListener(fn){
        if (Utils.isFunction(fn)) {
            this.afterRequestFailedListeners.push(fn)
        } else {
            console.error('Unable to register after request failed listener. It should be function')
        }
    },

    removeAfterRequestFailedListener(fn){
        Utils.arr.remove(this.afterRequestFailedListeners, fn)
    }
};

DataService.operations = {
    dictionaries: {
        load(){
            return DataService.get('data/dicts/')
        }
    },
    profile: {
        save(obj) {
            return DataService.post('data/profile/save', obj)
        }
    },
    children: {
        load(){
            return DataService.get('data/children/load')
        },
        save(obj) {
            return DataService.post('data/children/save', obj)
        }
    },
    lessons: {
        load: request => DataService.get('data/lessons/' + Utils.objToQueryString(request)),
        plan: request => DataService.post('data/lessons/plan', request),
        unplan: request => DataService.post('data/lessons/unplan', request),
        transfer: request => DataService.post('data/lessons/transfer', request),
        visit: request => DataService.post('data/lessons/visit', request),
        miss: request => DataService.post('data/lessons/miss', request),
        cancel: request => DataService.post('data/lessons/cancel', request),
        revoke: request => DataService.post('data/lessons/revoke', request),
        close: request => DataService.post('data/lessons/close', request),
        suspend: request => DataService.post('data/lessons/suspend', request),
        removeFile: ({lessonId, fileName}) => DataService.delete('data/lessons/' + lessonId + '/photos/' + fileName),
        findPhotos: ({lessonId}) => DataService.get('data/lessons/' + lessonId + '/photos/'),
        setPresenceInSchool: request => DataService.put('data/lessons/setPresenceInSchool', request),
        findStudentPlannedLessons: request => DataService.get('data/lessons/planned/student/' + request.studentId)
    },

    students: Utils.extend(
        DataService.ajaxResource('students'),
        {
            removeFile({studentId, fileName}){
                return DataService.delete('data/students/' + studentId + '/photos/' + fileName)
            },
            setPrimaryFile({studentId, fileName}){
                return DataService.put('data/students/' + studentId + '/photos/' + fileName + '/primary')
            },
            relatives: {

                removeFile({studentId, relativeId, fileName}){
                    return DataService.delete('data/students/' + studentId + '/relatives/' + relativeId + '/photos/' + fileName)
                },
                setPrimaryFile({studentId, relativeId, fileName}){
                    return DataService.put('data/students/' + studentId + '/relatives/' + relativeId + '/photos/' + fileName)
                },
                saveNotifications(studentRelatives) {
                    return DataService.post('data/students/student/relatives/notifications/', studentRelatives)
                }
            },
            total: {
                lessons(){
                    return DataService.put('data/students/total/lessons/')
                }
            }
        }
    ),
    studentCards: Utils.extend(DataService.ajaxResource('studentCards'), {
        addPayment(request){
            return DataService.put('data/studentCards/' + request.card.id + '/payments', request)
        },
        removeFile({cardId, fileName}){
            return DataService.delete('data/studentCards/' + cardId + '/photos/' + fileName)
        },
        findPhotos({cardId}){
            return DataService.get('data/studentCards/' + cardId + '/photos/')
        }
    }),
    studentCalls: DataService.ajaxResource('studentCalls'),
    studentDashboard: {
        findOne(id){
            return DataService.get('data/studentDashboard/' + id)
        },
        findLessons(request){
            return DataService.get('data/studentDashboard/' + request.id + '/lessons' + Utils.objToQueryString(Utils.omit(request, ['id'])))
        },
    },
    promotionSources: DataService.ajaxResource('promotionSources'),
    promotionDetails: DataService.ajaxResource('promotionDetails'),
    cards: DataService.ajaxResource('cards'),
    teachers: DataService.ajaxResource('teachers'),
    lessonTemplates: DataService.ajaxResource('lessonTemplates'),
    kindergardens: {
        findAll(text){
            return DataService.get('data/kinderGardens/' + Utils.objToQueryString({text}))
        }
    },
    payments: Utils.extend(DataService.ajaxResource('payments'),
        {
            stat(request){
                return DataService.get('data/payments/stat' + Utils.objToQueryString(request))
            },
            balance(request){
                return DataService.get('data/payments/balance' + Utils.objToQueryString(request))
            },
            removeFile({paymentId, fieldId, fileName}){
                return DataService.delete('data/payments/' + paymentId + '/photos/' + fieldId + '/' + fileName)
            },
            listPhotos({paymentId, fieldId}){
                return DataService.get('data/payments/' + paymentId + '/photos/' + fieldId)
            }
        }
    ),
    cities: DataService.ajaxResource('cities'),
    schools: DataService.ajaxResource('schools'),
    accounts: DataService.ajaxResource('accounts'),
    categories: DataService.ajaxResource('categories'),
    sms: {
        verifyMobile(mobile){
            return DataService.post('data/sms/verify/' + mobile)
        }
    },
    confirmation: {
        check(request){
            return DataService.put('data/confirmation/check', request)
        }
    },
    auth: {
        login(loginRequest){
            return DataService.post('data/auth/login', loginRequest)
        },
        register(user) {
            return DataService.post('data/auth/register', user)
        }
    },
    users: DataService.ajaxResource('users'),
    roles: DataService.ajaxResource('roles'),
    homework: Utils.extend(
        DataService.ajaxResource('homework'),
        {
            removeFile({homeworkId, fileName}){
                return DataService.delete('data/homework/' + homeworkId + '/attachments/' + fileName)
            }
        }),
    preferences: {
        get(id){
            return DataService.get('data/preferences/' + id)
        },
        set({id, preference}){
            return DataService.put('data/preferences/' + id, preference)
        }
    },
};

DataService.urls = {
    lessons: {
        downloadPhoto(lessonId, name){
            return DataService.resourceUrl('data/lessons/' + lessonId + '/photos/' + name)
        },
        uploadPhoto(lessonId){
            return DataService.resourceUrl('data/lessons/' + lessonId + '/photos')
        }
    },
    students: {
        downloadPhoto(studentId, name){
            return DataService.resourceUrl('data/students/' + studentId + '/photos/' + name)
        },
        uploadPhoto(studentId){
            return DataService.resourceUrl('data/students/' + studentId + '/photos')
        },
        relatives: {
            downloadPhoto(studentId, relativeId, name){
                return DataService.resourceUrl('data/students/' + studentId + '/relatives/' + relativeId + '/photos/' + name)
            },
            uploadPhoto(studentId, relativeId){
                return DataService.resourceUrl('data/students/' + studentId + '/relatives/' + relativeId + '/photos')
            }
        }
    },
    studentCards: {
        downloadPhoto(cardId, name){
            return DataService.resourceUrl('data/studentCards/' + cardId + '/photos/' + name)
        },
        uploadPhoto(cardId){
            return DataService.resourceUrl('data/studentCards/' + cardId + '/photos')
        }
    },
    payments: {
        downloadPhoto(paymentId, fieldId, photoName){
            return DataService.resourceUrl('data/payments/' + paymentId + '/photos/' + fieldId + '/' + photoName)
        },
        uploadPhoto(paymentId, fieldId){
            return DataService.resourceUrl('data/payments/' + paymentId + '/photos/' + fieldId)
        },
        exportList(searchRequest){
            return DataService.resourceUrl('data/payments/export/payments', Utils.objToQueryString(searchRequest))
        }
    },
    homework: {
        downloadHomework(homeworkId, fileName){
            return DataService.resourceUrl('data/homework/' + homeworkId + '/attachments/' + fileName)
        },
        uploadHomework(homeworkId){
            return DataService.resourceUrl('data/homework/' + homeworkId + '/attachments')
        }
    }
};

//noinspection JSUnresolvedVariable
module.exports = DataService;
