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
    Messages = require('./Messages'),
    Config = require('./Config'),
    Utils = require('./Utils'),
    Dictionaries = require('./Dictionaries'),
    Actions = require('./actions/Actions'),
    DialogService = require('./services/DialogService'),
    DataService = require('./services/DataService');

const planningProcedures = ['plan'];

const LessonUtils = {

    lessonSubjectFromId(lessonId) {
        return lessonId && Dictionaries.lessonSubject.byFirstLetter(lessonId.charAt(0));
    },
    canBeVisited(lessonMt, studentSlotStatus){
        return Utils.isLessonStarted(lessonMt) && Utils.arr.contains(['planned', 'missed'], studentSlotStatus)
    },
    canBeMissed(lessonMt, studentSlotStatus){
        return Utils.isLessonStarted(lessonMt) && Utils.arr.contains(['planned', 'visited'], studentSlotStatus)
    },
    canBeCanceled(lessonId, studentSlotStatus){
        const subjectOpt = LessonUtils.lessonSubjectFromId(lessonId);
        return !Utils.isLessonFinished(lessonId, subjectOpt.duration) && studentSlotStatus === 'planned'
    },
    willBeToday(lessonMt){
        return moment(lessonMt).startOf('d').diff(Utils.currentMoment().startOf('d'), 'd') === 0
    },
    availableStudentSlotActions(lessonId, slot, studentWillPresentAtLesson, lessonsProps){
        const actions = {},
            lessonMt = Utils.lessonMomentFromId(lessonId);

        if (LessonUtils.willBeToday(lessonMt)) {
            actions.setPresence = {
                title: Utils.select(studentWillPresentAtLesson, Utils.message('common.lessons.student.presence.action.came.out'), Utils.message('common.lessons.student.presence.action.came.in')),
                fn: dispatch => LessonUtils.doWithStudentPresenceConfirmation(studentWillPresentAtLesson,
                    () => dispatch(Actions.ajax.lessons.setPresenceInSchool(slot.studentId, !studentWillPresentAtLesson)))
            }
        }

        if (LessonUtils.canBeVisited(lessonMt, slot.status)) {
            actions.visit = {
                title: Utils.message('common.lesson.manage.action.visit'),
                fn: dispatch => LessonUtils.doWithConfirmation(
                    () => LessonUtils.doAndThenTryLoadLessons(lessonsProps,
                        () => dispatch(Actions.ajax.lessons.visit(lessonId, slot.id)))
                )
            }
        }

        if (LessonUtils.canBeMissed(lessonMt, slot.status)) {
            actions.miss = {
                title: Utils.message('common.lesson.manage.action.miss'),
                fn: dispatch => LessonUtils.doWithConfirmation(
                    () => LessonUtils.doAndThenTryLoadLessons(lessonsProps,
                        () => dispatch(Actions.ajax.lessons.miss(lessonId, slot.id)))
                )
            }
        }

        if (LessonUtils.canBeCanceled(lessonId, slot.status)) {
            actions.cancel = {
                title: Utils.message('common.lesson.manage.action.cancel'),
                fn: dispatch => LessonUtils.doWithStudentPresenceConfirmation(studentWillPresentAtLesson,
                    () => LessonUtils.doAndThenTryLoadLessons(lessonsProps,
                        () => LessonUtils.doWithSingleOrRepeatedLesson(slot.repeatsLeft > 1,
                            () => LessonUtils.doWithResponseWarningConfirmation(confirmed => dispatch(Actions.ajax.lessons.cancel(lessonId, slot.id, slot.visitType, false, confirmed))),
                            () => LessonUtils.doWithResponseWarningConfirmation(confirmed => dispatch(Actions.ajax.lessons.cancel(lessonId, slot.id, slot.visitType, true, confirmed)))
                        )
                    )
                )
            }
        }

        actions.unplan = {
            title: Utils.message('common.lesson.manage.action.unplan'),
            fn: dispatch => LessonUtils.doWithStudentPresenceConfirmation(studentWillPresentAtLesson,
                () => LessonUtils.doAndThenTryLoadLessons(lessonsProps,
                    () => dispatch(Actions.ajax.lessons.unplan(lessonId, slot.id, slot.visitType, slot.status)))
            )
        };
        return actions;
    },

    doWithStudentPresenceConfirmation(studentPresentInSchool, actionFn){
        const confirmationMessage = Utils.select(studentPresentInSchool,
            Utils.message('common.lessons.confirm.action.for.student.in.school'),
            Utils.message('common.dialog.confirm.common.message'));
        DialogService.doWithConfirmation(confirmationMessage, actionFn)
    },

    /**
     * @param ajaxActionFn function that accepts confirm flag, makes ajax request and returns promise with ajax action object
     * Ajax action object should have response object with optional warning message value
     * Warning will be shown in confirmation dialog
     * @return Promise
     */
    doWithResponseWarningConfirmation(ajaxActionFn){
        const successCallback = response => {
            if (response.warning) {
                return DialogService.doWithConfirmation(Utils.messages(response.warning),
                    () => ajaxActionFn(true).then(successCallback)
                )
            }
        };

        //At first try to invoke action without confirm flag
        return ajaxActionFn(false).then(successCallback);
    },

    doWithConfirmation(actionFn){
        DialogService.doWithConfirmation(Utils.message('common.dialog.confirm.common.message'), actionFn)
    },

    doWithSingleOrRepeatedLesson(isRepeatedLesson, singleLessonFn, repeatedLessonFn){
        if (isRepeatedLesson) {
            return DialogService.modal({
                title: 'Single or repeated?',
                content: 'Single or repeated?',
                buttons: {
                    'single': {
                        label: 'Single',
                        reject: singleLessonFn
                    },
                    'repeated': {
                        label: 'Repeated',
                        resolve: repeatedLessonFn
                    }
                }
            })
        } else {
            return singleLessonFn()
        }
    },

    doAndThenTryLoadLessons(lessonsProps, promiseFn){
        let promise = promiseFn();
        if (lessonsProps) {
            const loadLessonsPromiseFn = () => LessonUtils.actions.loadLessonsData(lessonsProps, lessonsProps.startDate, true),
                loadDataPromiseFns = [loadLessonsPromiseFn],
                filterStudent = lessonsProps.planLessonFilter.student;

            if (filterStudent) {
                const loadStudentPromiseFn = () => lessonsProps.dispatch(Actions.ajax.lessons.student(filterStudent.id));
                loadDataPromiseFns.push(loadStudentPromiseFn)
            }
            promise = promise.then(() => Promise.all(loadDataPromiseFns.map(fn => fn())))
        }
        return promise
    },

    availableLessonSlotActions(lessonSlot, p){
        const lessonMt = Utils.lessonMomentFromId(lessonSlot.id),
            actions = {};

        if (lessonSlot.status === 'planned') {
            if (Utils.isLessonStarted(lessonMt)) {
                actions.close = {
                    title: Utils.message('common.lesson.manage.action.close'),
                    fn: dispatch => LessonUtils.doWithConfirmation(() => dispatch(Actions.ajax.lessons.close(lessonSlot.id)))
                }
            } else {
                actions.revoke = {
                    title: Utils.message('common.lesson.manage.action.revoke'),
                    fn: () => LessonUtils.doWithConfirmation(() => LessonUtils.actions.revokeLesson(lessonSlot.id, p))
                }
            }
        }
        return actions;
    },

    getLessonSlotStatus(filter, lessonId){
        const result = filter._result;
        return result && result.lessonStatuses && result.lessonStatuses[lessonId]
    },

    isLessonBlocked(lessonId, filter, pickedLessons, blockedDateTimes, isDayBlocked) {
        const isSelected = LessonUtils.isLessonPicked(lessonId, pickedLessons),
            lessonStatus = LessonUtils.getLessonSlotStatus(filter, lessonId),
            lessonTime = Utils.lessonDateTime(lessonId);
        return isDayBlocked || (!isSelected && blockedDateTimes && (lessonStatus === 'available' || lessonStatus === 'occupied') && Utils.arr.contains(blockedDateTimes, lessonTime))
    },

    isLessonPicked(lessonId, pickedLessons) {
        return !!pickedLessons && Utils.arr.contains(pickedLessons, lessonId)
    },

    /**
     * @param p lessons page props
     */
    pickLesson(p, lessonId){
        if (lessonId) {
            const lessonIsBlockedByTime = p.blockedDateTimes && Utils.arr.contains(p.blockedDateTimes, Utils.lessonDateTime(lessonId)),
                lessonIsPicked = Utils.arr.contains(p.pickedLessons, lessonId),
                canBePicked = LessonUtils.lessonHasAvailableStatus(p, lessonId) && (!lessonIsBlockedByTime || lessonIsPicked);

            if (canBePicked) {
                DialogService.doWithOptionalConfirmation(!lessonIsPicked && !LessonUtils.isMatchingAgeGroupLessonToBePicked(p, lessonId),
                    Utils.message('common.lessons.confirm.pick.lessons.with.unmatching.age'),
                    () => p.dispatch(Actions.pickLesson(lessonId)))
            }
        }
    },

    lessonHasAvailableStatus(p, lessonId){
        const lessonStatuses = p.planLessonFilter._result.lessonStatuses || {},
            selectedLessonStatus = lessonStatuses[lessonId];
        return Utils.arr.contains(Dictionaries.lessonProcedure.byId(p.lessonProcedure).acceptableLessonStatusesToPlan, selectedLessonStatus)
    },

    isMatchingAgeGroupLessonToBePicked(p, lessonId){
        const studentAgeGroup = Utils.studentAgeGroup(p.planLessonFilter.student.birthDate, Dictionaries.studentAge),
            lessonTemplate = LessonUtils.getLessonTemplate(p, lessonId),
            lessonAgeGroup = lessonTemplate && lessonTemplate.ageGroup;

        return !lessonAgeGroup || studentAgeGroup === lessonAgeGroup
    },

    getLessonTemplate(p, lessonId){
        const lessonMt = Utils.lessonMomentFromId(lessonId),
            dayOfWeek = Utils.dayOfWeek(lessonMt),
            dayId = Dictionaries.day[dayOfWeek].id,
            subject = LessonUtils.lessonSubjectFromId(lessonId).id,
            lessonsByDay = p.currentTemplate.lessons[dayId] || {},
            lessonsBySubject = lessonsByDay[subject] || [],
            lessonMins = Utils.minutesFromMidnight(lessonMt);

        return lessonsBySubject.find(lesson => lesson.fromMins === lessonMins);
    },

    lessonDateSpan(lessonIds){
        let min, max;
        if (Array.isArray(lessonIds)) {
            lessonIds.map(lessonId => {
                return Utils.momentFromString(Utils.lessonDate(lessonId))
            }).forEach(lessonMt => {
                min = (!min || lessonMt.isBefore(min)) ? lessonMt : min;
                max = (!max || lessonMt.isAfter(max)) ? lessonMt : max;
            });
        }

        return {min: min, max: max, duration: min && max && (max.diff(min, 'days') + 1)};
    },

    isDayBlocked(filter, dateMt, lessonDateSpan, lessonProcedure){
        let blockFurtherDays = Dictionaries.lessonProcedure.byId(lessonProcedure).blockFurtherDays;
        if (blockFurtherDays && filter.card && Utils.isDefined(lessonDateSpan.duration)) {
            let maxPlannedLessonsSpan = Dictionaries.visitType.byId(filter.card.visitType).plannedLessonsSpan,
                spansDiff = maxPlannedLessonsSpan - lessonDateSpan.duration;

            return (lessonDateSpan.min && dateMt.isBefore(moment(lessonDateSpan.min).subtract(spansDiff, 'd'))) ||
                (lessonDateSpan.max && dateMt.isAfter(moment(lessonDateSpan.max).add(spansDiff, 'd')));
        }
        return false
    },

    isRepeatedLesson(lesson){
        return lesson && lesson.repeatsLeft > 1
    },

    lessonAgeGroup(lessonIdObj, state){
        const currentTemplate = state.currentTemplate,
            dayLessons = currentTemplate && currentTemplate.lessons[Dictionaries.day[lessonIdObj.day].id],
            lessonsBySubject = dayLessons && dayLessons[lessonIdObj.subject],
            fromMins = Utils.momentToMinutes(lessonIdObj.dateTime),
            lesson = lessonsBySubject && lessonsBySubject.find(lesson => lesson.fromMins === fromMins);

        return lesson.ageGroup
    },

    /**
     * Compacts specified array of lesson ids.
     * @param lessonIds array of lesson ids
     * @param startDate date after that lesson repeats should be counted
     * @return object with keys {dayTimeSubjectKey1: ChainedLessonSlot, dayTimeSubjectKey2: ChainedLessonSlot, ...},
     * where dayTimeSubjectKey is key from lesson.getDayTimeSubjectKey(),
     * repeatsLeft is count of repeats that lesson has.
     * Ex. 1 for single lesson at date specified in id.
     */
    compactLessons(lessonIds, startDate){
        const chainedLessons = {}, // {dayTimeSubjectKey: RepeatedLesson}
            startDateMt = Utils.momentFromString(startDate);

        lessonIds.forEach(id => {
            const lessonSlot = LessonUtils.lessonSlotObj(id);

            if (lessonSlot.dateTime.isAfter(startDateMt)) {
                let slotKey = lessonSlot.getDayTimeSubjectKey();
                let chainedLesson = chainedLessons[slotKey];
                if (!chainedLesson) {
                    chainedLesson = chainedLessons[slotKey] = new ChainedLessonSlot()
                }
                chainedLesson.add(lessonSlot);
            }
        });
        return chainedLessons
    },

    lessonSlotObj(lesson){
        return new LessonSlot(lesson)
    },

    lessonIdObj(lessonId){
        return new LessonSlotId(lessonId)
    },

    chainedLessonFromId(lessonId){
        return LessonUtils.chainedLessonFromLessonSlot(LessonUtils.lessonSlotObj({lessonId: lessonId}))
    },

    chainedLessonFromLessonSlot(lesson){
        const chainedLesson = new ChainedLessonSlot();
        chainedLesson.add(lesson);
        return chainedLesson
    },

    hasSameDayTimeAndSubject(lessonIdObj1, lessonIdObj2){
        if (!lessonIdObj1 || !lessonIdObj2) return false;
        return lessonIdObj1.day == lessonIdObj2.day && lessonIdObj1.time == lessonIdObj2.time && lessonIdObj1.subject == lessonIdObj2.subject
    },

    isPlanningProcedure(procedure){
        return Utils.arr.contains(planningProcedures, procedure)
    },

    isSelectedStudentPresentInSchool(selectedStudent, studentEntities){
        if (selectedStudent) {
            const studentEntity = studentEntities[selectedStudent.id];
            return studentEntity && studentEntity.presentInSchool
        }
        return false
    },

    getActiveStudentCards(student){
        return student ? Utils.objectValues(student.cards || [])
                .filter(card => {
                    if (card.activationDate) {
                        const expMt = Utils.momentFromString(card.activationDate);
                        expMt.add(card.durationDays, 'days');
                        return expMt.isSameOrAfter(moment())
                    }
                    return true
                }).sort(Utils.sort.reverse(Utils.sort.byKey('lessonsAvailable'))) : []
    },

    loadStudents(text, callback) {
        DataService.operations.students.findAll({text: text}).then(response => {
            callback(null, {
                options: response.results
            });
        }, error => {
            callback(error.status, null);
        });
    },


    actions: {
        navigateToLesson(p, lessonId){
            LessonUtils.actions.setDate(p, Utils.lessonDateTime(lessonId))
        },

        setDate(p, date){
            return LessonUtils.actions.loadLessonsData(p, date, false).then(() =>
                p.dispatch(Actions.setDate(date))
            );
        },

        loadLessonsData(p, date, forceLoad){
            let startLoadDataMt = Utils.momentFromString(p.startLoadDataDate),
                endLoadDataMt = Utils.momentFromString(p.endLoadDataDate),
                startVisibleDataMt = Utils.momentFromString(date, Config.dateFormat),
                endVisibleDataMt = moment(startVisibleDataMt).add(Config.lessonSlotsVisibleDaysCount, 'd');

            if (forceLoad || endVisibleDataMt.isAfter(endLoadDataMt) || startVisibleDataMt.isBefore(startLoadDataMt)) {
                const nextStartLoadMt = moment(startVisibleDataMt).subtract(Config.lessonSlotsBackwardLoadDaysCount, 'd'),
                    nextEndLoadMt = moment(startVisibleDataMt).add(Config.lessonSlotsForwardLoadDaysCount, 'd'),
                    student = p.planLessonFilter.student,
                    studentId = student && student.id;

                return p.dispatch(Actions.ajax.lessons.load(Utils.getLessonsDataRequest(nextStartLoadMt, nextEndLoadMt, studentId)));
            }
            return Promise.resolve()
        },

        revokeLesson(lessonId, p){
            const student = p.planLessonFilter.student,
                selectedStudentId = student && student.id,
                lessonSlot = p.entities.lessonPlans[lessonId],
                studentSlots = lessonSlot && lessonSlot.studentSlots,
                studentInRevokedLesson = studentSlots && Object.keys(studentSlots).some(id => studentSlots[id].studentId === selectedStudentId);
            p.dispatch(Actions.ajax.lessons.revoke(lessonId)).then(() => {
                if (studentInRevokedLesson) {
                    p.dispatch(Actions.ajax.lessons.findStudentPlannedLessons(selectedStudentId))
                }
                LessonUtils.actions.loadLessonsData(p, p.currentDate, true)
            })
        }
    }
};

