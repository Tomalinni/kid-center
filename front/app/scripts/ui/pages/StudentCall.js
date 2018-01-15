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

    Dictionaries = require('../Dictionaries'),
    Renderers = require('../Renderers'),
    Navigator = require('../Navigator'),
    DataService = require('../services/DataService'),
    DialogService = require('../services/DialogService'),
    Actions = require('../actions/Actions'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    PageToolbar = require('../components/PageToolbar'),
    DatePicker = require('../components/DateComponents').DatePicker,
    TextInput = require('../components/TextInput'),
    Validators = require('../Validators'),
    {Row, Col, FormGroup, ColFormGroup, ProgressButton, Select} = require('../components/CompactGrid'),
    EntityLifeCyclePanel = require('../components/EntityLifeCyclePanel'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const StudentCall = React.createClass({
    propTypes: {
        params: PropTypes.object,
        studentCall: PropTypes.object,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;

        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId && Utils.isValidNumberId(id)) {
            p.dispatch(Actions.ajax.studentCalls.findOne(id));
        } else if (isNewId) {
            p.dispatch(Actions.ajax.students.findOne(p.params.studentId));
            p.dispatch(Actions.initEntity('studentCalls'));
        }
        p.dispatch(Actions.clearValidationMessages('studentCalls'));
    },

    render(){
        const self = this, p = self.props;

        return <div>
            <PageToolbar leftButtons={[self.renderBackBtn(), self.renderSaveBtn()]}/>
            <h4 className="page-title">{Utils.message('common.student.call.edit.toolbar.title')}</h4>
            <EntityLifeCyclePanel {...p}
                                  entity="studentCalls"
                                  renderChildrenFn={self.renderForm}/>
        </div>
    },

    renderForm(){
        const self = this, p = self.props,
            student = p.studentCall.student,
            relatives = student && student.relatives || [];

        return <div className="container high-inputs">
            <Row>
                <ColFormGroup classes="col-xs-6">
                    <label>{Utils.message('common.student.call.form.date')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCalls', 'date')}/>
                    <DatePicker id="date"
                                showTime={true}
                                value={p.studentCall.date}
                                onChange={self.onFieldChange.bind(this, 'date')}>
                        <button type="button" className="form-control btn btn-default">
                            {Renderers.valOrNotDefined(Utils.humanReadableDateTimeFromString(p.studentCall.date))}
                            &nbsp;{Icons.caret()} </button>
                    </DatePicker>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-6">
                    <label>{Utils.message('common.student.call.form.relative')}</label>
                    <ValidationMessage message={self.getValidationMessage('studentCalls', 'relative')}/>
                    <Select
                        name="relative"
                        placeholder={Utils.message('common.student.call.form.relative')}
                        value={p.studentCall.relative}
                        valueRenderer={Renderers.student.relative.roleAndName}
                        optionRenderer={Renderers.student.relative.roleAndName}
                        options={relatives}
                        onChange={opt => self.onFieldChange('relative', opt && Utils.pick(opt, ['id', 'name', 'role']))}
                    />
                </ColFormGroup>
                <ColFormGroup classes="col-xs-6">
                    <label>{Utils.message('common.student.call.form.method')}</label>
                    <ValidationMessage message={self.getValidationMessage('studentCalls', 'method')}/>
                    <Select
                        name="method"
                        placeholder={Utils.message('common.student.call.form.method')}
                        value={Dictionaries.studentCallMethod.byId(p.studentCall.method)}
                        valueRenderer={Renderers.dictOption}
                        optionRenderer={Renderers.dictOption}
                        options={Dictionaries.studentCallMethod}
                        onChange={opt => self.onFieldChange('method', opt && opt.id)}
                    />
                </ColFormGroup>
                <ColFormGroup classes="col-xs-6">
                    <label>{Utils.message('common.student.call.form.result')}</label>
                    <ValidationMessage message={self.getValidationMessage('studentCalls', 'result')}/>
                    <Select
                        name="result"
                        placeholder={Utils.message('common.student.call.form.result')}
                        value={Dictionaries.studentCallResult.byId(p.studentCall.result)}
                        valueRenderer={Renderers.dictOption}
                        optionRenderer={Renderers.dictOption}
                        options={self.getStudentCallAvailableResults()}
                        onChange={opt => self.onFieldChange('result', opt && opt.id)}
                    />
                </ColFormGroup>
                <ColFormGroup classes="col-xs-6">
                    <label>{Utils.message('common.student.call.form.employee')}</label>
                    <ValidationMessage message={self.getValidationMessage('studentCalls', 'employee')}/>
                    <Select
                        name="employee"
                        placeholder={Utils.message('common.student.call.form.employee')}
                        value={p.studentCall.employee}
                        valueRenderer={Renderers.teacher}
                        optionRenderer={Renderers.teacher}
                        options={p.employees}
                        onChange={opt => self.onFieldChange('employee', opt)}
                    />
                </ColFormGroup>
                <ColFormGroup>
                    <label>{Utils.message('common.student.call.form.comment')}</label>
                    <ValidationMessage
                        message={self.getValidationMessage('studentCalls', 'comment')}/>
                    <TextInput id="comment"
                               owner={p.studentCall}
                               name="comment"
                               placeholder={Utils.message('common.student.call.form.comment')}
                               defaultValue={p.studentCall.comment}
                               onChange={val => self.onFieldChange('comment', val)}/>
                </ColFormGroup>
            </Row>
        </div>
    },

    getStudentCallAvailableResults(){
        const self = this, p = self.props,
            method = p.studentCall.method;
        return method ? Dictionaries.studentCallResult.byIds(Dictionaries.studentCallMethod.byId(method).results) : []
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

        const validationMessages = Validators.studentCalls(p.studentCall);

        if (Utils.isAllValuesEmpty(validationMessages)) {
            return p.dispatch(Actions.ajax.studentCalls.save(Utils.extend(p.studentCall, {student: {id: p.params.studentId}})))
                .then(
                    () => Navigator.back(),
                    (error) => {
                        if (error.status === DataService.status.badRequest) {
                            p.dispatch(Actions.setValidationMessages('studentCalls', validationMessages))
                        }
                    })
        } else {
            p.dispatch(Actions.setValidationMessages('studentCalls', validationMessages))
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
        if (!Utils.lifeCycle.entity(p, 'studentCalls').saved) {
            DialogService.confirmBack().then(() => {
                Navigator.back()
            }, Utils.fn.nop)
        } else {
            Navigator.back()
        }
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('studentCalls', fieldId, newValue))
    },

    getValidationMessage(entity, fieldId){
        return this.props.validationMessages[entity][fieldId];
    }
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.StudentCall,
        {
            employees: state.pages.Teachers.entities
        });
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(StudentCall);
