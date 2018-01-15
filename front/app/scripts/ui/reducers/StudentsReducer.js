const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    ReducersMap = require('./ReducersMap');

const reducers = new ReducersMap(getInitialState())
    .add({type: 'setSearchRequest', entity: 'students'},
        (state, action) => {
            return Utils.merge(state, {searchRequest: action.request});
        })
    .add({type: 'ajaxStarted', entity: 'students', operation: 'findAll'},
        (state, action) => {
            return Utils.merge(state, {_loading: true});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'students', operation: 'findAll'},
        (state, action) => {
            const entities = action.request.appendResults ?
                Utils.mergeArraysByObjectKey(state.entities, action.response.results, 'id') :
                action.response.results;

            const nextState = {
                _loading: false,
                entities: entities,
                searchRequest: action.request,
                totalRecords: action.response.total
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'students', operation: 'save'},
        (state, action) => {
            const savedStudent = action.response;
            if (state.student && savedStudent.id === state.student.id) {
                return Utils.merge(state, {
                    student: savedStudent
                });
            }
            return state;
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'students', operation: 'delete'},
        (state, action) => {
            const nextState = {
                entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
                totalRecords: state.totalRecords - 1
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'ajaxStarted', entity: 'studentCalls', operation: 'findAll'},
        (state, action) => {
            return Utils.extend(state, {
                studentCall: {}
            })
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCalls', operation: 'findAll'},
        (state, action) => {
            return Utils.merge(state, {
                student: {
                    calls: action.response.results
                }
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCards', operation: 'save'}, onStudentCardSave)
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCards', operation: 'addPayment'}, onStudentCardSave)
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCards', operation: 'delete'},
        (state, action) => {
            return Utils.merge(state, {
                student: {
                    cards: Utils.arr.remove(state.student.cards, action.response, Utils.obj.id)
                }
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCalls', operation: 'save'},
        (state, action) => {
            const savedCall = action.response,
                savedCallStudent = savedCall.student;
            let nextState = state;
            if (savedCallStudent && state.student && savedCallStudent.id === state.student.id) {
                nextState = Utils.merge(nextState, {
                    student: {
                        calls: Utils.arr.put(state.student.calls, savedCall, Utils.obj.id)
                    }
                });
            }
            if (state.studentCall && savedCall.id === state.studentCall.id) {
                nextState = Utils.extend(nextState, {studentCall: savedCall})
            }
            return nextState;
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentCalls', operation: 'delete'},
        (state, action) => {
            return Utils.merge(state, {
                student: {
                    calls: Utils.arr.remove(state.student.calls, action.response, Utils.obj.id)
                }
            });
        })
    .add({type: 'toggleSelectedObject', entity: 'students'},
        (state, action) => {
            return Utils.extend(state, {
                student: Utils.extend(action.obj, {_found: false}),
                studentCall: {},
                selectedRelativeIndex: 0,
                selectedCardIndex: 0
            });
        })
    .add({type: 'toggleSelectedObject', entity: 'studentCalls'},
        (state, action) => {
            return Utils.extend(state, {
                studentCall: action.obj
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentDashboard', operation: 'findOne'},
        (state, action) => {
            const student = Utils.extend({
                _found: true,
                lessonsSummary: {},
                lessons: {}
            }, action.response);
            const nextState = {
                student: student
            };
            return Utils.extend(state, nextState);
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentDashboard', operation: 'findLessons'},
        (state, action) => {
            const nextLessons = Object.keys(action.response).length > 0 ?
                action.response : {[Utils.momentToString(moment())]: []};

            const nextStudent = Utils.extend(state.student, {
                lessons: nextLessons
            });
            return Utils.extend(state, {student: nextStudent});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'lessons', operation: 'visit'}, onStudentSlotStatusChange('visited'))
    .add({type: 'ajaxFinishedSuccess', entity: 'lessons', operation: 'miss'}, onStudentSlotStatusChange('missed'))
    .add({type: 'ajaxFinishedSuccess', entity: 'lessons', operation: 'cancel'}, onStudentSlotStatusChange('canceled'))
    .add({type: 'ajaxFinishedSuccess', entity: 'lessons', operation: 'unplan'}, onStudentSlotUnplan)
    .add({type: 'ajaxFinishedSuccess', entity: 'lessons', operation: 'setPresenceInSchool'}, (state, action) => {
        return Utils.merge(state, {
            student: {
                presentInSchool: action.request.presentInSchool
            }
        })
    })
    .add({type: 'setPageMode', entity: 'studentDashboard', page: 'form', fieldId: 'tab'},
        (state, action) => {
            return Utils.merge(state, {studentDashboard: {pageMode: action.mode}});
        })
    .add({type: 'setPageMode', entity: 'students', page: 'list', fieldId: 'student'},
        (state, action) => {
            return Utils.merge(state, {detailsCollapsed: action.mode === 'noDetails'});
        })
    .add({type: 'setPageMode', entity: 'studentCard', page: 'studentCard', fieldId: 'card'},
        (state, action) => {
            if (action.mode === 'studentCardPayment') {
                return Utils.merge(state, getInitialStudentCardPayment(action.options));
            } else {
                return Utils.merge(state, {studentCardPayment: null});
            }
        })
    .add({type: 'selectEntity', entity: 'relatives', ownerEntity: 'students', page: 'list'},
        (state, action) => {
            return Utils.extend(state, {
                selectedRelativeIndex: action.id
            });
        })
    .add({type: 'selectEntity', entity: 'studentCards', ownerEntity: 'students', page: 'list'},
        (state, action) => {
            return Utils.extend(state, {
                selectedCardIndex: action.id
            });
        })
    .add({type: 'setEntityValue', entity: 'relativeNotifications'},
        (state, action) => {
            const index = action.options.relativeIndex,
                relatives = state.student.relatives,
                relativeToChange = index >= 0 && relatives && relatives[index];
            if (relativeToChange) {
                let newRelative = Utils.extend(relativeToChange, {[action.fieldId]: action.newValue});
                let newRelatives = [].concat(relatives);
                newRelatives.splice(index, 1, newRelative);
                const newStudent = Utils.merge(state.student, {relatives: newRelatives}),
                    nextState = {
                        student: newStudent,
                    };
                return Utils.merge(state, nextState);
            }
            return state;
        })
    .add({type: 'setEntityValue', entity: 'studentCardPayment'},
        (state, action) => {
            let nextNewStudentCard = Utils.extend(state.studentCardPayment);
            nextNewStudentCard[action.fieldId] = action.newValue;
            if (action.fieldId === 'card' && action.newValue) {
                const card = action.newValue;
                nextNewStudentCard.card.cardId = card.id;
                nextNewStudentCard.card.id = null;
                nextNewStudentCard.finalPrice = card.price;
                nextNewStudentCard.finalDuration = card.durationDaysMax;
                nextNewStudentCard = Utils.extend(nextNewStudentCard, Utils.pick(card, ['lessonsLimit', 'cancelsLimit', 'lateCancelsLimit', 'lastMomentCancelsLimit', 'undueCancelsLimit', 'missLimit', 'suspendsLimit']))
            }

            return Utils.extend(state, {studentCardPayment: nextNewStudentCard});
        })
    .add({type: 'setEntityValues', entity: 'lessonSearchRequest'},
        (state, action) => {
            return Utils.merge(state, {studentDashboard: {lessonSearchRequest: action.obj}});
        })
    .add({type: 'setValidationMessages', entity: 'studentCardPayment'},
        (state, action) => {
            return Utils.merge(state, {validationMessages: action.messages});
        });

function getInitialState() {
    return {
        entities: [],
        searchRequest: {
            text: '',
            status: 'all',
            firstRecord: 1,
            createdDatePeriod: 'all',
            sortColumn: 'businessId',
            sortOrder: 'desc'
        },
        totalRecords: 0,
        student: {
            lessonsSummary: {}, //lessons summary grouped by visit type
            lessons: {}
        },
        studentCall: {},
        studentDashboard: getInitialStudentDashboard(),
        detailsCollapsed: true,
        selectedRelativeIndex: 0,
        selectedCardIndex: 0,
        studentCardPayment: null,
        validationMessages: {
            studentCardPayment: {}
        },
        tableMessages: []
    };
}

function getInitialStudentDashboard() {
    return {
        pageMode: 'overview',
        lessonSearchRequest: {
            timeCategory: 'schedule',
            lessonDate: Utils.momentToString(Utils.currentMoment()),
            visitType: 'all'
        }
    }
}

function getInitialStudentCardPayment(options) {
    const studentCard = options && options.studentCard;
    return {
        studentCardPayment: {
            card: studentCard,
            finalPrice: studentCard && studentCard.price,
            finalDuration: studentCard && studentCard.durationDays,
            accountType: null,
            targetPartner: null,
            targetAccount: null,
            transferSourceStudentId: null,
            transferredSlotIds: []
        }
    }
}

function onStudentSlotStatusChange(status) {
    return (state, action) => {
        const lessonId = action.request.lessonId;
        return Utils.merge(state, {
            student: {
                lessons: {
                    [Utils.lessonDate(lessonId)]: (slots) => {
                        let modifiedSlot = Utils.arr.find(slots, {lessonId}, o => o.lessonId).obj;
                        if (modifiedSlot) {
                            modifiedSlot.status = status;
                            slots = Utils.arr.put(slots, modifiedSlot, o => o.lessonId)
                        }
                        return slots
                    }
                }
            }
        });
    }
}

function onStudentSlotUnplan(state, action) {
    const lessonId = action.request.lessonId;
    return Utils.merge(state, {
        student: {
            lessons: {
                [Utils.lessonDate(lessonId)]: (slots) =>
                    Utils.arr.remove(slots, {lessonId}, o => o.lessonId)
            }
        }
    });
}

function onStudentCardSave(state, action) {
    const savedCard = action.response,
        savedCardStudent = savedCard.student;
    if (savedCardStudent && state.student && savedCardStudent.id === state.student.id) {
        return Utils.merge(state, {
            student: {
                cards: Utils.arr.put(state.student.cards, savedCard, Utils.obj.id)
            }
        });
    }
    return state;
}

function StudentsReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = StudentsReducer;
