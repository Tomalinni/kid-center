'use strict';

const React = require('react'),
    Utils = require('../Utils'),
    LessonUtils = require('../LessonUtils'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    {ColFormGroup} = require('../components/CompactGrid');

const LessonsDayGrid = ({
    subjects = [],
    lessons = {},
    lessonPlans,
    planLessonFilter,
    day = {id: 'd0', title: 'Unknown'},
    date,
    visitsSummary,
    pickedLessons,
    selectedLesson,
    blockedDateTimes,
    isDayBlocked,
    onLessonClick
}) => {

    const dateMt = Utils.momentFromString(date),
        colWidth = (96 / subjects.length) + '%';

    return <ColFormGroup classes="col-md-6">
        <table className="day-lesson-slots-columns">
            <tbody>
            <tr>
                <td className="day-lesson-slots-day-title"
                    colSpan={subjects.length + 1}>
                    {day.title + ', ' + dateMt.format('DD.MM')}
                </td>
            </tr>
            <tr>
                <td key="day-time-scale"></td>
                {subjects.map((subject) => {
                    return <td key={subject.id}
                               style={{width: colWidth}}
                               className={'day-lesson-slots-column-header mixin-background-subject-' + subject.id}>{subject.shortTitle}</td>;
                })}
            </tr>
            {Dictionaries.lessonTime.map(time => {
                let lessonsDefinedForTime = false;

                const cells = subjects.map((subject) => {
                    const lesson = (lessons[subject.id] || []).find(lesson => lesson.fromMins === Utils.hoursMinutesToMinutes(time.id));

                    if (lesson) {
                        lessonsDefinedForTime = true;

                        const lessonId = Utils.lessonId(subject.id, date, lesson.fromMins),
                            lessonPlan = lessonPlans[lessonId],
                            lessonStatus = LessonUtils.getLessonSlotStatus(planLessonFilter, lessonId),
                            isLessonPicked = LessonUtils.isLessonPicked(lessonId, pickedLessons, lessonStatus),
                            cellClassName = 'day-lesson-slots-lesson-cell mixin-lesson-slot mixin-lesson-slot-background lesson-slot-table' +
                                Utils.select(LessonUtils.isLessonBlocked(lessonId, planLessonFilter, pickedLessons, blockedDateTimes, isDayBlocked), ' lesson-slot-blocked', '') +
                                Utils.select(lessonStatus, ' lesson-status-' + lessonStatus, '') +
                                Utils.select(isLessonPicked, Utils.select(lessonStatus === 'planned', ' lesson-slot-picked-remove', ' lesson-slot-picked'), '') +
                                Utils.select(lessonId === selectedLesson, ' lesson-slot-selected', '');

                        return <td key={lessonId}
                                   onClick={() => onLessonClick(lessonId)}
                                   data-lesson-id={lessonId}
                                   className={cellClassName}>
                            <div
                                className={'day-lesson-slots-lesson-cell-header mixin-background-subject-' + subject.id}>
                                {Dictionaries.studentAge.byId(lesson.ageGroup).title}
                            </div>
                            {renderLessonCellInfo(visitsSummary, lessonPlan)}
                        </td>
                    } else {
                        return <td key={subject.id + '-' + time.id}></td>
                    }
                });

                if (lessonsDefinedForTime) {
                    return <tr key={time.id}>
                        <td key={'day-time-scale' + time.id}
                            className="day-lesson-slots-time-cell">{time.title}</td>
                        {cells}
                    </tr>
                }
            })}
            </tbody>
        </table>
    </ColFormGroup>
};

function renderLessonCellInfo(visitsSummary, lessonPlan) {
    if (!lessonPlan && Config.showVisitSummaryForUndefinedLessons) {
        lessonPlan = {status: 'planned'}
    }

    if (lessonPlan) {
        return Utils.when(lessonPlan.status, {
            planned: () => renderPlannedLessonCellInfo(visitsSummary, lessonPlan),
        }, () => '-')
    }
    return '';
}

function renderPlannedLessonCellInfo(visitsSummary, lessonPlan) {
    const visits = Utils.defaultValues(lessonPlan.visitsSummary, ['regular', 'trial', 'bonus'], 0);
    if (visitsSummary.viewMode === 'free') {
        return renderFreeSlots(visits);
    } else if (visitsSummary.viewMode === 'booked') {
        return (<span
            className="day-lesson-slots-lesson-cell-info-booked">{getBookedSlots(visits, visitsSummary.sumType)}</span>);
    } else {
        let rest = Utils.zeroIfNo(visits.bonus);
        return (<div>
            <div className="day-lesson-slots-lesson-cell-info-advanced-booked">
                {visits.regular + ' ' + visits.trial + ' ' + rest}
            </div>
            {renderFreeSlots(visits)}
        </div>);
    }
}

function renderFreeSlots(visits) {
    let freeSlots = getFreeSlots(visits);
    return <span className="day-lesson-slots-lesson-cell-info-free">
        {freeSlots}
    </span>;
}

function getFreeSlots(visits) {
    return Config.maxSlotsSoftLimit - visits.regular - visits.trial - visits.bonus;
}

function getBookedSlots(visits, sumType) {
    if (sumType === 'total') {
        return Utils.visitsTotal(visits);
    } else {
        return Utils.zeroIfNo(visits[sumType])
    }
}

module.exports = LessonsDayGrid;
