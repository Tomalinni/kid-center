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
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    Icons = require('./Icons'),
    DatePicker = require('../components/DateComponents').DatePicker,
    {Row, Col, FormGroup, ColFormGroup, TableButtonGroup, Button} = require('../components/CompactGrid');

const PeriodsSearchGroup = React.createClass({

    propTypes: {
        periodOpts: React.PropTypes.array,
        searchRequest: React.PropTypes.object.isRequired,
        customPeriodId: React.PropTypes.string,
        periodFieldId: React.PropTypes.string.isRequired,
        startDateFieldId: React.PropTypes.string.isRequired,
        endDateFieldId: React.PropTypes.string.isRequired,
        onPeriodChange: React.PropTypes.func.isRequired
    },

    getDefaultProps: function () {
        return {
            periodOpts: Dictionaries.paymentsSearchPeriod,
            customPeriodId: 'custom'
        };
    },

    render() {
        const self = this, p = this.props;

        return (
            <Row>
                {self.renderPeriodBtns()}
                {self.renderCustomPeriodInputs()}
            </Row>
        )
    },

    renderPeriodBtns(){
        const self = this, p = self.props;
        return <TableButtonGroup options={p.periodOpts} renderBtnFn={opt =>
            <Button
                classes={Utils.select(p.searchRequest[p.periodFieldId] === opt.id, 'btn-success', 'btn-default')}
                onClick={() => p.onPeriodChange({[p.periodFieldId]: opt.id})}>
                {opt.title}
            </Button>
        }/>
    },

    renderCustomPeriodInputs(){
        const self = this, p = this.props;
        if (p.searchRequest[p.periodFieldId] === p.customPeriodId) {
            return [
                <ColFormGroup key="startDate"
                              classes="col-xs-5">
                    <DatePicker disabledDate={self.isStartDateDisabled}
                                value={p.searchRequest[p.startDateFieldId]}
                                onChange={val => p.onPeriodChange({[p.startDateFieldId]: val})}>
                        <Button>
                            {p.searchRequest[p.startDateFieldId] || Utils.message('common.payments.search.fields.period.startDate')}
                        </Button>
                    </DatePicker>
                </ColFormGroup>,

                <Col key="separator"
                     classes="col-xs-2">
                    <FormGroup classes="periods-date-separator">
                        &mdash;
                    </FormGroup>
                </Col>,

                <ColFormGroup key="endDate"
                              classes="col-xs-5">
                    <DatePicker disabledDate={self.isEndDateDisabled}
                                value={p.searchRequest[p.endDateFieldId]}
                                onChange={val => p.onPeriodChange({[p.endDateFieldId]: val})}>
                        <Button>
                            {p.searchRequest[p.endDateFieldId] || Utils.message('common.payments.search.fields.period.endDate')}
                        </Button>
                    </DatePicker>
                </ColFormGroup>
            ]
        }
    },

    isStartDateDisabled(calendarMt){
        const self = this, p = self.props;
        const periodEnd = Utils.momentFromString(p.searchRequest[p.endDateFieldId]);
        return periodEnd && calendarMt.isAfter(periodEnd)
    },

    isEndDateDisabled(calendarMt){
        const self = this, p = self.props;
        const periodStart = Utils.momentFromString(p.searchRequest[p.startDateFieldId]);
        return periodStart && calendarMt.isBefore(periodStart)
    },
});

module.exports = PeriodsSearchGroup;
