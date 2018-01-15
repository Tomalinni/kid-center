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
    PhotosList = require('../components/PhotosList'),
    StudentCardList = require('../components/StudentCardList'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    {Row, Col, FormGroup, ColFormGroup, ProgressButton, MenuItem, Select} = require('../components/CompactGrid'),
    Icons = require('../components/Icons');

const {PropTypes} = React;


const StudentCardPaymentPreferences = React.createClass({
    preferenceId: 'studentCardPayment',
    propTypes: {
        params: PropTypes.object,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;
        p.dispatch(Actions.ajax.preferences.get(self.preferenceId));
    },

    render(){
        const self = this, p = self.props;

        return <div>
            <PageToolbar leftButtons={[self.renderSaveBtn()]}/>
            <h4 className="page-title">{Utils.message('common.pages.preferences')}</h4>
            <div className="container with-nav-toolbar">
                { self.renderForm() }
            </div>
        </div>
    },

    renderForm () {
        const self = this, p = self.props;
        return <div>
            <h3>{Utils.message('common.preferences.studentCardPayment')}</h3>
            <Row>
                <ColFormGroup>
                    <label className="w100pc">{Utils.message('common.preferences.studentCardPayment.category')}</label>
                    <Select
                        name="categoryId"
                        placeholder={Utils.message('common.preferences.studentCardPayment.category')}
                        value={p.categoriesMap[p.preference.categoryId]}
                        valueRenderer={Renderers.category}
                        optionRenderer={Renderers.category}
                        options={p.rootCategoryIds.map(id => p.categoriesMap[id])}
                        onChange={opt => self.onFieldChange('categoryId', opt && opt.id)}
                    />
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup>
                    <label className="w100pc">{Utils.message('common.preferences.studentCardPayment.partner')}</label>
                    <Select
                        name="partner"
                        placeholder={Utils.message('common.preferences.studentCardPayment.partner')}
                        value={Utils.getSchoolsCollection(p.schoolsMap, 'incoming', 'source').find(partner => partner.id === p.preference.partnerId)}
                        valueRenderer={Renderers.school.cityAndName}
                        optionRenderer={Renderers.school.cityAndName}
                        options={Utils.getSchoolsCollection(p.schoolsMap, 'incoming', 'source')}
                        onChange={opt => self.onFieldChange('partnerId', opt && opt.id) }
                    />
                </ColFormGroup>
            </Row>
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

        return p.dispatch(Actions.ajax.preferences.set(self.preferenceId, p.preference))
            .then(
                () => {
                },
                (error) => {
                    if (error.status === DataService.status.badRequest) {

                    }
                }
            )
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('preference', fieldId, newValue))
    },

    getValidationMessage(entity, fieldId){
        const self = this, p = self.props;
        return p.validationMessages[entity][fieldId];
    }
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.Preferences,
        {
            schools: state.pages.Schools.entities,
            schoolsMap: state.pages.Schools.entitiesMap,
            rootCategoryIds: state.pages.Categories.rootIds,
            categoriesMap: state.pages.Categories.entitiesMap,
        });
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(StudentCardPaymentPreferences);
