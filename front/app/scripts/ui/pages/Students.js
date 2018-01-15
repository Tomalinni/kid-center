'use strict';

const React = require('react'),
    {connect} = require('react-redux'),
    AuthService = require('../services/AuthService'),
    Permissions = require('../Permissions'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    LessonUtils = require('../LessonUtils'),
    Actions = require('../actions/Actions'),
    DialogService = require('../services/DialogService'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    PageToolbar = require('../components/PageToolbar'),
    StudentCardPaymentScreen = require('../components/StudentCardPaymentScreen'),
    PeriodsSearchGroup = require('../components/PeriodsSearchGroup'),
    DataService = require('../services/DataService'),
    DropdownActionButton = require('../components/DropdownActionButton'),
    DatePicker = require('../components/DateComponents').DatePicker,
    ImageLoader = require('react-imageloader'),
    {Row, Col, FormGroup, ColFormGroup, ProgressButton, TableButtonGroup, Button, CircleButton, Select} = require('../components/CompactGrid'),
    Icons = require('../components/Icons');

const Students = React.createClass({
    render(){
        const self = this, p = self.props;
        if (!p.studentCardPayment) {
            return self.renderListScreen()
        } else {
            return <StudentCardPaymentScreen {...p}/>
        }
    },

    renderListScreen(){
        const self = this, p = self.props;

        if (p.detailsCollapsed) {
            return <ListScreen entity='students'
                               ajaxResource={Actions.ajax.students}
                               instantSearch={true}
                               renderMenuUp={false}
                               tableBodyClasses="entities-tbl-body-high"
                               entityModifyPermssion={Permissions.studentsModify}
                               entityRouteFn={Navigator.routes.student}
                               pageTitle={Utils.message('common.pages.students')}
                               filterElementFn={self.renderFilterElement}
                               entitiesTblCols={self.columns()}
                               onClickSelected={self.onSelectedStudentClicked}
                               selectedObj={p.student}
                               {...p} />

        } else {
            return self.renderDetailsSection()
        }
    },

    componentDidMount() {
        const self = this, p = self.props;
        window.addEventListener("resize", this.updateDimensions.bind(null, this));

        let lessonId = p.location.query.lessonId;
        if (lessonId) {
            p.dispatch(Actions.setPageMode('students', 'list', 'student', 'withDetails'));
            p.dispatch(Actions.setPageMode('studentDashboard', 'form', 'tab', 'lessons'));
            p.dispatch(Actions.setEntityValues('lessonSearchRequest', {
                timeCategory: 'schedule',
                visitType: 'all',
                lessonDate: Utils.momentToString(Utils.lessonMomentFromId(lessonId))
            }));
        }
    },

    componentDidUpdate() {
        this.updateDimensions();
    },

    componentWillUnmount() {
        window.removeEventListener("resize", this.updateDimensions.bind(null, this));
    },

    /**
     * Calculate and update height for tab
     */
    updateDimensions() {
        const headerPanel = this.tabHeaderPanel,
            tabPanel = this.tabPanel;
        if (headerPanel && tabPanel) {
            tabPanel.style.paddingTop = headerPanel.offsetHeight + 11 + "px";
        }
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;
        self.onSearchRequestChange = onSearchRequestChange;

        let navigatedStudentId = p.location.query.selection;
        if (navigatedStudentId) {
            return <Row>
                <div className="selection-line">{Renderers.student.info(p.student || {id: navigatedStudentId})}
                    <CircleButton classes="btn-default pull-right"
                                  onClick={() => {
                                      Navigator.navigate(Navigator.routes.students);
                                      self.onSearchRequestChange({selection: null})
                                  }}>
                        {Icons.glyph.remove()}
                    </CircleButton>
                </div>
            </Row>
        } else {
            return <div>
                {self.renderPeriodsGroup()}
                <Row>
                    <TableButtonGroup options={Dictionaries.studentStatus.withAll} renderBtnFn={opt =>
                        <Button
                            classes={Utils.select(p.searchRequest.status === opt.id, 'btn-success', 'btn-default')}
                            onClick={() => self.onSearchRequestChange({status: opt.id})}>
                            {opt.title}
                        </Button>
                    }/>

                    <ColFormGroup classes="col-xs-6">
                        <TextInput id="searchText"
                                   name="searchText"
                                   placeholder="Search text"
                                   defaultValue={p.searchRequest.text}
                                   onChange={text => self.onSearchRequestChange({text})}/>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-6">
                        <Select
                            name="manager"
                            placeholder={Utils.message('common.students.search.table.manager')}
                            value={p.searchRequest.manager && Utils.arr.find(p.employees, {id: p.searchRequest.manager}, Utils.obj.id).obj}
                            valueRenderer={Renderers.teacher}
                            optionRenderer={Renderers.teacher}
                            options={p.employees}
                            onChange={opt => self.onSearchRequestChange({manager: opt && opt.id})}
                        />
                    </ColFormGroup>
                </Row>
            </div>

        }
    },

    refreshList(){
        const self = this, p = self.props;

        const resetSelectionObj = {selection: null};
        let searchRequest = p.searchRequest;
        if (p.searchRequest.selection) {
            Navigator.navigate(Navigator.routes.students);
            self.onSearchRequestChange(resetSelectionObj);
            searchRequest = Utils.extend(searchRequest, resetSelectionObj)
        }
        p.dispatch(Actions.ajax.students.findAll(searchRequest))
    },

    renderPeriodsGroup(){
        const self = this, p = self.props;
        return <PeriodsSearchGroup searchRequest={p.searchRequest}
                                   periodFieldId="createdDatePeriod"
                                   startDateFieldId="createdDateStart"
                                   endDateFieldId="createdDateEnd"
                                   onPeriodChange={self.onSearchRequestChange}/>
    },

    renderToggleDetailsBtn(){
        const self = this, p = self.props;
        if (!p.detailsCollapsed) {
            return <button type="button"
                           key="details"
                           className="btn btn-default btn-lg nav-toolbar-btn"
                           onClick={() => p.dispatch(Actions.setPageMode('students', 'list', 'student', Utils.select(p.detailsCollapsed, 'withDetails', 'noDetails')))}>
                {Icons.glyph.list()}
            </button>
        }
    },

    renderEditStudentBtn(){
        const self = this, p = self.props;
        if (p.student) {
            return <button type="button"
                           key="edit"
                           className="btn btn-default btn-lg nav-toolbar-btn"
                           onClick={() => Navigator.navigate(Navigator.routes.student(p.student.id))}>
                {Icons.glyph.pencil()}
            </button>
        }
    },

    renderDetailsSection(){
        const self = this, p = self.props,
            student = p.student;

        return <div>
            <PageToolbar
                leftButtons={[self.renderToggleDetailsBtn(), self.renderEditStudentBtn()]}/>

            {Utils.selectFn(student && student._found, () => {
                return <div>
                    <div className="tabs-header-wrapper"
                         ref={(tabHeaderPanel) => {
                             self.tabHeaderPanel = tabHeaderPanel;
                         }}>
                        <ul className="nav nav-tabs">

                            {Dictionaries.studentDashboardTabs.map(o => {
                                return Utils.selectFn(self.tabIsShown(o.id), () =>
                                    <li key={o.id}
                                        className={Utils.select(o.id === p.studentDashboard.pageMode, 'active', '')}>
                                        <a onClick={() => {
                                            if (p.studentDashboard.pageMode !== o.id) {
                                                p.dispatch(Actions.setPageMode('studentDashboard', 'form', 'tab', o.id));
                                                self.requestTabEntities(o.id, student.id);
                                            }
                                        }}>
                                            {o.title}
                                        </a>
                                    </li>)
                            })}
                        </ul>
                    </div>
                    <div className="with-nav-toolbar with-tabs-header-wrapper container-back-white"
                         ref={(tabPanel) => {
                             self.tabPanel = tabPanel;
                         }}>
                        {Utils.when(p.studentDashboard.pageMode, {
                            overview: self.renderOverviewTab,
                            lessons: () => self.renderIfHasPermission(self.renderLessonsTab, Permissions.lessonsRead),
                            cards: () => self.renderIfHasPermission(self.renderCardsTab, Permissions.studentCardsRead),
                            calls: () => self.renderIfHasPermission(self.renderCallsTab, Permissions.studentCallsRead),
                            stat: () => self.renderIfHasPermission(self.renderStatTab, Permissions.studentCardsRead),
                            photo: self.renderPhotoTab,
                            notifications: () => self.renderIfHasPermission(self.renderNotificationsTab, Permissions.studentsModify)
                        })}
                    </div>
                </div>
            }, () => {
                const message = Utils.select(student && student.id, Utils.message('common.search.table.loading'), Utils.message('common.student.student.not.selected'));
                return <div className="entities-tbl-message">{message}</div>
            })}
        </div>
    },

    tabIsShown(tabId){
        switch (tabId) {
            case 'lessons':
                return AuthService.hasPermission(Permissions.lessonsRead);
            case 'cards':
            case 'stat':
                return AuthService.hasPermission(Permissions.studentCardsRead);
            case 'calls':
                return AuthService.hasPermission(Permissions.studentCallsRead);
            default:
                return true
        }
    },

    renderIfHasPermission(renderFn, permission){
        if (AuthService.hasPermission(permission)) {
            return renderFn.apply(this)
        } else {
            return <div className="entities-tbl-message">{Utils.message('common.pages.no.access')}</div>
        }
    },

    renderStudentInfo(){
        const self = this, p = self.props,
            student = p.student;

        return <div className="container">
            <Row>
                <ColFormGroup classes="col-xs-2">
                    <label>{Utils.message('common.students.search.table.businessId')}</label>
                    <div>{Renderers.student.businessId(student)}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-2">
                    <label>{Utils.message('common.students.search.table.nameCn')}</label>
                    <div>{student.nameCn}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-2">
                    <label>{Utils.message('common.students.search.table.nameEn')}</label>
                    <div>{student.nameEn}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-3">
                    <label>{Utils.message('common.students.search.table.age')}</label>
                    <div>{Renderers.studentBirthDateAndAge(student)}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-3">
                    <label>{Utils.message('common.students.search.table.gender')}</label>
                    <div>{Dictionaries.studentGender.byId(student.gender).title}</div>
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup classes="col-xs-2">
                    <label>{Utils.message('common.students.search.table.promotionSource')}</label>
                    <div>{Renderers.objName(student.promotionSource)}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-2">
                    <label>{Utils.message('common.students.search.table.promotionDetails')}</label>
                    <div>{Renderers.objName(student.promotionDetail)}</div>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-8">
                    <label>{Utils.message('common.students.search.table.kinderGarden')}</label>
                    <div>{Renderers.kinderGardenName(student.kinderGarden)}</div>
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup>
                    <label>{Utils.message('common.students.search.table.comment')}</label>
                    <div>{student.comment}</div>
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup>
                    <label>{Utils.message('common.students.advises.promoter')}</label>
                    <div>{Renderers.student.link(student.promoter, Renderers.student.names(student.promoter))}</div>
                </ColFormGroup>
                <ColFormGroup>
                    <label>{Utils.message('common.students.advises.promotedStudents')}</label>
                    <div>
                        {student.promotedStudents.map(s => {
                            return <span key={s.id}
                                         className="field-enum">
                            {Renderers.student.link(s, Renderers.student.names(s))}
                        </span>
                        })}
                    </div>
                </ColFormGroup>
                <ColFormGroup>
                    <label>{Utils.message('common.students.siblings.title')}</label>
                    <table className="entities-tbl-body">
                        <tbody>
                        {(student.siblings || []).map(sibling =>
                            <tr key={sibling.id}>
                                <td className="entities-tbl-cell-left-align">
                                    {Renderers.student.info(sibling)}
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </ColFormGroup>
            </Row>
        </div>
    },

    renderStatTab(){
        const self = this, p = self.props,
            sums = p.student.lessonsSummary;

        return <div className="container">
            <div className="row">
                <table className="entities-tbl-body">
                    <thead>
                    <tr>
                        <td></td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.total')}</td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.used')}</td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.available')}</td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.expired')}</td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.planned')}</td>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(sums || {}).map(visitType => {
                        return <tr key={visitType}>
                            <td>{Renderers.dictOption(Dictionaries.visitType.byId(visitType))}</td>
                            <td>{sums[visitType].total}</td>
                            <td>{sums[visitType].used}</td>
                            <td>{sums[visitType].available}</td>
                            <td>{sums[visitType].expired}</td>
                            <td>{sums[visitType].planned}</td>
                        </tr>
                    })}
                    </tbody>
                </table>
            </div>
            <div className="row">
                <table className="entities-tbl-body">
                    <thead>
                    <tr>
                        <td></td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.visited')}</td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.missed')}</td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.canceled')}</td>
                        <td>{Utils.message('common.students.dashboard.overview.table.col.suspended')}</td>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(p.student.lessonsSummary || {}).map(visitType => {
                        return <tr key={visitType}>
                            <td>{Renderers.dictOption(Dictionaries.visitType.byId(visitType))}</td>
                            <td>{sums[visitType].visited}</td>
                            <td>{sums[visitType].missed}</td>
                            <td>{sums[visitType].canceled}</td>
                            <td>{sums[visitType].suspended}</td>
                        </tr>
                    })}
                    </tbody>
                </table>
            </div>
        </div>
    },

    renderLessonsTab(){
        const self = this, p = self.props;

        return <div className="container">
            <Row>
                {Dictionaries.studentDashboardLessonTimeCategory.map(o => {
                    return <ColFormGroup key={o.id}
                                         classes="col-xs-3">
                        <Button
                            classes={Utils.select(o.id === p.studentDashboard.lessonSearchRequest.timeCategory, 'btn-success')}
                            onClick={() => self.onLessonSearchRequestChange({timeCategory: o.id})}>
                            {o.title}
                        </Button>
                    </ColFormGroup>
                })}
                <ColFormGroup key="lessonDate"
                              classes="col-xs-6">
                    <DatePicker value={p.studentDashboard.lessonSearchRequest.lessonDate}
                                onChange={val => self.onLessonSearchRequestChange({lessonDate: val})}>
                        <Button>
                            {p.studentDashboard.lessonSearchRequest.lessonDate || Utils.message('common.payments.search.fields.period.startDate')}
                        </Button>
                    </DatePicker>
                </ColFormGroup>
            </Row>
            <Row>
                <TableButtonGroup options={Dictionaries.studentDashboardLessonVisitTypeFilter.withAll}
                                  renderBtnFn={opt =>
                                      <Button
                                          classes={Utils.select(opt.id === p.studentDashboard.lessonSearchRequest.visitType, 'btn-success')}
                                          onClick={() => self.onLessonSearchRequestChange({visitType: opt.id})}>
                                          {opt.title}
                                      </Button>
                                  }/>
            </Row>

            <div className="row">
                <table className="entities-tbl-body">
                    <tbody>
                    {self.renderLessonDays()}
                    </tbody>
                </table>
            </div>
        </div>
    },

    renderLessonDays(){
        const self = this, p = self.props;
        const rows = [];
        if (p.student.lessons) {
            Object.keys(p.student.lessons).map(date => {

                rows.push(self.renderLessonDayHeader(date));

                let dayLessonSlots = p.student.lessons[date];
                if (!dayLessonSlots || dayLessonSlots.length === 0) {
                    rows.push(self.renderEmptyDayRow(date))
                } else {
                    dayLessonSlots.forEach(slot => {
                        rows.push(self.renderDayLessonRow(date, slot))
                    })
                }
            });
        }
        return rows;
    },

    renderEmptyDayRow(date){
        const self = this, p = self.props;
        return <tr key={'row-slot-empty-' + date}>
            <td colSpan="6"
                className="text-centered entities-tbl-cell-title">
                <div className="entities-tbl-message">{Utils.message('common.table.no.items')}</div>
            </td>
        </tr>
    },

    renderLessonDayHeader(date){
        const self = this, p = self.props;
        return <tr key={'row-date-' + date}>
            <td style={{width: '60px'}}
                className="entities-tbl-cell-no-border"></td>
            <td colSpan="4"
                className="text-centered entities-tbl-cell-title entities-tbl-cell-high entities-tbl-cell-no-border">
                {Renderers.lesson.date.dayAndDate(date)}
            </td>
            <td style={{width: '60px'}}
                className="entities-tbl-cell-no-border">
                {Utils.select(p.studentDashboard.lessonSearchRequest.timeCategory === 'schedule' && p.student.cards.length,
                    <button type="button"
                            className="btn btn-default btn-row"
                            onClick={() => self.startPlanLesson(date)}>
                        {Icons.glyph.plus()}
                    </button>)}
            </td>
        </tr>
    },

    startPlanLesson(date){
        const self = this, p = self.props;
        Navigator.navigate(Navigator.routes.lessons, {selection: p.student.id, startDate: date});
    },

    renderDayLessonRow(date, slot){
        const self = this, p = self.props, extendedStudentSlot = Utils.extend(slot, {studentId: p.student.id});

        return <tr key={'row-slot-' + slot.id}>
            <td>{Utils.lessonTime(slot.lessonId)}</td>
            <td onClick={() => self.navigateToLesson(p.student.id, date, slot.lessonId)}>
                {Renderers.lesson.subject.label(LessonUtils.lessonSubjectFromId(slot.lessonId))}
            </td>
            <td>{Renderers.dictOption(Dictionaries.visitType.byId(slot.visitType))}</td>
            <td>{Renderers.lesson.studentSlotStatus(slot.lessonId, slot, p.student.presentInSchool)}</td>
            <td>{Renderers.lesson.repeatsLeft(slot.repeatsLeft, slot.status !== 'planned')}</td>
            <td>
                <DropdownActionButton
                    btnClasses="btn-default btn-row"
                    actions={LessonUtils.availableStudentSlotActions(slot.lessonId, extendedStudentSlot, p.student.presentInSchool)}
                    dispatch={p.dispatch}/>
            </td>
        </tr>
    },

    navigateToLesson(studentId, date, lessonId){
        Navigator.navigate(Navigator.routes.lessons, {
            selection: studentId,
            startDate: date,
            lessonId: lessonId
        })
    },

    renderCardsTab(){
        const self = this, p = self.props,
            cards = p.student.cards || [],
            card = Utils.isDefined(p.selectedCardIndex) && cards[p.selectedCardIndex];

        return <div>
            <div className="container">
                <Row>
                    <Col>
                        <FormGroup classes="btn-toolbar">
                            <div className="btn-group">
                                <button type="button" className="btn btn-default"
                                        onClick={() => p.dispatch(Actions.setPageMode('studentCard', 'studentCard', 'card', 'studentCardPayment'))}>
                                    {Utils.message('button.new')}
                                </button>
                                {Utils.selectFn(card, () => [
                                    <button type="button"
                                            key="edit"
                                            className="btn btn-default"
                                            onClick={() => Navigator.navigate(Navigator.routes.studentCard(p.student.id, card.id))}>
                                        {Utils.message('button.edit')}
                                    </button>,
                                    <button type="button"
                                            key="delete"
                                            className="btn btn-default"
                                            onClick={() => DialogService.confirmDelete().then(() => {
                                                p.dispatch(Actions.ajax.studentCards.delete(card.id))
                                            })}>
                                        {Utils.message('button.delete')}
                                    </button>
                                ])}
                            </div>

                            <div className="btn-group">
                                {cards.map((card, i) => {
                                    return <button type="button"
                                                   key={'c' + i}
                                                   className={'btn ' + Utils.select(i === p.selectedCardIndex, 'btn-primary', 'btn-default')}
                                                   onClick={() => p.dispatch(Actions.selectEntity('studentCards', i, 'students', 'list'))}>
                                        {Dictionaries.visitType.byId(card.visitType).title}
                                    </button>
                                })}
                            </div>
                        </FormGroup>
                    </Col>
                </Row>
            </div>
            {self.renderSelectedCard()}
        </div>
    },

    renderSelectedCard(){
        const self = this, p = self.props,
            cards = p.student.cards || [],
            card = cards[p.selectedCardIndex];

        if (card) {
            return <div className="container">
                <Row>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.student.card.table.ageRange')}</label>
                        <div>{Dictionaries.ageRange.byId(card.ageRange).title}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-8">
                        <label>{Utils.message('common.student.card.table.allowedSubjects')}</label>
                        <div>{Renderers.arr(Utils.bitmaskToArrayItems(card.allowedSubjectsMask, Dictionaries.lessonSubject).map(opt => opt.title), ', ')}</div>
                    </ColFormGroup>
                </Row>
                <Row>
                    <ColFormGroup classes="col-xs-2">
                        <label>{Utils.message('common.student.card.table.visit.type')}</label>
                        <div>{Dictionaries.visitType.byId(card.visitType).title}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.student.card.table.price')}</label>
                        <div>{self.renderCardPrice(card)}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-3">
                        <label>{Utils.message('common.student.card.table.active.period')}</label>
                        <div>{Renderers.student.card.expirationDate(card)}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-3">
                        <label>{Utils.message('common.student.card.form.purchase.date')}</label>
                        <div>{card.purchaseDate}</div>
                    </ColFormGroup>
                </Row>
                <Row>
                    <ColFormGroup classes="col-xs-2">
                        <label>{Utils.message('common.student.card.table.lessons')}</label>
                        <div>{Renderers.student.card.lessons(card)}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.student.card.table.cancels')}</label>
                        <div>{Renderers.student.card.cancels(card)}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.student.card.table.miss')}</label>
                        <div>{Renderers.student.card.miss(card)}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-2">
                        <label>{Utils.message('common.student.card.table.suspends')}</label>
                        <div>{Renderers.student.card.suspends(card)}</div>
                    </ColFormGroup>
                </Row>
            </div>
        } else if (cards.length) {
            return <div
                className="entities-tbl-message">{Utils.message('common.student.tab.overview.select.card')}</div>
        }
    },

    renderCardPrice(card){
        const self = this, p = self.props,
            cardPrice = card.price || 0;

        if (card.payment && card.payment.id) {
            return <a
                onClick={Utils.invokeAndPreventDefaultFactory(() => Navigator.navigate(Navigator.routes.payment(card.payment.id)))}>{cardPrice}</a>
        } else if (cardPrice > 0) {
            return <span>
                {cardPrice} &nbsp;
                <a onClick={Utils.invokeAndPreventDefaultFactory(() => p.dispatch(Actions.setPageMode('studentCard', 'studentCard', 'card', 'studentCardPayment', {studentCard: card})))}>
                    {Utils.message('common.student.card.add.payment')}
                </a>
            </span>
        } else {
            return cardPrice
        }
    },

    renderCallsTab(){
        const self = this, p = self.props;

        return <div>
            <div className="container">
                <Row>
                    <ColFormGroup>
                        <div className="btn-group">
                            <button type="button" className="btn btn-default"
                                    onClick={() => Navigator.navigate(Navigator.routes.studentCall(p.student.id, 'new'))}>
                                {Utils.message('button.new')}
                            </button>
                            {Utils.selectFn(p.studentCall.id, () =>
                                [
                                    <button type="button"
                                            key="edit"
                                            className="btn btn-default"
                                            onClick={() => Navigator.navigate(Navigator.routes.studentCall(p.student.id, p.studentCall.id))}>
                                        {Utils.message('button.edit')}
                                    </button>,
                                    <button type="button"
                                            key="delete"
                                            className="btn btn-default"
                                            onClick={() => DialogService.confirmDelete().then(() => {
                                                p.dispatch(Actions.ajax.studentCalls.delete(p.studentCall.id))
                                            })}>
                                        {Utils.message('button.delete')}
                                    </button>
                                ])}
                        </div>
                    </ColFormGroup>
                </Row>
            </div>
            <table className="entities-tbl-body">
                <thead>
                <tr>
                    <td className="w85px">{Utils.message('common.student.call.form.date')}</td>
                    <td>{Utils.message('common.student.call.form.method')}</td>
                    <td>{Utils.message('common.student.call.form.result')}</td>
                    <td>{Utils.message('common.student.call.form.employee')}</td>
                </tr>
                </thead>
                <tbody>
                {(p.student.calls || []).map(self.renderStudentCallRowGroup)}
                </tbody>
            </table>
        </div>
    },

    renderStudentCallRowGroup(call) {
        const self = this, p = self.props;
        const rowClassName = Utils.select(p.studentCall.id === call.id, 'entities-tbl-row-selected', 'entities-tbl-row'),
            onClickFn = () => {
                if (p.studentCall.id !== call.id) {
                    p.dispatch(Actions.toggleSelectedObject('studentCalls', call));
                }
            };

        const row =
            <tr key={call.id + '-1'}
                className={rowClassName}
                onClick={onClickFn}>
                <td>{Utils.mt.convert.format(call.date, Config.dateTimeFormat, Config.dayMonthDateTimeFormat)}</td>
                <td>{call.method && Dictionaries.studentCallMethod.byId(call.method).title}</td>
                <td>{call.result && Dictionaries.studentCallResult.byId(call.result).title}</td>
                <td>{Renderers.teacher(call.employee)}</td>
            </tr>;
        const secondaryRow = Utils.select(call.comment,
            <tr key={call.id + '-2'}
                className={rowClassName}
                onClick={onClickFn}>
                <td colSpan={4}>
                    {call.comment}
                </td>
            </tr>);
        const spacingRow = Utils.select(call.comment,
            <tr key={call.id + '-3'}
                className={rowClassName}
                onClick={onClickFn}>
                <td colSpan={4}
                    className="entities-tbl-spacing-row-cell">
                </td>
            </tr>);

        return Utils.degradeArray([row, secondaryRow, spacingRow])
    },

    renderEditStudentCallButton(call){
        const self = this, p = self.props;
        return <div className="btn-group">
            <button type="button"
                    data-toggle="dropdown"
                    className="btn btn-default btn-row dropdown-toggle">
                {Icons.glyph.cog()}
            </button>
            <ul className="dropdown-menu dropdown-menu-right">
                <li><a onClick={() => Navigator.navigate(Navigator.routes.studentCall(p.student.id, call.id))}>
                    {Utils.message('button.edit')}
                </a></li>
                <li><a onClick={() => p.dispatch(Actions.ajax.studentCalls.delete(call.id))}>
                    {Utils.message('button.delete')}
                </a></li>
            </ul>
        </div>
    },

    renderOverviewTab(){
        const self = this, p = self.props,
            relatives = p.student.relatives || [];

        return <div>
            {self.renderStudentInfo()}
            <div className="container">
                <Row>
                    <ColFormGroup>
                        <div className="btn-group">
                            {relatives.map((rel, i) => {
                                return <button type="button"
                                               key={'r' + i}
                                               className={'btn ' + Utils.select(i === p.selectedRelativeIndex, 'btn-primary', 'btn-default')}
                                               onClick={() => p.dispatch(Actions.selectEntity('relatives', i, 'students', 'list'))}>
                                    {Renderers.student.relative.roleAndName(rel)}
                                </button>
                            })}
                        </div>
                    </ColFormGroup>
                </Row>
            </div>
            {self.renderSelectedRelative()}
        </div>
    },

    renderSelectedRelative(){
        const self = this, p = self.props,
            relatives = p.student.relatives || [],
            relative = relatives[p.selectedRelativeIndex];

        if (relative) {
            return <div className="container">
                <Row>
                    <ColFormGroup classes="col-xs-2">
                        <label>{Utils.message('common.students.relative.search.table.role')}</label>
                        <div>{relative.role}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-2">
                        <label>{Utils.message('common.students.relative.search.table.name')}</label>
                        <div>{relative.name}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.students.relative.search.table.mail')}</label>
                        <div>{relative.mail}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.students.relative.search.table.driverLicense')}</label>
                        <div>{relative.driverLicense}</div>
                    </ColFormGroup>
                </Row>
                <Row>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.students.relative.search.table.passport')}</label>
                        <div>{relative.passport}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.students.relative.search.table.mobile')}</label>
                        <div>
                            <span
                                className="icon-btn-rpad">{Utils.select(relative.mobileConfirmed, Icons.glyph.ok('icon-btn-success'), Icons.glyph.warningSign('icon-btn-warning'))}</span>
                            {relative.mobile}
                        </div>
                    </ColFormGroup>
                </Row>
            </div>
        } else if (relatives.length) {
            return <div
                className="entities-tbl-message">{Utils.message('common.student.tab.overview.select.relative')}</div>
        }

    },

    renderPhotoTab(){
        const self = this, p = self.props;

        return <div className="container text-centered">
            {self.getImageOrNoPhoto()}
        </div>
    },

    renderNotificationsTab(){
        const self = this, p = self.props,
            studentId = p.student.id;
        if (p.student.relatives.length) {
            return <div className="container">
                {p.student.relatives.map((obj, index) => {
                    return self.renderRelativeNotificationsList(obj, index)
                })}
                <ProgressButton className="btn btn-default btn-wide"
                                onClick={() => {
                                    const relatives = p.student.relatives.map(relative => self.mapToRelativeDto(relative));
                                    return p.dispatch(Actions.ajax.students.relatives.saveNotifications(studentId, relatives))
                                }}>
                    {Utils.message('button.save')}
                </ProgressButton>
            </div>

        } else {
            return <div className="entities-tbl-message">{Utils.message('common.students.relative.no.relatives')}</div>
        }
    },

    mapToRelativeDto(relative) {
        return {
            id: relative.id,
            emailNotifications: relative.emailNotifications,
            mobileNotifications: relative.mobileNotifications
        }
    },

    renderRelativeNotificationsList(relative, relativeIndex) {
        const self = this, p = self.props;
        return <Row key={relative.id}>
            <ColFormGroup classes="col-md-12 text-center">
                {Renderers.student.relative.roleAndName(relative)}
            </ColFormGroup>
            <ColFormGroup classes="col-xs-6">
                <label>{Utils.message('common.student.tab.notifications.fields.emailNotifications')}</label>
                <Select
                    name="emailNotifications"
                    placeholder={Utils.message('common.student.tab.notifications.fields.emailNotifications')}
                    multi={true}
                    value={relative.emailNotifications}
                    valueKey="id"
                    labelKey="title"
                    options={Dictionaries.emailNotifications}
                    onChange={(val) => {
                        p.dispatch(Actions.setEntityValue("relativeNotifications", "emailNotifications", val ? val.map(a => a.id) : val, {relativeIndex: relativeIndex}))
                    }}
                />
            </ColFormGroup>
            <ColFormGroup classes="col-xs-6">
                <label>{Utils.message('common.student.tab.notifications.fields.mobileNotifications')}</label>
                <Select
                    name="mobileNotifications"
                    placeholder={Utils.message('common.student.tab.notifications.fields.mobileNotifications')}
                    multi={true}
                    value={relative.mobileNotifications}
                    valueKey="id"
                    labelKey="title"
                    options={Dictionaries.mobileNotifications}
                    onChange={(val) => {
                        p.dispatch(Actions.setEntityValue("relativeNotifications", "mobileNotifications", val ? val.map(a => a.id) : val, {relativeIndex: relativeIndex}))
                    }}
                />
            </ColFormGroup>
        </Row>;
    },

    getImageOrNoPhoto() {
        const self = this, p = self.props;
        if (p.student.photoName) {
            return <ImageLoader
                src={DataService.urls.students.downloadPhoto(p.student.id, p.student.photoName)}
                imgProps={{
                    className: 'img-thumbnail',
                    style: {maxWidth: '100%', maxHeight: '400px'}
                }}
                preloader={Icons.spinner}>
                {Utils.message('common.image.load.failed')}
            </ImageLoader>
        } else {
            return <div>{Utils.message('common.student.tab.photo.absent.label')}</div>
        }
    },

    onSelectedStudentClicked(student){
        const self = this, p = self.props,
            id = student.id;
        p.dispatch(Actions.setPageMode('students', 'list', 'student', 'withDetails'));
        p.dispatch(Actions.ajax.studentDashboard.findOne(id)).then(
            () => self.requestTabEntities(p.studentDashboard.pageMode, id)
        );
    },

    requestTabEntities(pageMode, studentId){
        const self = this, p = self.props;
        if (pageMode === 'lessons') {
            p.dispatch(Actions.ajax.studentDashboard.findLessons(Utils.extend({id: studentId}, p.studentDashboard.lessonSearchRequest)));
        } else if (pageMode === 'calls') {
            p.dispatch(Actions.ajax.studentCalls.findAll({student: {id: studentId}}))
        }
    },

    onLessonSearchRequestChange(request){
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValues('lessonSearchRequest', request));
        p.dispatch(Actions.ajax.studentDashboard.findLessons(Utils.extend({id: p.student.id}, p.studentDashboard.lessonSearchRequest, request)));
    },

    confirmedMobileCellClassName(obj){
        return (obj && obj.mobileConfirmed) ? 'cell-right-border-success' : 'cell-right-border-danger'
    },

    genderClassName(obj){
        return Utils.select(obj.gender === 'boy', 'cell-border-gender-boy', 'cell-border-gender-girl');
    },

    columns(){
        const self = this, p = self.props,
            businessIdCol = Utils.cols.col(Utils.message('common.students.search.table.businessId'), Utils.obj.key('businessId'), '45px', self.confirmedMobileCellClassName, 'businessId'),
            createdDateCol = Utils.cols.col(Utils.message('common.students.search.table.createdDate'), obj => Utils.mt.convert.format(obj.createdDate, Config.dateTimeFormat, Config.dayMonthDateFormat), '40px', null, 'createdDate'),
            nameCol = Utils.cols.col(Utils.message('common.students.search.table.nameCn'), Utils.obj.key('nameCn'), '55px'),
            ageCol = Utils.cols.col(Utils.message('common.students.search.table.age'), obj => Utils.studentAgeYearMonths(obj.birthDate, true), '40px', self.genderClassName),
            primaryRelativeNameCol = Utils.cols.col(Utils.message('common.students.search.table.primaryRelative'), obj => obj.primaryRelativeName, '50px'),
            promotionSourceCol = Utils.cols.col(Utils.message('common.students.search.table.promotionSource'), obj => Renderers.objName(obj.promotionSource), '35px'),
            promotionDetailCol = Utils.cols.col(Utils.message('common.students.search.table.promotionDetails'), obj => Renderers.objName(obj.promotionDetail), '35px'),
            managerCol = Utils.cols.col(Utils.message('common.students.search.table.manager'), obj => Renderers.teacher(obj.manager), '35px'),
            paidLessonsCountCol = Utils.cols.col(Utils.message('common.students.search.table.paidLessonsCount'), Utils.obj.key('paidLessonsCount'), '40px'),
            bonusLessonsCountCol = Utils.cols.col(Utils.message('common.students.search.table.bonusLessonsCount'), Utils.obj.key('bonusLessonsCount'), '40px'),
            availableLessonsCountCol = Utils.cols.col(Utils.message('common.students.search.table.availableLessonsCount'), Utils.obj.key('availableLessonsCount'), '40px'),
            lastCardValidDateCol = Utils.cols.col(Utils.message('common.students.search.table.lastCardValidDate'), obj => Utils.daysLeftBeforeOrZero(Utils.momentFromString(obj.lastCardValidDate)), '40px'),
            promotedStudentCountCol = Utils.cols.col(Utils.message('common.students.search.table.promotedStudentCount'), obj => obj.promotedStudentCount, '40px'),
            nextLessonsColFactory = (lessonsCount, width) => Utils.cols.col(Utils.message('common.students.search.table.nextLessons'), obj => obj.nextLessons.slice(0, lessonsCount).map(Renderers.lesson.idAsDaysBefore), width);

        const allCols = [businessIdCol, createdDateCol, nameCol, ageCol, promotionSourceCol, promotionDetailCol, promotedStudentCountCol, managerCol],
            cardPaidCols = [businessIdCol, nameCol, ageCol, paidLessonsCountCol, bonusLessonsCountCol, availableLessonsCountCol, lastCardValidDateCol, nextLessonsColFactory(2, '40px')],
            registeredCols = [businessIdCol, createdDateCol, ageCol, nameCol, primaryRelativeNameCol, promotionSourceCol, promotionDetailCol, managerCol],
            trialCols = [businessIdCol, nameCol, ageCol, promotionSourceCol, promotionDetailCol, nextLessonsColFactory(4, '90px'), managerCol];

        return Utils.when(p.searchRequest.status, {
            cardPaid: () => cardPaidCols,
            registered: () => registeredCols,
            lessonPlanned: () => trialCols,
            lessonVisited: () => trialCols,
            trialEnd: () => trialCols,
            all: () => allCols

        }, () => allCols)
    },

});

function mapStateToProps(state) {
    return Utils.extend(state.pages.Students, {
        schools: state.pages.Schools.entities,
        schoolsMap: state.pages.Schools.entitiesMap,
        accounts: state.pages.Accounts.entities,
        accountsMap: state.pages.Accounts.entitiesMap,
        employees: state.pages.Teachers.entities
    });
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Students);
