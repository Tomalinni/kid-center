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
    DialogService = require('../services/DialogService'),
    Navigator = require('../Navigator'),
    Dictionaries = require('../Dictionaries'),
    Renderers = require('../Renderers'),
    DataService = require('../services/DataService'),
    Actions = require('../actions/Actions'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    DatePicker = require('../components/DateComponents').DatePicker,
    PhotosList = require('../components/PhotosList'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select, ProgressButton} = require('../components/CompactGrid'),
    Validators = require('../Validators'),
    EntityLifeCyclePanel = require('../components/EntityLifeCyclePanel'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const Card = React.createClass({
    propTypes: {
        params: PropTypes.object,
        card: PropTypes.object,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;

        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId && Utils.isValidNumberId(id)) {
            p.dispatch(Actions.ajax.cards.findOne(id));
        } else if (isNewId) {
            p.dispatch(Actions.initEntity('cards'));
        }
        p.dispatch(Actions.clearValidationMessages('cards'));
    },

    render(){
        const self = this, p = self.props;

        return <div>
            <PageToolbar leftButtons={[self.renderBackBtn(), self.renderSaveBtn()]}/>
            <h4 className="page-title">
                {Renderers.cardActiveState(p.card, 'title')}&nbsp;
                {Renderers.card.info.panel(p.card)}
            </h4>
            <EntityLifeCyclePanel {...p}
                                  entity="cards"
                                  renderChildrenFn={self.renderForm}/>
        </div>
    },

    renderForm () {
        const self = this, p = self.props;
        return <div className="container with-nav-toolbar">
            <Row>
                <ColFormGroup classes="col-sm-4">
                    <label>{Utils.message('common.cards.search.table.ageRange')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'ageRange')}/>
                    <Select
                        name="ageRange"
                        placeholder={Utils.message('common.cards.search.table.ageRange')}
                        value={Dictionaries.ageRange.byId(p.card.ageRange)}
                        valueRenderer={Renderers.dictOption}
                        optionRenderer={Renderers.dictOption}
                        options={Dictionaries.ageRange}
                        onChange={o => self.onFieldChange('ageRange', o && o.id)}
                    />
                </ColFormGroup>
                <ColFormGroup classes="col-sm-8">
                    <label>{Utils.message('common.cards.search.table.allowedSubjects')}</label>
                    <div>{Renderers.arr(Utils.bitmaskToArrayItems(p.card.allowedSubjectsMask, Dictionaries.lessonSubject).map(opt => opt.title), ', ')}</div>
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup classes="col-md-2 col-sm-6">
                    <label>{Utils.message('common.cards.search.table.creationDate')}</label>
                    <DatePicker id="creationDate"
                                value={p.card.creationDate}
                                onChange={self.onFieldChange.bind(this, 'creationDate')}>
                        <button type="button" className="form-control btn btn-default">
                            {Renderers.valOrNotDefined(p.card.creationDate)}
                            &nbsp;{Icons.caret()} </button>
                    </DatePicker>
                </ColFormGroup>
                <ColFormGroup classes="col-md-2 col-sm-6">
                    <label>{Utils.message('common.cards.search.table.expirationDate')}</label>
                    <DatePicker id="expirationDate"
                                value={p.card.expirationDate}
                                onChange={self.onFieldChange.bind(this, 'expirationDate')}>
                        <button type="button" className="form-control btn btn-default">
                            {Renderers.valOrNotDefined(p.card.expirationDate)}
                            &nbsp;{Icons.caret()} </button>
                    </DatePicker>
                </ColFormGroup>
                <ColFormGroup classes="col-md-2 col-sm-6">
                    <label>{Utils.message('common.cards.search.table.activeState')}</label>
                    <button type="button"
                            className="form-control btn btn-default"
                            onClick={self.toggleActiveState}>{
                        p.card.active ? Utils.message('common.cards.activeState.deactivate') : Utils.message('common.cards.activeState.activate')
                    }</button>
                </ColFormGroup>
                <ColFormGroup classes="col-md-2 col-sm-6">
                    <label>{Utils.message('common.cards.search.table.price')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'price')}/>
                    <TextInput id="price"
                               owner={p.card}
                               name="price"
                               placeholder={Utils.message('common.cards.search.table.price')}
                               defaultValue={p.card.price}
                               onChange={self.onFieldChange.bind(this, 'price')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-2 col-sm-6">
                    <label>{Utils.message('common.cards.search.table.maxDiscount')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'maxDiscount')}/>
                    <TextInput id="maxDiscount"
                               owner={p.card}
                               name="maxDiscount"
                               placeholder={Utils.message('common.cards.search.table.maxDiscount')}
                               defaultValue={p.card.maxDiscount}
                               onChange={self.onFieldChange.bind(this, 'maxDiscount')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-2 col-sm-6">
                    <label>{Utils.message('common.cards.search.table.maxSalesCount')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'maxSalesCount')}/>
                    <TextInput id="maxSalesCount"
                               owner={p.card}
                               name="maxSalesCount"
                               placeholder={Utils.message('common.cards.search.table.maxSalesCount')}
                               defaultValue={p.card.maxSalesCount}
                               onChange={self.onFieldChange.bind(this, 'maxSalesCount')}/>
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup classes="col-sm-4">
                    <label>{Utils.message('common.cards.search.table.visitType')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'visitType')}/>
                    <Select
                        name="visitType"
                        placeholder={Utils.message('common.cards.search.table.visitType')}
                        value={Dictionaries.visitType.byId(p.card.visitType)}
                        valueRenderer={self.renderOption}
                        optionRenderer={self.renderOption}
                        options={Dictionaries.visitType}
                        onChange={self.onOptionChanged.bind(this, self.onFieldChange.bind(this, 'visitType'))}
                    />
                </ColFormGroup>
                <ColFormGroup classes="col-sm-4">
                    <label>{Utils.message('common.cards.search.table.duration')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'durationDays')}/>
                    <TextInput id="durationDays"
                               owner={p.card}
                               name="durationDays"
                               placeholder={Utils.message('common.cards.search.table.duration')}
                               defaultValue={p.card.durationDays}
                               onChange={self.onFieldChange.bind(this, 'durationDays')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-sm-4">
                    <label>{Utils.message('common.cards.search.table.maxDuration')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'durationDaysMax')}/>
                    <TextInput id="durationDaysMax"
                               owner={p.card}
                               name="durationDaysMax"
                               placeholder={Utils.message('common.cards.search.table.maxDuration')}
                               defaultValue={p.card.durationDaysMax}
                               onChange={self.onFieldChange.bind(this, 'durationDaysMax')}/>
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup classes="col-sm-3">
                    <label>{self.getLessonsLimitLabel()}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'lessonsLimit')}/>
                    <TextInput id="lessonsLimit"
                               owner={p.card}
                               name="lessonsLimit"
                               placeholder={Utils.message('common.cards.search.table.lessonsLimit')}
                               defaultValue={p.card.lessonsLimit}
                               readOnly={Utils.isTransferCard(p.card)}
                               onChange={self.onFieldChange.bind(this, 'lessonsLimit')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-sm-3">
                    <label>{Utils.message('common.cards.search.table.cancelsLimit')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'cancelsLimit')}/>
                    <TextInput id="cancelsLimit"
                               owner={p.card}
                               name="cancelsLimit"
                               placeholder={Utils.message('common.cards.search.table.cancelsLimit')}
                               defaultValue={p.card.cancelsLimit}
                               onChange={self.onFieldChange.bind(this, 'cancelsLimit')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-sm-3">
                    <label>{Utils.message('common.cards.search.table.lateCancelsLimit')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'lateCancelsLimit')}/>
                    <TextInput id="lateCancelsLimit"
                               owner={p.card}
                               name="lateCancelsLimit"
                               placeholder={Utils.message('common.cards.search.table.lateCancelsLimit')}
                               defaultValue={p.card.lateCancelsLimit}
                               onChange={self.onFieldChange.bind(this, 'lateCancelsLimit')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-sm-3">
                    <label>{Utils.message('common.cards.search.table.lastMomentCancelsLimit')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'lastMomentCancelsLimit')}/>
                    <TextInput id="lastMomentCancelsLimit"
                               owner={p.card}
                               name="lastMomentCancelsLimit"
                               placeholder={Utils.message('common.cards.search.table.lastMomentCancelsLimit')}
                               defaultValue={p.card.lastMomentCancelsLimit}
                               onChange={self.onFieldChange.bind(this, 'lastMomentCancelsLimit')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-sm-3">
                    <label>{Utils.message('common.cards.search.table.undueCancelsLimit')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'undueCancelsLimit')}/>
                    <TextInput id="undueCancelsLimit"
                               owner={p.card}
                               name="undueCancelsLimit"
                               placeholder={Utils.message('common.cards.search.table.undueCancelsLimit')}
                               defaultValue={p.card.undueCancelsLimit}
                               onChange={self.onFieldChange.bind(this, 'undueCancelsLimit')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-sm-3">
                    <label>{Utils.message('common.cards.search.table.missLimit')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'missLimit')}/>
                    <TextInput id="missLimit"
                               owner={p.card}
                               name="missLimit"
                               placeholder={Utils.message('common.cards.search.table.missLimit')}
                               defaultValue={p.card.missLimit}
                               onChange={self.onFieldChange.bind(this, 'missLimit')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-sm-3">
                    <label>{Utils.message('common.cards.search.table.suspendsLimit')}</label>
                    <ValidationMessage message={self.getValidationMessage('cards', 'suspendsLimit')}/>
                    <TextInput id="suspendsLimit"
                               owner={p.card}
                               name="suspendsLimit"
                               placeholder={Utils.message('common.cards.search.table.suspendsLimit')}
                               defaultValue={p.card.suspendsLimit}
                               onChange={self.onFieldChange.bind(this, 'suspendsLimit')}/>
                </ColFormGroup>
            </Row>
        </div>
    },


    getLessonsLimitLabel() {
        const self = this, p = self.props;

        const label = Utils.message('common.cards.search.table.lessonsLimit');
        if (Utils.isTransferCard(p.card)) {
            return label + ' ' + Utils.message('common.cards.lessonsLimit.transfer.card.hint')
        }
        return label
    },

    renderSaveBtn(){
        const self = this, p = self.props;
        return <ProgressButton key="save"
                               disabled={self.isSaveDisabled()}
                               className="btn btn-default btn-lg nav-toolbar-btn"
                               onClick={() => self.onSave()}>
            {Icons.glyph.save()}
        </ProgressButton>

    },
    onSave(){
        const self = this, p = self.props;
        let validationMessages = {cards: Validators.cards(p.card)};

        if (Utils.isAllValuesEmpty(validationMessages.cards)) {
            return p.dispatch(Actions.ajax.cards.save(p.card))
                .then(
                    () => Navigator.navigate(Navigator.routes.cards),
                    (error) => {
                        if (error.status === DataService.status.badRequest) {
                            p.dispatch(Actions.setValidationMessages('cards', Utils.messages(error['cards'])));
                        }
                    })
        } else {
            p.dispatch(Actions.setValidationMessages('cards', validationMessages.cards));
        }
    },
    renderBackBtn(){
        const self = this, p = self.props;
        return <button type="button"
                       key="back"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={self.onBack}>
            {Icons.glyph.arrowLeft()}
        </button>
    },

    isSaveDisabled(){
        const self = this, p = self.props,
            card = p.card;
        return false
    },

    onBack(){
        const self = this, p = self.props;
        if (!Utils.lifeCycle.entity(p, 'cards').saved) {
            DialogService.confirmBack().then(() => {
                Navigator.navigate(Navigator.routes.cards)
            }, () => {
            })
        } else {
            Navigator.navigate(Navigator.routes.cards)
        }
    },

    renderOption(opt) {
        return opt.title;
    },
    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('cards', fieldId, newValue))
    },
    onOptionChanged(onFieldChange, opt) {
        onFieldChange.call(this, opt && opt.id);
    },
    toggleActiveState(){
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('cards', 'active', !p.card.active))
    },
    getValidationMessage(entity, fieldId){
        const self = this, p = self.props;
        let messages = p.validationMessages[entity];
        return messages ? messages[fieldId] : null;
    }

});

function mapStateToProps(state) {
    return state.pages.Card
}
function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Card);
