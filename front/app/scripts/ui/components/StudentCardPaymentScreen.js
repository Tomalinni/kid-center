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
    ResizeSensor = require('css-element-queries/src/ResizeSensor'),

    AuthService = require('../services/AuthService'),
    Dictionaries = require('../Dictionaries'),
    Renderers = require('../Renderers'),
    Navigator = require('../Navigator'),
    Actions = require('../actions/Actions'),
    DataService = require('../services/DataService'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    PhotosList = require('../components/PhotosList'),
    StudentCardList = require('../components/StudentCardList'),
    AccountSelect = require('../components/AccountSelect'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    {DatePicker} = require('../components/DateComponents'),
    Buttons = require('../components/Buttons'),
    ValidationMessage = require('../components/ValidationMessage'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    Icons = require('../components/Icons');

const StudentCardPaymentScreen = React.createClass({
    render(){
        const self = this, p = self.props;
        if (p.studentCardPayment) {
            if (p.studentCardPayment.card) {
                return self.renderStudentCardPaymentParamsScreen()
            } else {
                return self.renderSelectStudentCardScreen()
            }
        }
    },

    renderSelectStudentCardScreen(){
        const self = this, p = self.props;
        return <div>
            <PageToolbar leftButtons={[
                Buttons.toolbar.back(() => p.dispatch(Actions.setPageMode('studentCard', 'studentCard', 'card', 'close')))
            ]}/>
            <h4 className="page-title">{Utils.message('common.student.card.cards.title')}</h4>
            <StudentCardList forTransfer={!!p.studentCardPayment.transferSourceStudentId}
                             onSelect={card => self.onStudentCardPaymentFieldChange('card', card)}/>
        </div>
    },

    renderStudentCardPaymentParamsScreen(){
        const self = this, p = self.props;
        const card = p.studentCardPayment.card;
        return <div>
            {Utils.selectFn(card.id,
                () => <PageToolbar leftButtons={[
                    Buttons.toolbar.back(() => p.dispatch(Actions.setPageMode('studentCard', 'studentCard', 'card', 'close'))),
                    Buttons.toolbar.save(() => {
                        if (!Utils.objectHasDefinedValues(p.validationMessages['studentCardPayment'])) {
                            p.dispatch(Actions.ajax.studentCards.addPayment(Utils.studentCardAddPaymentRequest(p.studentCardPayment)))
                                .then(() => p.dispatch(Actions.setPageMode('studentCard', 'studentCard', 'card', 'close')),
                                    error => {
                                        if (error.status === DataService.status.badRequest) {
                                            self.setValidationMessages(p, Utils.messages(error))
                                        }
                                    })
                        }
                    })
                ]}/>,
                () => <PageToolbar leftButtons={[
                    Buttons.toolbar.back(() => self.onStudentCardPaymentFieldChange('card', null)),
                    Buttons.toolbar.save(() => {
                        if (!Utils.objectHasDefinedValues(p.validationMessages['studentCardPayment'])) {
                            p.dispatch(Actions.ajax.studentCards.save(Utils.studentCardCreateRequest(p.studentCardPayment, p.student.id)))
                                .then(() => p.dispatch(Actions.setPageMode('studentCard', 'studentCard', 'card', 'close')),
                                    error => {
                                        if (error.status === DataService.status.badRequest) {
                                            self.setValidationMessages(p, Utils.messages(error))
                                        }
                                    })
                        }
                    })
                ]}/>
            )}

            <h4 className="page-title">{Utils.message('common.student.card.set.card.params')}</h4>
            <div className="container high-inputs with-nav-toolbar">
                <Row>
                    <Col>
                        <ValidationMessage message={self.getValidationMessage('studentCardPayment', '_')}/>
                    </Col>
                </Row>

                {Utils.selectFn(!card.id, () =>
                    <Row>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.final.card.price', Renderers.numRange(card.price, card.price - card.maxDiscount))}</label>
                            <ValidationMessage message={self.getValidationMessage('studentCardPayment', 'price')}/>
                            <TextInput id="price"
                                       owner={p.studentCardPayment}
                                       name="price"
                                       placeholder={Utils.message('common.student.card.form.price')}
                                       defaultValue={p.studentCardPayment.finalPrice}
                                       onChange={val => self.onStudentCardPaymentFieldChange('finalPrice', self.onChangeCardPrice(val, card))}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.final.card.duration', Renderers.numRange(card.durationDays, card.durationDaysMax))}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'duration')}/>
                            <TextInput id="duration"
                                       owner={p.studentCardPayment}
                                       name="duration"
                                       placeholder={Utils.message('common.student.card.form.duration')}
                                       defaultValue={p.studentCardPayment.finalDuration}
                                       onChange={val => self.onStudentCardPaymentFieldChange('finalDuration', self.onChangeCardDuration(val, card))}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.form.purchase.date')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'purchaseDate')}/>
                            <DatePicker id="purchaseDate"
                                        value={p.studentCardPayment.purchaseDate}
                                        onChange={val => self.onStudentCardPaymentFieldChange('purchaseDate', val)}>
                                <button type="button" className="form-control btn btn-default">
                                    {p.studentCardPayment.purchaseDate || Utils.message('common.form.date.not.selected')}
                                    &nbsp;{Icons.caret()}
                                </button>
                            </DatePicker>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.form.lessons.limit')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'lessonsLimit')}/>
                            <TextInput id="lessonsLimit"
                                       owner={p.studentCardPayment}
                                       name="lessonsLimit"
                                       placeholder={Utils.message('common.student.card.form.lessons.limit')}
                                       defaultValue={p.studentCardPayment.lessonsLimit}
                                       onChange={val => self.onStudentCardPaymentFieldChange('lessonsLimit', val)}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.form.cancels.limit')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'cancelsLimit')}/>
                            <TextInput id="cancelsLimit"
                                       owner={p.studentCardPayment}
                                       name="cancelsLimit"
                                       placeholder={Utils.message('common.student.card.form.cancels.limit')}
                                       defaultValue={p.studentCardPayment.cancelsLimit}
                                       onChange={val => self.onStudentCardPaymentFieldChange('cancelsLimit', val)}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.form.lateCancels.limit')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'lateCancelsLimit')}/>
                            <TextInput id="lateCancelsLimit"
                                       owner={p.studentCardPayment}
                                       name="lateCancelsLimit"
                                       placeholder={Utils.message('common.student.card.form.lateCancels.limit')}
                                       defaultValue={p.studentCardPayment.lateCancelsLimit}
                                       onChange={val => self.onStudentCardPaymentFieldChange('lateCancelsLimit', val)}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.form.lastMomentCancels.limit')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'lastMomentCancelsLimit')}/>
                            <TextInput id="lastMomentCancelsLimit"
                                       owner={p.studentCardPayment}
                                       name="lastMomentCancelsLimit"
                                       placeholder={Utils.message('common.student.card.form.lastMomentCancels.limit')}
                                       defaultValue={p.studentCardPayment.lastMomentCancelsLimit}
                                       onChange={val => self.onStudentCardPaymentFieldChange('lastMomentCancelsLimit', val)}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.form.undueCancels.limit')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'undueCancelsLimit')}/>
                            <TextInput id="undueCancelsLimit"
                                       owner={p.studentCardPayment}
                                       name="undueCancelsLimit"
                                       placeholder={Utils.message('common.student.card.form.undueCancels.limit')}
                                       defaultValue={p.studentCardPayment.undueCancelsLimit}
                                       onChange={val => self.onStudentCardPaymentFieldChange('undueCancelsLimit', val)}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.form.miss.limit')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'missLimit')}/>
                            <TextInput id="missLimit"
                                       owner={p.studentCardPayment}
                                       name="missLimit"
                                       placeholder={Utils.message('common.student.card.form.miss.limit')}
                                       defaultValue={p.studentCardPayment.missLimit}
                                       onChange={val => self.onStudentCardPaymentFieldChange('missLimit', val)}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-3 col-sm-6">
                            <label>{Utils.message('common.student.card.form.suspends.limit')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'suspendsLimit')}/>
                            <TextInput id="suspendsLimit"
                                       owner={p.studentCardPayment}
                                       name="suspendsLimit"
                                       placeholder={Utils.message('common.student.card.form.suspends.limit')}
                                       defaultValue={p.studentCardPayment.suspendsLimit}
                                       onChange={val => self.onStudentCardPaymentFieldChange('suspendsLimit', val)}/>
                        </ColFormGroup>
                    </Row>
                )}
                {Utils.selectFn(p.studentCardPayment.finalPrice > 0, () =>
                    <Row>
                        <ColFormGroup classes="col-md-6">
                            <label>{Utils.message('common.student.card.student.account.type')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'accountType')}/>
                            <Select
                                name="type"
                                placeholder={Utils.message('common.student.card.student.account.type')}
                                value={Dictionaries.accountType.byId(p.studentCardPayment.accountType)}
                                valueRenderer={Renderers.dictOption}
                                optionRenderer={Renderers.dictOption}
                                options={Dictionaries.accountType}
                                onChange={opt => {
                                    self.onStudentCardPaymentFieldChange('accountType', opt && opt.id)
                                    if (opt) {
                                        self.setValidationMessages(p, {
                                            studentCardPayment: {accountType: null}
                                        })
                                    }
                                }}
                            />
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-6">
                            <label>{Utils.message('common.student.card.target.partner')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'targetPartner')}/>
                            <Select
                                name="targetPartner"
                                clearable={false}
                                placeholder={Utils.message('common.student.card.target.partner')}
                                value={p.studentCardPayment.targetPartner}
                                valueRenderer={Renderers.school.cityAndName}
                                optionRenderer={Renderers.school.cityAndName}
                                options={Utils.getSchoolsCollection(p.schoolsMap, 'incoming', 'target')}
                                onChange={opt => {
                                    self.onStudentCardPaymentFieldChange('targetPartner', opt);
                                    if (opt) {
                                        self.setValidationMessages(p, {
                                            studentCardPayment: {targetPartner: null}
                                        })
                                    }
                                } }
                            />
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-6">
                            <label>{Utils.message('common.student.card.target.account')}</label>
                            <ValidationMessage
                                message={self.getValidationMessage('studentCardPayment', 'targetAccount')}/>
                            <AccountSelect account={p.studentCardPayment.targetAccount}
                                           accountsMap={Utils.getAccountsSubMap(p.accountsMap, 'incoming', 'target')}
                                           schoolId={p.studentCardPayment.targetPartner && p.studentCardPayment.targetPartner.id}
                                           onSet={opt => {
                                               self.onStudentCardPaymentFieldChange('targetAccount', opt)
                                               if (opt) {
                                                   self.setValidationMessages(p, {
                                                       studentCardPayment: {targetAccount: null}
                                                   })
                                               }
                                           }}/>
                        </ColFormGroup>
                    </Row>
                )}
            </div>
        </div>
    },

    onStudentCardPaymentFieldChange(fieldId, val){
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('studentCardPayment', fieldId, val))
    },

    onChangeCardPrice(val, card){
        const self = this, p = self.props;
        let nextPrice = Utils.numberFromString(val, 0);
        if (!nextPrice || nextPrice < card.price - card.maxDiscount || nextPrice > card.price) {
            self.setValidationMessages(p, {
                studentCardPayment: {price: Utils.message('common.student.card.final.card.price', Renderers.numRange(card.price, card.price - card.maxDiscount))}
            });
        } else {
            self.setValidationMessages(p, {studentCardPayment: {price: null}});
        }
        return val;
    },

    onChangeCardDuration(val, card){
        const self = this, p = self.props;
        let nextDuration = Utils.numberFromString(val, 0);
        if (nextDuration < card.durationDays || nextDuration > card.durationDaysMax) {
            self.setValidationMessages(p, {
                studentCardPayment: {duration: Utils.message('common.student.card.final.card.duration', Renderers.numRange(card.durationDays, card.durationDaysMax))}
            });
        } else {
            self.setValidationMessages(p, {studentCardPayment: {duration: null}});
        }
        return val;
    },

    getValidationMessage(entity, fieldId){
        const self = this, p = self.props;
        let messages = p.validationMessages[entity];
        return messages ? messages[fieldId] : null;
    },

    setValidationMessages(props, validationMessages) {
        props.dispatch(Actions.setValidationMessages('studentCardPayment', validationMessages));
    }
});


module.exports = StudentCardPaymentScreen;
