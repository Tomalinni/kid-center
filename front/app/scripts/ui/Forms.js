'use strict';

const Actions = require('./actions/Actions'),
    Utils = require('./Utils');

const Forms = {

    fields: {
        onChangeFn(dispatch, entity, fieldId, accessor){
            return (val) => dispatch(Actions.setEntityValue(entity, fieldId, accessor(val)))
        },
        onChangeFns(dispatch, entities){
            const fnsObj = {};
            entities.forEach(entityId => {
                fnsObj[entityId] = {
                    val: fieldId => Forms.fields.onChangeFn(dispatch, entityId, fieldId, Utils.obj.self),
                    opt: fieldId => Forms.fields.onChangeFn(dispatch, entityId, fieldId, Utils.obj.id),
                    objName: fieldId => Forms.fields.onChangeFn(dispatch, entityId, fieldId, Utils.obj.name)
                }
            });
            return fnsObj
        }
    },
};

module.exports = Forms;
