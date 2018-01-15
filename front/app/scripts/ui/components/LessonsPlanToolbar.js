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
    Icons = require('../components/Icons'),
    Navigator = require('../Navigator'),
    Actions = require('../actions/Actions'),
    DialogService = require('../services/DialogService'),
    LessonUtils = require('../LessonUtils'),
    Renderers = require('../Renderers'),
    DropdownActionButton = require('../components/DropdownActionButton'),
    OverlayModal = require('../components/OverlayModal'),
    DatePicker = require('../components/DateComponents').DatePicker,
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid');

const LessonsPlanToolbar = React.createClass({

    container: null,

    componentDidMount(){
        this.applyAffix()
    },

    componentDidUpdate(){
        this.applyAffix()
    },

    componentWillUnmount(){
        this.disposeAffix()
    },

    applyAffix(){
        const self = this, p = self.props;
        if (self.container) {
            Utils.affix.applyFor(self.container)
        }
    },

    disposeAffix(){
        const self = this, p = self.props;
        if (self.container) {
            Utils.affix.disposeFor(self.container)
        }
    },

    render(){
        const self = this, p = self.props;

        return <div className="lesson-plan-toolbar-block"
                    ref={c => {
                        self.container = c
                    }}>
            <Row>
                {Utils.when(p.lessonProcedure, {
                    plan: self.renderPlanButton,
                    unplan: self.renderUnplanButton,
                    suspend: self.renderSuspendButton,
                    transfer: self.renderTransferLessonsButton,
                })}
                <ColFormGroup classes="col-xs-4">
                    <Button onClick={() => p.dispatch(Actions.setLessonProcedure("view"))}>
                        {Utils.message('button.back')}
                    </Button>
                </ColFormGroup>
            </Row>
            <Row>
                {self.renderPlanLessonStatusMessage()}
            </Row>
            {Utils.selectFn(p.lessonsTransfer.sourceStudent, self.renderSelectTransferTargetStudentModal)}
        </div>
    },

    renderPlanButton(){
        const self = this, p = self.props;
        const studentId = p.planLessonFilter.student && p.planLessonFilter.student.id,
            cardId = p.planLessonFilter.card && p.planLessonFilter.card.id,
            cardVisitType = p.planLessonFilter.card && p.planLessonFilter.card.visitType,
            pickedLessonsCount = p.pickedLessons && p.pickedLessons.length || 0;

        const loadDataCallback = () => Promise.all([
            LessonUtils.actions.loadLessonsData(p, p.currentDate, true),
            p.dispatch(Actions.ajax.lessons.student(studentId))
        ]);

        return [
            <ColFormGroup classes="col-xs-4">
                <Button disabled={!pickedLessonsCount}
                        onClick={() => self.onPlanLessons(studentId, cardId, cardVisitType, p.pickedLessons, false)
                            .then(loadDataCallback)
                        }>
                    {Utils.message('common.lessons.plan.action.plan.single')}
                </Button>
            </ColFormGroup>,
            <ColFormGroup classes="col-xs-4">
                <Button disabled={!pickedLessonsCount}
                        onClick={() => self.onPlanLessons(studentId, cardId, cardVisitType, p.pickedLessons, true)
                            .then(loadDataCallback)
                        }>
                    {Utils.message('common.lessons.plan.action.plan.repeated')}
                </Button>
            </ColFormGroup>
        ]
    },

    renderUnplanButton(){
        const self = this, p = self.props;
        return <ColFormGroup classes="col-xs-8">
            <Button disabled={self.isUnplanButtonDisabled()}
                    onClick={() => self.onUnplanLesson()}>
                {Utils.message('button.unplan')}
            </Button>
        </ColFormGroup>
    },

    renderSuspendButton(){
        const self = this, p = self.props,
            studentId = p.planLessonFilter.student && p.planLessonFilter.student.id,
            cardId = p.planLessonFilter.card && p.planLessonFilter.card.id,
            cardVisitType = p.planLessonFilter.card && p.planLessonFilter.card.visitType,
            suspendButtonDisabled = !cardId || cardVisitType !== 'regular';
        return <ColFormGroup classes="col-xs-8">
            <DatePicker
                onChange={(date) => LessonUtils.doWithConfirmation(() => p.dispatch(Actions.ajax.lessons.suspend(cardId, date, studentId, cardVisitType))
                    .then(() => LessonUtils.actions.loadLessonsData(p, date, true)))}>
                <Button disabled={suspendButtonDisabled}>
                    {Utils.message('button.suspendCard')} {Icons.caret()}
                </Button>
            </DatePicker>
        </ColFormGroup>
    },

    renderTransferLessonsButton(){
        const self = this, p = self.props,
            pickedLessonsCount = p.pickedLessons && p.pickedLessons.length || 0;
        return <ColFormGroup classes="col-xs-8">
            <Button disabled={pickedLessonsCount === 0}
                    onClick={() => self.onTransferLessons()}>
                {Utils.message('button.transferLessons')}
            </Button>
        </ColFormGroup>
    },

    renderPlanLessonStatusMessage(){
        const self = this, p = self.props;
        if (p.planLessonStatusMessage) {
            return <div className="col-sm-12 col-pad-5">
                {p.planLessonStatusMessage}
                &nbsp;{self.renderPlanLessonStatusDescription()}
            </div>
        }
    },

    renderPlanLessonStatusDescription(){
        const self = this, p = self.props;
        if (p.planLessonStatusDescription) {
            return <a onClick={() => self.showPlanLessonStatusDescription()}>
                {Utils.message('common.lessons.plan.result.more')}
            </a>
        }
    },

    renderSelectTransferTargetStudentModal(){
        const self = this, p = self.props;
        return <OverlayModal isOpened={!!p.lessonsTransfer.sourceStudent}
                             closeModal={() => p.dispatch(Actions.setEntityValues('lessonsTransfer', {
                                 sourceStudent: null,
                                 lessons: []
                             }))}
                             renderModalContent={self.renderSelectTransferTargetStudentContent}
        />
    },

    renderSelectTransferTargetStudentContent(){
        const self = this, p = self.props;
        return <Select
            async={true}
            name="student"
            placeholder={Utils.message('common.lessons.search.fields.studentName')}
            value={p.lessonsTransfer.targetStudent}
            valueRenderer={s => Renderers.student.info(s)}
            optionRenderer={s => Renderers.student.info(s)}
            loadOptions={Utils.debounceInput(self.loadStudents)}
            onChange={student => {
                p.dispatch(Actions.setEntityValues('lessonsTransfer', {
                    targetStudent: student
                }));
                if (student) {
                    Navigator.navigate(Navigator.routes.students, {selection: student.id})
                }
                p.dispatch(Actions.setPageMode('studentCard', 'studentCard', 'card', 'studentCardPayment', {
                    sourceStudent: {id: p.lessonsTransfer.sourceStudent.id},
                    lessons: p.lessonsTransfer.lessons
                }))
            }}
        />

    },

    showPlanLessonStatusDescription(){
        const self = this, p = self.props;
        DialogService.modal({
            title: Utils.message('common.lessons.plan.result.planned.lessons'),
            content: <div dangerouslySetInnerHTML={{__html: p.planLessonStatusDescription}}/>
        })
    },

    getPlanActions(){
        const self = this, p = self.props;
        const studentId = p.planLessonFilter.student && p.planLessonFilter.student.id,
            cardId = p.planLessonFilter.card && p.planLessonFilter.card.id,
            cardVisitType = p.planLessonFilter.card && p.planLessonFilter.card.visitType,
            pickedLessonsCount = p.pickedLessons && p.pickedLessons.length || 0;

        if (pickedLessonsCount > 0) {
            const loadDataCallback = () => Promise.all([
                LessonUtils.actions.loadLessonsData(p, p.currentDate, true),
                p.dispatch(Actions.ajax.lessons.student(studentId))
            ]);
            return {
                planSingle: {
                    title: Utils.message('common.lessons.plan.action.plan.single'),
                    fn: dispatch => self.onPlanLessons(studentId, cardId, cardVisitType, p.pickedLessons, false)
                        .then(loadDataCallback)
                },
                planRepeated: {
                    title: Utils.message('common.lessons.plan.action.plan.repeated'),
                    fn: dispatch => self.onPlanLessons(studentId, cardId, cardVisitType, p.pickedLessons, true)
                        .then(loadDataCallback)
                }
            }
        } else {
            return {}
        }
    },

    onPlanLessons(studentId, cardId, cardVisitType, pickedLessons, repeatWeekly){
        const self = this, p = self.props;
        return p.dispatch(Actions.ajax.lessons.plan(studentId, cardId, cardVisitType, pickedLessons, repeatWeekly, p.lessonProcedure))
    },

    onUnplanLesson(){
        const self = this, p = self.props,
            studentSlot = self.findStudentSlotToUnplan();
        if (studentSlot) {
            const studentPresentInSchool = LessonUtils.isSelectedStudentPresentInSchool(p.planLessonFilter.student, p.entities.students),
                confirmationMessage = Utils.select(studentPresentInSchool, Utils.message('common.lessons.confirm.action.for.student.in.school'), Utils.message('common.dialog.confirm.common.message'));
            DialogService.doWithConfirmation(confirmationMessage,
                () => p.dispatch(Actions.ajax.lessons.unplan(p.selectedLesson, studentSlot.id, studentSlot.visitType, studentSlot.status))
                    .then(() => LessonUtils.actions.loadLessonsData(p, p.startDate, true)))
        }
    },

    onTransferLessons(){
        const self = this, p = self.props;

        DialogService.doWithConfirmation(Utils.message('common.dialog.confirm.common.message'), () => {
            const student = p.planLessonFilter.student,
                card = p.planLessonFilter.card,
                targetStudent = p.lessonsTransfer.targetStudent,
                transferCard = p.lessonsTransfer.transferCard,
                studentId = student && student.id,
                cardId = card && card.id,
                targetStudentId = targetStudent && targetStudent.id,
                transferCardId = transferCard && transferCard.id;
            p.dispatch(Actions.ajax.lessons.transfer(studentId, cardId, targetStudentId, transferCardId, p.pickedLessons, false))
                .then(() => LessonUtils.actions.loadLessonsData(p, p.startDate, true));
        });
    },

    isUnplanButtonDisabled(){
        return this.findStudentSlotToUnplan() == null
    },

    findStudentSlotToUnplan(){
        const self = this, p = self.props,
            selectedStudent = p.planLessonFilter.student,
            notUnplannableSlotStatuses = ['visited', 'missed'];
        if (p.selectedLesson && selectedStudent) {
            const lessonPlan = p.entities.lessonPlans[p.selectedLesson];
            if (lessonPlan) {
                const studentSlots = lessonPlan.studentSlots,
                    unplannedSlotId = Object.keys(studentSlots).find(slotId => {
                        const studentSlot = studentSlots[slotId],
                            sameStudentId = studentSlot.studentId === selectedStudent.id,
                            unplannableSlotStatus = !Utils.arr.contains(notUnplannableSlotStatuses, studentSlot.status);

                        return sameStudentId && unplannableSlotStatus
                    });
                const unplannedSlot = unplannedSlotId && studentSlots[unplannedSlotId];
                return unplannedSlot && {
                        id: unplannedSlotId,
                        visitType: unplannedSlot.visitType,
                        status: unplannedSlot.status
                    }
            }
        }
        return null
    }
});

module.exports = LessonsPlanToolbar;
