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
    LessonUtils = require('../LessonUtils'),
    Renderers = require('../Renderers'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    DataService = require('../services/DataService'),
    Actions = require('../actions/Actions'),
    Icons = require('./Icons'),
    ValidationMessage = require('./ValidationMessage');

const LessonsCardList = React.createClass({

    render(){
        const self = this, p = self.props;

        return <div>
            {self.renderCardInfoMessage()}
            <table className="entities-tbl-body entities-tbl-body-bordered">
                <tbody>
                {self.renderHeader()}
                </tbody>
            </table>
            <div className="lessons-card-list-rows container-bordered">
                <table className="entities-tbl-body entities-tbl-body-bordered-sides">
                    <tbody>
                    {p.studentCards.map((studentCard, i) => self.renderStudentCard(studentCard))}
                    </tbody>
                </table>
            </div>
        </div>
    },

    renderCardInfoMessage(){
        const self = this, p = self.props,
            boundToCard = Dictionaries.lessonProcedure.byId(p.lessonProcedure).boundToCard;
        let message = null;

        if (!p.loading && boundToCard) {
            if (!p.studentCards || p.studentCards.length === 0) {
                message = Utils.message('common.info.add.card.to.plan.lesson');
            } else if (!p.selectedStudentCard) {
                message = Utils.message('common.info.select.card.to.plan.lesson');
            }
        }

        if (message) {
            return <ValidationMessage message={message}/>
        }
    },

    renderHeader(){
        return <tr key="header">
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.ageRange')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.visitType')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.lessonLimit')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.lessonSpent')}</td>
            <td className="entities-tbl-cell-xs-40p land-visible-tbl-cell">{Utils.message('common.lessons.card.list.lessonNotSpent')}</td>
            <td className="entities-tbl-cell-xs-40p land-visible-tbl-cell">{Utils.message('common.lessons.card.list.lessonPlanned')}</td>
            <td className="entities-tbl-cell-xs-40p land-visible-tbl-cell">{Utils.message('common.lessons.card.list.lessonAvailable')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.missed')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.cancels')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.lateCancels')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.lastMomentCancels')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.undueCancels')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.suspends')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.message('common.lessons.card.list.daysBeforeExpiration')}</td>
        </tr>
    },

    renderStudentCard(studentCard){
        const self = this, p = self.props,
            selectedStudentCardId = p.selectedStudentCard && p.selectedStudentCard.id;
        return <tr key={studentCard.id}
                   className={Utils.select(selectedStudentCardId === studentCard.id, 'entities-tbl-row-selected', 'entities-tbl-row')}
                   onClick={() => p.onChange(studentCard)}>
            <td className="entities-tbl-cell-xs-40p">{Renderers.dictOption(Dictionaries.ageRange.byId(studentCard.ageRange))}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.dictOption(Dictionaries.visitType.byId(studentCard.visitType), '')}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.valueOrZero(studentCard.lessonsLimit)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.student.card.lessonsSpent(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p land-visible-tbl-cell">{Renderers.student.card.lessonsNotSpent(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p land-visible-tbl-cell">{Renderers.student.card.lessonsPlanned(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p land-visible-tbl-cell">{Renderers.student.card.lessonsAvailable(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.student.card.miss(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.student.card.earlyCancels(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.student.card.lateCancels(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.student.card.lastMomentCancels(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.student.card.undueCancels(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.student.card.suspends(studentCard)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.student.card.daysBeforeExpiration(studentCard)}</td>
        </tr>
    },
});

module.exports = LessonsCardList;
