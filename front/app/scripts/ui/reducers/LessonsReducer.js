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

const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    LessonUtils = require('../LessonUtils'),

    planLessonsActions = ['plan'],
    changeLessonStatusOps = ['visit', 'miss', 'cancel', 'unplan'],
    unplanningLessonOperations = ['unplan', 'transfer'],
    changeFoundLessonStatusesTriggerFields = ['student', 'card'];

function LessonsReducer(state = getInitialState(), action) {
    if (action.type === 'ajaxStarted' && action.entity === 'lessons' && action.operation === 'load') {
        return Utils.merge(state, {
            entities: {
                _loading: true
            }
        })
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'load') {
        const currentTemplate = findCurrentTemplate(Utils.momentFromString(state.currentDate), action.response.templates);
        let nextState = Utils.extend(state, {
            entities: Utils.extend(action.response, {
                _loading: false
            }),
            currentTemplate: currentTemplate,
            startLoadDataDate: action.request.startDate,
            endLoadDataDate: action.request.endDate
        });

        return Utils.merge(nextState, getLessonStatusesDiff(nextState));

    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'student') {
        const filterStudent = state.planLessonFilter.student,
            foundStudent = action.response.results[0];
        if (filterStudent && filterStudent.id === action.request.selection && foundStudent) {

            return Utils.merge(state, {
                planLessonFilter: {
                    student: () => foundStudent
                }
            })
        }
        return state

    } else if (action.type === 'setDate') {
        const weekRange = getWeekRange(moment(action.date, Config.dateFormat));
        let nextState = Utils.extend(state,
            weekRange,
            {currentTemplate: findCurrentTemplate(Utils.momentFromString(weekRange.currentDate), state.entities.templates)});
        nextState = Utils.merge(nextState, {
                planLessonFilter: {
                    _result: getStudentPlannedLessonIdsDiff(state.planLessonFilter._result.studentPlannedLessons, nextState.startDate)
                }
            }
        );

        return Utils.merge(nextState, getLessonStatusesDiff(nextState));

    } else if (action.type === 'setEntityValue' && action.entity === 'visitsSummary') {
        return Utils.extend(state, {visitsSummary: action.newValue});
    } else if (action.type === 'pickLesson') {
        const pickedLessonIdObj = LessonUtils.lessonIdObj(action.lessonId),
            pickedLessonDate = pickedLessonIdObj.getDate(),
            normalizedPickedLessons = state.pickedLessons.filter(id => id === action.lessonId || !LessonUtils.lessonIdObj(id).dateTime.isSame(pickedLessonIdObj.dateTime)),
            nextPickedLessons = Utils.arr.toggle(normalizedPickedLessons, action.lessonId);
        let latestPickedFromMins;
        if (nextPickedLessons.length > normalizedPickedLessons.length) { //lesson was added to selection
            latestPickedFromMins = Math.max(Utils.timeToMinutes(pickedLessonIdObj.time) || 0, state.latestPickedLessons[pickedLessonDate] || 0);
        } else {
            const pickedLessonsByDate = nextPickedLessons.map(lessonId => LessonUtils.lessonIdObj(lessonId))
                .filter(idObj => idObj.getDate() === pickedLessonDate);
            latestPickedFromMins = Utils.timeToMinutes(Utils.arr.max(pickedLessonsByDate, (acc, cur) => Utils.max(acc.time, cur.time)))
        }

        return Utils.extend(state, {
            pickedLessons: nextPickedLessons,
            latestPickedLessons: Utils.extend(state.latestPickedLessons, {
                [pickedLessonDate]: latestPickedFromMins
            }),
            changeLessonStatusMessage: null
        });
    } else if (action.type === 'selectLesson') {
        return Utils.extend(state, {selectedLesson: action.lesson});
    } else if (action.type === 'selectSidebar') {
        return Utils.extend(state, {selectedSidebar: (!action.sidebarId || action.sidebarId == state.selectedSidebar) ? null : action.sidebarId});
    } else if (action.type === 'setLessonsView') {
        return Utils.extend(state, {lessonsView: action.view});
    } else if (action.type === 'setPlanLessonFilter') {
        let lessons = state.currentTemplate && state.currentTemplate.lessons || {},
            lessonPlans = state.entities.lessonPlans || {},
            filter = Utils.extend(state.planLessonFilter),
            startDate = getStartDateForLessonStatusesSearch(state),
            pickedLessons = state.pickedLessons,
            latestPickedLessons = state.latestPickedLessons,
            lessonProcedure = state.lessonProcedure;

        filter[action.fieldId] = action.newValue;
        if (action.fieldId === 'student') {
            filter.usePreciseAge = false; //requirement
            const boundToCard = Dictionaries.lessonProcedure.byId(lessonProcedure).boundToCard;
            filter.card = (filter.student && boundToCard) ? LessonUtils.getActiveStudentCards(filter.student)[0] : null;
            pickedLessons = [];
            latestPickedLessons = {};
        }

        filter._result.lessons = filterLessons(lessons, filter, lessonProcedure);
        if (Utils.arr.contains(changeFoundLessonStatusesTriggerFields, action.fieldId)) {
            filter._result.lessonStatuses = findLessonStatuses(lessons, lessonPlans, filter, lessonProcedure, startDate, pickedLessons);
        }
        if (!filter.student) {
            filter._result.studentPlannedLessons = [];
            filter._result.compactedStudentPlannedLessons = {};
        }

        return Utils.extend(state, {
            planLessonFilter: filter,
            pickedLessons: pickedLessons,
            latestPickedLessons: latestPickedLessons,
            planLessonStatusMessage: null,
            planLessonStatusDescription: null
        })
    } else if (action.type === 'setLessonProcedure') {
        return createSetLessonProcedureState(state, action.lessonProcedure, null);
    } else if (action.type === 'startMoveStudentSlot') {
        const slotId = action.slotId,
            studentSlot = state.entities.lessonPlans[action.lessonId].studentSlots[slotId],
            studentId = studentSlot.studentId,
            cardId = studentSlot.cardId;
        return createSetLessonProcedureState(state, 'plan', {slotId, studentId, cardId});
    } else if (action.type === 'ajaxStated' && action.entity === 'lessons' && (Utils.arr.contains(planLessonsActions, action.operation))) {
        return Utils.merge(state, {planLessonStatusMessage: 'Planning...', planLessonStatusDescription: null})
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && (Utils.arr.contains(planLessonsActions, action.operation))) {
        return onPlanLesson(state, action);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'suspend') {
        return Utils.merge(state, {
            planLessonStatusMessage: createSimpleSuccessStatusMessage(action.response),
            planLessonStatusDescription: null,
            planLessonFilter: {
                _result: getStudentPlannedLessonIdsDiff(action.response.studentPlannedLessons, state.startDate)
            }
        })
    } else if (action.type === 'ajaxStarted' && action.entity === 'lessons' && Utils.arr.contains(changeLessonStatusOps, action.operation)) {
        return Utils.merge(state, {changeLessonStatusMessage: null})
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && Utils.arr.contains(unplanningLessonOperations, action.operation)) {
        return createUnplanLessonState(state, action);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'visit') {
        return createChangedStatusState(state, action.response, action.request.lessonId, action.request.slotId, 'visited');
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'miss') {
        return createChangedStatusState(state, action.response, action.request.lessonId, action.request.slotId, 'missed');
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'cancel') {
        return createCancelLessonState(state, action);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'revoke') {
        return createRevokedLessonState(state, action.response, action.request.lessonId);
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'close') {
        return createClosedLessonState(state, action.response, action.request.lessonId);
    } else if (action.type === 'setPageMode' && action.entity === 'lessons' && action.page === 'home' && action.fieldId === 'lesson') {
        return Utils.extend(state, {pageMode: action.mode});
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'findPhotos') {
        const photos = action.response.names;
        return Utils.merge(state, {lessonPhotos: photos, shownPhoto: photos[0] || ""});
    } else if (action.type === 'filesUploaded' && action.entity === 'lessons') {
        const originalPhotos = state.lessonPhotos,
            nextPhotos = Utils.pushNew(originalPhotos, action.fileNames);
        return Utils.merge(state, {lessonPhotos: nextPhotos, shownPhoto: action.fileNames[0]});
    } else if (action.type === 'changeShownFile' && action.entity === 'lessons') {
        return Utils.merge(state, {shownPhoto: action.fileName});
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'removeFile') {
        const nextPhotos = Utils.removeAndCopy(state.lessonPhotos, action.request.fileName);
        return Utils.merge(state, {lessonPhotos: nextPhotos, shownPhoto: nextPhotos[0] || ""});
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'setPresenceInSchool') {
        return Utils.merge(state, {
            entities: {
                students: {
                    [action.request.studentId]: {
                        presentInSchool: action.request.presentInSchool
                    }
                }
            }
        });
    } else if (action.type === 'ajaxFinishedSuccess' && action.entity === 'lessons' && action.operation === 'findStudentPlannedLessons') {
        return Utils.merge(state, {
            planLessonFilter: {
                _result: getStudentPlannedLessonIdsDiff(action.response, state.startDate)
            }
        });
    } else if (action.type === 'setEntityValue' && action.entity === 'lessonsTransfer') {
        return Utils.merge(state, {
            lessonsTransfer: {
                [action.fieldId]: action.newValue
            }
        });
    }

    return state;
}

