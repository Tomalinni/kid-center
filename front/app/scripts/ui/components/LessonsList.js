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
    moment = require('moment-timezone'),
    Utils = require('../Utils'),
    LessonUtils = require('../LessonUtils'),
    Renderers = require('../Renderers'),
    Actions = require('../actions/Actions'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    {Row} = require('../components/CompactGrid'),
    DropdownActionButton = require('../components/DropdownActionButton'),
    Icons = require('./Icons');

const LessonsList = React.createClass({

    lessonRefs: {},
    studentSlotStatuses: Dictionaries.studentSlotStatus.map(o => o.id),
    studentSlotStatusAccessor: slot => slot.status,

    render(){

        const self = this, p = self.props,
            startDateMt = Utils.momentFromString(p.startDate),
            currentMt = moment(),
            currentDate = Utils.momentToString(currentMt),
            currentMins = Utils.momentToMinutes(currentMt),
            filterByCurrentTime = Dictionaries.lessonProcedure.byId(p.lessonProcedure).useAvailableLessons;

        let rows = [];
        self.lessonRefs = {};

        for (let i = 0; i < 7; i++) {
            const dateMt = moment(startDateMt).add(i, 'days'),
                date = Utils.momentToString(dateMt),
                weekDayIndex = Utils.dayOfWeek(dateMt),
                day = Dictionaries.day[weekDayIndex],
                lessonsByDay = p.lessons[day.id],
                latestPickedLessonFromMins = p.latestPickedLessons[date],
                filterByCurrentTimePredicate = Utils.select(filterByCurrentTime && date === currentDate,
                    lessonTemplate => self.filterByCurrentTime(lessonTemplate, currentMins),
                    Utils.fn.truth
                );

            if (lessonsByDay) {
                const lessonDateSpan = LessonUtils.lessonDateSpan(p.pickedLessons),
                    lessonsByDayArr = (Utils.arr.concatArrayValues(lessonsByDay) || [])
                        .filter(lessonTemplate =>
                        filterByCurrentTimePredicate(lessonTemplate) &&
                        self.filterByLatestPickedLesson(lessonTemplate, date, latestPickedLessonFromMins));

                lessonsByDayArr.sort(Utils.lessonFromMinsCompareFn);
                const lessonByDayRows = lessonsByDayArr.map(lesson => self.renderLessonRows(lesson, dateMt, lessonDateSpan)).filter(Utils.isDefined);

                if (lessonByDayRows.length > 0) {
                    rows.push(self.renderDayHeader(dateMt));
                    rows.push(lessonByDayRows);
                }
            }
        }

        return <Row>
            <table className="entities-tbl-body">
                <tbody>
                {rows}
                </tbody>
            </table>
        </Row>
    },

    filterByLatestPickedLesson(lessonTemplate, date, latestPickedLessonFromMins){
        const self = this, p = self.props,
            lessonSubject = Utils.subjectFromLessonTemplateId(lessonTemplate.id),
            lessonId = Utils.lessonId(lessonSubject, date, lessonTemplate.fromMins),
            isAfterLastPickedLesson = !latestPickedLessonFromMins || lessonTemplate.fromMins > latestPickedLessonFromMins,
            isAlreadyPicked = Utils.arr.contains(p.pickedLessons, lessonId);

        return isAfterLastPickedLesson || isAlreadyPicked
    },

    filterByCurrentTime(lessonTemplate, currentMins){
        const subjectOpt = Dictionaries.lessonSubject.byId(Utils.subjectFromLessonTemplateId(lessonTemplate.id));
        return lessonTemplate.fromMins + subjectOpt.duration > currentMins
    },


    renderDayHeader(date){
        const self = this, p = self.props;
        return <tr key={'day-row-' + date}>
            <td colSpan="2"
                className="entities-tbl-cell-no-border entities-tbl-cell-xs-120p entities-tbl-cell-sm-160p"></td>
            <td colSpan="2"
                className="text-centered entities-tbl-cell-title entities-tbl-cell-high entities-tbl-cell-no-border">
                {Renderers.lesson.date.dayAndDate(date)}
            </td>
            <td className="entities-tbl-cell-no-border entities-tbl-cell-xs-120p entities-tbl-cell-sm-160p"></td>
        </tr>
    },

    renderLessonRows(lessonTemplate, dateMt, lessonDateSpan){
        const self = this, p = self.props,
            subjectId = Utils.subjectFromLessonTemplateId(lessonTemplate.id),
            subjectOpt = Dictionaries.lessonSubject.byId(subjectId);

        if (subjectOpt) {
            const lessonId = Utils.lessonId(subjectOpt.id, Utils.momentToString(dateMt), lessonTemplate.fromMins),
                lessonPlan = p.entities.lessonPlans[lessonId] || Utils.lessonPlanFromId(lessonId, subjectOpt.duration),
                isDayBlocked = LessonUtils.isDayBlocked(p.planLessonFilter, dateMt, lessonDateSpan, p.lessonProcedure),
                isLessonBlocked = LessonUtils.isLessonBlocked(lessonId, p.planLessonFilter, p.pickedLessons, p.blockedDateTimes, isDayBlocked);

            if (!isDayBlocked && !isLessonBlocked) {
                const studentSlotRows = self.renderStudentSlots(lessonId, lessonPlan),
                    filterStudentsAndCards = Dictionaries.lessonProcedure.byId(p.lessonProcedure).filterLessonsByStudent,
                    showLessonWithNoStudentSlots = (!filterStudentsAndCards || !p.planLessonFilter.student) && p.planLessonFilter.visitType == 'all';

                if (showLessonWithNoStudentSlots || studentSlotRows.length > 0) {
                    return [self.renderLessonHeader(lessonTemplate, subjectOpt, lessonPlan, dateMt, lessonDateSpan)]
                        .concat(studentSlotRows)
                }
            }
        }
        return null;
    },

    renderLessonHeader(lessonTemplate, subjectOpt, lessonPlan, dateMt, lessonDateSpan){
        const self = this, p = self.props,
            actions = LessonUtils.availableLessonSlotActions(lessonPlan, p),
            lessonId = lessonPlan.id,
            lessonStatus = LessonUtils.getLessonSlotStatus(p.planLessonFilter, lessonPlan.id),
            lessonSlotRowClasses = 'mixin-lesson-slot lesson-slot-list' +
                Utils.select(LessonUtils.isLessonPicked(lessonId, p.pickedLessons), Utils.select(lessonStatus === 'planned', ' lesson-slot-picked-remove', ' lesson-slot-picked'), '') +
                Utils.select(lessonId === p.selectedLesson, ' lesson-slot-selected', ''),
            lessonSlotClasses = 'mixin-lesson-slot mixin-lesson-slot-background lesson-slot-list' +
                Utils.select(lessonStatus, ' lesson-status-' + lessonStatus, '');


        return <tr key={'lesson-row-' + lessonTemplate.id}
                   className={lessonSlotRowClasses}
                   ref={elem => {
                       self.lessonRefs[lessonId] = elem
                   }}>
            <td onClick={() => self.onLessonSelect(lessonId)}
                className={'entities-tbl-cell-no-border entities-tbl-cell-high ' + lessonSlotClasses}>
                {Utils.minutesToTime(lessonTemplate.fromMins)}
            </td>
            <td onClick={() => self.onLessonSelect(lessonId)}
                className="entities-tbl-cell-no-border entities-tbl-cell-high">
                {Dictionaries.studentAge.byId(lessonTemplate.ageGroup).title}
            </td>
            <td colSpan="2"
                onClick={() => self.onLessonSelect(lessonId)}
                className="text-centered entities-tbl-cell-title entities-tbl-cell-high entities-tbl-cell-no-border">
                <span className={'outline-label outline-label-' + subjectOpt.id}>
                    {subjectOpt.title}
                </span>
            </td>
            <td className="entities-tbl-cell-no-border entities-tbl-cell-high">
                <DropdownActionButton
                    title={<span>{Dictionaries.lessonSlotStatus.byId(lessonPlan.status).title}&nbsp;
                        {Icons.caret()}</span>}
                    btnClasses="btn-link"
                    actions={actions}
                    dispatch={p.dispatch}/>
            </td>
        </tr>
    },

    onLessonSelect(lessonId){
        const self = this, p = self.props;
        p.dispatch(Actions.selectLesson(lessonId));
        LessonUtils.pickLesson(p, lessonId);
    },

    renderStudentSlots(lessonId, lessonPlan){
        const self = this, p = self.props,
            studentSlotsObj = lessonPlan.studentSlots,
            groupedStudentSlots = Utils.arr.groupByOrdered(Utils.objectValues(studentSlotsObj), self.studentSlotStatuses, self.studentSlotStatusAccessor).filter(o => !!o),
            studentSlots = Array.prototype.concat.apply([], groupedStudentSlots);

        return studentSlots.map((slot, i) => {
            const slotId = slot.id,
                studentId = slot.studentId,
                student = p.entities.students[studentId],
                filterStudentsAndCards = Dictionaries.lessonProcedure.byId(p.lessonProcedure).filterLessonsByStudent,
                selectedStudent = p.planLessonFilter.student,
                selectedStudentId = selectedStudent && selectedStudent.id,
                selectedCard = p.planLessonFilter.card,
                selectedCardId = selectedCard && selectedCard.id,
                selectedVisitType = p.planLessonFilter.visitType,
                slotStudentMatchSelected = !Utils.isDefined(selectedStudentId) || studentId === selectedStudentId,
                slotCardMatchSelected = !Utils.isDefined(selectedCardId) || slot.cardId === selectedCardId,
                slotVisitTypeMatchSelected = selectedVisitType === 'all' || slot.visitType === selectedVisitType,
                studentWillPresentAtLesson = student.presentInSchool && LessonUtils.willBeToday(Utils.lessonMomentFromId(lessonId)),
                slotMatchByStudentCardFilter = !filterStudentsAndCards || (slotStudentMatchSelected && slotCardMatchSelected);

            //apply matching by visit type in all cases, as it`s useful for plan lesson procedure
            if (slotMatchByStudentCardFilter && slotVisitTypeMatchSelected) {
                if (student) {
                    return <tr key={'slot-row-' + slotId}
                               className={Utils.select(i === 0, 'container-bordered-top')}>
                        <td colSpan="2"
                            className="text-centered">
                            <a onClick={Utils.invokeAndPreventDefaultFactory(() => Navigator.navigate(Navigator.routes.students, {
                                selection: student.id,
                                lessonId
                            }))}>
                                {Renderers.student.info(student)}
                            </a>
                        </td>
                        <td className="text-centered entities-tbl-cell-xs-40p">
                            {Dictionaries.visitType.byId(slot.visitType).title}
                        </td>
                        <td className="text-centered entities-tbl-cell-xs-40p">
                            {Renderers.lesson.repeatsLeft(slot.repeatsLeft, slot.status !== 'planned')}
                        </td>
                        <td className="entities-tbl-cell-high">
                            <DropdownActionButton
                                title={
                                    <span>{Renderers.lesson.studentSlotStatus(lessonId, slot, studentWillPresentAtLesson)}&nbsp;
                                        {Icons.caret()}</span>
                                }
                                btnClasses="btn-link"
                                actions={LessonUtils.availableStudentSlotActions(lessonId, slot, studentWillPresentAtLesson, p)}
                                dispatch={p.dispatch}/>
                        </td>
                    </tr>
                } else {
                    console.warn('Can not render student slot', slot, 'Student is not defined ', studentId);
                }
            }
        }).filter(Utils.isDefined)
    },

    lessonOffset(lessonId){
        return Utils.scroll.offset(this.lessonRefs[lessonId])
    }
});

module.exports = LessonsList;
