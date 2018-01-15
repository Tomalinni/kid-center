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
    Config = require('../Config'),
    {Button} = require('./CompactGrid'),
    OverlayModal = require('./OverlayModal');

const RepeatedLessonsTable = React.createClass({

    getInitialState(){
        return {clickedLesson: null}
    },

    render(){
        const self = this, p = self.props, s = self.state;
        return <div>
            <table className="entities-tbl-body entities-tbl-body-bordered">
                <tbody>
                {p.repeatedLessons.map((repeatedLesson, i) => self.renderClickableRepeatedLesson(repeatedLesson, p.lessonGroupName))}
                </tbody>
            </table>
            {Utils.select(s.clickedLesson, <OverlayModal isOpened={!!s.clickedLesson}
                                                         closeModal={() => self.resetClickedLesson()}
                                                         renderModalContent={self.renderModalActions}
            />)}
        </div>
    },

    renderClickableRepeatedLesson(repeatedLesson, groupName){
        const self = this, p = self.props, firstLesson = repeatedLesson.first;
        return <tr key={firstLesson.lessonId}>
            <td className="entities-tbl-cell-xs-40p">{Utils.select(groupName, <b>{groupName}</b>)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.dictOption(Dictionaries.lessonSubject.byId(firstLesson.subject))}</td>
            <td className="entities-tbl-cell-xs-40p">{Utils.momentToString(firstLesson.dateTime, Config.dayMonthDateFormat)}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.dictOption(Dictionaries.day[firstLesson.day])}</td>
            <td className="entities-tbl-cell-xs-40p">{firstLesson.time}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.dictOption(Dictionaries.studentAge.byId(firstLesson.ageGroup))}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.dictOption(Dictionaries.visitType.byId(firstLesson.visitType))}</td>
            <td className="entities-tbl-cell-xs-40p">{Renderers.dictOption(Dictionaries.studentSlotStatus.byId(firstLesson.status))}</td>
            <td className="entities-tbl-cell-xs-40p"> {self.renderNavigateButton(repeatedLesson)} </td>
            <td className="entities-tbl-cell-xs-80p land-visible-tbl-cell">{Utils.humanReadableDateTimeFromString(firstLesson.modifiedDate)}</td>
            <td className="entities-tbl-cell-xs-40p land-visible-tbl-cell">{firstLesson.modifiedBy}</td>
        </tr>
    },

    renderNavigateButton(repeatedLesson){
        const self = this, p = self.props;
        return <Button classes="btn-link btn-wide"
                       onClick={() => {
                           if (repeatedLesson.repeatsLeft > 1) {
                               self.setState({clickedLesson: repeatedLesson})
                           } else {
                               LessonUtils.actions.navigateToLesson(p, repeatedLesson.first.lessonId)
                           }
                       }}>{repeatedLesson.repeatsLeft}</Button>
    },

    renderModalActions(){
        const self = this, p = self.props, repeatedLesson = self.state.clickedLesson;
        if (repeatedLesson) {
            return <table className="entities-tbl-body">
                <tbody>
                <tr>
                    <td onClick={() => self.navigateToLesson(repeatedLesson.first.lessonId)}>
                        {Utils.message('common.lessons.action.navigate.first')}</td>
                </tr>
                <tr>
                    <td onClick={() => self.navigateToLesson(repeatedLesson.last.lessonId)}>
                        {Utils.message('common.lessons.action.navigate.last')}</td>
                </tr>
                </tbody>
            </table>
        }
    },

    navigateToLesson(lessonId){
        const self = this, p = self.props;
        self.resetClickedLesson();
        LessonUtils.actions.navigateToLesson(p, lessonId)
    },

    resetClickedLesson(){
        this.setState({clickedLesson: null})
    }
});

module.exports = RepeatedLessonsTable;