function getInitialState() {
    let initialState = {
        entities: {},
        pageMode: 'lessons', //studentSlots
        lessonsView: 'list', //table, list
        visitsSummary: Dictionaries.visitsSummary.byId('freeSlots'),
        planLessonFilter: {
            student: null,
            card: null,
            usePreciseAge: true,
            useExtraControls: false,
            visitType: 'all',
            _result: getInitialPlanLessonFilterResult()
        },
        pickedLessons: [],
        latestPickedLessons: {}, //date to max time (minutes from midnight) of picked lesson
        lessonsTransfer: {
            targetStudent: null
        }
    };
    return Object.assign(initialState, getLessonProcedure(),
        getWeekRange());
}

function getInitialPlanLessonFilterResult() {
    return {
        lessonStatuses: {},
        //compacted list of planned lesson ids, used for informational purposes only
        studentPlannedLessons: [],
        compactedStudentPlannedLessons: {},
    }
}

function getWeekRange(curDate = moment()) {
    return {
        currentDate: curDate.format(Config.dateFormat),
        startDate: curDate.format(Config.dateFormat),
        endDate: moment(curDate).add(Config.lessonSlotsVisibleDaysCount, 'd').format(Config.dateFormat)
        //Study week starts from wednesday by default
    }
}

function getLessonProcedure(procId = 'view') {
    return {
        lessonProcedure: procId
    }
}

