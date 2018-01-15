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
            firstRecord: 1
        },
        totalRecords: 0,
        selectedObj: null,
        tableMessages: []
    };
    return initialState;
}

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxStarted', entity: 'roles', operation: 'findAll'},
        (state, action) => {
            return Utils.merge(state, {searchRequest: action.request, _loading: true});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'roles', operation: 'findAll'},
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
    .add({type: 'ajaxFinishedSuccess', entity: 'roles', operation: 'delete'},
        (state, action) => {
            const nextState = {
                entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
                totalRecords: state.totalRecords - 1
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'toggleSelectedObject', entity: 'roles'},
        (state, action) => {
            const nextState = {
                selectedObj: action.obj
            };
            return Utils.merge(state, nextState);
        });

function RolesReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = RolesReducer;
