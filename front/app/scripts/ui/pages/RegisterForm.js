/*
 * (C) Copyright ${YEAR} Legohuman (https://github.com/Legohuman).
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    Forms = require('../Forms'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    DatePicker = require('../components/DateComponents').DatePicker,
    PhotosList = require('../components/PhotosList'),
    StudentCardList = require('../components/StudentCardList'),
    StudentCardForm = require('../components/StudentCardForm'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    ConfirmPhoneInput = require('../components/ConfirmPhoneInput'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    Validators = require('../Validators'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const RegisterForm = React.createClass({
    propTypes: {
        params: PropTypes.object,
        regRelative: PropTypes.object,
        selectedRelativeIndex: PropTypes.number,
        shownFileName: PropTypes.string,
        smsVerificationCode: PropTypes.string,
        shownRelativeFileName: PropTypes.string,
        dispatch: PropTypes.func
    },


    render(){
        const self = this, p = self.props;
        return <div>
            <h4 className="page-title">{Utils.message('common.pages.register')}</h4>
            { self.renderForm() }
        </div>

    },

    renderForm(){
        const self = this, p = self.props;

        return <div className="container with-nav-toolbar">
            <Row>
                <Col>
                    <ValidationMessage message={self.getValidationMessage('regRelatives', '_')}/>
                </Col>
                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.register.fields.login')}</label>
                    <ValidationMessage message={self.getValidationMessage('regRelatives', 'login')}/>
                    <TextInput id="login"
                               owner={p.regRelative}
                               name="login"
                               placeholder={Utils.message('common.register.fields.login')}
                               defaultValue={p.regRelative.login}
                               onChange={p.onChange.regRelatives.val('login')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.register.fields.mobile')}</label>
                    <ValidationMessage message={self.fetchMessage()}/>
                    <ConfirmPhoneInput
                        owner={p.regRelative}
                        phone={{
                            id: 'mobile',
                            name: 'mobile',
                            placeholder: Utils.message('common.register.fields.mobile'),
                            defaultValue: p.regRelative.mobile,
                            onChange: p.onChange.regRelatives.val('mobile')
                        }}
                        confirm={{
                            id: 'confirmationCode',
                            name: 'confirmationCode',
                            placeholder: Utils.message('common.register.fields.confirmationCode'),
                            defaultValue: p.regRelative.confirmationCode,
                            onChange: p.onChange.regRelatives.val('confirmationCode')
                        }}
                        mobileConfirmed={false}
                        validationMessage={self.getValidationMessage('regRelatives', 'mobile')}
                        confirmationId={p.regRelative.confirmationId}
                        sendSms={() => p.dispatch(Actions.ajax.register.verifyMobileNumber(p.regRelative.mobile))}
                        goBack={() => p.onChange.regRelatives.val('confirmationId')(null)}
                    />
                </ColFormGroup>
                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.register.fields.password')}</label>
                    <ValidationMessage message={self.getValidationMessage('regRelatives', 'pass')}/>
                    <TextInput id="pass"
                               owner={p.regRelative}
                               name="pass"
                               placeholder={Utils.message('common.register.fields.password')}
                               password={true}
                               defaultValue={p.regRelative.pass}
                               onChange={p.onChange.regRelatives.val('pass')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.register.fields.repeatPassword')}</label>
                    <ValidationMessage message={self.getValidationMessage('regRelatives', 'passRepeat')}/>
                    <TextInput id="passRepeat"
                               owner={p.regRelative}
                               name="passRepeat"
                               placeholder={Utils.message('common.register.fields.repeatPassword')}
                               password={true}
                               defaultValue={p.regRelative.passRepeat}
                               onChange={p.onChange.regRelatives.val('passRepeat')}/>
                </ColFormGroup>
            </Row>
            <Row>
                {self.renderRegisterBtn()}
            </Row>
        </div>
    },

    renderRegisterBtn(){
        const self = this, p = self.props;

        return <Col classes="text-centered">
            <button className="btn btn-success btn-lg"
                    onClick={() => self.register()}>
                {Utils.message('button.register')}
            </button>
        </Col>
    },

    register(){
        const self = this, p = self.props;

        DataService.operations.auth.register(p.regRelative).then((response) => {
            AuthService.applyToken(response.token);
            Navigator.navigate(Navigator.routes.children)
        }, (error) => {
            p.dispatch(Actions.setValidationMessages('regRelatives', error.regRelatives));
        });
    },

    fetchMessage () {
        const self = this, p = self.props;

        const validationMessage = self.getValidationMessage('regRelatives', 'mobile');
        if (validationMessage) return validationMessage;
        if (p.regRelative.confirmationId) return Utils.message('common.sms.confirmation.sent');
        return null
    },

    getValidationMessage(entity, fieldId, index) {
        const self = this, p = self.props;
        let messages = p.validationMessages[entity];
        if (!messages) {
            return null;
        }
        if (index == null) {
            return messages[fieldId];
        }
        const messagesObj = messages[index];
        return messagesObj ? messagesObj[fieldId] : null;
    }
});

function mapStateToProps(state) {
    return state.pages.RegisterForm
}

function mapDispatchToProps(dispatch) {
    return {
        dispatch,
        onChange: Forms.fields.onChangeFns(dispatch, ['regRelatives'])
    }
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(RegisterForm);
