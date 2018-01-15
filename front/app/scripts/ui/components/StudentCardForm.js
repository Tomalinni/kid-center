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
    DialogService = require('../services/DialogService'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    Actions = require('../actions/Actions'),
    PageToolbar = require('../components/PageToolbar'),
    {Row, Col, FormGroup, ColFormGroup, Button} = require('../components/CompactGrid'),
    {DatePicker} = require('../components/DateComponents'),
    ValidationMessage = require('../components/ValidationMessage'),
    TextInput = require('../components/TextInput'),
    DataService = require('../services/DataService'),
    PhotosList = require('../components/PhotosList'),
    Icons = require('../components/Icons');

const StudentCardForm = React.createClass({

    render(){
        const self = this, p = self.props;
        return <div className="container high-inputs with-nav-toolbar">
            <Row>
                <Col>
                    <ValidationMessage message={self.getValidationMessage('studentCards', '_')}/>
                </Col>
                <ColFormGroup classes="col-md-12">
                    <label>{Utils.message('common.student.card.form.main.info')}</label>
                    <div className="form-block">{Renderers.student.card.info(p.studentCard)}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-md-12">
                    <label>{Utils.message('common.student.card.form.active.period')}</label>
                    <div className="form-block">{Renderers.student.card.activePeriod(p.studentCard)}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.price')}</label>
                    <div className="form-block">{p.studentCard.price}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.duration')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCards', 'durationDays')}/>
                    <TextInput id="durationDays"
                               owner={p.studentCard}
                               name="durationDays"
                               placeholder={Utils.message('common.student.card.form.duration')}
                               defaultValue={p.studentCard.durationDays}
                               onChange={val => p.onFieldChange('durationDays', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.purchase.date')}</label>
                    <ValidationMessage message={self.getValidationMessage('studentCards', 'purchaseDate')}/>
                    <DatePicker id="purchaseDate"
                                value={p.studentCard.purchaseDate}
                                onChange={val => p.onFieldChange('purchaseDate', val)}>
                        <button type="button" className="form-control btn btn-default">
                            {p.studentCard.purchaseDate || Utils.message('common.form.date.not.selected')}
                            &nbsp;{Icons.caret()}
                        </button>
                    </DatePicker>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.lessons.limit')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCards', 'lessonsLimit')}/>
                    <TextInput id="lessonsLimit"
                               owner={p.studentCard}
                               name="lessonsLimit"
                               placeholder={Utils.message('common.student.card.form.lessons.limit')}
                               defaultValue={p.studentCard.lessonsLimit}
                               onChange={val => p.onFieldChange('lessonsLimit', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.cancels.limit')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCards', 'cancelsLimit')}/>
                    <TextInput id="cancelsLimit"
                               owner={p.studentCard}
                               name="cancelsLimit"
                               placeholder={Utils.message('common.student.card.form.cancels.limit')}
                               defaultValue={p.studentCard.cancelsLimit}
                               onChange={val => p.onFieldChange('cancelsLimit', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.lateCancels.limit')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCards', 'lateCancelsLimit')}/>
                    <TextInput id="lateCancelsLimit"
                               owner={p.studentCard}
                               name="lateCancelsLimit"
                               placeholder={Utils.message('common.student.card.form.lateCancels.limit')}
                               defaultValue={p.studentCard.lateCancelsLimit}
                               onChange={val => p.onFieldChange('lateCancelsLimit', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.lastMomentCancels.limit')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCards', 'lastMomentCancelsLimit')}/>
                    <TextInput id="lastMomentCancelsLimit"
                               owner={p.studentCard}
                               name="lastMomentCancelsLimit"
                               placeholder={Utils.message('common.student.card.form.lastMomentCancels.limit')}
                               defaultValue={p.studentCard.lastMomentCancelsLimit}
                               onChange={val => p.onFieldChange('lastMomentCancelsLimit', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.undueCancels.limit')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCards', 'undueCancelsLimit')}/>
                    <TextInput id="undueCancelsLimit"
                               owner={p.studentCard}
                               name="undueCancelsLimit"
                               placeholder={Utils.message('common.student.card.form.undueCancels.limit')}
                               defaultValue={p.studentCard.undueCancelsLimit}
                               onChange={val => p.onFieldChange('undueCancelsLimit', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.miss.limit')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCards', 'missLimit')}/>
                    <TextInput id="missLimit"
                               owner={p.studentCard}
                               name="missLimit"
                               placeholder={Utils.message('common.student.card.form.miss.limit')}
                               defaultValue={p.studentCard.missLimit}
                               onChange={val => p.onFieldChange('missLimit', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-3 col-sm-6">
                    <label>{Utils.message('common.student.card.form.suspends.limit')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCards', 'suspendsLimit')}/>
                    <TextInput id="suspendsLimit"
                               owner={p.studentCard}
                               name="suspendsLimit"
                               placeholder={Utils.message('common.student.card.form.suspends.limit')}
                               defaultValue={p.studentCard.suspendsLimit}
                               onChange={val => p.onFieldChange('suspendsLimit', val)}/>
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup classes="col-md-12">
                    <label>{Utils.message('common.student.card.bills')}</label>
                    {self.renderPhotoList()}
                </ColFormGroup>
            </Row>
        </div>
    },

    renderPhotoList() {
        const self = this, p = self.props;
        return <div className="container-pad-top-10">
            <PhotosList hasPrimaryPhoto={true}
                        uploadUrl={DataService.urls.studentCards.uploadPhoto(p.studentCard.id)}
                        downloadUrlFn={(name) => {
                            return DataService.urls.studentCards.downloadPhoto(p.studentCard.id, name)
                        }}
                        onFileUploaded={p.onPhotoUploaded}
                        onRemoveFile={p.onRemovePhoto}
                        onChangeShownFile={p.onChangeShownPhoto}
                        fileNames={p.photos || []}
                        shownFileName={p.shownPhotoName}/></div>
    },

    getValidationMessage(entity, fieldId){
        return this.props.validationMessages[entity][fieldId];
    }

});

module.exports = StudentCardForm;