class LessonSlotId {

    constructor(lessonId) {
        const lessonMt = Utils.lessonMomentFromId(lessonId),
            lessonSubject = LessonUtils.lessonSubjectFromId(lessonId);

        this.dateTime = lessonMt;
        this.day = Utils.dayOfWeek(lessonMt);
        this.time = Utils.momentToTime(lessonMt);
        this.subject = lessonSubject && lessonSubject.id

    }

    getDate() {
        return Utils.momentToString(this.dateTime)
    }

    getDayTimeSubjectKey() {
        return this.day + '-' + this.time + '-' + this.subject
    }
}

class LessonSlot extends LessonSlotId {

    /**
     * @param lesson js object {lessonId: 'someId', ...}
     * other lesson fields are get from server
     */
    constructor(lesson) {
        super(lesson.lessonId);
        Object.assign(this, lesson);
    }
}

class ChainedLessonSlot {

    constructor() {
        this.repeatsLeft = 0;
        this.first = null;
        this.last = null;
    }

    /**
     * @param lesson LessonSlot object
     */
    add(lesson) {
        if (!this.first || this.first.getDayTimeSubjectKey() === lesson.getDayTimeSubjectKey()) {
            this.repeatsLeft++;

            if (!this.first || this.first.dateTime.isAfter(lesson.dateTime)) {
                this.first = lesson
            }

            if (!this.last || this.last.dateTime.isBefore(lesson.dateTime)) {
                this.last = lesson
            }
        }
    }
}

module.exports = LessonUtils;
