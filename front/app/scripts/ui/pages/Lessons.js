'use strict';

const React = require('react'),
    moment = require('moment-timezone'),
    {connect} = require('react-redux'),
    ResizeSensor = require('css-element-queries/src/ResizeSensor'),
    AuthService = require('../services/AuthService'),
    Permissions = require('../Permissions'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Actions = require('../actions/Actions'),
    Renderers = require('../Renderers'),
    LessonUtils = require('../LessonUtils'),
    Utils = require('../Utils'),

    DataService = require('../services/DataService'),
    DialogService = require('../services/DialogService'),
    Icons = require('../components/Icons'),
    {Row, Col, FormGroup, ColFormGroup, TableButtonGroup, Button, InlineButton, Select} = require('../components/CompactGrid'),
    LessonsToolbar = require('../components/LessonsToolbar'),
    LessonsPlanToolbar = require('../components/LessonsPlanToolbar'),
    PageToolbar = require('../components/PageToolbar'),
    DropdownActionButton = require('../components/DropdownActionButton'),
    LessonsPlanSummary = require('../components/LessonsPlanSummary'),
    LessonsStudentList = require('../components/LessonsStudentList'),
    LessonsCardList = require('../components/LessonsCardList'),
    LessonsGrid = require('../components/LessonsGrid'),
    LessonsList = require('../components/LessonsList');

const Lessons = React.createClass({

    lessonsListElem: null,
    lessonPlanToolbarElem: null,
    prevPickedLessonsLength: 0,

    componentDidMount(){
        const self = this, p = self.props,
            selectedStudentId = p.location.query.selection;

        LessonUtils.actions.setDate(p, p.location.query.startDate || p.startDate).then(() => {
            if (selectedStudentId) {
                p.dispatch(Actions.setLessonProcedure('plan'));

                p.dispatch(Actions.ajax.lessons.student(selectedStudentId)).then(response => {
                    const student = response.results[0];
                    if (student) {
                        p.dispatch(Actions.ajax.lessons.findStudentPlannedLessons(student.id))
                            .then(() => {
                                p.dispatch(Actions.setPlanLessonFilter('student', student));
                                p.dispatch(Actions.setPlanLessonFilter('usePreciseAge', false));

                                const focusedLessonId = p.location.query.lessonId;
                                if (focusedLessonId) {
                                    if (self.lessonsListElem) {
                                        self.scrollToLesson(focusedLessonId)
                                    } else {
                                        console.warn('lessonsListElem ref is not defined. Can not scroll to it.')
                                    }
                                }
                            })
                    }
                });
            }
        });
    },

    componentDidUpdate(){
        const self = this, p = self.props,
            usePickedLessons = Dictionaries.lessonProcedure.byId(p.lessonProcedure).usePickedLessons,
            lessonIdToShow = Utils.arr.lastItem(p.pickedLessons);

        if (usePickedLessons && p.pickedLessons.length > self.prevPickedLessonsLength && lessonIdToShow) {
            self.scrollToLesson(lessonIdToShow)
        }
        self.prevPickedLessonsLength = p.pickedLessons.length;
    },

    render(){
        const self = this, p = self.props;

        return <div className="page-container">
            <PageToolbar
                leftButtons={[self.renderBackButton(), self.renderStudentSlotsButton()]}/>
            {Utils.when(p.pageMode, {
                lessons: self.renderLessons,
                studentSlots: self.renderStudentSlots
            })}
        </div>
    },

    renderBackButton(){
        const self = this, p = self.props;
        if (p.pageMode === 'studentSlots') {
            return <button type="button"
                           key="lessons"
                           className="btn btn-default btn-lg nav-toolbar-btn"
                           onClick={() => p.dispatch(Actions.setPageMode('lessons', 'home', 'lesson', 'lessons'))}>
                {Icons.glyph.arrowLeft()}
            </button>
        }
    },

    renderStudentSlotsButton(){
        const self = this, p = self.props;
        if (p.pageMode === 'lessons' && p.selectedLesson) {
            const subjectOpt = LessonUtils.lessonSubjectFromId(p.selectedLesson);
            return <button type="button"
                           key="studentSlots"
                           className="btn btn-default btn-lg nav-toolbar-btn"
                           onClick={() => {
                               p.dispatch(Actions.setPageMode('lessons', 'home', 'lesson', 'studentSlots'));
                               const lessonPlan = p.entities.lessonPlans[p.selectedLesson] || Utils.lessonPlanFromId(p.selectedLesson, subjectOpt.duration);
                               p.dispatch(Actions.ajax.lessons.findPhotos(lessonPlan.id))
                           }}>
                {Icons.glyph.user()}
            </button>
        }
    },

    renderLessons(){
        const self = this, p = self.props;

        return <div>
            <h4 className="page-title">{Utils.message('common.pages.lessons')}</h4>
            <div className="with-nav-toolbar">
                <div className="container">

                    <LessonsToolbar startDate={p.startDate}
                                    endDate={p.endDate}
                                    onSetDate={(date) => LessonUtils.actions.setDate(p, date)}/>
                    {Utils.selectFn(p.planLessonFilter.useExtraControls, () =>
                        <Row>
                            <ColFormGroup classes="col-xs-6 col-sm-4">
                                <div>
                                    <button type="button"
                                            className="btn btn-default btn-right-gap"
                                            onClick={() => self.onPlanLessonFilterChange('age', Utils.arr.toggleAll(p.planLessonFilter.age, Dictionaries.studentAge))}>
                                        {Icons.glyph.check()}
                                    </button>
                                    <Select
                                        name="age"
                                        className="field-with-right-gap-1"
                                        placeholder={Utils.message('common.lessons.search.fields.age')}
                                        multi={true}
                                        value={p.planLessonFilter.age}
                                        valueKey="id"
                                        labelKey="title"
                                        options={Dictionaries.studentAge}
                                        onChange={(val) => self.onPlanLessonFilterChange('age', val)}
                                    />
                                </div>
                            </ColFormGroup>
                            <ColFormGroup classes="col-xs-6 col-sm-4">
                                <div>
                                    <button type="button"
                                            className="btn btn-default btn-right-gap"
                                            onClick={() => self.onPlanLessonFilterChange('day', Utils.arr.toggleAll(p.planLessonFilter.day, Dictionaries.day))}>
                                        {Icons.glyph.check()}
                                    </button>
                                    <Select
                                        name="day"
                                        className="field-with-right-gap-1"
                                        placeholder={Utils.message('common.lessons.search.fields.day')}
                                        multi={true}
                                        value={p.planLessonFilter.day}
                                        valueKey="id"
                                        labelKey="title"
                                        options={Dictionaries.day}
                                        onChange={(val) => self.onPlanLessonFilterChange('day', val)}
                                    />
                                </div>
                            </ColFormGroup>
                            <ColFormGroup classes="col-xs-6 col-sm-4">
                                <div>
                                    <button type="button"
                                            className="btn btn-default btn-right-gap"
                                            onClick={() => self.onPlanLessonFilterChange('time', Utils.arr.toggleAll(p.planLessonFilter.time, Dictionaries.lessonTime))}>
                                        {Icons.glyph.check()}
                                    </button>
                                    <Select
                                        name="timeSlot"
                                        className="field-with-right-gap-1"
                                        placeholder={Utils.message('common.lessons.search.fields.time')}
                                        multi={true}
                                        value={p.planLessonFilter.time}
                                        valueKey="id"
                                        labelKey="title"
                                        options={Dictionaries.lessonTime}
                                        onChange={(val) => self.onPlanLessonFilterChange('time', val)}
                                    />
                                </div>
                            </ColFormGroup>
                            <ColFormGroup classes="col-xs-6 col-sm-4">
                                <div>
                                    <button type="button"
                                            className="btn btn-default btn-right-gap"
                                            onClick={() => self.onPlanLessonFilterChange('subject', Utils.arr.toggleAll(p.planLessonFilter.subject, Dictionaries.lessonSubject))}
                                    >
                                        {Icons.glyph.check()}
                                    </button>
                                    <Select
                                        name="subject"
                                        className="field-with-right-gap-1"
                                        placeholder={Utils.message('common.lessons.search.fields.subject')}
                                        multi={true}
                                        value={p.planLessonFilter.subject}
                                        valueKey="id"
                                        labelKey="title"
                                        options={Dictionaries.lessonSubject}
                                        onChange={(val) => self.onPlanLessonFilterChange('subject', val)}
                                    />
                                </div>
                            </ColFormGroup>
                        </Row>
                    )}
                    {self.renderComponents()}
                </div>

                <div className='panel panel-default panel-compact'>
                    <div className="container-fluid">
                        <Row>
                            <ColFormGroup classes="col-xs-12 container-pad-top-10"
                                          formGroupClasses="form-group-minimal">
                                <div className="btn-group">
                                    <InlineButton classes='btn-sm btn-default'
                                                  onClick={() => p.dispatch(Actions.setLessonsView(Utils.select(p.lessonsView === 'table', 'list', 'table')))}>
                                        {Icons.glyph.custom(Dictionaries.lessonsView.byId(p.lessonsView).icon)}
                                    </InlineButton>
                                </div>

                                {Utils.selectFn(p.lessonsView === 'table', () =>
                                    <DropdownActionButton
                                        inline={true}
                                        title={<span>{Icons.glyph.eyeOpen()}
                                            &nbsp;{Dictionaries.visitsSummary.byId(p.visitsSummary.id).title}
                                            &nbsp;{Icons.caret()}</span>}
                                        btnClasses="btn-default btn-sm"
                                        actions={Dictionaries.visitsSummary.obj()}
                                        actionFn={id => p.dispatch(Actions.setEntityValue('visitsSummary', 'visitsSummary', Dictionaries.visitsSummary.byId(id)))}/>
                                )}

                                {Utils.selectFn(p.lessonsView === 'list' && p.lessonProcedure === 'view', () =>
                                    <div className="btn-group">
                                        {Dictionaries.visitType.withAll.map(opt =>
                                            <InlineButton key={opt.id}
                                                          classes={'btn-sm ' + Utils.select(opt.id === p.planLessonFilter.visitType, 'btn-success', 'btn-default')}
                                                          onClick={() => p.dispatch(Actions.setPlanLessonFilter('visitType', opt.id))}>
                                                {opt.title}
                                            </InlineButton>
                                        )}
                                    </div>
                                )}

                                {Utils.selectFn(p.lessonProcedure === 'plan', () =>
                                    <div className="btn-group">
                                        {Dictionaries.lessonSubject.withAll.map(opt =>
                                            <InlineButton key={opt.id}
                                                          classes={'btn-sm ' + Utils.select(Utils.arr.contains(p.planLessonFilter.subject, {id: opt.id}, Utils.obj.id), 'btn-success', 'btn-default')}
                                                          onClick={() => {
                                                              if (opt.id === 'all') {
                                                                  self.onPlanLessonFilterChange('subject', Utils.arr.toggleAll(p.planLessonFilter.subject || [], Dictionaries.lessonSubject))
                                                              } else {
                                                                  self.onPlanLessonFilterChange('subject', Utils.arr.toggle(p.planLessonFilter.subject || [], opt))
                                                              }
                                                          }}>
                                                {opt.shortTitle}
                                            </InlineButton>
                                        )}
                                    </div>
                                )}
                            </ColFormGroup>
                        </Row>

                        {self.renderLessonSlots()}
                    </div>
                </div>
            </div>
        </div>
    },

    renderLessonSlots(){
        const self = this, p = self.props,
            lessons = p.planLessonFilter._result.lessons || p.currentTemplate && p.currentTemplate.lessons || {};

        if (p.entities._loading && !p.entities.templates) { //means that load is made first time
            return <div className="entities-tbl-message">{Utils.message('common.search.table.loading')}</div>
        } else {
            if (p.lessonsView === 'list') {
                return <LessonsList {...p}
                                    lessons={lessons}
                                    ref={elem => {
                                        self.lessonsListElem = elem
                                    }}
                />
            } else {
                return <LessonsGrid startDate={p.startDate}
                                    visitsSummary={p.visitsSummary}
                                    lessonProcedure={p.lessonProcedure}
                                    planLessonFilter={p.planLessonFilter}
                                    subjects={Dictionaries.lessonSubject}
                                    lessons={lessons}
                                    lessonPlans={p.entities.lessonPlans}
                                    pickedLessons={p.pickedLessons}
                                    selectedLesson={p.selectedLesson}
                                    blockedDateTimes={p.blockedDateTimes}
                                    lessonDateSpan={p.lessonDateSpan}
                                    onLessonClick={id => {
                                        p.dispatch(Actions.selectLesson(id));
                                        LessonUtils.pickLesson(p, id);
                                    }}
                                    onDeepPress={(lessonId) => {
                                        p.dispatch(Actions.selectLesson(lessonId));
                                        p.dispatch(Actions.setPageMode('lessons', 'home', 'lesson', 'studentSlots'))
                                    }}/>
            }
        }
    },

    onPlanLessonFilterChange(fieldId, value){
        const self = this, p = self.props;
        p.dispatch(Actions.setPlanLessonFilter(fieldId, value))
    },

    renderStudentSlots(){
        const self = this, p = self.props,
            lessonId = p.selectedLesson,
            subjectOpt = LessonUtils.lessonSubjectFromId(lessonId);

        if (lessonId) {
            const lessonPlan = p.entities.lessonPlans[lessonId] || Utils.lessonPlanFromId(lessonId, subjectOpt.duration);
            return <LessonsStudentList
                changeLessonStatusMessage={p.changeLessonStatusMessage}
                students={p.entities.students}
                lessonPlan={lessonPlan}
                photos={p.lessonPhotos}
                onPhotoUploaded={(fileNames) => {
                    p.dispatch(Actions.filesUploaded(fileNames, 'lessons'))
                }}
                onRemovePhoto={(fileName) => {
                    p.dispatch(Actions.ajax.lessons.removeFile(lessonId, fileName))
                }}
                onChangeShownPhoto={(fileName) => {
                    p.dispatch(Actions.changeShownFile(fileName, 'lessons'))
                }}
                shownPhotoName={p.shownPhoto}
                onVisitStudentLesson={slotId => p.dispatch(Actions.ajax.lessons.visit(lessonId, slotId))}
                onMissStudentLesson={slotId => p.dispatch(Actions.ajax.lessons.miss(lessonId, slotId))}
                onMoveStudentLesson={slotId => p.dispatch(Actions.startMoveStudentSlot(lessonId, slotId))}
                onCancelStudentLesson={(slotId, visitType) => p.dispatch(Actions.ajax.lessons.cancel(lessonId, slotId, visitType))}
                onRevokeLesson={() => LessonUtils.actions.revokeLesson(lessonId, p)}
                onCloseLesson={() => p.dispatch(Actions.ajax.lessons.close(lessonId))}
                {...p}/>
        }
    },

    renderComponents(){
        const self = this, p = self.props;
        return Utils.when(p.lessonProcedure, {
            view: self.renderViewComponents,
            plan: self.renderPlanComponents,
            unplan: self.renderPlanComponents,
            suspend: self.renderPlanComponents,
            transfer: self.renderLessonTransferComponents,
        })
    },

    renderViewComponents(){
        const self = this, p = self.props,
            procedureOptions = Dictionaries.lessonProcedure.filter(opt => opt.id !== 'view');

        return [self.renderStudentAndCardComponents(),
            <TableButtonGroup key="procedure-select"
                              options={procedureOptions}
                              renderBtnFn={opt =>
                                  <Button
                                      onClick={() => p.dispatch(Actions.setLessonProcedure(opt.id))}>{opt.title}</Button>
                              }/>
        ]
    },

    renderPlanComponents(){
        const self = this;
        return [
            self.renderStudentAndCardComponents(),
            self.renderLessonPlanToolbar()
        ]
    },

    renderLessonTransferComponents(){
        const self = this;
        return [
            self.renderStudentAndCardComponents(),
            self.renderTargetStudentSelect(),
            self.renderTransferCardSelect(),
            self.renderLessonPlanToolbar()
        ]
    },

    renderStudentAndCardComponents() {
        const self = this, p = self.props;

        return <Row key="plan-controls">
            <ColFormGroup classes="col-xs-12 high2-inputs">
                <InlineButton
                    classes={'btn-right-gap ' + Utils.select(p.planLessonFilter.useExtraControls, 'btn-success', 'btn-default')}
                    onClick={() => p.dispatch(Actions.setPlanLessonFilter('useExtraControls', !p.planLessonFilter.useExtraControls))}>
                    {Icons.glyph.menuUp()}
                </InlineButton>
                <InlineButton
                    classes={'btn-right-gap ' + Utils.select(p.planLessonFilter.usePreciseAge, 'btn-success', 'btn-default')}
                    onClick={() => p.dispatch(Actions.setPlanLessonFilter('usePreciseAge', !p.planLessonFilter.usePreciseAge))}>
                    {Icons.glyph.dashboard()}
                </InlineButton>
                <Select
                    async={true}
                    name="studentName"
                    className="field-with-right-gap-2"
                    placeholder={Utils.message('common.lessons.search.fields.studentName')}
                    value={p.planLessonFilter.student}
                    valueRenderer={s => Renderers.student.info(s)}
                    optionRenderer={s => Renderers.student.info(s)}
                    loadOptions={Utils.debounceInput(LessonUtils.loadStudents)}
                    onChange={val => {
                        const oldVal = p.planLessonFilter.student;
                        if ((oldVal && oldVal.id) !== (val && val.id)) {
                            if (val) {
                                p.dispatch(Actions.ajax.lessons.findStudentPlannedLessons(val.id));
                                p.dispatch(Actions.ajax.lessons.student(val.id)).then(data => {
                                    const foundStudent = data.results[0];
                                    p.dispatch(Actions.setPlanLessonFilter('student', foundStudent));
                                });
                            } else {
                                p.dispatch(Actions.setPlanLessonFilter('student', val));
                            }
                        }
                    }}
                />
            </ColFormGroup>
            {Utils.selectFn(p.planLessonFilter.student, () =>
                <ColFormGroup classes="lesson-plan-summary-block"
                              formGroupClasses="">
                    <LessonsCardList studentCards={LessonUtils.getActiveStudentCards(p.planLessonFilter.student)}
                                     selectedStudentCard={p.planLessonFilter.card}
                                     loading={Utils.ajax.isInProgress(p.ajaxStatuses, 'student', 'lessons')}
                                     lessonProcedure={p.lessonProcedure}
                                     onChange={val => {
                                         const oldVal = p.planLessonFilter.card,
                                             oldId = oldVal && oldVal.id,
                                             nextId = val && val.id,
                                             nextVal = oldId !== nextId ? val : null;
                                         p.dispatch(Actions.setPlanLessonFilter('card', nextVal))
                                     }}
                    />
                </ColFormGroup>
            )}
            <LessonsPlanSummary {...p}/>
        </Row>

    },

    renderTargetStudentSelect(){
        const self = this, p = self.props;
        return <Row key="transfer-target-student">
            <ColFormGroup>
                <Select
                    async={true}
                    name="targetStudent"
                    placeholder={Utils.message('common.lessons.transfer.target.student')}
                    value={p.lessonsTransfer.targetStudent}
                    valueRenderer={Renderers.student.info}
                    optionRenderer={Renderers.student.info}
                    loadOptions={Utils.debounceInput(LessonUtils.loadStudents)}
                    onChange={val => p.dispatch(Actions.setEntityValue('lessonsTransfer', 'targetStudent', val))}
                />
            </ColFormGroup>
        </Row>
    },

    renderTransferCardSelect(){
        const self = this, p = self.props;
        return <Row>
            <ColFormGroup>
                <Select
                    async={true}
                    name="transferCard"
                    placeholder={Utils.message('common.preferences.transferCard.card')}
                    value={p.lessonsTransfer.transferCard}
                    valueRenderer={Renderers.card.info.select}
                    optionRenderer={Renderers.card.info.select}
                    loadOptions={Utils.debounceInput(self.loadTransferCards)}
                    onChange={val => p.dispatch(Actions.setEntityValue('lessonsTransfer', 'transferCard', val))}
                />
            </ColFormGroup>
        </Row>
    },

    renderLessonPlanToolbar(){
        const self = this, p = self.props;
        return <LessonsPlanToolbar key="plan-toolbar"
                                   ref={elem => {
                                       self.lessonPlanToolbarElem = elem;
                                   }}
                                   {...p}/>
    },

    loadTransferCards(text, callback) {
        DataService.operations.cards.findAll({visitType: 'transfer'}).then(
            response => {
                callback(null, {
                    options: response.results
                });
            },
            error => {
                callback(error.status, null);
            });
    },

    scrollToLesson(lessonId){
        const self = this, p = self.props,
            offset = self.lessonsListElem.lessonOffset(lessonId),
            scrollAdjustmentGap = 15;

        if (offset) {
            Utils.scroll.top(offset.top - $(self.lessonPlanToolbarElem.container).outerHeight(true) - scrollAdjustmentGap)
        }
    }
});

function mapStateToProps(state) {
    let pickedLessonIds = state.pages.Lessons.pickedLessons,
        lessonStatuses = state.pages.Lessons.planLessonFilter._result.lessonStatuses,
        plannedLessonIds = lessonStatuses && Object.keys(lessonStatuses).filter(lessonId => lessonStatuses[lessonId] === 'planned'),
        blockSameTime = Dictionaries.lessonProcedure.byId(state.pages.Lessons.lessonProcedure).blockSameTime,
        blockedDateTimes = blockSameTime ? [].concat(pickedLessonIds, plannedLessonIds).filter(o => !!o).map(lessonId => Utils.lessonDateTime(lessonId)) : [];

    return Utils.extend(state.pages.Lessons, {
        ajaxStatuses: state.common.ajaxStatuses,
        blockedDateTimes: blockedDateTimes
    });
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Lessons);
