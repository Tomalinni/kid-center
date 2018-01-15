const Utils = require('../Utils');

function ReducersMap(initialState) {
    this.initialState = initialState;
    this._reducers = [];
    this._reducerKeys = [];
}

ReducersMap.prototype.add = function (actionTemplate, reducerFn) {
    let templateKey = Utils.objToKey(actionTemplate);
    if (Utils.arr.contains(this._reducerKeys, templateKey)) {
        console.error('Reducer with the following template already exists in map.', actionTemplate)
    } else {
        this._reducers.push({template: actionTemplate, fn: reducerFn});
        this._reducerKeys.push(templateKey);
    }
    return this;
};

ReducersMap.prototype.reduce = function (state, action) {
    if (!Utils.isDefined(state)) {
        return this.initialState;
    }
    let reducer = this._get(action);
    return reducer ? reducer.fn.call(this, state, action) : state;
};

ReducersMap.prototype._get = function (action) {
    if (Utils.isDefined(action) && this._reducers.length > 0) {
        return this._reducers.find(reducer=>_match(action, reducer.template));
    }
    return undefined;
};

function _match(action, actionTemplate) {
    if (!Utils.isDefined(action) || !Utils.isDefined(actionTemplate)) {
        return false;
    }
    for (let key in actionTemplate) {
        if (actionTemplate.hasOwnProperty(key) && actionTemplate[key] !== action[key]) {
            return false;
        }
    }
    return true;
}

module.exports = ReducersMap;