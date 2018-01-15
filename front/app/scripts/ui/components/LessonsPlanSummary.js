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
    {FormGroup} = require('../components/CompactGrid'),
    RepeatedLessonsTable = require('./RepeatedLessonsTable');

const LessonsPlanSummary = React.createClass({

    render(){
        const self = this, p = self.props;
        const items = [
            self.renderPlannedRepeatedLessonsSummaryItem(),
            self.renderPlannedSingleLessonsSummaryItem(),
            self.renderPickedLessonsSummaryItem()
        ].filter(o => !!o);

        if (items.length) {
            return <FormGroup>
                <div className="lesson-plan-summary-block">{items}</div>
            </FormGroup>
        }
        return null
    },

    renderPlannedRepeatedLessonsSummaryItem(){
        const self = this, p = self.props,
            lessons = p.planLessonFilter._result.compactedStudentPlannedLessons || {},
            repeatedLessons = Utils.objectValues(lessons).filter(lesson => LessonUtils.isRepeatedLesson(lesson));

        if (repeatedLessons.length) {
            return <RepeatedLessonsTable key="repeated-lessons"
                                         repeatedLessons={repeatedLessons}
                                         lessonGroupName={Utils.message('common.lessons.plan.planned.repeated.lessons')}
                                         {...p}/>
        }
    },

    renderPlannedSingleLessonsSummaryItem(){
        const self = this, p = self.props,
            lessons = p.planLessonFilter._result.compactedStudentPlannedLessons || {},
            singleLessons = Utils.objectValues(lessons).filter(lesson => !LessonUtils.isRepeatedLesson(lesson));

        if (singleLessons.length) {
            return <RepeatedLessonsTable key="single-lessons"
                                         repeatedLessons={singleLessons}
                                         lessonGroupName={Utils.message('common.lessons.plan.planned.single.lessons')}
                                         {...p}/>
        }
    },

    renderPickedLessonsSummaryItem(){
        const self = this, p = self.props,
            pickedLessons = p.pickedLessons.map(id => {
                let lessonSlot = LessonUtils.lessonSlotObj({lessonId: id});
                lessonSlot.ageGroup = LessonUtils.lessonAgeGroup(lessonSlot, p);
                return LessonUtils.chainedLessonFromLessonSlot(lessonSlot)
            });

        if (pickedLessons.length) {
            return <RepeatedLessonsTable key="picked-lessons"
                                         repeatedLessons={pickedLessons}
                                         lessonGroupName={Utils.message('common.lessons.plan.picked.lessons')}
                                         {...p}/>
        }
    }
});

module.exports = LessonsPlanSummary;
