const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    Validators = require('../Validators'),
    EntityLifeCycleReducerFactory = require('./EntityLifeCycleReducerFactory'),
    ReducersMap = require('./ReducersMap');

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'homework', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {homework: action.response});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'homework', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                homework: Utils.extend(state.homework, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'homework'},
        (state, action) => {
            return Utils.extend(state, {homework: {}});
        })
    .add({type: 'setEntityValue', entity: 'homework'},
        (state, action) => {
            let homework = Utils.extend(state.homework, {[action.fieldId]: action.newValue});
            return Utils.merge(state, {
                homework: homework,
                validationMessages: {homework: Validators.homework(homework, action.fieldId)}
            });
        })
    .add({type: 'filesUploaded', entity: 'homework'},
        (state, action) => {
            const attachments = state.homework.files,
                nextAttachments = Utils.pushNew(attachments, action.fileNames),
                nextShownFileName = (!attachments || nextAttachments.length > attachments.length) ? nextAttachments[nextAttachments.length - 1] : state.shownFileName;

            return Utils.merge(state, {
                homework: {
                    files: nextAttachments
                },
                shownFileName: nextShownFileName
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'homework', operation: 'removeFile'},
        (state, action) => {
            const nextAttachments = Utils.removeAndCopy(state.homework.files, action.request.fileName);
            return Utils.merge(state, {
                homework: {
                    files: nextAttachments
                }
            });
        })
    .add({type: 'setValidationMessages', entity: 'homework'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {homework: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'homework'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {homework: {}}});
        });

function getInitialState() {
    return {
        homework: {},
        statusMessage: "",
        validationMessages: {
            homework: {}
        }
    };
}

function HomeworkReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('homework'), HomeworkReducer);
