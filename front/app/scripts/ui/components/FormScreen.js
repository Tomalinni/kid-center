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
    Navigator = require('../Navigator'),
    DialogService = require('../services/DialogService'),
    Dictionaries = require('../Dictionaries'),
    Renderers = require('../Renderers'),
    DataService = require('../services/DataService'),
    Actions = require('../actions/Actions'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    PhotosList = require('../components/PhotosList'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    {ProgressButton} = require('../components/CompactGrid'),
    Validators = require('../Validators'),
    EntityLifeCyclePanel = require('../components/EntityLifeCyclePanel'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const FormScreen = React.createClass({
    propTypes: {
        params: PropTypes.object,
        entity: PropTypes.object,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;

        if (!p.isEntityPreloaded) {
            const id = p.params.id,
                isNewId = Utils.isNewId(id);
            if (!isNewId && Utils.isValidNumberId(id)) {
                p.dispatch(p.ajaxResource.findOne(id));
            } else if (isNewId) {
                p.dispatch(Actions.initEntity(p.entitiesId));
            }
        }
        p.dispatch(Actions.clearValidationMessages(p.entitiesId));
    },


    render(){
        const self = this, p = self.props;

        return <div>
            <PageToolbar leftButtons={[self.renderBackBtn(), self.renderSaveBtn()]}/>
            <h4 className="page-title">{p.pageTitle}</h4>
            <div className="container with-nav-toolbar">
                <EntityLifeCyclePanel {...p}
                                      entity={p.entitiesId}
                                      renderChildrenFn={self.renderFormElement}/>
            </div>
        </div>
    },

    renderFormElement () {
        const self = this, p = self.props;
        return <div>
            <div className="row row-compact">
                <div className="col-compact col-xs-12">
                    <ValidationMessage message={self.getValidationMessage(p.entitiesId, '_')}/>
                </div>
            </div>
            {p.formElementFn(self.onFieldChange)}
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

        let validationMessages = Validators.getByEntityId(p.entitiesId, p.entity);
        if (!validationMessages || Utils.isEmptyArray(Utils.objectValues(validationMessages).filter(Boolean))) {
            return p.dispatch(p.ajaxResource.save(p.entity))
                .then(
                    () => {
                        console.log('After save response');
                        Navigator.navigate(p.entitiesRoute)
                    },
                    (error) => {
                        if (error.status === DataService.status.badRequest) {
                            p.dispatch(Actions.setValidationMessages(p.entitiesId, Utils.messages(error)[p.entitiesId]))
                        }
                    }
                )
        } else {
            p.dispatch(Actions.setValidationMessages(p.entitiesId, validationMessages))
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
        if (!Utils.lifeCycle.entity(p, p.entitiesId).saved) {
            DialogService.confirmBack().then(() => {
                Navigator.navigate(p.entitiesRoute)
            }, () => {
            })
        } else {
            Navigator.navigate(p.entitiesRoute)
        }
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue(p.entitiesId, fieldId, newValue))
    },

    getValidationMessage(entity, fieldId){
        const self = this, p = self.props,
            entityMessages = p.validationMessages[entity] || {};
        return entityMessages[fieldId];
    }
});

module.exports = FormScreen;