function getLessonStatusesDiff(state) {
    const lessons = state.currentTemplate && state.currentTemplate.lessons || {},
        lessonPlans = state.entities.lessonPlans || {},
        lessonProcedure = state.lessonProcedure,
        startDate = getStartDateForLessonStatusesSearch(state),
        pickedLessons = state.pickedLessons;

    return {
        planLessonFilter: {
            _result: {
                lessonStatuses: () => findLessonStatuses(lessons, lessonPlans, state.planLessonFilter, lessonProcedure, startDate, pickedLessons)
            }
        }
    }
}

function createSetLessonProcedureState(state, lessonProcedure) {
    const usePickedLessons = Dictionaries.lessonProcedure.byId(lessonProcedure).usePickedLessons;

    return Utils.extend(state, {
        lessonProcedure: lessonProcedure,
        pageMode: 'lessons',
        planLessonStatusMessage: null,
        pickedLessons: Utils.select(usePickedLessons, [], state.pickedLessons),
        latestPickedLessons: Utils.select(usePickedLessons, {}, state.latestPickedLessons),
        planLessonFilter: createSetLessonProcedureFilterState(state, lessonProcedure)
    })
}

function createSetLessonProcedureFilterState(state, newProcedure) {
    const oldProcedure = state.lessonProcedure;

    if (oldProcedure !== newProcedure && !arePlanningLessonProcedures(oldProcedure, newProcedure)) {
        const lessons = state.currentTemplate && state.currentTemplate.lessons || {},
            lessonPlans = state.entities.lessonPlans || {},
            filter = Utils.extend(state.planLessonFilter),
            startDate = getStartDateForLessonStatusesSearch(state),
            pickedLessons = state.pickedLessons,
            newProcedureIsBoundToCard = Dictionaries.lessonProcedure.byId(newProcedure).boundToCard;

        if (newProcedureIsBoundToCard && state.planLessonFilter.student) {
            filter.card = LessonUtils.getActiveStudentCards(filter.student)[0]
        }
        if (!newProcedureIsBoundToCard) {
            filter.card = null
        }

        filter._result.lessons = filterLessons(lessons, filter, newProcedure);
        filter._result.lessonStatuses = findLessonStatuses(lessons, lessonPlans, filter, newProcedure, startDate, pickedLessons);
        return filter;
    }
    return state.planLessonFilter
}

