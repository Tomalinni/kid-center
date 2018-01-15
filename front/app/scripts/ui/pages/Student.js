'use strict';

const React = require('react'),
    {connect} = require('react-redux'),
    ResizeSensor = require('css-element-queries/src/ResizeSensor'),

    AuthService = require('../services/AuthService'),
    Dictionaries = require('../Dictionaries'),
    Renderers = require('../Renderers'),
    Navigator = require('../Navigator'),
    DataService = require('../services/DataService'),
    DialogService = require('../services/DialogService'),
    Actions = require('../actions/Actions'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    AgeSelector = require('../components/DateComponents').AgeSelector,
    PhotosList = require('../components/PhotosList'),
    AuditHeader = require('../components/AuditHeader'),
    StudentCardForm = require('../components/StudentCardForm'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    ConfirmPhoneInput = require('../components/ConfirmPhoneInput'),
    {Row, Col, FormGroup, ColFormGroup, ProgressButton, MenuItem, Select} = require('../components/CompactGrid'),
    EntityLifeCyclePanel = require('../components/EntityLifeCyclePanel'),
    SelectSiblingsScreen = require('../components/SelectSiblingsScreen'),
    Validators = require('../Validators'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const Student = React.createClass({
    propTypes: {
        params: PropTypes.object,
        student: PropTypes.object,
        selectedRelativeIndex: PropTypes.number,
        shownFileName: PropTypes.string,
        smsVerificationCode: PropTypes.string,
        shownRelativeFileName: PropTypes.string,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;

        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId && Utils.isValidNumberId(id)) {
            p.dispatch(Actions.ajax.students.findOne(id));
        } else if (isNewId) {
            p.dispatch(Actions.initEntity('students'));
        }
        p.dispatch(Actions.clearValidationMessages('students'));
    },

    render(){
        const self = this, p = self.props;
        return Utils.when(p.pageMode, {
            edit: self.renderEditScreen,
            selectSibling: self.renderSelectSiblingScreen
        })
    },

    renderEditScreen(){
        const self = this, p = self.props;
        return <div>
            <PageToolbar leftButtons={[self.renderBackBtn(), self.renderSaveBtn()]}/>
            <h4 className="page-title">{Renderers.student.info(p.student)}</h4>
            <AuditHeader auditable={p.student}/>
            <EntityLifeCyclePanel {...p}
                                  entity="students"
                                  renderChildrenFn={self.renderForm}/>
        </div>
    },

    renderSelectSiblingScreen(){
        const self = this, p = self.props;
        return <SelectSiblingsScreen
            ownerStudent={p.student}
            onSiblingSelected={self.addSibling}
            onBack={() => p.dispatch(Actions.setPageMode('students', 'form', 'siblings', 'edit'))}/>
    },

    getSelectedRelative() {
        const self = this, p = self.props;
        return self.getStudentSelectedRelative(p.student, p.selectedRelativeIndex)
    },
    getSelectedPrevRelative(){
        const self = this, p = self.props;
        return self.getStudentSelectedRelative(p._student, p.selectedRelativeIndex)
    },
    getStudentSelectedRelative(student, selectedIndex){
        return student && student.relatives ? student.relatives[selectedIndex] : undefined;
    },

    renderForm () {
        const self = this, p = self.props,
            isNew = Utils.isNewId(p.params.id),
            inputsColClassName = isNew ? 'col-md-12' : 'col-md-8';

        return <div className="container with-nav-toolbar high-inputs container-pad-top-10">
            <Row>
                {self.tryRenderPhotoRow()}
                <Col classes={inputsColClassName}>
                    <Row>
                        <Col>
                            <ValidationMessage message={self.getValidationMessage('students', '_')}/>
                        </Col>
                        <ColFormGroup classes="col-xs-6">
                            <TextInput id="businessId"
                                       owner={p.student}
                                       name="businessId"
                                       classNames={['form-control-transparent']}
                                       placeholder={Utils.message('common.students.search.table.businessId')}
                                       defaultValue={p.student.businessId}
                                       readOnly={true}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('students', 'nameCn')}/>
                            <TextInput id="nameCn"
                                       owner={p.student}
                                       name="nameCn"
                                       placeholder={Utils.message('common.students.search.table.nameCn.full')}
                                       defaultValue={p.student.nameCn}
                                       onChange={self.onFieldChange.bind(this, 'nameCn')}/>
                        </ColFormGroup>
                    </Row>
                    <Row>
                        <ColFormGroup classes="col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('students', 'nameEn')}/>
                            <TextInput id="nameEn"
                                       owner={p.student}
                                       name="nameEn"
                                       placeholder={Utils.message('common.students.search.table.nameEn.full')}
                                       defaultValue={p.student.nameEn}
                                       onChange={self.onFieldChange.bind(this, 'nameEn')}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('students', 'birthDate')}/>
                            <AgeSelector minAge={Math.floor(Config.minStudentAgeYears)}
                                         maxAge={Math.ceil(Config.maxStudentAgeYears)}
                                         value={p.student.birthDate}
                                         onChange={self.onFieldChange.bind(this, 'birthDate')}/>
                        </ColFormGroup>
                    </Row>
                    <Row>
                        <ColFormGroup classes="col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('students', 'gender')}/>
                            <Select
                                name="gender"
                                placeholder={Utils.message('common.students.search.table.gender')}
                                value={Dictionaries.studentGender.byId(p.student.gender)}
                                valueRenderer={Renderers.dictOption}
                                optionRenderer={Renderers.dictOption}
                                options={Dictionaries.studentGender}
                                onChange={self.onOptionChanged.bind(this, self.onFieldChange.bind(this, 'gender'))}
                            />
                        </ColFormGroup>
                        <ColFormGroup classes="col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('students', 'manager')}/>
                            <Select
                                name="manager"
                                placeholder={Utils.message('common.students.search.table.manager')}
                                value={p.student.manager}
                                valueRenderer={Renderers.teacher}
                                optionRenderer={Renderers.teacher}
                                options={p.employees}
                                onChange={opt => self.onFieldChange('manager', opt)}
                            />
                        </ColFormGroup>
                        <ColFormGroup classes="col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('students', 'promotionSource')}/>
                            <button type="button"
                                    className="btn btn-default dropdown-toggle btn-right-gap"
                                    data-toggle="dropdown"
                                    aria-haspopup="true"
                                    aria-expanded="false">
                                {Icons.glyph.cog()}
                            </button>
                            <ul className="dropdown-menu dropdown-menu-right">
                                <MenuItem
                                    onClick={() => self.editPromotionSource(null)}>{Utils.message('button.new')}</MenuItem>
                                {Utils.selectFn(p.student.promotionSource, () =>
                                    <MenuItem
                                        onClick={() => self.editPromotionSource(p.student.promotionSource)}>{Utils.message('button.edit')}</MenuItem>)}
                                {Utils.selectFn(p.student.promotionSource, () =>
                                    <MenuItem
                                        onClick={() => self.deletePromotionSource(p.student.promotionSource)}>{Utils.message('button.delete')}</MenuItem>)}
                            </ul>
                            <Select
                                name="promotionSource"
                                placeholder={Utils.message('common.students.search.table.promotionSource')}
                                className="field-with-right-gap-1"
                                value={p.student.promotionSource}
                                valueRenderer={Renderers.objName}
                                optionRenderer={Renderers.objName}
                                options={p.promotionSources}
                                onChange={(opt) => {
                                    self.onFieldChange('promotionSource', opt)
                                }}
                            />
                        </ColFormGroup>
                        <ColFormGroup classes="col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('students', 'promotionDetail')}/>
                            <button type="button"
                                    className="btn btn-default dropdown-toggle btn-right-gap"
                                    disabled={!p.student.promotionSource}
                                    data-toggle="dropdown"
                                    aria-haspopup="true"
                                    aria-expanded="false">
                                {Icons.glyph.cog()}
                            </button>
                            <ul className="dropdown-menu dropdown-menu-right">
                                <MenuItem
                                    onClick={() => self.editPromotionDetail(null)}>{Utils.message('button.new')}</MenuItem>
                                {Utils.selectFn(p.student.promotionDetail, () =>
                                    <MenuItem
                                        onClick={() => self.editPromotionDetail(p.student.promotionDetail)}>{Utils.message('button.edit')}</MenuItem>)}
                                {Utils.selectFn(p.student.promotionDetail, () =>
                                    <MenuItem
                                        onClick={() => self.deletePromotionDetail(p.student.promotionDetail)}>{Utils.message('button.delete')}</MenuItem>)}
                            </ul>
                            <Select
                                name="promotionDetail"
                                placeholder={Utils.message('common.students.search.table.promotionDetails')}
                                className="field-with-right-gap-1"
                                value={p.student.promotionDetail}
                                valueRenderer={Renderers.objName}
                                optionRenderer={Renderers.objName}
                                options={p.student.promotionSource ? p.promotionDetails.filter(d => d.promotionSource.id === p.student.promotionSource.id) : []}
                                onChange={(opt) => {
                                    self.onFieldChange('promotionDetail', opt)
                                }}
                            />
                        </ColFormGroup>
                        {Utils.selectFn(p.student.promotionSource && p.student.promotionSource.hasPromoter, () =>
                            <ColFormGroup classes="col-xs-6">
                                <ValidationMessage message={self.getValidationMessage('students', 'promoter')}/>
                                <Select
                                    async={true}
                                    name="promoter"
                                    placeholder={Utils.message('common.students.search.table.promoter')}
                                    value={p.student.promoter}
                                    valueRenderer={Renderers.student.info}
                                    optionRenderer={Renderers.student.info}
                                    cache={false}
                                    loadOptions={Utils.debounceInput((text, cb) =>
                                        Utils.loadSelectOptions(DataService.operations.students.findAll({text}), cb))}
                                    onChange={(opt) => {
                                        self.onFieldChange('promoter', opt)
                                    }}/>
                            </ColFormGroup>
                        )}
                    </Row>
                </Col>
            </Row>


            <h4>{Utils.message('common.students.relatives.title')}</h4>

            <Row>
                <Col>
                    <FormGroup classes="btn-toolbar">
                        <div className="btn-group">
                            <button type="button" className="btn btn-default"
                                    onClick={() => {
                                        p.dispatch(Actions.addStudentRelative())
                                    }}>
                                {Utils.message('button.add')}
                            </button>
                            <button type="button" className="btn btn-default"
                                    onClick={() => {
                                        DialogService.confirmDelete().then(() => {
                                            p.dispatch(Actions.removeStudentRelative())
                                        })
                                    }}>
                                {Utils.message('button.remove')}
                            </button>
                        </div>

                        <div className="btn-group">
                            {(p.student.relatives || []).map((rel, i) => {
                                return <button type="button"
                                               key={'r' + i}
                                               className={'btn ' + Utils.select(i === p.selectedRelativeIndex, 'btn-primary', 'btn-default')}
                                               onClick={() => p.dispatch(Actions.selectEntity('relatives', i, 'students', 'form'))}>
                                    {rel.role || Utils.message('common.students.relative.prefix') + (i + 1)}
                                    {Renderers.validation.iconForArrIndex(p.validationMessages.relatives, i)}
                                </button>
                            })}
                        </div>
                    </FormGroup>
                </Col>
            </Row>
            {self.renderSelectedRelative()}

            <Row>
                <ColFormGroup>
                    <Select
                        async={true}
                        name="kinderGarden"
                        placeholder={Utils.message('common.students.search.table.kinderGarden')}
                        value={p.student.kinderGarden}
                        valueRenderer={Renderers.kinderGarden}
                        optionRenderer={Renderers.kinderGarden}
                        cache={false}
                        loadOptions={Utils.debounceInput((text, cb) =>
                            Utils.loadSelectOptions(DataService.operations.kindergardens.findAll(text), cb))}
                        onChange={(opt) => {
                            self.onFieldChange('kinderGarden', opt)
                        }}/>
                </ColFormGroup>
            </Row>
            <Row>
                <ColFormGroup>
                    <TextInput id="comment"
                               multiline={true}
                               rows={2}
                               owner={p.student}
                               name="comment"
                               placeholder={Utils.message('common.students.search.table.comment')}
                               defaultValue={p.student.comment}
                               onChange={self.onFieldChange.bind(this, 'comment')}/>
                </ColFormGroup>
            </Row>

            <h4>{Utils.message('common.students.siblings.title')}</h4>
            <Row>
                <Col>
                    <FormGroup classes="btn-toolbar">
                        <div className="btn-group">
                            <button type="button" className="btn btn-default"
                                    onClick={() => {
                                        p.dispatch(Actions.setPageMode('students', 'form', 'siblings', 'selectSibling'));
                                    }}>
                                {Utils.message('button.add')}
                            </button>
                            {Utils.selectFn(p.selectedSiblingId, () => {
                                return <button type="button" className="btn btn-default"
                                               onClick={self.removeSibling}>
                                    {Utils.message('button.remove')}
                                </button>
                            })}
                        </div>
                    </FormGroup>
                </Col>
            </Row>
            <Row>
                <table className="entities-tbl-body">
                    <tbody>
                    {(p.student.siblings || []).map(sibling =>
                        <tr key={sibling.id}
                            className={Utils.select(sibling.id === p.selectedSiblingId, 'entities-tbl-row-selected')}>
                            <td className="entities-tbl-cell-left-align"
                                onClick={() => p.dispatch(Actions.selectEntity('siblings', sibling.id, 'students', 'student'))}>
                                {Renderers.student.info(sibling)}
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </Row>
        </div>
    },

    renderSaveBtn(){
        const self = this, p = self.props;
        return <ProgressButton key="save"
                               disabled={self.isSaveDisabled()}
                               className="btn btn-default btn-lg nav-toolbar-btn"
                               onClick={() => self.onSave()}>
            {Icons.glyph.save()}
        </ProgressButton>

    },

    editPromotionSource(promotionSource){
        const self = this, p = self.props;

        let {name, hasPromoter} = promotionSource || {};
        return DialogService.modal({
            title: Utils.message('common.students.form.edit.promotion.source'),
            content: <Row>
                <ColFormGroup>
                    <label htmlFor="promotionSourceName">{Utils.message('common.promotion.source.form.name')}</label>
                    <input type="text"
                           id="promotionSourceName"
                           className="form-control"
                           placeholder={Utils.message('common.promotion.source.form.name')}
                           defaultValue={name || ''}
                           onBlur={e => {
                               name = e.target.value;
                           }}
                    />
                </ColFormGroup>
                <ColFormGroup>
                    <input type="checkbox"
                           id="promotionSourceHasPromoter"
                           className="hide-checkbox-input"
                           defaultChecked={hasPromoter || false}
                           onChange={e => {
                               hasPromoter = e.target.checked;
                           }}
                    />
                    <label htmlFor="promotionSourceHasPromoter"
                           className="hide-checkbox-label">{Utils.message('common.promotion.source.form.hasPromoter')}</label>
                </ColFormGroup>
            </Row>,
            buttons: {
                'cancel': {
                    label: Utils.message('button.cancel'),
                    reject: null
                },
                'ok': {
                    label: Utils.message('button.ok'),
                    resolve: () => ({name, hasPromoter})
                }
            }
        }).then(() => {
            p.dispatch(Actions.ajax.promotionSources.save({
                id: promotionSource && promotionSource.id || null,
                name, hasPromoter
            })).then((action) => {
                p.dispatch(Actions.setEntityValue('students', 'promotionSource', Utils.extend(action.request, action.response)))
            })
        });
    },

    editPromotionDetail(promotionDetail){
        const self = this, p = self.props;

        let name;
        return DialogService.modal({
            title: Utils.message('common.students.form.edit.promotion.detail'),
            content: <Row>
                <ColFormGroup>
                    <label for="name">{Utils.message('common.promotion.detail.form.name')}</label>
                    <input type="text"
                           name="name"
                           className="form-control"
                           placeholder={Utils.message('common.promotion.detail.form.name')}
                           defaultValue={promotionDetail && promotionDetail.name || ''}
                           onBlur={e => {
                               name = e.target.value;
                           }}
                    />
                </ColFormGroup>
            </Row>,
            buttons: {
                'cancel': {
                    label: Utils.message('button.cancel'),
                    reject: null
                },
                'ok': {
                    label: Utils.message('button.ok'),
                    resolve: () => name
                }
            }
        }).then(() => {
            if (p.student.promotionSource) {
                p.dispatch(Actions.ajax.promotionDetails.save({
                    id: promotionDetail && promotionDetail.id || null,
                    name,
                    promotionSource: {id: p.student.promotionSource.id}
                })).then((action) => {
                    p.dispatch(Actions.setEntityValue('students', 'promotionDetail', Utils.extend(action.request, action.response)))
                })
            }
        });
    },

    deletePromotionSource(promotionSource){
        const self = this, p = self.props;

        DialogService.confirmDelete().then(() => {
            p.dispatch(Actions.ajax.promotionSources.delete(promotionSource.id)).then(() => {
                p.dispatch(Actions.setEntityValue('students', 'promotionSource', null))
            });
        });
    },

    deletePromotionDetail(promotionDetail){
        const self = this, p = self.props;

        DialogService.confirmDelete().then(() => {
            p.dispatch(Actions.ajax.promotionDetails.delete(promotionDetail.id)).then(() => {
                p.dispatch(Actions.setEntityValue('students', 'promotionDetail', null))
            });
        });
    },

    addSibling(sibling){
        const self = this, p = self.props;
        DataService.operations.students.findOne(sibling.id).then(loadedSibling => {
            p.dispatch(Actions.setEntityValue('students', 'siblings', Utils.arr.put(p.student.siblings || [], loadedSibling, Utils.obj.id), {addedSibling: loadedSibling}));
            p.dispatch(Actions.setPageMode('students', 'form', 'siblings', 'edit'))
        })
    },

    removeSibling(){
        const self = this, p = self.props;

        DialogService.confirmDelete().then(() => {
            const removedSibling = Utils.arr.find(p.student.siblings, {id: p.selectedSiblingId}, Utils.obj.id).obj;

            if (removedSibling.relatives) { //sibling was previously added and has loaded relaties
                self.dispatchRemovedSibling(removedSibling);
            } else { //load sibling that includes relatives
                DataService.operations.students.findOne(removedSibling.id).then(loadedSibling => {
                    self.dispatchRemovedSibling(loadedSibling);
                });
            }
        })
    },

    dispatchRemovedSibling(removedSibling){
        const self = this, p = self.props,
            nextSiblings = Utils.arr.remove(p.student.siblings || [], {id: p.selectedSiblingId}, Utils.obj.id);

        p.dispatch(Actions.setEntityValue('students', 'siblings', nextSiblings, {removedSibling: removedSibling}));
        p.dispatch(Actions.selectEntity('siblings', null, 'students', 'student'))
    },

    onSave(){
        const self = this, p = self.props,
            validationMessages = {
                students: Validators.students(p.student)
            };

        let relativesHaveErrors = false;
        // fill each relative's validation messages
        if (Utils.isArray(p.student.relatives)) {
            for (let i = 0; i < p.student.relatives.length; i++) {
                const relativeMessages = Validators.relatives(p.student.relatives[i]);
                validationMessages['relatives-' + i] = relativeMessages;
                relativesHaveErrors = relativesHaveErrors || !Utils.isAllValuesEmpty(relativeMessages);
            }
        }

        if (Utils.isAllValuesEmpty(validationMessages.students) && !relativesHaveErrors) {
            return p.dispatch(Actions.ajax.students.save(p.student))
                .then(
                    () => Navigator.navigate(Navigator.routes.students),
                    (error) => {
                        if (error.status === DataService.status.badRequest) {
                            setValidationMessages(p, error.students)
                        }
                    })
        } else {
            setValidationMessages(p, validationMessages);
        }
    },

    renderBackBtn(){
        const self = this, p = self.props;
        return <button type="button"
                       key="back"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={self.onBack}>
            {Icons.glyph.arrowLeft()}
        </button>
    },

    renderStudentCardBackBtn(){
        const self = this, p = self.props;
        return <button type="button"
                       key="back"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={() => p.dispatch(Actions.setPageMode('students', 'form', 'studentCards', 'edit'))}>
            {Icons.glyph.arrowLeft()}
        </button>
    },

    isSaveDisabled(){
        const self = this, p = self.props,
            student = p.student;
        return false
    },

    onBack(){
        const self = this, p = self.props;
        if (!Utils.lifeCycle.entity(p, 'students').saved) {
            DialogService.confirmBack().then(() => {
                Navigator.navigate(Navigator.routes.students)
            }, () => {
            })
        } else {
            Navigator.navigate(Navigator.routes.students)
        }
    },

    renderSelectedRelative () {
        const self = this, p = self.props,
            relative = self.getSelectedRelative(),
            index = !p.selectedRelativeIndex ? 0 : p.selectedRelativeIndex;
        if (relative) {
            const isNew = !relative.id,
                inputsColClassName = isNew ? 'col-md-12' : 'col-md-8';
            return <Row>
                {self.tryRenderRelativePhotoRow()}
                <Col classes={inputsColClassName}>
                    <Row>
                        <ColFormGroup classes="col-md-4 col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('relatives', 'role', index)}/>
                            <Select
                                name="role"
                                placeholder={Utils.message('common.students.relative.search.table.role')}
                                value={{name: relative.role}}
                                valueKey="name"
                                labelKey="name"
                                options={p.relativeRoles}
                                onChange={(opt) => {
                                    self.onRelativeFieldChange('role', opt && opt.name)
                                }}
                            />
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-4 col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('relatives', 'name', index)}/>
                            <TextInput id="name"
                                       name="name"
                                       owner={relative}
                                       placeholder={Utils.message('common.students.relative.search.table.name')}
                                       defaultValue={relative.name}
                                       onChange={self.onRelativeFieldChange.bind(this, 'name')}/>
                        </ColFormGroup>
                        <Col classes="col-md-4 col-xs-6">
                            {self.renderMobilePhoneInput()}
                        </Col>
                        <ColFormGroup classes="col-md-4 col-xs-6">
                            <ValidationMessage message={self.getValidationMessage('relatives', 'mail', index)}/>
                            <TextInput id="mail"
                                       name="mail"
                                       owner={relative}
                                       placeholder={Utils.message('common.students.relative.search.table.mail')}
                                       defaultValue={relative.mail}
                                       onChange={self.onRelativeFieldChange.bind(this, 'mail')}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-4 col-xs-6">
                            <ValidationMessage
                                message={self.getValidationMessage('relatives', 'driverLicense', index)}/>
                            <TextInput id="driverLicense"
                                       name="driverLicense"
                                       owner={relative}
                                       placeholder={Utils.message('common.students.relative.search.table.driverLicense')}
                                       defaultValue={relative.driverLicense}
                                       onChange={self.onRelativeFieldChange.bind(this, 'driverLicense')}/>
                        </ColFormGroup>
                        <ColFormGroup classes="col-md-4 col-xs-6">
                            <ValidationMessage
                                message={self.getValidationMessage('relatives', 'passport', index)}/>
                            <TextInput id="passport"
                                       name="passport"
                                       owner={relative}
                                       placeholder={Utils.message('common.students.relative.search.table.passport')}
                                       defaultValue={relative.passport}
                                       onChange={self.onRelativeFieldChange.bind(this, 'passport')}/>
                        </ColFormGroup>
                    </Row>
                </Col>
            </Row>
        }
    },
    tryRenderPhotoRow () {
        const self = this, p = self.props,
            isNew = !p.student.id;
        if (!isNew) {
            return <Col classes="col-md-4">
                <PhotosList hasPrimaryPhoto={true}
                            uploadUrl={DataService.urls.students.uploadPhoto(p.student.id)}
                            downloadUrlFn={(name) => {
                                return DataService.urls.students.downloadPhoto(p.student.id, name)
                            }}
                            onFileUploaded={(fileNames) => {
                                p.dispatch(Actions.filesUploaded(fileNames, 'students'))
                            }}
                            onRemoveFile={(fileName) => {
                                p.dispatch(Actions.ajax.students.removeFile(p.student.id, fileName))
                            }}
                            onSetPrimaryFile={(fileName) => {
                                p.dispatch(Actions.ajax.students.setPrimaryFile(p.student.id, fileName))
                            }}
                            onChangeShownFile={(fileName) => {
                                p.dispatch(Actions.changeShownFile(fileName, 'students'))
                            }}
                            fileNames={p.student.photos || []}
                            primaryFileName={p.student.primaryPhotoName}
                            shownFileName={p.shownFileName}/>
            </Col>
        }
    },
    tryRenderRelativePhotoRow () {
        const self = this, p = self.props,
            relative = self.getSelectedRelative();
        const isNew = !relative.id;
        if (!isNew) {
            return <Col classes="col-md-4">
                <PhotosList
                    hasPrimaryPhoto={true}
                    uploadUrl={DataService.urls.students.relatives.uploadPhoto(p.student.id, relative.id)}
                    downloadUrlFn={(name) => {
                        return DataService.urls.students.relatives.downloadPhoto(p.student.id, relative.id, name)
                    }}
                    onFileUploaded={(fileNames) => {
                        p.dispatch(Actions.studentRelativeFilesUploaded(fileNames))
                    }}
                    onRemoveFile={(fileName) => {
                        p.dispatch(Actions.ajax.students.relatives.removeFile(p.student.id, relative.id, fileName))
                    }}
                    onSetPrimaryFile={(fileName) => {
                        p.dispatch(Actions.ajax.students.relatives.setPrimaryFile(p.student.id, relative.id, fileName))
                    }}
                    onChangeShownFile={(fileName) => {
                        p.dispatch(Actions.studentRelativeChangeShownFile(fileName))
                    }}
                    fileNames={relative.photos || []}
                    primaryFileName={relative.primaryPhotoName}
                    shownFileName={p.shownRelativeFileName}/>
            </Col>
        }
    },
    renderMobilePhoneInput () {
        const self = this, p = self.props,
            relative = self.getSelectedRelative(),
            prevRelative = self.getSelectedPrevRelative(),
            prevMobile = prevRelative && prevRelative.mobile || '';

        //function to generate validation message or sms status message
        const fetchMessage = function () {
            const validationMessage = self.getValidationMessage('relatives', 'mobile', p.selectedRelativeIndex);
            if (validationMessage) return validationMessage;
            if (relative.confirmationId) return Utils.message('common.sms.confirmation.sent');
            return null
        };
        return <div className="form-group form-group-compact">
            <ValidationMessage message={fetchMessage()}/>
            <ConfirmPhoneInput
                owner={relative}
                phone={{
                    id: 'mobile',
                    name: 'mobile',
                    placeholder: Utils.message('common.students.relative.search.table.mobile'),
                    defaultValue: relative.mobile,
                    prevValue: prevMobile,
                    onChange: val => self.onRelativeFieldChange('mobile', val)
                }}
                confirm={{
                    id: 'confirmationCode',
                    name: 'confirmationCode',
                    placeholder: Utils.message('common.students.relative.search.table.confirmationCode'),
                    defaultValue: relative.confirmationCode,
                    onChange: self.onConfirmationCodeChange
                }}
                mobileConfirmed={relative.mobileConfirmed}
                validationMessage={self.getValidationMessage('relatives', 'mobile', p.selectedRelativeIndex)}
                confirmationId={relative.confirmationId}
                sendSms={() => p.dispatch(Actions.ajax.students.relatives.verifyMobileNumber(relative.mobile))}
                goBack={() => self.onRelativeFieldChange('confirmationId', null)}
            />
        </div>
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('students', fieldId, newValue))
    },
    onOptionChanged(onFieldChange, opt) {
        onFieldChange.call(this, opt && opt.id);
    },
    onConfirmationCodeChange(val){
        const self = this, p = self.props;
        self.onRelativeFieldChange('confirmationCode', val);

        if (val && val.length === 4 && !isNaN(+val)) {
            const relative = self.getSelectedRelative();
            p.dispatch(Actions.ajax.students.relatives.checkConfirmation(p.selectedRelativeIndex, relative.mobile, relative.confirmationId, val))
        }
    },
    onRelativeFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('relatives', fieldId, newValue))
    },
    renderOption(opt) {
        return opt.title;
    },

    getValidationMessage(entity, fieldId, index) {
        const self = this, p = self.props;
        let entityKey = entity + (Utils.isDefined(index) ? ('-' + index) : '');
        let messages = p.validationMessages[entityKey];
        if (!messages) {
            return null;
        }
        return messages[fieldId];
    }
});

/**
 * Updates error messages for student and relatives entities.
 *
 * @param props current student properties
 * @param validationMessages messages to set in state
 */
function setValidationMessages(props, validationMessages) {
    props.dispatch(Actions.setValidationMessages('students', validationMessages));
}

function mapStateToProps(state) {
    return Utils.extend(state.pages.Student, {
        relativeRoles: state.common.relativeRoles,
        promotionSources: state.pages.PromotionSources.entities,
        promotionDetails: state.pages.PromotionDetails.entities,
        employees: state.pages.Teachers.entities
    })
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Student);
