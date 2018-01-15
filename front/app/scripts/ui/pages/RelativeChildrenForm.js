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

const RelativeChildrenForm = React.createClass({
    propTypes: {
        params: PropTypes.object,
        regRelative: PropTypes.object,
        selectedStudentIndex: PropTypes.number,
        dispatch: PropTypes.func
    },

    render(){
        const self = this, p = self.props;
        return <div>
            <PageToolbar/>
            <h4 className="page-title">{Utils.message('common.pages.children')}</h4>
            { self.renderForm() }
        </div>

    },

    renderForm () {
        const self = this, p = self.props,
            student = self.getSelectedStudent(),
            index = p.selectedStudentIndex || 0;

        return <div className="container with-nav-toolbar">
            <Row>
                <ColFormGroup>
                    <label>{Utils.message('common.students.relative.search.table.role')}</label>
                    <ValidationMessage message={self.getValidationMessage('regRelatives', 'role')}/>
                    <Select
                        name="role"
                        placeholder={Utils.message('common.students.relative.search.table.role')}
                        value={{name: p.regRelative.role}}
                        valueKey="name"
                        labelKey="name"
                        options={p.relativeRoles}
                        onChange={p.onChange.regRelatives.objName('role')}
                    />
                </ColFormGroup>
            </Row>

            <Row>
                <Col>
                    <FormGroup classes="btn-toolbar">
                        <div className="btn-group">
                            <button type="button" className="btn btn-default"
                                    onClick={() => p.dispatch(Actions.addRegRelativeChild())}>
                                {Utils.message('button.create')}
                            </button>
                            {Utils.select(Utils.isNotEmptyArray(p.regRelative.students) && p.regRelative.students.length > 1,
                                <button type="button" className="btn btn-default"
                                        onClick={() => DialogService.confirmDelete().then(() => {
                                            p.dispatch(Actions.removeRegRelativeChild())
                                        })}>
                                    {Utils.message('button.remove')}
                                </button>
                            )}
                        </div>

                        <div className="btn-group">
                            {(p.regRelative.students || []).map((child, i) => {
                                return <button type="button"
                                               key={'r' + i}
                                               className={'btn ' + Utils.select(p.selectedStudentIndex === i, 'btn-success', 'btn-default')}
                                               onClick={() => p.dispatch(Actions.selectRegRelativeChild(i))}>
                                    {child.nameCn || child.nameEn || Utils.message('common.students.relative.child.prefix') + (i + 1)}
                                    {self.renderValidationErrorsSign(p.validationMessages.regStudents, i)}
                                </button>
                            })}
                        </div>
                    </FormGroup>
                </Col>
            </Row>

            <Row>
                <Col>
                    <ValidationMessage message={self.getValidationMessage('regStudents', '_')}/>
                </Col>
                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.students.search.table.nameCn.full')}</label>
                    <ValidationMessage message={self.getValidationMessage('regStudents', 'nameCn', index)}/>
                    <TextInput id="nameCn"
                               owner={student}
                               name="nameCn"
                               placeholder={Utils.message('common.students.search.table.nameCn.full')}
                               defaultValue={student.nameCn}
                               onChange={p.onChange.regStudents.val('nameCn')}/>
                </ColFormGroup>
                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.students.search.table.nameEn.full')}</label>
                    <ValidationMessage message={self.getValidationMessage('regStudents', 'nameEn', index)}/>
                    <TextInput id="nameEn"
                               owner={student}
                               name="nameEn"
                               placeholder={Utils.message('common.students.search.table.nameEn.full')}
                               defaultValue={student.nameEn}
                               onChange={p.onChange.regStudents.val('nameEn')}/>
                </ColFormGroup>

                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.students.search.table.birthDate')}</label>
                    <ValidationMessage message={self.getValidationMessage('regStudents', 'birthDate', index)}/>
                    <DatePicker id="birthDate"
                                value={p.student.birthDate}
                                onChange={p.onChange.regStudents.val('birthDate')}>
                        <button type="button" className="form-control btn btn-default">
                            {Renderers.studentBirthDateAndAge(student)}
                            &nbsp;{Icons.caret()} </button>
                    </DatePicker>
                </ColFormGroup>
                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.students.search.table.gender')}</label>
                    <ValidationMessage message={self.getValidationMessage('regStudents', 'gender', index)}/>
                    <Select
                        name="gender"
                        placeholder={Utils.message('common.students.search.table.gender')}
                        value={Dictionaries.studentGender.byId(student.gender)}
                        valueRenderer={Renderers.dictOption}
                        optionRenderer={Renderers.dictOption}
                        options={Dictionaries.studentGender}
                        onChange={p.onChange.regStudents.opt('gender')}
                    />
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup>
                    <label>{Utils.message('common.students.search.table.kinderGarden')}</label>
                    <Select
                        async={true}
                        name="kinderGarden"
                        placeholder={Utils.message('common.students.search.table.kinderGarden')}
                        value={student.kinderGarden}
                        valueRenderer={Renderers.kinderGarden}
                        optionRenderer={Renderers.kinderGarden}
                        cache={false}
                        loadOptions={Utils.debounceInput((text, cb) =>
                            Utils.loadSelectOptions(DataService.operations.kindergardens.findAll(text), cb))}
                        onChange={p.onChange.regStudents.val('kinderGarden')}/>
                </ColFormGroup>
            </Row>
            <Row>
                {self.renderSaveBtn()}
            </Row>
        </div>
    },

    renderValidationErrorsSign(indexedMessages, i){
        if (indexedMessages && !Utils.isAllValuesEmpty(indexedMessages[i])) {
            return Icons.glyph.warningSign('icon-needs-validation')
        }
    },

    renderSaveBtn(){
        const self = this, p = self.props;

        return <Col classes="text-centered">
            <button className="btn btn-success btn-lg"
                    onClick={() => self.save()}>
                {Utils.message('button.save')}
            </button>
        </Col>
    },

    save(){
        const self = this, p = self.props;

        p.dispatch(Actions.ajax.children.save(p.regRelative)).then(
            () => {
                p.dispatch(Actions.setPageMode('regRelatives', 'children', 'save', 'overview'))
            },
            (error) => {
                if (error.status === DataService.status.badRequest) {
                    p.dispatch(Actions.setValidationMessages('regRelatives', error.regRelatives));
                }
            })

    },

    getSelectedStudent() {
        const self = this, p = self.props;
        return p.regRelative && p.regRelative.students ? p.regRelative.students[p.selectedStudentIndex] : null;
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
    return Utils.extend(state.pages.RelativeChildren,
        {
            relativeRoles: state.common.relativeRoles
        })
}

function mapDispatchToProps(dispatch) {
    return {
        dispatch,
        onChange: Forms.fields.onChangeFns(dispatch, ['regRelatives', 'regStudents'])
    }
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(RelativeChildrenForm);
