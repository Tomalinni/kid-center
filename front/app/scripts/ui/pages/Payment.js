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

//noinspection JSUnresolvedFunction
const React = require('react'),
    moment = require('moment-timezone'),
    {connect} = require('react-redux'),
    AuthService = require('../services/AuthService'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    DataService = require('../services/DataService'),
    DialogService = require('../services/DialogService'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    Actions = require('../actions/Actions'),
    DatePicker = require('../components/DateComponents').DatePicker,
    PhotosList = require('../components/PhotosList'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    AccountSelect = require('../components/AccountSelect'),
    EntityLifeCyclePanel = require('../components/EntityLifeCyclePanel'),
    {Row, Col, FormGroup, ColFormGroup, Button, ProgressButton, Select} = require('../components/CompactGrid'),
    Validators = require('../Validators'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const Payment = React.createClass({

    componentDidMount(){
        const self = this, p = self.props;

        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId && Utils.isValidNumberId(id)) {
            p.dispatch(Actions.ajax.payments.findOne(id));
        } else if (isNewId) {
            p.dispatch(Actions.initEntity('payments'));
        }
        p.dispatch(Actions.clearValidationMessages('payments'));
    },

    render(){
        const self = this, p = self.props;
        return <div>
            <PageToolbar leftButtons={[self.renderBackBtn(), self.renderSaveBtn()]}/>
            <h4 className="page-title">{Utils.message('common.pages.payment')}</h4>
            <EntityLifeCyclePanel {...p}
                                  entity="payments"
                                  renderChildrenFn={self.renderForm}/>
        </div>
    },

    renderForm() {
        const self = this, p = self.props;

        return <div className="container with-nav-toolbar high-inputs">
            {self.renderPaymentDirections()}
            <div className="row row-compact">
                <div className="col-md-12">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('payments', 'school')}/>
                        <Select
                            name="school"
                            clearable={false}
                            placeholder={Utils.message('common.payments.search.fields.source.school')}
                            value={p.payment.school}
                            valueRenderer={Renderers.school.cityAndName}
                            optionRenderer={Renderers.school.cityAndName}
                            options={self.getSchools('source')}
                            onChange={opt => self.onFieldChange('school', opt) }
                        />
                    </div>
                </div>
                <div className="col-md-12">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('payments', 'account')}/>
                        <AccountSelect account={p.payment.account}
                                       accountsMap={Utils.getAccountsSubMap(p.accountsMap, p.payment.direction, 'source')}
                                       schoolId={p.payment.school && p.payment.school.id}
                                       onSet={(opt) => {
                                           self.onFieldChange('account', opt)
                                       }}/>
                    </div>
                </div>
                <div className="col-md-12">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('payments', 'targetSchool')}/>
                        <Select
                            name="targetSchool"
                            clearable={false}
                            placeholder={Utils.message('common.payments.search.fields.target.school')}
                            value={p.payment.targetSchool}
                            valueRenderer={Renderers.school.cityAndName}
                            optionRenderer={Renderers.school.cityAndName}
                            options={self.getSchools('target')}
                            onChange={opt => self.onFieldChange('targetSchool', opt) }
                        />
                    </div>
                </div>
                <div className="col-md-12">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('payments', 'targetAccount')}/>
                        <AccountSelect account={p.payment.targetAccount}
                                       accountsMap={Utils.getAccountsSubMap(p.accountsMap, p.payment.direction, 'target')}
                                       schoolId={p.payment.targetSchool && p.payment.targetSchool.id}
                                       onSet={(opt) => {
                                           self.onFieldChange('targetAccount', opt)
                                       }}/>
                    </div>
                </div>
                <div className="col-md-6">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('payments', 'date')}/>
                        <DatePicker id="date"
                                    value={p.payment.date}
                                    onChange={self.onFieldChange.bind(this, 'date')}>
                            <button type="button" className="form-control btn btn-default">
                                {p.payment.date || Utils.message('common.form.date.not.selected')}
                                &nbsp;{Icons.caret()}
                            </button>
                        </DatePicker>
                    </div>
                </div>
                <div className="col-md-6">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('payments', 'category')}/>
                        <button type="button"
                                className="btn btn-default dropdown-toggle btn-right-gap"
                                data-toggle="dropdown"
                                aria-haspopup="true"
                                aria-expanded="false">
                            {Icons.glyph.cog()}
                        </button>
                        <ul className="dropdown-menu dropdown-menu-right">
                            <li><a
                                onClick={() => self.addCategory(null, 'category')}>{Utils.message('button.new')}</a>
                            </li>
                            {self.renderEditCategoryOption(p.payment.category, 'category', null)}
                            {self.renderDeleteCategoryOption(p.payment.category, 'category')}
                        </ul>
                        <Select
                            name="category"
                            placeholder={Utils.message('common.payments.search.table.category1')}
                            className="field-with-right-gap-1"
                            value={p.payment.category}
                            valueRenderer={Renderers.category}
                            optionRenderer={Renderers.category}
                            options={p.rootCategoryIds.map(id => p.categoriesMap[id])}
                            onChange={(opt) => {
                                self.onFieldChange('category', opt)
                            }}
                        />
                    </div>
                </div>

                {self.renderCategoryInput(p.payment.category2, 'category2', p.payment.category)}
                {self.renderCategoryInput(p.payment.category3, 'category3', p.payment.category2)}
                {self.renderCategoryInput(p.payment.category4, 'category4', p.payment.category3)}
                {self.renderCategoryInput(p.payment.category5, 'category5', p.payment.category4)}

                <div className="col-md-6">
                    <div className="form-group">
                        <DatePicker id="monthDate"
                                    value={p.payment.monthDate}
                                    mode="month"
                                    onChange={self.onFieldChange.bind(this, 'monthDate')}>
                            <button type="button" className="form-control btn btn-default">
                                {p.payment.monthDate ? Utils.momentFromString(p.payment.monthDate).format(Config.yearMonthDateFormat) : Utils.message('common.form.date.not.selected')}
                                &nbsp;{Icons.caret()}
                            </button>
                        </DatePicker>
                    </div>
                </div>
                <div className="col-md-6">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('payments', 'price')}/>
                        <TextInput id="price"
                                   owner={p.payment}
                                   name="price"
                                   placeholder={Utils.message('common.payments.search.table.price')}
                                   defaultValue={p.payment.price}
                                   onChange={self.onFieldChange.bind(this, 'price')}/>
                    </div>
                </div>
                <div className="col-md-6">
                    <div className="form-group">
                        <TextInput id="comment"
                                   owner={p.payment}
                                   name="comment"
                                   placeholder={Utils.message('common.payments.search.table.comment')}
                                   defaultValue={p.payment.comment}
                                   onChange={self.onFieldChange.bind(this, 'comment')}/>
                    </div>
                </div>
                <div className="col-md-6">
                    <div className="form-group">
                        <TextInput id="productUrl"
                                   owner={p.payment}
                                   name="productUrl"
                                   placeholder={Utils.message('common.payments.search.table.link')}
                                   defaultValue={p.payment.productUrl}
                                   onChange={self.onFieldChange.bind(this, 'productUrl')}/>
                    </div>
                </div>
            </div>
            {self.renderSaveAndContinueButton()}
            <div className="row row-compact">
                {self.tryRenderReceiptPhotosRow('receiptPhotos')}
                {self.tryRenderReceiptPhotosRow('productPhotos')}
            </div>
        </div>
    },

    renderPaymentDirections(){
        const self = this, p = self.props;
        return <Row>
            {Dictionaries.paymentDirectionType.map(opt => {
                return <ColFormGroup key={opt.id} classes="col-xs-4">
                    <Button classes={Utils.ui.btn.successClassForVal(p.payment.direction, opt.id)}
                            onClick={() => self.onFieldChange('direction', opt.id)}>
                        {opt.title}
                    </Button>
                </ColFormGroup>
            })}
        </Row>
    },

    renderSaveBtn(){
        const self = this, p = self.props;
        return <ProgressButton key="save"
                               className="btn btn-default btn-lg nav-toolbar-btn"
                               onClick={() => self.onSave(true)}>
            {Icons.glyph.save()}
        </ProgressButton>
    },

    onSave(redirect){
        const self = this, p = self.props;

        let validationMessages = Validators.getByEntityId('payments', p.payment);
        if (!validationMessages || Utils.isEmptyArray(Utils.objectValues(validationMessages).filter(Boolean))) {
            return p.dispatch(Actions.ajax.payments.save(p.payment))
                .then(
                    () => {
                        if (redirect) {
                            Navigator.navigate(Navigator.routes.payments)
                        }
                    },
                    (error) => {
                        if (error.status === DataService.status.badRequest) {
                            p.dispatch(Actions.setValidationMessages('payments', error['payments']))
                        }
                    }
                )
        } else {
            p.dispatch(Actions.setValidationMessages('payments', validationMessages))
        }
    },

    renderBackBtn(){
        const self = this;
        return <button type="button"
                       key="back"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={self.onBack}>
            {Icons.glyph.arrowLeft()}
        </button>
    },

    onBack(){
        const self = this, p = self.props;
        if (!Utils.lifeCycle.entity(p, 'payments').saved) {
            DialogService.confirmBack().then(() => {
                Navigator.navigate(Navigator.routes.payments)
            }, () => {
            })
        } else {
            Navigator.navigate(Navigator.routes.payments)
        }
    },

    renderEditCategoryOption(category, fieldId, parentCategory){
        const self = this;
        if (category) {
            return <li>
                <a onClick={() => self.editCategory(category, fieldId, parentCategory)}>{Utils.message('button.edit')}</a>
            </li>
        }
    },

    renderDeleteCategoryOption(category, fieldId){
        const self = this;
        if (category) {
            return <li>
                <a onClick={() => self.deleteCategory(category, fieldId)}>{Utils.message('button.delete')}</a>
            </li>
        }
    },

    addCategory(parentCategory, fieldId){
        const self = this, p = self.props;

        let categoryName;
        return DialogService.modal({
            title: Utils.message('common.payments.form.add.category.title'),
            content: <div className="col-md-6">
                <div className="form-group">
                    <label>{Utils.message('common.payments.form.category.name')}</label>
                    <input type="text"
                           name="name"
                           className="form-control"
                           placeholder={Utils.message('common.payments.form.category.name')}
                           defaultValue=''
                           onBlur={e => {
                               categoryName = e.target.value;
                           }}
                    />
                </div>
            </div>,
            buttons: {
                'cancel': {
                    label: Utils.message('button.cancel'),
                    reject: null
                },
                'ok': {
                    label: Utils.message('button.ok'),
                    resolve: () => categoryName
                }
            }
        }).then(() => {
            p.dispatch(Actions.ajax.categories.save({
                name: categoryName,
                parent: parentCategory && {id: parentCategory.id}
            })).then((action) => {
                p.dispatch(Actions.setEntityValue('payments', fieldId, Utils.extend(action.request, action.response)))
            })
        });
    },

    editCategory(category, fieldId, parentCategory){
        const self = this, p = self.props;

        let categoryName = category.name;
        return DialogService.modal({
            title: Utils.message('common.payments.form.add.category.title'),
            content: <div className="col-md-6">
                <div className="form-group">
                    <label>{Utils.message('common.payments.form.category.name')}</label>
                    <input type="text"
                           name="name"
                           className="form-control"
                           placeholder={Utils.message('common.payments.form.category.name')}
                           defaultValue={category.name}
                           onBlur={e => {
                               categoryName = e.target.value;
                           }}
                    />
                </div>
            </div>,
            buttons: {
                'cancel': {
                    label: Utils.message('button.cancel'),
                    reject: null
                },
                'ok': {
                    label: Utils.message('button.ok'),
                    resolve: () => categoryName
                }
            }
        }).then(() => {
            p.dispatch(Actions.ajax.categories.save({
                id: category.id,
                name: categoryName,
                parent: parentCategory && {id: parentCategory.id}
            })).then((action) => {
                p.dispatch(Actions.setEntityValue('payments', fieldId, Utils.extend(action.request, action.response), {refreshDependentFields: false}))
            })
        });
    },

    deleteCategory(category, fieldId){
        const self = this, p = self.props;

        DialogService.confirmDelete().then(() => {
            p.dispatch(Actions.ajax.categories.delete(category.id)).then(() => {
                p.dispatch(Actions.setEntityValue('payments', fieldId, null))
            });
        });
    },

    showEditDialog(defaultValue){

    },

    renderCategoryInput(category, fieldId, parentCategory){
        const self = this, p = self.props;

        return <div className="col-md-6">
            <div className="form-group">
                <button type="button"
                        className="btn btn-default dropdown-toggle btn-right-gap"
                        disabled={!parentCategory}
                        data-toggle="dropdown"
                        aria-haspopup="true"
                        aria-expanded="false">
                    {Icons.glyph.cog()}
                </button>
                <ul className="dropdown-menu dropdown-menu-right">
                    <li><a onClick={() => self.addCategory(parentCategory, fieldId)}>{Utils.message('button.new')}</a>
                    </li>
                    {self.renderEditCategoryOption(category, fieldId, parentCategory)}
                    {self.renderDeleteCategoryOption(category, fieldId)}
                </ul>

                <Select
                    className="field-with-right-gap-1"
                    name={fieldId}
                    placeholder={Utils.message('common.payments.search.table.' + fieldId)}
                    value={category}
                    valueRenderer={Renderers.category}
                    optionRenderer={Renderers.category}
                    options={(parentCategory && parentCategory.children || []).map((cat) => p.categoriesMap[cat.id])}
                    onChange={(opt) => {
                        self.onFieldChange(fieldId, opt)
                    }}
                />
            </div>
        </div>
    },

    getValidationMessage(entity, fieldId){
        return this.props.validationMessages[entity][fieldId];
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('payments', fieldId, newValue))
    },

    renderAccountOpt(opt){
        return opt && Utils.joinDefined([opt.name]) || ''
    },

    loadAccounts(text, callback){
        DataService.operations.accounts.findAll(text).then(response => {
            callback(null, {
                options: response.results
            });
        }, error => {
            callback(error.status, null);
        });
    },

    getSchools(origin){
        const self = this, p = self.props,
            direction = p.payment.direction;
        return Utils.getSchoolsCollection(p.schoolsMap, direction, origin)
    },

    renderOption(opt) {
        return opt.title;
    },

    renderSaveAndContinueButton(){
        const self = this, p = self.props,
            isNew = !p.payment.id;
        if (isNew) {
            return <div className="row row-compact">
                <div className="col-md-12">
                    <div className="form-group">
                        <ProgressButton className="btn btn-default btn-wide"
                                        onClick={() => self.onSave(false)}>
                            {Icons.glyph.save()} {Utils.message('button.save.continue')}
                        </ProgressButton>
                    </div>
                </div>
            </div>
        }
    },

    tryRenderReceiptPhotosRow (fieldId) {
        const self = this, p = self.props,
            isNew = !p.payment.id;
        if (!isNew) {
            return <div className="col-md-6">
                <label>{Utils.message('common.payments.search.table.' + fieldId)}</label>
                <PhotosList
                    uploadUrl={DataService.urls.payments.uploadPhoto(p.payment.id, fieldId)}
                    downloadUrlFn={(name) => {
                        return DataService.urls.payments.downloadPhoto(p.payment.id, fieldId, name)
                    }}
                    onFileUploaded={(fileNames) => {
                        p.dispatch(Actions.filesUploaded(fileNames, 'payments', fieldId))
                    }}
                    onRemoveFile={(fileName) => {
                        p.dispatch(Actions.ajax.payments.removeFile(p.payment.id, fieldId, fileName))
                    }}
                    onChangeShownFile={(fileName) => {
                        p.dispatch(Actions.changeShownFile(fileName, 'payments', fieldId))
                    }}
                    fileNames={p.payment[fieldId] || []}
                    shownFileName={p.shownFiles[fieldId]}/>
            </div>
        }
    }
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.Payment,
        {
            cities: state.pages.Cities.entities,
            schools: state.pages.Schools.entities,
            schoolsMap: state.pages.Schools.entitiesMap,
            accounts: state.pages.Accounts.entities,
            accountsMap: state.pages.Accounts.entitiesMap,
            rootCategoryIds: state.pages.Categories.rootIds,
            categoriesMap: state.pages.Categories.entitiesMap,
        });
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

//noinspection JSUnresolvedVariable
module.exports = connect(mapStateToProps, mapDispatchToProps)(Payment);
