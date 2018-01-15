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

//noinspection JSUnresolvedFunction
const React = require('react'),
    {connect} = require('react-redux'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Actions = require('../actions/Actions'),
    Utils = require('../Utils'),
    Dictionaries = require('../Dictionaries'),
    Config = require('../Config'),
    FormScreen = require('../components/FormScreen'),
    ValidationMessage = require('../components/ValidationMessage'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    TextInput = require('../components/TextInput');

const {PropTypes} = React;

const Account = React.createClass({
    propTypes: {
        params: PropTypes.object,
        account: PropTypes.object,
        dispatch: PropTypes.func
    },

    render(){
        const self = this, p = self.props;
        return <FormScreen entitiesId='accounts'
                           entity={p.account}
                           pageTitle={Renderers.account.numberOrLogin(p.account)}
                           ajaxResource={Actions.ajax.accounts}
                           entitiesRoute={Navigator.routes.accounts}
                           formElementFn={self.renderFormElement}
                           {...p}
        />
    },

    renderFormElement (onFieldChange) {
        const self = this, p = self.props;
        return <div>
            <Row>
                {self.renderSchoolGroup(onFieldChange)}

                <ColFormGroup classes="col-md-6">
                    <label>{Utils.message('common.accounts.search.table.type')}</label>
                    <ValidationMessage message={self.getValidationMessage('accounts', 'type')}/>
                    <Select
                        name="type"
                        placeholder={Utils.message('common.accounts.search.table.type')}
                        value={Dictionaries.accountType.byId(p.account.type)}
                        valueRenderer={Renderers.dictOption}
                        optionRenderer={Renderers.dictOption}
                        options={Dictionaries.accountType}
                        onChange={(opt) => onFieldChange('type', opt && opt.id)}
                    />
                </ColFormGroup>

                {self.renderTypeDependentGroups(onFieldChange)}
            </Row>
        </div>
    },

    renderTypeDependentGroups(onFieldChange){
        const self = this, p = self.props;
        if (p.account.type === 'cashless') {
            return self.renderCashlessGroups(onFieldChange)
        } else {
            return self.renderDefaultGroups(onFieldChange)
        }
    },

    renderSchoolGroup(onFieldChange){
        const self = this, p = self.props;
        return <ColFormGroup classes="col-md-6">
            <label>{Utils.message('common.accounts.search.table.school')}</label>
            <ValidationMessage message={self.getValidationMessage('accounts', 'schools')}/>
            <Select
                name="schools"
                placeholder={Utils.message('common.accounts.search.table.school')}
                multi={true}
                value={p.account.schools}
                valueKey="id"
                labelKey="name"
                options={p.schools}
                onChange={(opt) => onFieldChange('schools', opt)}
            />
        </ColFormGroup>
    },

    renderCashlessGroups(onFieldChange){
        const self = this, p = self.props;
        return [
            <ColFormGroup key="city" classes="col-md-6">
                <label>{Utils.message('common.accounts.search.table.city')}</label>
                <ValidationMessage message={self.getValidationMessage('accounts', 'city')}/>
                <Select
                    name="city"
                    placeholder={Utils.message('common.accounts.search.table.city')}
                    value={p.account.city}
                    valueRenderer={Renderers.city}
                    optionRenderer={Renderers.city}
                    options={p.cities}
                    onChange={(opt) => {
                        onFieldChange('city', opt)
                    }}
                />
            </ColFormGroup>,
            <ColFormGroup key="bank" classes="col-md-6">
                <label>{Utils.message('common.accounts.search.table.bank')}</label>
                <ValidationMessage message={self.getValidationMessage('accounts', 'bank')}/>
                <TextInput id="bank"
                           owner={p.account}
                           name="bank"
                           placeholder={Utils.message('common.accounts.search.table.bank')}
                           defaultValue={p.account.bank}
                           onChange={(val) => onFieldChange('bank', val)}/>
            </ColFormGroup>,
            <ColFormGroup key="department" classes="col-md-6">
                <label>{Utils.message('common.accounts.search.table.department')}</label>
                <ValidationMessage message={self.getValidationMessage('accounts', 'department')}/>
                <TextInput id="department"
                           owner={p.account}
                           name="department"
                           placeholder={Utils.message('common.accounts.search.table.department')}
                           defaultValue={p.account.department}
                           onChange={(val) => onFieldChange('department', val)}/>
            </ColFormGroup>,
            <ColFormGroup key="owner" classes="col-md-6">
                <label>{Utils.message('common.accounts.search.table.owner')}</label>
                <ValidationMessage message={self.getValidationMessage('accounts', 'owner')}/>
                <TextInput id="owner"
                           owner={p.account}
                           name="owner"
                           placeholder={Utils.message('common.accounts.search.table.owner')}
                           defaultValue={p.account.owner}
                           onChange={(val) => onFieldChange('owner', val)}/>
            </ColFormGroup>,
            <ColFormGroup key="number" classes="col-md-6">
                <label>{Utils.message('common.accounts.search.table.number')}</label>
                <ValidationMessage message={self.getValidationMessage('accounts', 'number')}/>
                <TextInput id="number"
                           owner={p.account}
                           name="number"
                           placeholder={Utils.message('common.accounts.search.table.number')}
                           defaultValue={p.account.number}
                           onChange={(val) => onFieldChange('number', val)}/>
            </ColFormGroup>
        ]
    },

    renderDefaultGroups(onFieldChange){
        const self = this, p = self.props;
        return <ColFormGroup classes="col-md-6">
            <label>{Utils.message('common.accounts.search.table.login')}</label>
            <ValidationMessage message={self.getValidationMessage('accounts', 'login')}/>
            <TextInput id="login"
                       owner={p.account}
                       name="login"
                       placeholder={Utils.message('common.accounts.search.table.login')}
                       defaultValue={p.account.login}
                       onChange={(val) => onFieldChange('login', val)}/>
        </ColFormGroup>
    },

    getValidationMessage(entity, fieldId){
        const entityMessages = this.props.validationMessages[entity] || {};
        return entityMessages[fieldId];
    },
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.Account,
        {
            cities: state.pages.Cities.entities,
            schools: state.pages.Schools.entities
        });
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

//noinspection JSUnresolvedVariable
module.exports = connect(mapStateToProps, mapDispatchToProps)(Account);
