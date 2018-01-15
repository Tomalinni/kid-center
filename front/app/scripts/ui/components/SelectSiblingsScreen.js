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
    AuthService = require('../services/AuthService'),
    Permissions = require('../Permissions'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    LessonUtils = require('../LessonUtils'),
    Actions = require('../actions/Actions'),
    DialogService = require('../services/DialogService'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    PageToolbar = require('../components/PageToolbar'),
    StudentCardPaymentScreen = require('../components/StudentCardPaymentScreen'),
    PeriodsSearchGroup = require('../components/PeriodsSearchGroup'),
    DataService = require('../services/DataService'),
    DropdownActionButton = require('../components/DropdownActionButton'),
    {Row, Col, FormGroup, ColFormGroup, ProgressButton, TableButtonGroup, Button, CircleButton, Select} = require('../components/CompactGrid'),
    Icons = require('../components/Icons');

const SelectSiblingsScreen = React.createClass({
    render(){
        const self = this, p = self.props;
        return <ListScreen entity='siblings'
                           ajaxResource={Actions.ajax.siblings}
                           instantSearch={true}
                           renderMenuUp={false}
                           renderModifyBtns={false}
                           tableBodyClasses="entities-tbl-body-high"
                           pageTitle={Utils.message('common.screens.select.siblings')}
                           filterElementFn={self.renderFilterElement}
                           filterRowsFn={student => p.ownerStudent.id !== student.id}
                           entitiesTblCols={self.columns()}
                           renderToolbarBtns={() => [self.renderBackBtn()]}
                           onItemSelectedFn={p.onSiblingSelected}
                           selectedObj={p.selectedObj}
                           {...p} />
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;
        self.onSearchRequestChange = onSearchRequestChange;

        return <Row>
            <ColFormGroup>
                <TextInput id="searchText"
                           name="searchText"
                           placeholder="Search text"
                           defaultValue={p.searchRequest.text}
                           onChange={text => self.onSearchRequestChange({text})}/>
            </ColFormGroup>
        </Row>
    },

    renderBackBtn(){
        const self = this, p = self.props;
        return <button type="button"
                       key="back"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={p.onBack}>
            {Icons.glyph.arrowLeft()}
        </button>
    },

    confirmedMobileCellClassName(obj){
        return (obj && obj.mobileConfirmed) ? 'cell-right-border-success' : 'cell-right-border-danger'
    },

    genderClassName(obj){
        return Utils.select(obj.gender === 'boy', 'cell-border-gender-boy', 'cell-border-gender-girl');
    },

    columns(){
        const self = this, p = self.props,
            businessIdCol = Utils.cols.col(Utils.message('common.students.search.table.businessId'), Utils.obj.key('businessId'), '45px', self.confirmedMobileCellClassName),
            nameCol = Utils.cols.col(Utils.message('common.students.search.table.nameCn'), Utils.obj.key('nameCn'), '55px'),
            ageCol = Utils.cols.col(Utils.message('common.students.search.table.age'), obj => Utils.studentAgeYearMonths(obj.birthDate, true), '40px', self.genderClassName),
            managerCol = Utils.cols.col(Utils.message('common.students.search.table.manager'), obj => Renderers.teacher(obj.manager), '35px');

        return [businessIdCol, nameCol, ageCol, managerCol]
    },

});

function mapStateToProps(state) {
    return state.pages.Siblings;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(SelectSiblingsScreen);
