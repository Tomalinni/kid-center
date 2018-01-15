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
    ValidationMessage = require('../components/ValidationMessage'),
    FormScreen = require('../components/FormScreen'),
    TextInput = require('../components/TextInput');

const {PropTypes} = React;

const City = React.createClass({
    propTypes: {
        params: PropTypes.object,
        city: PropTypes.object,
        dispatch: PropTypes.func
    },

    render(){
        const self = this, p = self.props;
        return <FormScreen entitiesId='cities'
                           entity={p.city}
                           pageTitle={Renderers.city(p.city)}
                           ajaxResource={Actions.ajax.cities}
                           entitiesRoute={Navigator.routes.cities}
                           formElementFn={self.renderFormElement}
                           {...p}
        />
    },

    renderFormElement (onFieldChange) {
        const self = this, p = self.props;
        return <div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.cities.search.table.name')}</label>
                        <ValidationMessage message={self.getValidationMessage('cities', 'name')}/>
                        <TextInput id="name"
                                   owner={p.city}
                                   name="name"
                                   placeholder={Utils.message('common.cities.search.table.name')}
                                   defaultValue={p.city.name}
                                   onChange={(val)=>onFieldChange('name', val)}/>
                    </div>
                </div>
            </div>
        </div>
    },

    getValidationMessage(entity, fieldId){
        const entityMessages = this.props.validationMessages[entity] || {};
        return entityMessages[fieldId];
    },
});

function mapStateToProps(state) {
    return state.pages.City
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(City);
