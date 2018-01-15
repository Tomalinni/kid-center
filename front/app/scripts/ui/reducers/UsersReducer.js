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
    .add({type: 'setSearchRequest', entity: 'students'},
        (state, action) => {
            return Utils.merge(state, {searchRequest: action.request});
        })
    .add({type: 'ajaxStarted', entity: 'users', operation: 'findAll'},
        (state, action) => {
            return Utils.merge(state, {_loading: true});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'users', operation: 'findAll'},
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
    .add({type: 'ajaxFinishedSuccess', entity: 'users', operation: 'delete'},
        (state, action) => {
            const nextState = {
                entities: Utils.arr.remove(state.entities, action.response, Utils.obj.id),
                totalRecords: state.totalRecords - 1
            };
            return Utils.merge(state, nextState);
        })
    .add({type: 'toggleSelectedObject', entity: 'users'},
        (state, action) => {
            const nextState = {
                selectedObj: action.obj
            };
            return Utils.merge(state, nextState);
        });

function UsersReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = UsersReducer;
