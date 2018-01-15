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
    TextInput = require('../components/TextInput');

const {PropTypes} = React;

const Teacher = React.createClass({
    propTypes: {
        params: PropTypes.object,
        teacher: PropTypes.object,
        dispatch: PropTypes.func
    },

    render(){
        const self = this, p = self.props;
        return <FormScreen entitiesId='teachers'
                           entityId='teacher'
                           entity={p.teacher}
                           pageTitle={Renderers.teacher(p.teacher)}
                           ajaxResource={Actions.ajax.teachers}
                           entitiesRoute={Navigator.routes.teachers}
                           formElementFn={self.renderFormElement}
                           {...p}
        />
    },

    getValidationMessage(entity, fieldId){
        const self = this, p = self.props;
        return p.validationMessages[entity][fieldId];
    },

    renderFormElement (onFieldChange) {
        const self = this, p = self.props;
        return <div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.teachers.search.table.name')}</label>
                        <ValidationMessage message={self.getValidationMessage('teachers', 'name')}/>
                        <TextInput id="name"
                                   owner={p.teacher}
                                   name="name"
                                   placeholder={Utils.message('common.teachers.search.table.name')}
                                   defaultValue={p.teacher.name}
                                   onChange={(val)=>onFieldChange('name', val)}/>
                    </div>
                </div>
            </div>
        </div>
    }
});

function mapStateToProps(state) {
    return state.pages.Teacher
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Teacher);
