const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    ReducersMap = require('./ReducersMap');

function getInitialState() {
    let initialState = {
        entities: [],
        searchRequest: {
            text: '',
            firstRecord: 1,
            activeDatePeriod: 'all'
        },
        totalRecords: 0,
        selectedObj: null,
        tableMessages: []
    };
    return initialState;
}

const reducers = new ReducersMap(getInitialState())
    .add({type: 'setSearchRequest', entity: 'homework'},
        (state, action) => {
            return Utils.merge(state, {searchRequest: action.request});
        })
    .add({type: 'ajaxStarted', entity: 'homework', operation: 'findAll'},
        (state, action) => {
            return Utils.merge(state, {_loading: true});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'homework', operation: 'findAll'},
        (state, action) => {
            const entities = action.request.appendResults ?
                Utils.mergeArraysByObjectKey(state.entities, action.response.results, 'id') :
                action.response.results;

            const nextState = {
                _loading: false,
                entities: entities,
                totalRecords: action.response.total
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'homework', operation: 'delete'},
        (state, action) => {
            const nextState = {
                entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
                totalRecords: state.totalRecords - 1
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'toggleSelectedObject', entity: 'homework'},
        (state, action) => {
            const nextState = {
                selectedObj: action.obj
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'showCarouselGallery', entity: 'homework'},
        (state, action) => {
            const files = action.obj ? action.obj.files : null;
            const nextState = {
                selectedObj: action.obj,
                shownAttachments: files
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'hideCarouselGallery'},
        (state, action) => {
            return Utils.extend(state, {
                shownAttachments: null
            });
        });

function HomeworksReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = HomeworksReducer;