function arePlanningLessonProcedures(oldProcedure, newProcedure) {
    return LessonUtils.isPlanningProcedure(oldProcedure) && LessonUtils.isPlanningProcedure(newProcedure)
}

function getStartDateForLessonStatusesSearch(state) {
    return Config.backDatePlanningEnabled ? state.startLoadDataDate : Utils.momentToString(Utils.currentMoment())
}

function findCurrentTemplate(curDate, templates) {
    let latestTemplate,
        latestTemplateStartDate;
    if (templates) {
        for (let t in templates) {
            if (templates.hasOwnProperty(t)) {
                let template = templates[t],
                    templateStartDate = Utils.momentFromString(template.startDate);
                if (templateStartDate.isSameOrBefore(curDate) && !latestTemplate ||
                    latestTemplateStartDate && latestTemplateStartDate.isBefore(templateStartDate)) {
                    latestTemplate = template;
                    latestTemplateStartDate = templateStartDate;
                }
            }
        }
    }
    return latestTemplate;
}


function createSimpleSuccessStatusMessage(response) {
    const error = response.error;
    if (error) {
        return Utils.message(error.text, error.params)
    }
    return 'Success'
}

function onPlanLesson(state, action) {
    return Utils.merge(state, {
        pickedLessons: Utils.select(action.response.error, state.pickedLessons, []),
        latestPickedLessons: Utils.select(action.response.error, state.latestPickedLessons, Utils.fn.emptyObj),
        planLessonStatusMessage: createSimpleSuccessStatusMessage(action.response),
        planLessonStatusDescription: createPlanLessonStatusDescription(action.response),
        planLessonFilter: {
            _result: getStudentPlannedLessonIdsDiff(action.response.studentPlannedLessons, state.startDate)
        }
    });
}

function createPlanLessonStatusDescription(response) {
    const plannedLessonsMessage = '<b>Planned lessons: </b>' + (Utils.joinDefined(Object.keys(response.plannedLessonIds), ', ') || 'None'),
        skippedLessons = response.skippedLessons,
        skippedLessonsMessages = Object.keys(skippedLessons).map(id => (id + ': ' + Utils.messages(skippedLessons[id]))),
        skippedLessonsMessage = '<b>Skipped lessons: </b>' + (Utils.joinDefined(skippedLessonsMessages, '<br/>') || 'None');
    return plannedLessonsMessage + '<br/>' + skippedLessonsMessage
}

function createUnplanLessonState(state, action) {
    const {response} = action;

    return Utils.merge(state, {
        planLessonStatusMessage: createSimpleSuccessStatusMessage(response),
        pickedLessons: [],
        latestPickedLessons: Utils.fn.emptyObj,
        startLoadDataDate: undefined,
        endLoadDataDate: undefined,
        planLessonFilter: {
            _result: getStudentPlannedLessonIdsDiff(action.response.studentPlannedLessons, state.startDate)
        }
    })
}

