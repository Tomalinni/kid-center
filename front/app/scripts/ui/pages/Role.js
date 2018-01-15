'use strict';

const React = require('react'),
    {connect} = require('react-redux'),
    ResizeSensor = require('css-element-queries/src/ResizeSensor'),

    AuthService = require('../services/AuthService'),
    Dictionaries = require('../Dictionaries'),
    Renderers = require('../Renderers'),
    Navigator = require('../Navigator'),
    DataService = require('../services/DataService'),
    DialogService = require('../services/DialogService'),
    Actions = require('../actions/Actions'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    DatePicker = require('../components/DateComponents').DatePicker,
    PhotosList = require('../components/PhotosList'),
    StudentCardList = require('../components/StudentCardList'),
    StudentCardForm = require('../components/StudentCardForm'),
    TextInput = require('../components/TextInput'),
    ConfirmPhoneInput = require('../components/ConfirmPhoneInput'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    Validators = require('../Validators'),
    ValidationMessage = require('../components/ValidationMessage'),
    FormScreen = require('../components/FormScreen'),
    Icons = require('../components/Icons');


const {PropTypes} = React;

const Role = React.createClass({
    propTypes: {
        params: PropTypes.object,
        role: PropTypes.object,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;

        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId) {
            p.dispatch(Actions.ajax.roles.findOne(id));
        } else {
            p.dispatch(Actions.initEntity('roles'));
        }
        p.dispatch(Actions.clearValidationMessages('roles'));
    },

    render(){
        const self = this, p = self.props;
        return <FormScreen entitiesId='roles'
                           entityId='role'
                           entity={p.role}
                           pageTitle={Renderers.role.info(p.role)}
                           ajaxResource={Actions.ajax.roles}
                           entitiesRoute={Navigator.routes.roles}
                           formElementFn={self.renderFormElement}
                           {...p}
        />
    },

    renderFormElement (onFieldChange) {
        const self = this, p = self.props;
        return <div>
            { self.renderIdInput() }
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.roles.search.table.permissions')}</label>
                        <ValidationMessage message={self.getValidationMessage('permissions')}/>
                        <Select
                            name="permissions"
                            placeholder={Utils.message('common.roles.search.table.permissions')}
                            value={p.role.permissions}
                            valueKey="id"
                            labelKey="name"
                            multi={true}
                            options={Dictionaries.permissions}
                            onChange={(opt) => {
                                self.onFieldChange('permissions', opt)
                            }}/>
                    </div>
                </div>
            </div>
        </div>
    },

    renderIdInput() {
        const self = this, p = self.props;
        const isNew = !p.role.id;
        if (isNew) {
            return <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.roles.search.table.id')}</label>
                        <ValidationMessage message={self.getValidationMessage('newId')}/>
                        <TextInput id="newId"
                                   owner={p.role}
                                   name="newId"
                                   placeholder={Utils.message('common.roles.search.table.id')}
                                   defaultValue={p.role.newId}
                                   onChange={(val)=>self.onFieldChange('newId', val)}
                        />
                    </div>
                </div>
            </div>
        }
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('roles', fieldId, newValue))
    },

    getValidationMessage(fieldId){
        const self = this, p = self.props;
        return p.validationMessages['roles'][fieldId];
    }
});

function mapStateToProps(state) {
    return state.pages.Role;
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Role);