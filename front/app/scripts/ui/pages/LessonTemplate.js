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
    Utils = require('../Utils'),
    Config = require('../Config'),
    {ButtonDatePicker, DatePicker} = require('../components/DateComponents'),
    PhotosList = require('../components/PhotosList'),
    AuditHeader = require('../components/AuditHeader'),
    StudentCardForm = require('../components/StudentCardForm'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    ConfirmPhoneInput = require('../components/ConfirmPhoneInput'),
    TemplateLessonSlots = require('../components/TemplateLessonSlots'),
    {Row, Col, FormGroup, ColFormGroup, Button, ProgressButton, MenuItem, Select} = require('../components/CompactGrid'),
    EntityLifeCyclePanel = require('../components/EntityLifeCyclePanel'),
    Validators = require('../Validators'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const Student = React.createClass({
    propTypes: {
        params: PropTypes.object,
        lessonTemplate: PropTypes.object,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;

        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId && Utils.isValidNumberId(id)) {
            p.dispatch(Actions.ajax.lessonTemplates.findOne(id));
        } else if (isNewId) {
            p.dispatch(Actions.initEntity('lessonTemplates'));
        }
        p.dispatch(Actions.clearValidationMessages('lessonTemplates'));
    },

    render(){
        const self = this, p = self.props;
        return <div>
            <PageToolbar leftButtons={[self.renderBackBtn(), self.renderSaveBtn()]}/>
            <h4 className="page-title">{Renderers.objName(p.lessonTemplate)}</h4>
            {Utils.selectFn(p.selectedLesson, () =>
                <div className="row row-compact actions-toolbar">
                    {Dictionaries.studentAge.map(opt =>
                        <div className="col-compact col-xs-3"
                             key={opt.id}>
                            <Button
                                classes={Utils.ui.btn.successClassForVal(p.selectedLesson && p.selectedLesson.ageGroup, opt.id)}
                                onClick={() => {
                                    if (p.selectedLesson.ageGroup != opt.id) {
                                        p.dispatch(Actions.setLessonAgeGroup(p.selectedLesson, opt.id))
                                    }
                                }}>
                                {opt.title}
                            </Button>
                        </div>
                    )}
                    <div className="col-compact col-xs-3"
                         key={'null'}>
                        <Button
                            classes={Utils.ui.btn.successClassForVal(p.selectedLesson && p.selectedLesson.ageGroup || null, null)}
                            onClick={() => {
                                if (p.selectedLesson.ageGroup) {
                                    p.dispatch(Actions.setLessonAgeGroup(p.selectedLesson, null))
                                }
                            }}>
                            {Utils.message('button.remove')}
                        </Button>
                    </div>
                </div>
            )}
            <EntityLifeCyclePanel {...p}
                                  entity="lessonTemplates"
                                  renderChildrenFn={self.renderForm}/>
        </div>
    },

    renderForm () {
        const self = this, p = self.props;
        return <div className="container with-nav-toolbar">
            <Row>
                <Col>
                    <ValidationMessage message={self.getValidationMessage('lessonTemplates', '_')}/>
                </Col>
                <ColFormGroup classes="col-md-4">
                    <label>{Utils.message('common.lessonTemplates.search.table.name')}</label>
                    <ValidationMessage message={self.getValidationMessage('lessonTemplates', 'name')}/>
                    <TextInput id="name"
                               owner={p.lessonTemplate}
                               name="name"
                               placeholder={Utils.message('common.lessonTemplates.search.table.name')}
                               defaultValue={p.lessonTemplate.name}
                               onChange={val => self.onFieldChange('name', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-6 col-md-4">
                    <label>{Utils.message('common.lessonTemplates.search.table.startDate')}</label>
                    <ValidationMessage message={self.getValidationMessage('lessonTemplates', 'startDate')}/>
                    <ButtonDatePicker date={p.lessonTemplate.startDate}
                                      onChange={val => self.onFieldChange('startDate', val)}/>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-6 col-md-4">
                    <label>{Utils.message('common.lessonTemplates.search.table.endDate')}</label>
                    <ValidationMessage message={self.getValidationMessage('lessonTemplates', 'endDate')}/>
                    <ButtonDatePicker date={p.lessonTemplate.endDate}
                                      onChange={val => self.onFieldChange('endDate', val)}/>
                </ColFormGroup>
            </Row>

            <div className='panel panel-default panel-compact'>
                <div className="container-fluid">
                    <TemplateLessonSlots
                        lessonTemplate={p.lessonTemplate}
                        onLessonClick={lesson => {
                            if (!p.selectedLesson || lesson.id != p.selectedLesson.id) {
                                p.dispatch(Actions.selectLesson(lesson));
                            }
                        }}
                        selectedLesson={p.selectedLesson}/>
                </div>
            </div>
        </div>
    },

    renderSaveBtn(){
        const self = this, p = self.props;
        return <ProgressButton key="save"
                               className="btn btn-default btn-lg nav-toolbar-btn"
                               onClick={() => self.onSave()}>
            {Icons.glyph.save()}
        </ProgressButton>
    },

    onSave(){
        const self = this, p = self.props;

        let validationMessages = {lessonTemplates: Validators.lessonTemplates(p.lessonTemplate)};

        if (Utils.isAllValuesEmpty(validationMessages.lessonTemplates)) {
            return p.dispatch(Actions.ajax.lessonTemplates.save(p.lessonTemplate))
                .then(
                    () => Navigator.navigate(Navigator.routes.lessonTemplates),
                    (error) => {
                        if (error.status === DataService.status.badRequest) {
                            p.dispatch(Actions.setValidationMessages('lessonTemplates', Utils.messages(error['lessonTemplates'])));
                        }
                    })
        } else {
            p.dispatch(Actions.setValidationMessages('lessonTemplates', validationMessages.lessonTemplates));
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

    onBack(){
        const self = this, p = self.props;
        if (!Utils.lifeCycle.entity(p, 'lessonTemplates').saved) {
            DialogService.confirmBack().then(() => {
                Navigator.navigate(Navigator.routes.lessonTemplates)
            }, () => {
            })
        } else {
            Navigator.navigate(Navigator.routes.lessonTemplates)
        }
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('lessonTemplates', fieldId, newValue))
    },


    getValidationMessage(entity, fieldId){
        const self = this, p = self.props;
        let messages = p.validationMessages[entity];
        return messages ? messages[fieldId] : null;
    }
});

function mapStateToProps(state) {
    return state.pages.LessonTemplate
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Student);
