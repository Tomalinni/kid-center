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
    Dictionaries = require('../Dictionaries'),
    Config = require('../Config'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    Icons = require('../components/Icons');

const AccountsSelect = React.createClass({

    getInitialState(){
        const self = this, p = self.props;

        return {
            addingItem: {},
        }
    },

    componentWillReceiveProps(nextProps){
        const self = this, p = self.props;

        if (nextProps.accountIds != p.accountIds) {
            self.setState({addingItem: {type: self.state.addingItem.type}})
        }
    },

    getAddedItems(accountIds){
        const self = this, p = self.props;
        let addedItems = null;

        if (accountIds) {
            addedItems = accountIds.map(id => {
                const account = Utils.arr.find(p.accounts, {id}, Utils.obj.id).obj,
                    accType = account && account.type,
                    type = accType && Dictionaries.accountType.byId(accType);
                return account && {account, type}
            }).filter(obj => Utils.isDefined(obj))
        }
        return addedItems
    },

    render(){
        const self = this, p = self.props, s = self.state,
            addedItems = self.getAddedItems(p.accountIds),
            allAdded = addedItems === null;

        return <div>
            <Row>
                <ColFormGroup classes="col-xs-12">
                    <Button onClick={() => self.setAllAccounts()}>
                        {self.renderAllAccountsCheckMark(allAdded)}
                        {Utils.message('common.payments.search.fields.options.all')}
                    </Button>
                </ColFormGroup>
            </Row>
            <Row>
                {self.renderTypeInput()}
                {self.renderAccountInput()}
            </Row>
            {(allAdded ? [] : addedItems).map((obj) => {
                return <Row key={obj.account.id}>
                    {self.renderType(obj.type)}
                    {self.renderAccount(obj.account)}
                    {self.renderBtn(Icons.glyph.remove(), () => self.onRemove(obj.account.id))}
                </Row>
            })}
        </div>
    },

    renderAllAccountsCheckMark(allAdded){
        const self = this, p = self.props, s = self.state;
        if (allAdded) {
            return Icons.glyph.ok('icon-btn-success icon-btn-rpad')
        }
    },

    setAllAccounts(){
        const self = this, p = self.props, s = self.state;
        p.onSet(null);
    },

    onRemove(accountId){
        const self = this, p = self.props, s = self.state;
        let nextAccountIds = Utils.arr.remove(p.accountIds, accountId);
        nextAccountIds = nextAccountIds.length === 0 ? null : nextAccountIds;
        p.onSet(nextAccountIds);
    },

    renderTypeInput(){
        const self = this, p = self.props, s = self.state;

        return <ColFormGroup classes="col-xs-3">
            <Select
                name="type"
                clearable={false}
                openOnFocus={true}
                searchable={false}
                tabSelectsValue={false}
                placeholder={Utils.message('common.accounts.search.table.type')}
                value={s.addingItem.type}
                valueRenderer={Renderers.dictOption}
                optionRenderer={Renderers.dictOption}
                options={Dictionaries.accountType}
                onChange={(opt) => self.onFieldChange('type', opt)}
            />
        </ColFormGroup>
    },

    renderAccountInput(){
        const self = this, p = self.props, s = self.state;

        return <ColFormGroup classes="col-xs-9">
            <Select
                name="account"
                clearable={false}
                openOnFocus={true}
                tabSelectsValue={false}
                placeholder={Utils.message('common.accounts.search.table.name')}
                value={s.addingItem.account}
                valueRenderer={Renderers.account.name}
                optionRenderer={Renderers.account.name}
                options={self.getAccountOptions()}
                onChange={(opt) => self.onFieldChange('account', opt)}
            />
        </ColFormGroup>
    },

    getAccountOptions(){
        const self = this, p = self.props, s = self.state,
            accounts = (s.addingItem.type && p.accountsMap[s.addingItem.type.id] || []);
        if (p.schoolId) {
            return accounts.filter((account) => {
                return account.schools && account.schools.some(s => s.id === p.schoolId)
            })
        } else {
            return accounts
        }
    },

    renderBtn(title, onClickFn, btnClass = 'col-xs-2'){
        return <ColFormGroup classes={btnClass}>
            <Button onClick={onClickFn}>
                {title}
            </Button>
        </ColFormGroup>
    },

    renderType(type){
        return <ColFormGroup classes="col-xs-3">
            {Renderers.dictOption(type)}
        </ColFormGroup>
    },

    renderAccount(account){
        return <ColFormGroup classes="col-xs-7">
            {Renderers.account.name(account)}
        </ColFormGroup>
    },

    getAccountId(obj){
        return obj && obj.account && obj.account.id;
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props, s = self.state,
            addingItem = Utils.extend(s.addingItem, {[fieldId]: newValue});
        if (fieldId === 'type') {
            addingItem.account = null;
        }
        self.setState({addingItem});
        self.updateAddedItems(addingItem);
    },

    updateAddedItems(addingItem){
        const self = this, p = self.props;

        if (self.isValidItem(addingItem)) {
            const accId = addingItem.account.id,
                nextAccIds = (!p.accountIds || Utils.isEmptyArray(p.accountIds)) ? [accId] : Utils.arr.push(p.accountIds, accId);
            p.onSet(nextAccIds);
        }
    },

    isValidItem(item){
        const self = this, p = self.props;
        return item.type && item.account && (!p.accountIds || !p.accountIds.some(id => id === item.account.id))
    }

});

module.exports = AccountsSelect;
