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
    Permissions = require('../Permissions'),
    DialogService = require('../services/DialogService'),
    DataService = require('../services/DataService'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Config = require('../Config'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    PeriodsSearchGroup = require('../components/PeriodsSearchGroup'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    Icons = require('../components/Icons');

const StudentCalls = React.createClass({
    render(){
        const self = this, p = self.props;

        return <ListScreen entity='studentCalls'
                           instantSearch={true}
                           ajaxResource={Actions.ajax.studentCalls}
                           entityModifyPermssion={Permissions.studentsModify}
                           entityRouteFn={callId => {
                               const student = callId === 'new' ? p.searchRequest.student : p.selectedObj.student;
                               return student ? Navigator.routes.studentCall(student.id, callId) : null
                           }}
                           pageTitle={Utils.message('common.pages.studentCalls')}
                           filterElementFn={self.renderFilterElement}
                           entitiesTblCols={self.columns()}
                           entitiesTblSecondRow={self.secondRow()}
                           {...p}
        />
    },

    loadStudents(text, callback) {
        DataService.operations.students.findAll({text: text}).then(response => {
            callback(null, {
                options: response.results
            });
        }, error => {
            callback(error, null);
        });
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;
        self.onSearchRequestChange = onSearchRequestChange;

        return <Row>
            {self.renderPeriodsGroup()}
            <ColFormGroup classes="col-xs-6">
                <Select
                    async={true}
                    name="student"
                    placeholder={Utils.message('common.student.call.form.student')}
                    value={p.searchRequest.student}
                    valueRenderer={s => Renderers.student.info(s)}
                    optionRenderer={s => Renderers.student.info(s)}
                    loadOptions={Utils.debounceInput(self.loadStudents)}
                    onChange={val => self.onSearchRequestChange({student: val})}
                />
            </ColFormGroup>
            <ColFormGroup classes="col-xs-6">
                <Select
                    name="studentStatus"
                    placeholder={Utils.message('common.student.call.form.studentStatus')}
                    value={Dictionaries.studentStatus.byId(p.searchRequest.studentStatus)}
                    valueRenderer={Renderers.dictOption}
                    optionRenderer={Renderers.dictOption}
                    options={Dictionaries.studentStatus}
                    onChange={opt => self.onSearchRequestChange({studentStatus: opt && opt.id})}
                />
            </ColFormGroup>
            <ColFormGroup classes="col-xs-6">
                <Select
                    name="method"
                    placeholder={Utils.message('common.student.call.form.method')}
                    value={Dictionaries.studentCallMethod.byId(p.searchRequest.method)}
                    valueRenderer={Renderers.dictOption}
                    optionRenderer={Renderers.dictOption}
                    options={Dictionaries.studentCallMethod}
                    onChange={opt => self.onSearchRequestChange({method: opt && opt.id})}
                />
            </ColFormGroup>
            <ColFormGroup classes="col-xs-6">
                <Select
                    name="result"
                    placeholder={Utils.message('common.student.call.form.result')}
                    value={Dictionaries.studentCallResult.byId(p.searchRequest.result)}
                    valueRenderer={Renderers.dictOption}
                    optionRenderer={Renderers.dictOption}
                    options={Dictionaries.studentCallResult}
                    onChange={opt => self.onSearchRequestChange({result: opt && opt.id})}
                />
            </ColFormGroup>
        </Row>
    },

    renderPeriodsGroup(){
        const self = this, p = self.props;
        return <PeriodsSearchGroup searchRequest={p.searchRequest}
                                   periodFieldId="period"
                                   startDateFieldId="periodStart"
                                   endDateFieldId="periodEnd"
                                   onPeriodChange={self.onSearchRequestChange}/>
    },

    columns(){
        const self = this, p = self.props;
        return [
            Utils.cols.col(Utils.message('common.student.call.form.studentStatus'), call => Dictionaries.studentStatus.byId(call.studentStatus).title, '40px', 'student-calls-tbl-border-top'),
            Utils.cols.col(Utils.message('common.student.call.form.relative'), self.renderRelativeCell, '40px', 'student-calls-tbl-border-top'),
            Utils.cols.col(Utils.message('common.student.call.form.date'), call => Utils.mt.convert.format(call.date, Config.dateTimeFormat, Config.dayMonthDateTimeFormat), '65px', 'student-calls-tbl-border-top'),
            Utils.cols.col(Utils.message('common.student.call.form.method'), call => call.method && Dictionaries.studentCallMethod.byId(call.method).title, '35px', 'student-calls-tbl-border-top'),
            Utils.cols.col(Utils.message('common.student.call.form.result'), call => call.result && Dictionaries.studentCallResult.byId(call.result).title, '35px', 'student-calls-tbl-border-top'),
            Utils.cols.col(Utils.message('common.student.call.form.employee'), call => Renderers.teacher(call.employee), '35px', 'student-calls-tbl-border-top'),
        ]
    },

    renderRelativeCell(call){
        return <a
            onClick={Utils.invokeAndPreventDefaultFactory(() => Navigator.navigate(Navigator.routes.students, {selection: call.student.id}))}>
            {Renderers.objName(call.relative, Utils.message('common.label.none'))}
        </a>
    },

    secondRow(){
        return {
            childrenFn: Utils.obj.key('comment')
        }
    }
});

function mapStateToProps(state) {
    return state.pages.StudentCalls;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(StudentCalls);
