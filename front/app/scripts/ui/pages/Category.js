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
    Icons = require('../components/Icons'),
    EntitiesTree = require('../components/EntitiesTree'),
    PageToolbar = require('../components/PageToolbar'),
    ValidationMessage = require('../components/ValidationMessage'),
    TextInput = require('../components/TextInput');

const {PropTypes} = React;

const Category = React.createClass({
    propTypes: {
        params: PropTypes.object,
        category: PropTypes.object,
        dispatch: PropTypes.func
    },

    onFieldChange: null,

    componentDidMount(){
        const self = this, p = self.props;
        self.setPageMode('edit');
        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId && Utils.isValidNumberId(id)) {
            p.dispatch(Actions.ajax.categories.findOne(id));
        } else if (isNewId) {
            p.dispatch(Actions.initEntity('categories'));
        }
    },

    render(){
        const self = this, p = self.props;
        if (p.pageMode === 'edit') {
            return <FormScreen entitiesId='categories'
                               entity={p.category}
                               isEntityPreloaded={true}
                               pageTitle={Renderers.category(p.category)}
                               ajaxResource={Actions.ajax.categories}
                               entitiesRoute={Navigator.routes.categories}
                               formElementFn={self.renderFormElement}
                               {...p}
            />
        } else if (p.pageMode === 'selectCategory') {
            return <div>
                <h4 className="page-title">{Utils.message('common.payments.form.select.category')}</h4>
                <PageToolbar leftButtons={[self.renderBackBtn(), self.renderSelectNoParentBtn()]}
                             hasOptionsBtn={false}/>
                <EntitiesTree
                    header={Utils.message('common.payments.form.category.name')}
                    nodesMap={p.entitiesMap}
                    rootIds={p.rootIds}
                    onSelectRow={(obj)=> {
                        self.onFieldChange && self.onFieldChange('parent', obj);
                        self.setPageMode('edit')
                    }}
                    isNodeVisibleFn={(obj)=>(obj.id !== p.category.id )}
                    renderRowFn={Renderers.category}
                />
            </div>
        }
        return null
    },

    renderFormElement (onFieldChange) {
        const self = this, p = self.props;
        self.onFieldChange = onFieldChange;
        return <div>
            <div className="row row-compact">
                <div className="col-compact col-md-6">
                    <div className="form-group">
                        <label className="w100pc">
                            {Utils.message('common.payments.form.parent.category')}
                        </label>
                        <button type="button"
                                className="btn btn-default btn-right-gap"
                                onClick={()=>self.setPageMode('selectCategory')}>
                            {Icons.glyph.cog()}
                        </button>
                        <div className="form-control field-with-right-gap-1">
                            {p.category.parent && Renderers.category(p.entitiesMap[p.category.parent.id]) || Utils.message('common.label.none')}
                        </div>
                    </div>
                </div>

                <div className="col-compact col-md-6">
                    <div className="form-group">
                        <label>{Utils.message('common.payments.form.category.name')}</label>
                        <ValidationMessage message={self.getValidationMessage('categories', 'name')}/>
                        <TextInput id="name"
                                   owner={p.category}
                                   name="name"
                                   placeholder={Utils.message('common.payments.form.category.name')}
                                   defaultValue={p.category.name}
                                   onChange={(val)=>onFieldChange('name', val)}/>
                    </div>
                </div>
                {self.renderHasTargetMonthInput(onFieldChange)}
            </div>
        </div>
    },

    renderHasTargetMonthInput(onFieldChange){
        const self = this, p = self.props;
        if (!p.category.parent) {
            return <div className="col-md-6">
                <div className="form-group">
                    <label>{Utils.message('common.payments.form.category.has.target.month')}</label>
                    <input type="checkbox"
                           id="hasTargetMonth"
                           name="hasTargetMonth"
                           checked={p.category.hasTargetMonth || false}
                           onChange={()=>onFieldChange('hasTargetMonth', !p.category.hasTargetMonth)}/>
                </div>
            </div>
        }
    },

    renderBackBtn(){
        const self = this, p = self.props;
        return <button type="button"
                       key="back"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={()=>self.setPageMode('edit')}>
            {Icons.glyph.arrowLeft()}
        </button>
    },

    renderSelectNoParentBtn(){
        const self = this, p = self.props;
        return <button type="button"
                       key="noParent"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={()=> {
                           self.onFieldChange && self.onFieldChange('parent', null);
                           self.setPageMode('edit')
                       }}>
            {Icons.glyph.banCircle()}
        </button>
    },

    getValidationMessage(entity, fieldId){
        const entityMessages = this.props.validationMessages[entity] || {};
        return entityMessages[fieldId];
    },

    setPageMode(mode){
        const self = this, p = self.props;
        if (mode === 'selectCategory') {
            p.dispatch(Actions.ajax.categories.findAll());
        }
        p.dispatch(Actions.setPageMode('categories', 'form', 'parent', mode))
    }
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.Category,
        {
            entitiesMap: state.pages.Categories.entitiesMap,
            rootIds: state.pages.Categories.rootIds
        });
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Category);
