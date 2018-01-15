'use strict';

const React = require('react'),
    moment = require('moment-timezone'),
    Utils = require('../Utils'),
    LessonUtils = require('../LessonUtils'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    LessonsDayGrid = require('./LessonsDayGrid'),
    {Row} = require('../components/CompactGrid'),
    Pressure = require('pressure');

const LessonsGrid = React.createClass({
    slotsContainer: null,

    componentDidMount(){
        const self = this, p = self.props;

        Pressure.set(self.slotsContainer, {
            startDeepPress: function (event) {
                const lessonIdAttr = event.target.attributes['data-lesson-id'];
                if (lessonIdAttr) {
                    p.onDeepPress(lessonIdAttr.value)
                }
            }
        });
    },

    render(){
        const self = this, p = self.props;

        let daysCount = Object.keys(p.lessons).length,
            dayContainerDayCountClassNameSuffix = daysCount <= 5 ? '5days' : (daysCount == 6 ? '6days' : null),
            dayContainerDayCountClassName = dayContainerDayCountClassNameSuffix ? 'day-lesson-slots-container-' + dayContainerDayCountClassNameSuffix : null,
            startOffsetMins = 24 * 60,
            endOffsetMins = 0,
            startDateMt = moment(p.startDate, Config.dateFormat),
            dayLessonSlots = [],
            lessonDateSpan = LessonUtils.lessonDateSpan(p.pickedLessons);

        Dictionaries.day.forEach((day) => {
            let lessonsByDay = p.lessons[day.id];
            if (lessonsByDay) {
                Object.keys(lessonsByDay).forEach((subjectId) => {
                    const lessonsBySubject = lessonsByDay[subjectId],
                        subjectOpt = Dictionaries.lessonSubject.byId(subjectId);
                    lessonsBySubject.forEach((lesson) => {
                        startOffsetMins = Math.min(lesson.fromMins, startOffsetMins);
                        endOffsetMins = Math.max(lesson.fromMins + subjectOpt.duration, endOffsetMins);
                    });
                });
            }
        });

        startOffsetMins = Math.floor(startOffsetMins / 60) * 60;
        endOffsetMins = Math.ceil(endOffsetMins / 60) * 60;

        for (let i = 0; i < 7; i++) {
            let dateMt = moment(startDateMt).add(i, 'days'),
                weekDayIndex = Utils.dayOfWeek(dateMt),
                day = Dictionaries.day[weekDayIndex],
                lessonsByDay = p.lessons[day.id],
                isDayBlocked = LessonUtils.isDayBlocked(p.planLessonFilter, dateMt, lessonDateSpan, p.lessonProcedure);

            if (lessonsByDay) {
                let extraClassNames = ['day-lesson-slots-container-' + (i + 1)];
                if (!!dayContainerDayCountClassName) {
                    extraClassNames.push(dayContainerDayCountClassName)
                }

                dayLessonSlots.push(<LessonsDayGrid key={day.id}
                                                    subjects={p.subjects}
                                                    lessons={lessonsByDay}
                                                    lessonPlans={p.lessonPlans}
                                                    planLessonFilter={p.planLessonFilter}
                                                    day={day}
                                                    date={dateMt.format(Config.dateFormat)}
                                                    visitsSummary={p.visitsSummary}
                                                    extraContainerClassNames={extraClassNames}
                                                    startOffsetMins={startOffsetMins}
                                                    endOffsetMins={endOffsetMins}
                                                    pickedLessons={p.pickedLessons}
                                                    selectedLesson={p.selectedLesson}
                                                    blockedDateTimes={p.blockedDateTimes}
                                                    isDayBlocked={isDayBlocked}
                                                    onLessonClick={p.onLessonClick}/>);
            }
        }

        return <div ref={c => {
            self.slotsContainer = c;
        }}>
            <Row>
                {dayLessonSlots}
            </Row>
        </div>
    }
});

module.exports = LessonsGrid;