function createChangedStatusState(state, response, lessonId, studentSlotId, status) {
    if (response.error) {
        return Utils.merge(state, {
            changeLessonStatusMessage: response.error
        })
    } else {
        return Utils.merge(state, {
            entities: {
                lessonPlans: {
                    [lessonId]: {
                        studentSlots: {
                            [studentSlotId]: {
                                status: status
                            }
                        }
                    }
                }
            },
            planLessonFilter: {
                _result: getStudentPlannedLessonIdsDiff(response.studentPlannedLessons, state.startDate)
            }
        })
    }
}

function createCancelLessonState(state, action) {
    const {response} = action,
        {lessonId, slotId, visitType} = action.request;

    if (response.error) {
        return Utils.merge(state, {
            changeLessonStatusMessage: response.error
        })
    } else if (!response.warning) {
        const useAvailableLessons = Dictionaries.lessonProcedure.byId(state.lessonProcedure).useAvailableLessons,
            canceledSlot = Utils.arr.find(action.response.studentPlannedLessons, {id: slotId}, Utils.obj.id).obj;
        if (canceledSlot) {
            return Utils.merge(state, {
                entities: {
                    _loading: false,
                    lessonPlans: {
                        [lessonId]: {
                            studentSlots: {
                                [slotId]: canceledSlot
                            },
                            visitsSummary: {
                                [visitType]: Utils.decrementor,
                                total: Utils.decrementor
                            }
                        }
                    }
                },
                planLessonFilter: {
                    _result: Utils.extend({
                        lessonStatuses: {
                            [lessonId]: useAvailableLessons ? 'available' : null
                        }
                    }, getStudentPlannedLessonIdsDiff(action.response.studentPlannedLessons, state.startDate))
                }
            })
        }
    }
    return state
}

function createRevokedLessonState(state, response, lessonId) {
    if (response.error) {
        return Utils.merge(state, {
            changeLessonStatusMessage: response.error
        })
    } else {
        return Utils.merge(state, {
            entities: {
                lessonPlans: {
                    [lessonId]: {
                        id: lessonId,
                        status: 'revoked',
                        studentSlots: (oldStudentSlots) => {
                            const nextStudentSlots = {};
                            if (oldStudentSlots) {
                                Object.keys(oldStudentSlots).forEach(k => {
                                    const oldStudentSlot = oldStudentSlots[k];
                                    nextStudentSlots[k] = oldStudentSlot.status === 'canceled' ? oldStudentSlot : Utils.merge(oldStudentSlot, {status: 'revoked'})
                                });
                            }
                            return nextStudentSlots;
                        },
                        visitsSummary: () => {
                        }
                    }
                }
            },
            planLessonFilter: {
                _result: {
                    lessonStatuses: {
                        [lessonId]: 'revoked'
                    }
                }
            }
        })
    }
}

function createClosedLessonState(state, response, lessonId) {
    if (response.error) {
        return Utils.merge(state, {
            changeLessonStatusMessage: response.error
        })
    } else {
        return Utils.merge(state, {
            entities: {
                lessonPlans: {
                    [lessonId]: {
                        id: lessonId,
                        status: 'closed',
                        studentSlots: (oldStudentSlots) => {
                            const nextStudentSlots = {};
                            if (oldStudentSlots) {
                                Object.keys(oldStudentSlots).forEach(k => {
                                    const oldStudentSlot = oldStudentSlots[k];
                                    nextStudentSlots[k] = oldStudentSlot.status === 'planned' ? Utils.merge(oldStudentSlot, {status: 'visited'}) : oldStudentSlot
                                });
                            }
                            return nextStudentSlots;
                        }
                    }
                }
            },
            planLessonFilter: {
                _result: getStudentPlannedLessonIdsDiff(response.studentPlannedLessons, state.startDate)
            }
        })
    }
}


/**
 *
 * @param lessons
 * @param lessonPlans
 * @param filter
 * @param lessonProcedure
 * @param startDate
 * @param pickedLessons
 */
