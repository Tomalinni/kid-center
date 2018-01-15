'use strict';

const React = require('react'),
    Utils = require('../Utils'),
    LessonUtils = require('../LessonUtils'),
    Dictionaries = require('../Dictionaries'),
    Renderers = require('../Renderers'),
    Navigator = require('../Navigator'),
    {Row, Col, FormGroup, ColFormGroup, Button} = require('../components/CompactGrid'),
    DataService = require('../services/DataService'),
    Actions = require('../actions/Actions'),
    PhotosList = require('../components/PhotosList'),
    DropdownActionButton = require('../components/DropdownActionButton'),
    Icons = require('./Icons');

const LessonsStudentList = React.createClass({

    render(){
        const self = this, p = self.props,
            studentSlots = p.lessonPlan.studentSlots || {};

        return <div>
            {self.renderHeader()}
            <div className="with-nav-toolbar">
                <div className="container">
                    {self.renderToolbar()}
                    {self.renderChangeLessonStatusMessage()}
                    {Utils.select(Object.keys(studentSlots).length > 0, self.renderStudentSlots(), self.renderNoStudentSlotsMessage())}
                    {self.renderPhotoList()}
                </div>
            </div>
        </div>
    },

    renderStudentSlots(){
        const self = this, p = self.props,
            studentSlots = p.lessonPlan.studentSlots || {};

        return Object.keys(studentSlots).map(slotId => {
            const studentSlot = studentSlots[slotId],
                studentId = studentSlot.studentId,
                student = p.students[studentId],
                actions = LessonUtils.availableStudentSlotActions(p.lessonPlan.id, studentSlot, student.presentInSchool, p);

            if (student) {
                return <div key={slotId}
                            className="border-bottom-gray mrg-btm-10">
                    {self.renderStudentSlotItem(student, p.lessonPlan.id, studentSlot)}
                    {Utils.selectFn(Object.keys(actions).length, () =>
                        <Row>
                            <ColFormGroup>
                                <table className="period-btn-table">
                                    <tbody>
                                    <tr>
                                        {Object.keys(actions).map(actionId => {
                                            const action = actions[actionId];
                                            return <td key={actionId}>
                                                <Button onClick={() => action.fn(p.dispatch)}>
                                                    {action.title}
                                                </Button>
                                            </td>
                                        })}
                                    </tr>
                                    </tbody>
                                </table>
                            </ColFormGroup>
                        </Row>
                    )}
                </div>
            } else {
                console.warn('Can not render student slot', studentSlot, 'Student is not defined ', studentId);
            }
        })
    },

    renderNoStudentSlotsMessage(){
        return <div className="entities-tbl-message">No planned lessons</div>
    },

    renderStudentSlotItem(student, lessonId, studentSlot) {
        const itemClasses = ['lesson-students-sidebar-student-name'];
        if (studentSlot.status === 'canceled') {
            itemClasses.push('item-canceled')
        }
        return <Row classes={Utils.joinDefined(itemClasses)}>
            <ColFormGroup>
                <div className="pad-3">
                    <b className="label-rpad">Student</b>
                    <a onClick={Utils.invokeAndPreventDefaultFactory(() => Navigator.navigate(Navigator.routes.students, {selection: student.id}))}>
                        {Renderers.student.info(student)}
                    </a>
                </div>
                <div className="pad-3">
                    <b className="label-rpad">Type</b> {Dictionaries.visitType.byId(studentSlot.visitType).title}<br/>
                </div>
                <div className="pad-3">
                    <b className="label-rpad">Status</b> {Renderers.lesson.studentSlotStatus(lessonId, studentSlot, student.presentInSchool)}
                </div>
            </ColFormGroup>
        </Row>
    },

    renderHeader() {
        const self = this, p = self.props;
        return <h4 className="page-title">{Renderers.lessonPlanAndStatus(p.lessonPlan)}</h4>
    },

    renderToolbar() {
        const self = this, p = self.props,
            actions = LessonUtils.availableLessonSlotActions(p.lessonPlan, p);

        if (Object.keys(actions).length) {
            return <Row>
                <ColFormGroup classes="col-xs-3">
                    <DropdownActionButton dropdownMenuClasses="dropdown-menu-left"
                                          actions={actions}
                                          dispatch={p.dispatch}/>
                </ColFormGroup>
            </Row>
        }
    },

    renderPhotoList() {
        const self = this, p = self.props;
        if (p.lessonPlan.status !== 'revoked' && p.lessonPlan.studentSlots && Object.keys(p.lessonPlan.studentSlots).length) {
            return <div>
                <h4>{Utils.message('common.lesson.slot.photos')}</h4>
                <PhotosList hasPrimaryPhoto={true}
                            uploadUrl={DataService.urls.lessons.uploadPhoto(p.lessonPlan.id)}
                            downloadUrlFn={(name) => {
                                return DataService.urls.lessons.downloadPhoto(p.lessonPlan.id, name)
                            }}
                            onFileUploaded={p.onPhotoUploaded}
                            onRemoveFile={p.onRemovePhoto}
                            onChangeShownFile={p.onChangeShownPhoto}
                            fileNames={p.photos || []}
                            shownFileName={p.shownPhotoName}/></div>
        }
    },

    renderChangeLessonStatusMessage() {
        const self = this, p = self.props;

        if (p.changeLessonStatusMessage) {
            return <div
                className="alert alert-warning pad-3 mrg-hor-3 mrg-vert-0 font-xs">{p.changeLessonStatusMessage}</div>
        }
    }
});

module.exports = LessonsStudentList;
