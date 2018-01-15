const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    Validators = require('../Validators'),
    EntityLifeCycleReducerFactory = require('./EntityLifeCycleReducerFactory'),
    ReducersMap = require('./ReducersMap');

const formEntities = ['students', 'relatives', 'studentCards'];

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'students', operation: 'findOne'},
        (state, action) => {
            const student = action.response;
            const selectedRelativeIndex = student.relatives && student.relatives.length ? 0 : undefined,
                relative = Utils.isDefined(selectedRelativeIndex) ? student.relatives[selectedRelativeIndex] : undefined;
            const nextState = {
                student: student,
                _student: student,
                selectedRelativeIndex: selectedRelativeIndex,
                shownFileName: Utils.specificOrFirst(student.photos, student.primaryPhotoName),
                shownRelativeFileName: relative && Utils.specificOrFirst(relative.photos, relative.primaryPhotoName)
            };
            return Utils.extend(state, nextState);
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'students', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                student: Utils.extend(state.student, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'students'},
        (state, action) => {
            const student = {
                    relatives: [{}]
                },
                nextState = {
                    student: student,
                    _student: student,
                    selectedRelativeIndex: 0
                };
            return Utils.extend(state, nextState);
        })
    .add({type: 'setEntityValue', entity: 'students'},
        (state, action) => {
            let student = Utils.extend(state.student, {[action.fieldId]: action.newValue});
            if (action.fieldId === 'promotionSource') {
                student.promotionDetail = null
            }
            if (action.fieldId === 'siblings') {
                //one ofoptions is used
                let addedSibling = action.options.addedSibling;
                let removedSibling = action.options.removedSibling;
                if (addedSibling) {
                    student.relatives = Utils.arr.merge(student.relatives, addedSibling.relatives, Utils.relative.idOrEqualFieldsAccessor, Utils.relative.mergeSimilar);
                }
                if (removedSibling) {
                    const nextRelatives = [];
                    //Restore student relatives that were before addition of sibling
                    (student.relatives || []).forEach(relative => {
                        let removedSiblingHasRelative = Utils.arr.contains(removedSibling.relatives, relative, Utils.obj.id);
                        let prevStudentHasRelative = Utils.arr.contains(state._student.relatives, relative, Utils.obj.id);

                        if (prevStudentHasRelative && removedSiblingHasRelative) {
                            //Split the family, leave relative as it was bound to saved student,
                            //but duplicate it to not mix it with sibling relative
                            nextRelatives.push(Utils.extend(relative, {id: null}));
                        }
                        if (!removedSiblingHasRelative) {
                            //Just restore relative from, as it was before adding sibling. Came from saved student or other sibling
                            nextRelatives.push(Utils.extend(relative));
                        }
                        //else do not add relative, as it belongs to removed sibling only
                    });
                    student.relatives = nextRelatives
                }
            }
            const nextState = {
                student: student,
                validationMessages: {students: Validators.students(student, action.fieldId)}
            };

            return Utils.merge(state, nextState);
        })
    .add({type: 'setValidationMessages'},
        (state, action) => {
            if (Utils.arr.contains(formEntities, action.entity)) {
                return Utils.merge(state, {validationMessages: action.messages});
            }
            return state;
        })
    .add({type: 'filesUploaded', entity: 'students'},
        (state, action) => {
            const originalPhotos = state.student.photos,
                nextPhotos = Utils.pushNew(originalPhotos, action.fileNames),
                nextShownFileName = (!originalPhotos || nextPhotos.length > originalPhotos.length) ? nextPhotos[nextPhotos.length - 1] : state.shownFileName;

            return Utils.merge(state, {
                student: {
                    photos: nextPhotos
                },
                shownFileName: nextShownFileName
            });
        })
    .add({type: 'changeShownFile', entity: 'students'},
        (state, action) => {
            return Utils.merge(state, {
                shownFileName: action.fileName
            });
        })
    .add({type: 'setEntityValue', entity: 'relatives'},
        (state, action) => {
            const index = state.selectedRelativeIndex,
                relatives = state.student.relatives,
                relativeToChange = index >= 0 && relatives && relatives[index];

            if (relativeToChange) {
                let newRelative = Utils.extend(relativeToChange, {[action.fieldId]: action.newValue});
                let newRelatives = [].concat(relatives);
                newRelatives.splice(index, 1, newRelative);

                const relativeMessage = Validators.relatives(newRelative, action.fieldId);

                if (!newRelative.confirmationId && relativeToChange.confirmationId) {
                    relativeMessage.mobile = null;  //remove verification validation message if confirmation id was cleared
                }

                if (newRelative.mobile !== relativeToChange.mobile) {
                    newRelative.mobileConfirmed = false
                }

                const newStudent = Utils.merge(state.student, {relatives: newRelatives}),
                    nextState = {
                        student: newStudent,
                        validationMessages: {['relatives-' + index]: relativeMessage}
                    };

                return Utils.merge(state, nextState);
            }
            return state;
        })
    .add({type: 'addStudentRelative'},
        (state, action) => {
            let student = Utils.extend(state.student);
            if (!student.relatives) {
                student.relatives = [];
            }
            student.relatives.push({});

            const nextState = {
                student: student
            };
            return Utils.extend(state, nextState);
        })
    .add({type: 'removeStudentRelative'},
        (state, action) => {
            let validationMessages = state.validationMessages.relatives;
            let student = Utils.extend(state.student);
            if (student.relatives) {
                let newRelatives = [].concat(student.relatives);
                newRelatives.splice(state.selectedRelativeIndex, 1);
                validationMessages = {};
                student.relatives = newRelatives;
            }

            const nextState = {
                student: student,
                shownRelativeFileName: undefined,
                validationMessages: {students: state.validationMessages.students, relatives: validationMessages}
            };
            return Utils.extend(state, nextState);
        })

    .add({type: 'selectEntity', entity: 'relatives', ownerEntity: 'students', page: 'form'},
        (state, action) => {
            const relative = state.student.relatives[action.id];
            return Utils.merge(state, {
                selectedRelativeIndex: action.id,
                shownRelativeFileName: relative && Utils.specificOrFirst(relative.photos, relative.primaryPhotoName)
            });
        })
    .add({type: 'studentRelativeFilesUploaded'},
        (state, action) => {
            const originalPhotos = state.student.relatives[state.selectedRelativeIndex].photos,
                nextPhotos = Utils.pushNew(originalPhotos, action.fileNames),
                nextShownFileName = (!originalPhotos || nextPhotos.length > originalPhotos.length) ? nextPhotos[nextPhotos.length - 1] : state.shownRelativeFileName;

            return Utils.merge(state, {
                student: {
                    relatives: (oldRelatives) => {
                        let oldRelative = oldRelatives[state.selectedRelativeIndex];
                        let newRelatives = [].concat(oldRelatives);
                        let newRelative = Utils.merge(oldRelative, {
                            photos: nextPhotos
                        });
                        newRelatives[state.selectedRelativeIndex] = newRelative;
                        return newRelatives;
                    }
                },
                shownRelativeFileName: nextShownFileName
            });
        })
    .add({type: 'studentRelativeChangeShownFile'},
        (state, action) => {
            return Utils.merge(state, {
                shownRelativeFileName: action.fileName
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentRelatives', operation: 'verifyMobileNumber'},
        (state, action) => {
            const index = state.selectedRelativeIndex,
                relatives = state.student.relatives,
                nextRelatives = Utils.arr.putAt(relatives, index, Utils.extend(relatives[index], {
                    confirmationId: action.response.data,
                    confirmationCode: '',
                    mobileConfirmed: false
                }));

            return Utils.merge(state, {
                student: {
                    relatives: nextRelatives
                }
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentRelatives', operation: 'checkConfirmation'},
        (state, action) => {
            const index = action.request.relativeIndex,
                relatives = state.student.relatives,
                nextRelatives = Utils.arr.putAt(relatives, index, Utils.extend(relatives[index], {
                    mobileConfirmed: !action.response.error,
                }));

            return Utils.merge(state, {
                student: {
                    relatives: nextRelatives
                }
            });
        })
    .add({type: 'ajaxFinishedError', entity: 'students', operation: 'setPrimaryFile'},
        (state, action) => {
            return Utils.merge(state, {
                student: {
                    primaryPhotoName: action.request.fileName
                }
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'students', operation: 'removeFile'},
        (state, action) => {
            const nextPhotos = Utils.removeAndCopy(state.student.photos, action.request.fileName);
            return Utils.merge(state, {
                student: {
                    photos: nextPhotos
                },
                shownFileName: Utils.specificOrFirst(nextPhotos, state.shownFileName)
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentRelatives', operation: 'removeFile'},
        (state, action) => {
            const relativeAndIndex = Utils.arr.find(state.student.relatives, {id: action.request.relativeId}, Utils.obj.id),
                oldRelative = relativeAndIndex.obj;
            if (oldRelative) {
                const newPhotos = Utils.removeAndCopy(oldRelative.photos, action.request.fileName);
                return Utils.merge(state, {
                    student: {
                        relatives: (oldRelatives) => {
                            let newRelatives = [].concat(oldRelatives);
                            newRelatives[relativeAndIndex.index] = Utils.merge(oldRelative, {
                                photos: newPhotos
                            });
                            return newRelatives;
                        }
                    },
                    shownRelativeFileName: Utils.specificOrFirst(newPhotos, state.shownRelativeFileName)
                });
            }
            return state;
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'studentRelatives', operation: 'setPrimaryFile'},
        (state, action) => {
            const relativeAndIndex = Utils.arr.find(state.student.relatives, {id: action.request.relativeId}, Utils.obj.id),
                oldRelative = relativeAndIndex.obj;
            if (oldRelative) {
                return Utils.merge(state, {
                    student: {
                        relatives: (oldRelatives) => {
                            let newRelatives = [].concat(oldRelatives);
                            newRelatives[relativeAndIndex.index] = Utils.merge(oldRelative, {
                                primaryPhotoName: action.request.fileName
                            });
                            return newRelatives;
                        }
                    }
                });
            }
            return state;
        })
    .add({type: 'clearValidationMessages', entity: 'students'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {students: {}, relatives: {}}});
        })
    .add({type: 'setPageMode', entity: 'students', page: 'form', fieldId: 'siblings'},
        (state, action) => {
            return Utils.merge(state, {pageMode: action.mode});
        })
    .add({type: 'selectEntity', entity: 'siblings', ownerEntity: 'students', page: 'student'},
        (state, action) => {
            return Utils.merge(state, {selectedSiblingId: action.id});
        });

function getInitialState() {
    return {
        student: {},
        _student: {}, //unmodified read-only copy of student to get prev values
        pageMode: 'edit', //selectSibling
        selectedSiblingId: null,
        validationMessages: {
            students: {}
        }
    };
}

function StudentReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

function mapSendSmsStatus(status) {
    return smsStatusMessages[status] || smsStatusMessages.unknownError;
}

const smsStatusMessages = {
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
};


module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('students'), StudentReducer);
