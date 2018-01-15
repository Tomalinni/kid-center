'use strict';

const Utils = require('./Utils'),
    LessonUtils = require('./LessonUtils'),
    moment = require('moment-timezone'),
    Config = require('./Config'),
    Navigator = require('./Navigator'),
    Dictionaries = require('./Dictionaries'),
    Icons = require('./components/Icons');

const Renderers = {
    valOrNotDefined(val, renderFn){
        return val ? (renderFn ? renderFn(val) : val) : 'Not defined';
    },

    bool(val, trueMessage, falseMessage){
        return val ? trueMessage : falseMessage
    },

    numRange(start, end, defaultVal = '-', separator = ' - '){
        if (Utils.isDefined(start) && Utils.isDefined(end) && start != end) {
            return start + separator + end
        } else if (Utils.isDefined(start)) {
            return start
        } else if (Utils.isDefined(end)) {
            return end
        } else {
            return defaultVal
        }
    },

    dictOption(opt, defaultVal){
        return _objPropOrDefault(opt, 'title', defaultVal);
    },

    objName: _objNameOrDefault,

    validation: {
        iconForArrIndex(arr, i){
            return Utils.select(arr && !Utils.isAllValuesEmpty(arr[i]), Icons.glyph.warningSign('icon-needs-validation'))
        }
    },

    arr(arr, delimiter = ' ', renderFn = Utils.obj.self, filterFn = Utils.fn.truth){
        if (!Array.isArray(arr)) {
            return '';
        }
        return arr.filter(filterFn.bind(this)).map(renderFn.bind(this)).join(delimiter);
    },

    /**
     * Renders student birth date and age
     * @param student
     * @returns {string} formatted info representation
     */
    studentBirthDateAndAge(student) {
        const birthDate = Utils.momentToString(Utils.momentFromString(student.birthDate), Config.shortDateFormat),
            age = Utils.studentAgeYearMonths(birthDate);
        return birthDate ? (age + ' ' + birthDate) : '-'
    },
    cardActiveState (card, titleProp) {
        const self = this;
        if (card) {
            if (!card.active) {
                return <span className="label label-default">
                    {self._cardStateTitle('inactive', titleProp)}
                </span>
            }
            const curMoment = moment();
            if (curMoment.isAfter(Utils.momentFromString(card.expirationDate))) {
                return <span className="label label-default">
                    {self._cardStateTitle('expired', titleProp)}
                </span>
            }
            if (curMoment.isBefore(Utils.momentFromString(card.creationDate))) {
                return <span className="label label-default">
                    {self._cardStateTitle('notStarted', titleProp)}
                </span>
            }
            return <span className="label label-success">
                {self._cardStateTitle('active', titleProp)}
            </span>
        }
        return '';
    },
    _cardStateTitle(stateId, titleProp){
        return Dictionaries.cardState.byId(stateId)[titleProp]
    },
    cardValidityRange(card){
        const renderFn = date => Utils.momentToString(Utils.momentFromString(date), Config.shortDateFormat),
            renderedCreationDate = this.valOrNotDefined(card.creationDate, renderFn),
            renderedExpirationDate = this.valOrNotDefined(card.expirationDate, renderFn);
        return renderedCreationDate + ' - ' + renderedExpirationDate
    },
    cardValidity(card){
        if (card.expirationDate) {
            return Utils.daysLeftBeforeExpiration(Utils.momentFromString(card.expirationDate))
        } else {
            return '-'
        }
    },
    cardDuration(card){
        if (card) {
            return Renderers.numRange(card.durationDays, card.durationDaysMax);
        }
        return '';
    },

    card: {
        info: {
            panel(card){
                const visitTypeTitle = card.visitType + ' card',
                    duration = (card.durationDays || 'no') + ' days',
                    lessonsLimit = (card.lessonsLimit || 'no') + ' lessons';
                return card ? Utils.renderArray([card.id, visitTypeTitle, duration, lessonsLimit], ', ') : '';
            },
            select(card){
                const lessonsAvailableStr = card.visitType !== 'transfer' ? ((card.lessonsLimit || 0) + 'L') : null,
                    cancelsAvailableStr = (card.cancelsLimit || 0) + ';' + (card.lateCancelsLimit || 0) + ';' + (card.lastMomentCancelsLimit || 0) + ';' + (card.undueCancelsLimit || 0) + 'C',
                    missAvailableStr = (card.missLimit || 0) + 'M',
                    visitTypeTitle = Dictionaries.visitType.byId(card.visitType).title,
                    infoArr = [visitTypeTitle, lessonsAvailableStr, cancelsAvailableStr, missAvailableStr];

                if (card.visitType === 'regular') {
                    const suspendsAvailableStr = (card.suspendsLimit || 0) + 'S';
                    infoArr.push(suspendsAvailableStr)
                }
                return card ? Utils.renderArray(infoArr, ' ') : '';
            }
        }
    },

    studentCard: {
        info: {
            select(card){
                const lessonsAvailableStr = (card.lessonsAvailable || 0) + 'L',
                    cancelsAvailableStr = (card.cancelsAvailable || 0) + ';' + (card.lateCancelsAvailable || 0) + ';' + (card.lastMomentCancelsAvailable || 0) + ';' + (card.undueCancelsAvailable || 0) + 'C',
                    visitTypeTitle = Dictionaries.visitType.byId(card.visitType).title,
                    purchaseDate = Utils.mt.convert.format(card.purchaseDate, Config.dateTimeFormat, Config.dayMonthDateFormat),
                    infoArr = [visitTypeTitle, purchaseDate, lessonsAvailableStr, cancelsAvailableStr];

                if (card.visitType === 'regular') {
                    const suspendsAvailableStr = (card.suspendsAvailable || 0) + 'S';
                    infoArr.push(suspendsAvailableStr)
                }
                return card ? Utils.renderArray(infoArr, ' ') : '';
            }
        }
    },


    profile: {
        /**
         * Renders profile info
         * @param profile
         * @returns {string} formatted info representation
         */
        info: _objNameOrDefault
    },

    student: {
        /**
         * Renders student info
         * @param student
         * @returns {string} formatted info representation
         */
        info(student) {
            let age = Utils.studentAgeYearMonths(student.birthDate),
                gender = student.gender && Dictionaries.studentGender.byId(student.gender).title;
            return student ? Utils.renderArray([student.businessId, student.nameCn, student.nameEn, age, gender], ' ') : '';
        },

        names(student){
            return student ? Utils.renderArray([student.businessId, student.nameCn, student.nameEn], ' ') : ''
        },

        link(student, info){
            return student ?
                <a onClick={Utils.invokeAndPreventDefaultFactory(() => Navigator.navigate(Navigator.routes.student(student.id)))}>{info}</a> : ''
        },

        businessId(student, defaultValue = '-'){
            if (student) {
                return student.status === 'cardPaid' ? student.businessId + ' (' + student.trialBusinessId + ')' : student.businessId || defaultValue
            }
            return defaultValue
        },

        card: {
            expirationDate(card){
                if (card) {
                    let durationDays = Utils.valueOrZero(card.durationDays);
                    let activationDateMt = Utils.momentFromString(card.activationDate);
                    let expirationDate = Utils.isDefined(activationDateMt) ? Utils.momentToString(activationDateMt.add(durationDays, 'days'), Config.shortDateFormat) : null;
                    return Utils.isDefined(expirationDate) ? expirationDate : Utils.message('common.student.card.active.state.not.active.abbr')
                }
                return ''
            },

            activePeriod(card){
                if (card) {
                    let durationDays = Utils.valueOrZero(card.durationDays);
                    let durationStr = durationDays + ' ' + Utils.message('common.student.card.duration.days');
                    let activationDateMt = Utils.momentFromString(card.activationDate);
                    let expirationDate = Utils.isDefined(activationDateMt) ? Utils.momentToString(activationDateMt.add(durationDays, 'days')) : null;
                    let periodStr = Utils.isDefined(activationDateMt) ? (card.activationDate + ' - ' + expirationDate) : Utils.message('common.student.card.active.state.not.active');
                    return durationStr + ', ' + periodStr
                }
                return ''

            },

            info(card){
                return card ? (Renderers.student.card.lessons(card) + ' ' + Utils.message('common.student.card.form.lessons') + ', ' +
                    Renderers.student.card.cancels(card) + ' ' + Utils.message('common.student.card.form.cancels') + ', ' +
                    Renderers.student.card.suspends(card) + ' ' + Utils.message('common.student.card.form.suspends')) : ''
            },

            lessons(card){
                return card ? (Utils.valueOrZero(card.lessonsAvailable) + '/' + Utils.valueOrZero(card.lessonsLimit)) : ''
            },

            lessonsSpent(card){
                return card ? Utils.valueOrZero(card.spentLessonsCount) : ''
            },

            lessonsNotSpent(card){
                return card ? (Utils.valueOrZero(card.plannedLessonsCount) + Utils.valueOrZero(card.lessonsAvailable)) : ''
            },

            lessonsPlanned(card){
                return card ? Utils.valueOrZero(card.plannedLessonsCount) : ''
            },

            lessonsAvailable(card){
                return card ? Utils.valueOrZero(card.lessonsAvailable) : ''
            },

            cancels(card){
                const cardRenderer = Renderers.student.card;
                return card ? Utils.joinTruthy(
                        [cardRenderer.earlyCancels(card),
                            cardRenderer.lateCancels(card),
                            cardRenderer.lastMomentCancels(card),
                            cardRenderer.undueCancels(card)], ' ') : ''
            },
            earlyCancels(card){
                return card ? availableAndLimitNumbers(card.cancelsAvailable, card.cancelsLimit) : ''
            },
            lateCancels(card){
                return card ? availableAndLimitNumbers(card.lateCancelsAvailable, card.lateCancelsLimit) : ''
            },
            lastMomentCancels(card){
                return card ? availableAndLimitNumbers(card.lastMomentCancelsAvailable, card.lastMomentCancelsLimit) : ''
            },
            undueCancels(card){
                return card ? availableAndLimitNumbers(card.undueCancelsAvailable, card.undueCancelsLimit) : ''
            },
            miss(card){
                return card ? availableAndLimitNumbers(card.missAvailable, card.missLimit) : ''
            },
            suspends(card){
                return card ? availableAndLimitNumbers(card.suspendsAvailable, card.suspendsLimit) : ''
            },
            daysBeforeExpiration(card){
                if (card && card.activationDate && card.durationDays) {
                    return Utils.daysLeftBeforeExpiration(Utils.momentFromString(card.activationDate).add(card.durationDays, 'days'))
                } else {
                    return '-'
                }
            }
        },

        relative: {
            roleAndName(relative){
                return relative ? Utils.renderArray([relative.role, relative.name], ' ') : ''
            }
        }
    },

    lesson: {
        subject: {
            label(subjectOpt){
                return <span className={'outline-label outline-label-' + subjectOpt.id}
                             style={{borderColor: subjectOpt.color}}>
                                {subjectOpt.title}
                            </span>
            }
        },
        date: {
            dayAndDate(date){
                const mt = Utils.momentFromString(date);
                if (moment().isSame(mt, 'day')) {
                    return Utils.message('common.moment.day.today')
                } else {
                    const weekDayIndex = Utils.dayOfWeek(mt),
                        day = Dictionaries.day[weekDayIndex].title;
                    return day + ', ' + Utils.momentToString(mt, Config.dayMonthDateFormat)
                }
            }
        },
        idAsObj(lessonId, onClick){
            const subjectOpt = LessonUtils.lessonSubjectFromId(lessonId),
                dateTime = Utils.momentToString(Utils.lessonMomentFromId(lessonId), 'DD.MM HH:mm');
            return <span key={lessonId}
                         className={'outline-label outline-label-' + subjectOpt.id}
                         onClick={onClick}
                         style={{borderColor: subjectOpt.color}}>
                                {dateTime}
                            </span>
        },
        repeatedLesson(lessonId, repeatsLeft, onClick){
            const subjectOpt = LessonUtils.lessonSubjectFromId(lessonId),
                dateTime = Utils.momentToString(Utils.lessonMomentFromId(lessonId), 'DD.MM HH:mm');

            return <span key={lessonId}
                         className={'outline-label outline-label-' + subjectOpt.id}
                         onClick={onClick}
                         style={{borderColor: subjectOpt.color}}>
                                {dateTime + Renderers.lesson.repeatsLeft(repeatsLeft, false)}
                            </span>
        },
        repeatsLeft(repeatsLeft, emptyIfSingle){
            return Utils.select(emptyIfSingle && !LessonUtils.isRepeatedLesson(repeatsLeft), '', ' x ' + repeatsLeft)
        },
        idAsDaysBefore(lessonId){
            const subjectOpt = LessonUtils.lessonSubjectFromId(lessonId),
                days = Utils.daysLeftBeforeExpiration(Utils.lessonMomentFromId(lessonId));
            return <span key={lessonId}
                         className={'outline-label outline-label-compact outline-label-' + subjectOpt.id}
                         style={{borderColor: subjectOpt.color}}>
                                {days}
                            </span>
        },
        lessonAndVisitType(lessonId, visitType){
            const subjectOpt = LessonUtils.lessonSubjectFromId(lessonId),
                dateTime = Utils.momentToString(Utils.lessonMomentFromId(lessonId), 'DD.MM HH:mm'),
                visitTypeTitle = Dictionaries.visitType.byId(visitType).title;
            return <span key={lessonId}
                         className={'outline-label outline-label-' + subjectOpt.id}
                         style={{borderColor: subjectOpt.color}}>
                                {visitTypeTitle + ' ' + dateTime}
                            </span>
        },
        repeatsCount(repeatsCount){
            return repeatsCount ? 'x ' + repeatsCount : ''
        },
        studentSlotStatus(lessonId, slot, studentWillPresentAtLesson){
            let status = slot.status,
                lessonMt = Utils.lessonMomentFromId(lessonId);
            if (status === 'planned') {
                if (Utils.isLessonStarted(lessonMt)) {
                    if (studentWillPresentAtLesson) {
                        return Utils.message('common.student.slot.status.planned.atLesson')
                    }
                    return redFont(Utils.message('common.student.slot.status.planned.notAtLesson'))
                }
                if (Utils.lessonWillBeToday(lessonMt)) {
                    if (studentWillPresentAtLesson) {
                        return Utils.message('common.student.slot.status.planned.inSchool')
                    }
                    return Utils.message('common.student.slot.status.planned.soon')
                }
            }
            if (status === 'canceled') {
                if (slot.cancelType === 'late') {
                    return redFont(Utils.message('common.student.slot.status.canceledLate'))
                } else if (slot.cancelType === 'lastMoment') {
                    return redFont(Utils.message('common.student.slot.status.canceledLastMoment'))
                } else if (slot.cancelType === 'undue') {
                    return redFont(Utils.message('common.student.slot.status.canceledUndue'))
                }
            }
            let message = Dictionaries.studentSlotStatus.byId(status).title;
            if (status === 'missed' && slot.invalidated) {
                return redFont(<span>{Icons.glyph.lock()}&nbsp;{message}</span>)
            }
            if (status === 'canceled' || status === 'missed') {
                return redFont(message)
            }
            return message
        }
    },

    kinderGarden(obj){
        return obj && Utils.joinDefined([obj.name, obj.address, obj.phone], '; ') || ''
    },

    kinderGardenName(obj){
        return obj && obj.name || ''
    },

    lessonPlanAndStatus(lessonPlan){
        return lessonPlan ? lessonPlan.id.toUpperCase() + ' - ' + Dictionaries.lessonSlotStatus.byId(lessonPlan.status).title : ''
    },

    /**
     * Renders cateacherrd info
     * @param obj
     * @returns {string} formatted info representation
     */
    teacher: _objNameOrDefault,


    account: {
        name: _objNameOrDefault,
        number: (obj) => _objPropOrDefault(obj, 'number'),
        numberOrLogin: (obj) => _objPropOrDefault(obj, obj.type === 'cashless' ? 'number' : 'login'),

        suffix(account){
            const name = account && account.name || '', length = name.length;
            return length >= 4 ? name.substring(length - 4) : ''
        },

        typeAbbr(obj){
            const opt = Dictionaries.accountType.byId(obj.type);
            return _objPropOrDefault(opt, 'abbr')
        }
    },

    paymentCategory: _objNameOrDefault,
    city: _objNameOrDefault,
    school: {
        name: _objNameOrDefault,
        cityAndName: (obj) => ((obj && obj.name || '') + ' ' + (obj && obj.city && obj.city.name || ''))
    },
    category: _objNameOrDefault,

    user: {
        info: (obj) => {
            var name = _objNameOrDefault(obj);
            var id = _objPropOrDefault(obj, 'id');
            var newId = _objPropOrDefault(obj, 'newId');
            id = id ? id : newId;
            return name ? name + ' (' + id + ')' : id
        }
    },

    role: {
        info: (obj) => obj.id
    },

    homework: {
        info: (obj) => (obj.id) ? Utils.message("common.homework.title.prefix") + obj.id : Utils.message("common.homework.title.new")
    }
};

function redFont(component) {
    return <span className="label-red">{component}</span>
}

function availableAndLimitNumbers(available, limit) {
    return Utils.valueOrZero(available) + '/' + Utils.valueOrZero(limit)
}

function _objNameOrDefault(obj, defaultVal) {
    return _objPropOrDefault(obj, 'name', defaultVal);
}

function _objPropOrDefault(obj, prop, defaultVal = '') {
    return obj && obj[prop] || defaultVal;
}

module.exports = Renderers;
