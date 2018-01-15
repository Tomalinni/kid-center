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
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    Icons = require('../components/Icons');

const CategoryChainSelect = React.createClass({


    getInitialState(){
        return {
            selectedCategories: {},
            categoryChains: [] //list of objs: {id: last category id, categories: categories array}
        }
    },

    render(){
        const self = this, p = self.props, s = self.state;

        return <div>
            <Row>
                {self.renderCategoryInput('category1', p.rootCategoryIds.map(id=>p.categoriesMap[id]))}
                {self.renderCategoryInput('category2', self.childCategories(s.selectedCategories['category1']))}
                {self.renderCategoryInput('category3', self.childCategories(s.selectedCategories['category2']))}
                {self.renderCategoryInput('category4', self.childCategories(s.selectedCategories['category3']))}
                {self.renderCategoryInput('category5', self.childCategories(s.selectedCategories['category4']))}
                {self.renderBtn(Icons.glyph.plus(), ()=>self.onAdd(s.selectedCategories))}
            </Row>
            {s.categoryChains.map((obj)=> {
                const categoriesElem = obj.categories;
                return <Row key={obj.id}>
                    {self.renderLabel(categoriesElem['category1'])}
                    {self.renderLabel(categoriesElem['category2'])}
                    {self.renderLabel(categoriesElem['category3'])}
                    {self.renderLabel(categoriesElem['category4'])}
                    {self.renderLabel(categoriesElem['category5'])}
                    {self.renderBtn(Icons.glyph.remove(), ()=>self.onRemove(obj.id))}
                </Row>
            })}
            {self.renderRemoveAllBtn()}
        </div>
    },

    renderRemoveAllBtn(){
        const self = this, p = self.props, s = self.state;
        const btnTitle = <span>{Icons.glyph.remove()}&nbsp;
            {Utils.message('button.clear.all')}</span>;
        if (s.categoryChains.length > 0) {
            return self.renderBtn(btnTitle, ()=>self.onRemoveAll(), 'col-xs-12')
        }
    },

    onAdd(selectedCategories){
        const self = this, p = self.props, s = self.state,
            lastCategoryId = self.getLastChainCategoryId(selectedCategories),
            categoryElement = {id: lastCategoryId, categories: selectedCategories};
        if (lastCategoryId) {
            self.setState({categoryChains: Utils.arr.put(s.categoryChains, categoryElement, Utils.obj.id)});
            p.onAdd(lastCategoryId);
        }
    },

    onRemove(lastCategoryId){
        const self = this, p = self.props, s = self.state;
        self.setState({categoryChains: Utils.arr.remove(s.categoryChains, {id: lastCategoryId}, Utils.obj.id)});
        p.onRemove(lastCategoryId);
    },

    onRemoveAll(){
        const self = this, p = self.props, s = self.state;
        self.setState({categoryChains: []});
        p.onRemoveAll();
    },

    renderCategoryInput(fieldId, childCategories){
        const self = this, p = self.props, s = self.state;

        return <ColFormGroup classes="col-xs-2">
            <Select
                name={fieldId}
                ref={fieldId}
                clearable={false}
                openOnFocus={true}
                searchable={false}
                tabSelectsValue={false}
                placeholder={Utils.message('common.payments.search.table.' + fieldId)}
                value={s.selectedCategories[fieldId]}
                valueRenderer={Renderers.category}
                optionRenderer={Renderers.category}
                options={childCategories}
                onChange={(opt)=> {
                    self.onFieldChange(fieldId, opt)
                }}
            />
        </ColFormGroup>
    },

    renderBtn(title, onClickFn, btnClass = 'col-xs-2'){
        return <ColFormGroup classes={btnClass}>
            <Button onClick={onClickFn}>
                {title}
            </Button>
        </ColFormGroup>
    },

    renderLabel(category){
        return <ColFormGroup classes="col-xs-2">
            {Renderers.category(category)}
        </ColFormGroup>
    },

    childCategories(category){
        const self = this, p = self.props;
        return (category && category.children || []).map((cat)=>p.categoriesMap[cat.id])
    },

    getLastChainCategoryId(categoriesChain){
        const self = this, p = self.props;
        for (let i = Config.categoryFields.length - 1; i >= 0; i--) {
            const fieldId = Config.categoryFields[i],
                category = categoriesChain[fieldId];
            if (category) {
                return categoriesChain[fieldId].id
            }
        }
        return null;
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props,
            categoryFieldIndex = Config.categoryFields.indexOf(fieldId),
            changedVals = {[fieldId]: newValue};
        if (categoryFieldIndex >= 0) {
            for (let i = categoryFieldIndex + 1; i < Config.categoryFields.length; i++) {
                changedVals[Config.categoryFields[i]] = null
            }
            if (Config.categoryFields.length - 1 !== categoryFieldIndex) {
                const nextFieldId = Config.categoryFields[categoryFieldIndex + 1];
                self.refs[nextFieldId].focus()
            }
        }
        self.setState(Utils.merge(self.state, {selectedCategories: changedVals}));
    }

});

module.exports = CategoryChainSelect;
