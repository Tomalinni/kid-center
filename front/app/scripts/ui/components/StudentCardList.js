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
    Utils = require('../Utils'),
    Renderers = require('../Renderers'),
    Dictionaries = require('../Dictionaries'),
    DataService = require('../services/DataService'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    Icons = require('./Icons');

const StudentCardList = React.createClass({
    getInitialState() {
        return {
            items: [],
            searchRequest: {
                activeState: 'all',
                visitType: 'all',
                ageRange: 'all'
            }
        };
    },
    componentDidMount(){
        const self = this, p = self.props, s = self.state;
        self.loadCards(s.searchRequest);
    },
    render(){
        const self = this, p = self.props;

        return <div>
            {self.renderFilterElement()}
            <table className="entities-tbl-body entities-tbl-body-high">
                <tbody>
                <tr>
                    <td>{Utils.message('common.student.card.table.visit.type')}</td>
                    <td>{Utils.message('common.student.card.table.duration')}</td>
                    <td>{Utils.message('common.student.card.table.lessons')}</td>
                    <td>{Utils.message('common.student.card.table.price')}</td>
                    <td>{Utils.message('common.student.card.table.lessonCost')}</td>
                </tr>
                {  self.state.items.map(card => {
                    return <tr key={card.id}
                               onClick={() => {
                                   p.onSelect(card)
                               }}>
                        <td>{Dictionaries.visitType.byId(card.visitType).title}</td>
                        <td>{Renderers.cardDuration(card)}</td>
                        <td>{card.lessonsLimit}</td>
                        <td>{card.price}</td>
                        <td>{Math.floor(card.price / card.lessonsLimit) || '-'}</td>
                    </tr>
                })}
                </tbody>
            </table>
        </div>
    },

    renderFilterElement(){
        const self = this, p = self.props, s = self.state;

        return <div className="container">
            <Row>
                {Dictionaries.cardStateFilter.map(opt => {
                    return <ColFormGroup key={'cardState-' + opt.id} classes="col-xs-4">
                        <Button type="button"
                                onClick={() => self.onSearchRequestChange({activeState: opt.id})}>
                            {s.searchRequest.activeState === opt.id ? Icons.glyph.ok('icon-btn-success icon-btn-rpad') : undefined}
                            {opt.title}
                        </Button>
                    </ColFormGroup>
                })}

                <ColFormGroup>
                    <table className="period-btn-table">
                        <tbody>
                        <tr>
                            {Dictionaries.studentDashboardLessonVisitTypeFilter.withAll
                                .filter(o => o.id !== 'transfer')
                                .map(opt => {
                                    return <td key={opt.id}>
                                        <Button type="button"
                                                onClick={() => self.onSearchRequestChange({visitType: opt.id})}>
                                            {s.searchRequest.visitType === opt.id ? Icons.glyph.ok('icon-btn-success icon-btn-rpad') : undefined}
                                            {opt.title}
                                        </Button>
                                    </td>
                                })}
                        </tr>
                        </tbody>
                    </table>
                </ColFormGroup>

                <ColFormGroup>
                    <table className="period-btn-table">
                        <tbody>
                        <tr>
                            {Dictionaries.ageRange.withAll
                                .map(opt => {
                                    return <td key={opt.id}>
                                        <Button type="button"
                                                onClick={() => self.onSearchRequestChange({ageRange: opt.id})}>
                                            {s.searchRequest.ageRange === opt.id ? Icons.glyph.ok('icon-btn-success icon-btn-rpad') : undefined}
                                            {opt.title}
                                        </Button>
                                    </td>
                                })}
                        </tr>
                        </tbody>
                    </table>
                </ColFormGroup>
            </Row>
        </div>
    },

    onSearchRequestChange(searchRequestChange){
        const self = this, p = self.props, s = self.state;
        let nextSearchRequest = Utils.extend(s.searchRequest, searchRequestChange);
        self.setState({searchRequest: nextSearchRequest});
        self.loadCards(nextSearchRequest);
    },

    loadCards(searchRequest){
        const self = this, p = self.props, s = self.state,
            screenModeVisitType = Utils.query.option('transfer', !p.forTransfer),
            filterVisitType = searchRequest.visitType,
            effectiveFilterVisitType = filterVisitType === 'all' ? null : filterVisitType,
            effectiveAgeRange = searchRequest.ageRange === 'all' ? null : searchRequest.ageRange;
        DataService.operations.cards.findAll({
            text: '',
            activeState: searchRequest.activeState,
            visitType: Utils.select(p.forTransfer || !effectiveFilterVisitType, screenModeVisitType, effectiveFilterVisitType),
            ageRange: effectiveAgeRange
        }).then(
            data => {
                self.setState({items: data.results});
            },
            error => {
            });
    }
});

module.exports = StudentCardList;
