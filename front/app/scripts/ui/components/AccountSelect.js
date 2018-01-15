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

const AccountSelect = React.createClass({

    getInitialState(){
        const self = this, p = self.props,
            accountTypeId = p.account && p.account.type,
            accountType = accountTypeId ? Dictionaries.accountType.byId(accountTypeId) : null;

        return {
            type: accountType //selected type option
        }
    },

    componentWillReceiveProps(nextProps){
        const self = this, p = self.props;

        if (nextProps.account) {
            self.setState({type: Dictionaries.accountType.byId(nextProps.account.type)})
        }
    },

    render(){
        const self = this, p = self.props;

        return <Row>
            {self.renderTypeInput()}
            {self.renderAccountInput()}
        </Row>

    },

    renderTypeInput(){
        const self = this, p = self.props, s = self.state;

        return <ColFormGroup classes="col-xs-4">
            <Select
                name="type"
                clearable={false}
                openOnFocus={true}
                searchable={false}
                tabSelectsValue={false}
                placeholder={Utils.message('common.accounts.search.table.type')}
                value={s.type}
                valueRenderer={Renderers.dictOption}
                optionRenderer={Renderers.dictOption}
                options={Dictionaries.accountType}
                onChange={(opt) => self.onTypeChange(opt)}
            />
        </ColFormGroup>
    },

    renderAccountInput(){
        const self = this, p = self.props, s = self.state;

        return <ColFormGroup classes="col-xs-8">
            <Select
                name="account"
                clearable={false}
                openOnFocus={true}
                tabSelectsValue={false}
                placeholder={Utils.message('common.accounts.search.table.name')}
                value={p.account}
                valueRenderer={Renderers.account.name}
                optionRenderer={Renderers.account.name}
                options={self.getAccountOptions()}
                onChange={(opt) => self.onAccountChange(opt)}
            />
        </ColFormGroup>
    },

    getAccountOptions(){
        const self = this, p = self.props, s = self.state,
            accounts = (p.schoolId && s.type && p.accountsMap[s.type.id] || []);
        return accounts.filter((account) => {
            return account.schools && account.schools.some(s => s.id === p.schoolId)
        })
    },

    onTypeChange(type){
        const self = this, p = self.props;
        self.setState({type: type});
        if (p.account) { //If account is already null, we should not trigger validation again
            p.onSet(null);
        }
    },

    onAccountChange(account){
        const self = this, p = self.props;
        p.onSet(account);
    }

});

module.exports = AccountSelect;
