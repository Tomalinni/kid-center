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

    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Actions = require('../actions/Actions'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    FormScreen = require('../components/FormScreen'),
    ValidationMessage = require('../components/ValidationMessage'),
    TextInput = require('../components/TextInput'),
    Switch = require('../components/Switch'),
    {Row, Col, FormGroup, ColFormGroup, Button, MenuItem, Select} = require('../components/CompactGrid');

const {PropTypes} = React;

const School = React.createClass({
    propTypes: {
        params: PropTypes.object,
        school: PropTypes.object,
        dispatch: PropTypes.func
    },

    render(){
        const self = this, p = self.props;
        return <FormScreen entitiesId='schools'
                           entity={p.school}
                           pageTitle={Renderers.school.name(p.school)}
                           ajaxResource={Actions.ajax.schools}
                           entitiesRoute={Navigator.routes.schools}
                           formElementFn={self.renderFormElement}
                           {...p}
        />
    },

    renderFormElement (onFieldChange) {
        const self = this, p = self.props,
            isNew = !p.school.id;
        return <Row>
            <ColFormGroup classes="col-md-6">
                <label>{Utils.message('common.schools.search.table.city')}</label>
                <ValidationMessage message={self.getValidationMessage('schools', 'city')}/>
                <Select
                    name="city"
                    placeholder={Utils.message('common.schools.search.table.city')}
                    value={p.school.city}
                    valueRenderer={Renderers.city}
                    optionRenderer={Renderers.city}
                    options={p.cities}
                    onChange={(opt) => {
                        onFieldChange('city', opt)
                    }}
                /> </ColFormGroup>

            <ColFormGroup classes="col-md-6">
                <label>{Utils.message('common.schools.search.table.name')}</label>
                <ValidationMessage message={self.getValidationMessage('schools', 'name')}/>
                <TextInput id="name"
                           owner={p.school}
                           name="name"
                           placeholder={Utils.message('common.schools.search.table.name')}
                           defaultValue={p.school.name}
                           onChange={(val) => onFieldChange('name', val)}/>
            </ColFormGroup>
            <ColFormGroup>
                <label>{Utils.message('common.schools.search.table.external')}</label>
                <Switch checked={p.school.external || false}
                        disabled={!isNew}
                        style={{marginLeft: '10px'}}
                        onChange={val => onFieldChange('external', val)}/>
            </ColFormGroup>
        </Row>
    },

    getValidationMessage(entity, fieldId){
        const entityMessages = this.props.validationMessages[entity] || {};
        return entityMessages[fieldId];
    },
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.School, {cities: state.pages.Cities.entities});
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(School);
