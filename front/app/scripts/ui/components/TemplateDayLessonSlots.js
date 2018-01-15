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
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    {ColFormGroup} = require('../components/CompactGrid');

const TemplateDayLessonSlots = React.createClass({

    render(){
        const self = this, p = self.props;
        const subjects = Dictionaries.lessonSubject;
        const colWidth = (96 / subjects.length) + '%';

        return <ColFormGroup classes="col-md-6">
            <table className="day-lesson-slots-columns">
                <tbody>
                <tr>
                    <td className="day-lesson-slots-day-title"
                        colSpan={subjects.length + 1}>
                        {p.day.title}
                    </td>
                </tr>
                <tr>
                    <td key="day-time-scale"></td>
                    {subjects.map((subject) => {
                        return <td key={subject.id}
                                   className={'day-lesson-slots-column-header mixin-background-subject-' + subject.id}>{subject.shortTitle}</td>;
                    })}
                </tr>
                {Dictionaries.lessonTime.map(time => {

                    return <tr key={time.id}>
                        <td key={'day-time-scale' + time.id}
                            className="day-lesson-slots-time-cell">{time.title}</td>
                        {Dictionaries.lessonSubject.map((subject) => {
                            const fromMins = Utils.hoursMinutesToMinutes(time.id),
                                lessonId = Utils.lessonTemplateId(p.lessonTemplate.id, p.day.id, subject.id, Utils.hoursMinutesToTime(time.id)),
                                cellClassName = 'day-lesson-slots-lesson-cell' +
                                    Utils.select(p.selectedLesson && lessonId === p.selectedLesson.id, ' day-lesson-slots-lesson-cell-selected', ''),
                                lesson = (p.lessons[subject.id] || []).find(lesson => lesson.fromMins === fromMins);

                            return <td key={lessonId}
                                       onClick={() => p.onLessonClick({
                                           id: lessonId,
                                           day: p.day.id,
                                           fromMins: fromMins,
                                           subject: subject.id,
                                           ageGroup: lesson && lesson.ageGroup
                                       })}
                                       style={{width: colWidth}}
                                       className={cellClassName}>
                                <div
                                    className={'day-lesson-slots-lesson-cell-header mixin-background-subject-' + subject.id}>
                                    {Utils.selectFn(lesson, () => Dictionaries.studentAge.byId(lesson.ageGroup).title)}
                                </div>
                                {self.renderLessonCellInfo()}
                            </td>
                        })}
                    </tr>

                })}
                </tbody>
            </table>
        </ColFormGroup>
    },

    renderLessonCellInfo() {
        return '';
    }
});

module.exports = TemplateDayLessonSlots;
