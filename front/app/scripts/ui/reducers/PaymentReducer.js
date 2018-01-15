const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    Validators = require('../Validators'),
    EntityLifeCycleReducerFactory = require('./EntityLifeCycleReducerFactory'),
    ReducersMap = require('./ReducersMap');

const possibleParentCategories = ['category', 'category2', 'category3', 'category4'];
const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'payments', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {
                payment: action.response,
                shownFiles: {}
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'payments', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                payment: Utils.extend(state.payment, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'payments'},
        (state, action) => {
            const nextState = {
                payment: {
                    direction: 'outgoing'
                },
                shownFiles: {}
            };
            return Utils.extend(state, nextState);
        })
    .add({type: 'setEntityValue', entity: 'payments'},
        (state, action) => {
            let payment = state.payment;
            payment[action.fieldId] = action.newValue;
            if (action.options.refreshDependentFields) {
                if (action.fieldId === 'category') {
                    payment.category2 = null;
                    payment.category3 = null;
                    payment.category4 = null;
                    payment.category5 = null;
                }
                if (action.fieldId === 'category2') {
                    payment.category3 = null;
                    payment.category4 = null;
                    payment.category5 = null;
                }
                if (action.fieldId === 'category3') {
                    payment.category4 = null;
                    payment.category5 = null;
                }
                if (action.fieldId === 'category4') {
                    payment.category5 = null;
                }
                if (action.fieldId === 'productUrl' && !Utils.isAbsoluteUrl(payment.productUrl)) {
                    payment.productUrl = Utils.convertUrlToAbsolute(payment.productUrl);
                }
            }

            if (action.fieldId === 'direction') {
                payment.account = null;
                payment.targetAccount = null;
            }

            if (action.fieldId === 'school' && payment.account && !payment.account.schools.some(s => s.id === action.newValue.id)) {
                payment.account = null;
            }

            if (action.fieldId === 'targetSchool' && payment.targetAccount && !payment.targetAccount.schools.some(s => s.id === action.newValue.id)) {
                payment.targetAccount = null;
            }

            const nextState = {
                payment: payment,
                validationMessages: {
                    payments: Validators.payments(payment, action.fieldId)
                }
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'setValidationMessages', entity: 'payments'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {payments: action.messages}});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'categories', operation: 'save'},
        (state, action) => {
            const category = action.response,
                categoryParent = category.parent,
                p = state.payment;
            if (categoryParent) {
                const changedParentKey = possibleParentCategories.find((k) => {
                    const category = p[k];
                    return category && category.id === categoryParent.id
                });
                if (changedParentKey) {
                    const changedParent = state.payment[changedParentKey];
                    return Utils.merge(state, {
                        payment: {
                            [changedParentKey]: {children: Utils.arr.put(changedParent.children || [], category, Utils.obj.id)}
                        },
                    });
                }
            }
            return state;
        })
    .add({type: 'filesUploaded', entity: 'payments'},
        (state, action) => {
            const originalPhotos = state.payment[action.fieldId],
                nextPhotos = Utils.pushNew(originalPhotos, action.fileNames),
                nextShownFileName = (!originalPhotos || nextPhotos.length > originalPhotos.length) ? nextPhotos[nextPhotos.length - 1] : state.shownFiles[action.fieldId];

            return Utils.merge(state, {
                payment: {
                    [action.fieldId]: nextPhotos
                },
                shownFiles: {
                    [action.fieldId]: nextShownFileName
                }
            });
        })
    .add({type: 'changeShownFile', entity: 'payments'},
        (state, action) => {
            return Utils.merge(state, {
                shownFiles: {
                    [action.fieldId]: action.fileName
                }
            });
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'payments', operation: 'removeFile'},
        (state, action) => {
            const fieldId = action.request.fieldId,
                nextPhotos = Utils.removeAndCopy(state.payment[fieldId], action.request.fileName);
            return Utils.merge(state, {
                payment: {
                    [fieldId]: nextPhotos
                },
                shownFiles: {
                    [fieldId]: Utils.specificOrFirst(nextPhotos, state.shownFiles[fieldId])
                }
            });
        })
    .add({type: 'clearValidationMessages', entity: 'payments'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {payments: {}}});
        });

function getInitialState() {
    return {
        payment: {},
        shownFiles: {
            receiptPhotos: null,
            productPhotos: null
        },
        validationMessages: {
            payments: {},
        }
    };
}

function PaymentReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('payments'), PaymentReducer);