function findLessonStatuses(lessons, lessonPlans, filter, lessonProcedure, startDate) {
    const useAvailableLessons = Dictionaries.lessonProcedure.byId(lessonProcedure).useAvailableLessons;
    return useAvailableLessons ? findLessonStatusesInPlanProcedure(lessons, lessonPlans, filter, startDate) : {};
}

function findLessonStatusesInPlanProcedure(lessons, lessonPlans, filter, startDate) {
    let lessonStatuses = findLessonStatusesForStudent(lessonPlans, filter);
    if (filter.card) {
        lessonStatuses = findLessonStatusesForCard(lessons, lessonPlans, filter, lessonStatuses, startDate)
    }
    return lessonStatuses
}

function findLessonStatusesForStudent(lessonPlans, filter) {
    let lessonStatuses = {};
    if (filter.student) {
        Object.keys(lessonPlans).forEach(lessonId => {
            const lessonPlan = lessonPlans[lessonId];

            Utils.setValueIfDefined(lessonStatuses, lessonId, getLessonStatusByStudentSlots(lessonPlan, filter.student.id))
        })
    }
    return lessonStatuses;
}

/**
 * Finds lessons statuses that can be shown in plan procedure
 * @param lessons lessons to find statuses in
 * @param lessonPlans planned lessons records that are used in find
 * @param filter filter parameters that are used in find
 * @param lessonStatuses already defined lesson statuses obj id-status
 * @param startDate origin date that start to find statuses from
 */
function findLessonStatusesForCard(lessons, lessonPlans, filter, lessonStatuses, startDate) {
    if (filter.student && filter.card) {
        const planAheadDays = Dictionaries.visitType.byId(filter.card.visitType).planAheadDaysLimit;

        for (let i = 0; i <= planAheadDays; i++) { //compare by <= so days start to be count from tomorrow
            let dateMt = Utils.momentFromString(startDate).add(i, 'd'),
                date = dateMt.format(Config.dateFormat),
                weekDayIndex = Utils.dayOfWeek(dateMt),
                day = Dictionaries.day[weekDayIndex],
                lessonsByDay = lessons[day.id];

            if (lessonsByDay) {
                Object.keys(lessonsByDay).forEach(subjectId => {
                    let lessonsBySubject = lessonsByDay[subjectId];
                    lessonsBySubject.forEach(lesson => {
                        const lessonId = Utils.lessonId(subjectId, date, lesson.fromMins),
                            lessonPlan = lessonPlans[lessonId];

                        if (!lessonStatuses[lessonId] && isLessonAvailableByTime(lessonId)) {
                            Utils.setValueIfDefined(lessonStatuses, lessonId, getLessonStatusForStudent(lessonPlan, filter.student.id))
                        }
                    })
                })
            }
        }
    }
    return lessonStatuses;
}

function isLessonAvailableByTime(lessonId) {
    return Config.backDatePlanningEnabled || !isLessonFinished(lessonId)
}

function isLessonFinished(lessonId) {
    const subjectOpt = LessonUtils.lessonSubjectFromId(lessonId);
    return Utils.isLessonFinished(lessonId, subjectOpt.duration)
}

function getLessonStatusForStudent(lessonPlan, studentId) {
    if (!lessonPlan) return 'available';
    if (lessonPlan.status === 'revoked') return 'revoked';

    const statusBySlot = getLessonStatusByStudentSlots(lessonPlan, studentId);

    return Utils.select(statusBySlot, statusBySlot,
        Utils.select(Utils.visitsTotal(lessonPlan.visitsSummary) < Config.maxSlotsHardLimit, 'available', 'occupied')
    )
}

function getLessonStatusByStudentSlots(lessonPlan, studentId) {
    let statusBySlot = null;
    if (lessonPlan) {
        Object.keys(lessonPlan.studentSlots).forEach(slotId => {
            const slot = lessonPlan.studentSlots[slotId];
            if (slot.studentId === studentId) {
                statusBySlot = slot.status
            }
        });
    }
    return statusBySlot;
}

