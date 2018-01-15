'use strict';

const React = require('react'),
    {connect} = require('react-redux'),
    ResizeSensor = require('css-element-queries/src/ResizeSensor'),

    AuthService = require('../services/AuthService'),
    Dictionaries = require('../Dictionaries'),
    Renderers = require('../Renderers'),
    Navigator = require('../Navigator'),
    DialogService = require('../services/DialogService'),
    Actions = require('../actions/Actions'),
    Utils = require('../Utils'),
    Config = require('../Config'),
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

const User = React.createClass({
    propTypes: {
        params: PropTypes.object,
        user: PropTypes.object,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;

        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId) {
            p.dispatch(Actions.ajax.users.findOne(id));
        } else {
            p.dispatch(Actions.initEntity('users'));
        }
        p.dispatch(Actions.clearValidationMessages('users'));
    },

    render(){
        const self = this, p = self.props;
        return <FormScreen entitiesId='users'
                           entityId='user'
                           entity={p.user}
                           pageTitle={Renderers.user.info(p.user)}
                           ajaxResource={Actions.ajax.users}
                           entitiesRoute={Navigator.routes.users}
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
                        <label>{Utils.message('common.users.search.table.name')}</label>
                        <ValidationMessage message={self.getValidationMessage('name')}/>
                        <TextInput id="name"
                                   owner={p.user}
                                   name="name"
                                   placeholder={Utils.message('common.users.search.table.name')}
                                   defaultValue={p.user.name}
                                   onChange={(val) => onFieldChange('name', val)}/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.users.search.table.roles')}</label>
                        <ValidationMessage message={self.getValidationMessage('roles')}/>
                        <Select
                            name="roles"
                            placeholder={Utils.message('common.users.search.table.roles')}
                            value={p.user.roles}
                            valueKey="id"
                            labelKey="id"
                            multi={true}
                            options={p.roles}
                            onChange={(opt) => {
                                self.onFieldChange('roles', opt)
                            }}/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.users.search.table.password')}</label>
                        <ValidationMessage message={self.getValidationMessage('pass')}/>
                        <TextInput id="pass"
                                   owner={p.user}
                                   name="pass"
                                   password={true}
                                   placeholder={Utils.message('common.users.search.table.password')}
                                   defaultValue={p.user.pass}
                                   onChange={self.onFieldChange.bind(this, 'pass')}/>
                    </div>
                </div>
            </div>
        </div>
    },

    renderIdInput() {
        const self = this, p = self.props;
        const isNew = !p.user.id;
        if (isNew) {
            return <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.users.search.table.id')}</label>
                        <ValidationMessage message={self.getValidationMessage('newId')}/>
                        <TextInput id="newId"
                                   owner={p.user}
                                   name="newId"
                                   placeholder={Utils.message('common.users.search.table.id')}
                                   defaultValue={p.user.newId}
                                   onChange={(val) => self.onFieldChange('newId', val)}
                        />
                    </div>
                </div>
            </div>
        }
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('users', fieldId, newValue))
    },

    getValidationMessage(fieldId){
        const self = this, p = self.props;
        return p.validationMessages['users'][fieldId];
    }
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.User,
        {
            roles: state.pages.Roles.entities,
        });
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(User);