/**
 * Filter lessons from template corresponding to specified current date by filter parameters
 * @param lessons lessons to filter
 * @param filter filter object
 * @return lessons that match current date and filter parameters or empty object if no lessons were found
 */
function filterLessons(lessons, filter, lessonProcedure) {
    if (filter && filter.day || filter.subject || filter.age || filter.time || filter.student) {
        let days = filter.useExtraControls && Utils.isNotEmptyArray(filter.day) && filter.day.map(d => d.id) || null,
            subjects = lessonSubjects(filter, lessonProcedure),
            ageGroups = (filter.student && filter.usePreciseAge) ?
                studentAgeGroups(filter.student) :
                (filter.useExtraControls && Utils.isNotEmptyArray(filter.age) && filter.age.map(a => a.id) || null),
            fromMins = filter.useExtraControls && Utils.isNotEmptyArray(filter.time) && filter.time.map(t => Utils.hoursMinutesToMinutes(t.id)) || null;

        return filterTemplateLessons(lessons, days, subjects, ageGroups, fromMins);
    }
    return lessons;
}

function lessonSubjects(filter, lessonProcedure) {
    const lessonSubjectIds = Dictionaries.lessonSubject.ids(),
        requestedSubjects = (filter.useExtraControls || lessonProcedure === 'plan') && Utils.isNotEmptyArray(filter.subject) && filter.subject.map(s => s.id) || null,
        allowedSubjects = filter.card ? Utils.bitmaskToArrayItems(filter.card.allowedSubjectsMask, lessonSubjectIds) : lessonSubjectIds;
    return requestedSubjects === null ? allowedSubjects : requestedSubjects.filter(subjectId => Utils.arr.contains(allowedSubjects, subjectId))
}

function studentAgeGroups(student) {
    const group = Utils.studentAgeGroup(student.birthDate, Dictionaries.studentAge);
    return group && [group] || []
}

/**
 * Filters template lessons object by days, subjects and ageGroups
 * @param lessons
 * @param days
 * @param subjects
 * @param ageGroups
 * @param fromMins
 */
function filterTemplateLessons(lessons, days, subjects, ageGroups, fromMins) {
    return Utils.filterMapObject(lessons,
        (day) => {
            return !days || Utils.arr.contains(days, day)
        },
        (day, dayLessons) => {
            let filteredDayLessons = filterDayLessons(dayLessons, subjects, ageGroups, fromMins);
            return Utils.isEmptyArray(filteredDayLessons) ? undefined : filteredDayLessons;
        }
    );
}

/**
 * Filters day lessons object by subjects and ageGroups
 * @param lessons
 * @param subjects
 * @param ageGroups
 * @param fromMins
 */
function filterDayLessons(lessons, subjects, ageGroups, fromMins) {
    return Utils.filterMapObject(lessons,
        (subject) => {
            return !subjects || Utils.arr.contains(subjects, subject);
        },
        (subject, subjLessons) => {
            let filteredSubjLessons = filterSubjectLessons(subjLessons, ageGroups, fromMins);
            return Utils.isEmptyArray(filteredSubjLessons) ? undefined : filteredSubjLessons;
        }
    );
}

/**
 * Filters lessons array by age group
 * @param lessons
 * @param ageGroups
 * @param fromMins
 */
function filterSubjectLessons(lessons, ageGroups, fromMins) {
    if (ageGroups || fromMins) {
        return Array.isArray(lessons) &&
            lessons.filter(lesson => {
                let ageGroupsMatch = !ageGroups || Utils.arr.contains(ageGroups, lesson.ageGroup),
                    fromMinsMatch = !fromMins || Utils.arr.contains(fromMins, lesson.fromMins);
                return ageGroupsMatch && fromMinsMatch;
            }) || [];
    }
    return lessons;

}

function getStudentPlannedLessonIdsDiff(lessons, startDate) {
    return {
        studentPlannedLessons: () => lessons,
        compactedStudentPlannedLessons: () => LessonUtils.compactLessons(lessons, startDate)
    }
}

module.exports = LessonsReducer;
