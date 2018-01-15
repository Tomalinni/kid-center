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
    {connect} = require('react-redux'),
    DataService = require('../services/DataService'),
    Permissions = require('../Permissions'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    Locale = require('../Locale'),
    Config = require('../Config'),
    Actions = require('../actions/Actions'),
    PaymentsMutator = require('../mutators/PaymentsMutator'),
    PageToolbar = require('../components/PageToolbar'),
    EntitiesTree = require('../components/EntitiesTree'),
    AccountsSelect = require('../components/AccountsSelect'),
    CategoryChainSelect = require('../components/CategoryChainSelect'),
    PeriodsSearchGroup = require('../components/PeriodsSearchGroup'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    Chart = require('../components/Chart'),
    AttachmentsCarousel = require('../components/AttachmentsCarousel'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    Icons = require('../components/Icons');

const Payments = React.createClass({

    componentDidMount(){
        const self = this, p = self.props;
        if (p.entities.length === 0) {
            self.fetchData()
        }
    },

    render(){
        const self = this, p = self.props;

        return <div><ListScreen entity='payments'
                                ajaxResource={Actions.ajax.payments}
                                fetchOnMount={false}
                                entityModifyPermssion={Permissions.paymentsModify}
                                entityRouteFn={Navigator.routes.payment}
                                pageTitle={Utils.message('common.pages.payments')}
                                filterElementFn={self.renderFilterElement}
                                entitiesTblCols={self.columns()}
                                currentViewFn={() => p.view}
                                renderViewFn={self.renderView}
                                listClasses="with-payments-detail-tbl"
                                renderToolbarBtns={self.renderChangeSearchMethodBtn}
                                {...p}
        />
            {self.renderDetailsTable()}
            {self.renderPhotos()}
        </div>
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;
        self.onSearchRequestChange = onSearchRequestChange;

        return <div>
            {self.renderPaymentDirections()}
            {Utils.when(p.searchRequest.searchMethod, {
                filters() {
                    return <div>
                        {self.renderGroupTitle(Utils.message('common.payments.search.fields.period'), 'period')}
                        {self.renderPeriodsGroup()}
                        {self.renderSchoolsSection('source', Utils.message('common.payments.search.fields.source.schools'))}
                        {self.renderGroupTitle(Utils.message('common.payments.search.fields.source.accounts'), 'sourceAccounts')}
                        {self.renderAccountsGroup('source')}
                        {self.renderSchoolsSection('target', Utils.message('common.payments.search.fields.target.schools'))}
                        {self.renderGroupTitle(Utils.message('common.payments.search.fields.target.accounts'), 'targetAccounts')}
                        {self.renderAccountsGroup('target')}
                        {self.renderGroupTitle(Utils.message('common.payments.search.fields.categories'), 'categories')}
                        {self.renderCategoriesGroup()}
                        {self.renderCategories()}
                        {self.renderGroupTitle(Utils.message('common.payments.search.fields.view'), 'views')}
                        {self.renderViewsGroup()}
                        {self.renderGroupBySection()}
                        {self.renderRefreshBtn()}
                        {self.renderBalanceSection()}
                    </div>
                },
                text() {
                    return <div>
                        {self.renderGroupTitle(Utils.message('common.payments.search.fields.text'), 'text')}
                        {self.renderSearchTextGroup()}
                        {self.renderRefreshBtn()}
                    </div>
                }
            })}
        </div>
    },

    columns(){
        const self = this, p = self.props;
        return [
            {
                className: 'entities-tbl-cell-xs-70p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.date')
                },
                bodyCell: {
                    childrenFn: (obj) => Utils.momentFromString(obj.date).format(Config.shortDateFormat)
                }
            },
            {
                className: 'entities-tbl-cell-xs-40p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.category1')
                },
                bodyCell: {
                    childrenFn: (obj) => Renderers.paymentCategory(obj.category)
                }
            },
            {
                className: 'entities-tbl-cell-xs-40p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.category2')
                },
                bodyCell: {
                    childrenFn: (obj) => Renderers.paymentCategory(obj.category2)
                }
            },
            {
                className: 'entities-tbl-cell-xs-40p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.category3')
                },
                bodyCell: {
                    childrenFn: (obj) => Renderers.paymentCategory(obj.category3)
                }
            },
            {
                className: 'entities-tbl-cell-xs-40p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.category4')
                },
                bodyCell: {
                    childrenFn: (obj) => Renderers.paymentCategory(obj.category4)
                }
            },
            {
                className: 'entities-tbl-cell-xs-40p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.category5')
                },
                bodyCell: {
                    childrenFn: (obj) => Renderers.paymentCategory(obj.category5)
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.payments.search.table.price')
                },
                bodyCell: {
                    childrenFn: (obj) => Math.floor(obj.price)
                }
            },
            {
                className: 'land-visible-tbl-cell entities-tbl-cell-xs-50p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.student.businessId')
                },
                bodyCell: {
                    childrenFn: (obj) => obj.studentBusinessId
                }
            },
            {
                className: 'land-visible-tbl-cell entities-tbl-cell-xs-50p entities-tbl-cell-sm-140p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.comment')
                },
                bodyCell: {
                    childrenFn: (obj) => obj.comment
                }
            },
            {
                className: 'land-visible-tbl-cell entities-tbl-cell-xs-50p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.link')
                },
                bodyCell: {
                    childrenFn: (obj) => self.renderProductUrlBtn(obj)
                }
            },
            {
                className: 'land-visible-tbl-cell entities-tbl-cell-xs-50p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.receiptPhotos')
                },
                bodyCell: {
                    childrenFn: (obj) => self.renderReceiptBtn(obj)
                }
            },
            {
                className: 'land-visible-tbl-cell entities-tbl-cell-xs-50p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.productPhotos')
                },
                bodyCell: {
                    childrenFn: (obj) => self.renderPhotosBtn(obj)
                }
            },
            {
                className: 'land-visible-tbl-cell entities-tbl-cell-xs-50p',
                headerCell: {
                    children: Utils.message('common.payments.search.table.account')
                },
                bodyCell: {
                    childrenFn: (obj) => Renderers.account.suffix(obj.account)
                }
            }
        ]
    },

    renderPaymentDirections(){
        const self = this, p = self.props;
        return <Row>
            {Dictionaries.paymentsSearchDirectionType.map(opt => {
                return <ColFormGroup key={opt.id} classes="col-xs-3">
                    <Button classes={Utils.ui.btn.successClassForVal(p.searchRequest.direction, opt.id)}
                            onClick={() => self.setDirection(opt.id)}>
                        {opt.title}
                    </Button>
                </ColFormGroup>
            })}
        </Row>
    },

    renderSchoolsSection(origin, title){
        const self = this, p = self.props,
            schools = p.schools;
        if (schools.length > 1) {
            return [
                self.renderSchoolsTitle(title),
                self.renderSchoolsGroup(origin),
            ]
        } else if (schools.length === 1) {
            return self.renderSingleSchool()
        }
    },

    renderSingleSchool(){
        const self = this, p = self.props;

        return <Row>
            <Col classes="col-xs-6">
                <label>{Utils.message('common.payments.search.fields.school')}</label>
                <span className="container-pad-left-10">{Renderers.school.cityAndName(p.schools[0])}</span>
            </Col>
        </Row>
    },

    renderSchoolsTitle(title){
        return <Row key="school-title">
            <Col classes="col-xs-12">
                <label>{title}</label>
            </Col>
        </Row>
    },

    renderSchoolsGroup(origin){
        const self = this, p = self.props;
        return <Row key="school-group">
            <ColFormGroup classes="col-xs-10">
                <Select
                    name="school"
                    clearable={false}
                    placeholder={Utils.message('common.payments.search.fields.school')}
                    value={Utils.arr.find(p.schools, {id: p.searchRequest[origin].schoolId}, Utils.obj.id).obj}
                    valueRenderer={Renderers.school.cityAndName}
                    optionRenderer={Renderers.school.cityAndName}
                    options={self.getSchools(origin)}
                    onChange={opt => self.setSchool(origin, opt && opt.id) }
                />
            </ColFormGroup>
            <ColFormGroup classes="col-xs-2">
                <Button onClick={() => self.setSchool(origin, null)}>
                    {Icons.glyph.remove()}
                </Button>
            </ColFormGroup>
        </Row>
    },

    setDirection(direction){
        const self = this, p = self.props;
        self.setFilterFields({direction});

    },

    /**
     * Sets school in filter
     * @param origin origin of school 'source' or 'target'
     * @param schoolId school id
     */
    setSchool(origin, schoolId){
        const self = this, p = self.props,
            filterFields = {schoolId};

        if (schoolId) {
            filterFields.accountIds = null
        }

        self.setFilterFields({
            [origin]: filterFields
        });
    },

    renderGroupTitle(titleElem, key){
        return <Row key={key + 'Title'}>
            <Col>
                <label>{titleElem}</label>
            </Col>
        </Row>
    },

    renderAccountsGroup(origin){
        const self = this, p = self.props;
        return <AccountsSelect accountsMap={self.getAccounts(origin)}
                               accounts={p.accounts}
                               accountIds={p.searchRequest[origin].accountIds}
                               schoolId={p.searchRequest[origin].schoolId}
                               onSet={(accountIds) => self.setFilterFields({[origin]: {accountIds}})}
        />
    },

    getAccounts(origin){
        const self = this, p = self.props,
            direction = p.searchRequest.direction;
        return Utils.getAccountsSubMap(p.accountsMap, direction, origin)
    },


    getSchools(origin){
        const self = this, p = self.props,
            direction = p.searchRequest.direction;
        return Utils.getSchoolsCollection(p.schoolsMap, direction, origin)
    },

    renderCategoriesGroup(){
        const self = this, p = self.props;

        return <Row>
            <ColFormGroup classes="col-xs-6">
                <Button classes={Utils.ui.btn.successClassForVal(p.searchRequest.useInnerCategories, false)}
                        onClick={() => self.setFilterFields({useInnerCategories: false})}>
                    {Utils.message('common.payments.search.fields.category.top')}
                </Button>
            </ColFormGroup>
            <ColFormGroup classes="col-xs-6">
                <Button classes={Utils.ui.btn.successClassForVal(p.searchRequest.useInnerCategories, true)}
                        onClick={() => self.setFilterFields({useInnerCategories: true})}>
                    {Utils.message('common.payments.search.fields.category.inner')}
                </Button>
            </ColFormGroup>
        </Row>
    },

    renderCategories(){
        const self = this, p = self.props;
        if (!p.searchRequest.useInnerCategories) {
            return self.renderTopCategories();
        } else {
            return self.renderInnerCategories();
        }
    },

    renderTopCategories(){
        const self = this, p = self.props;

        return <Row>
            <ColFormGroup>
                <Button onClick={self.toggleAllCategories}>
                    {self.renderAllCategoriesCheckMark()}
                    {Utils.message('common.payments.search.fields.options.all')}
                </Button>
            </ColFormGroup>
            {p.rootCategoryIds.map(id => {
                return <ColFormGroup key={id} classes="col-xs-4">
                    <Button onClick={() => self.toggleCategory(id)}>
                        {self.renderCategoryCheckMark(id)}
                        {p.categoriesMap[id].name}
                    </Button>
                </ColFormGroup>
            })}
        </Row>
    },

    renderInnerCategories(){
        const self = this, p = self.props;

        return <CategoryChainSelect categoriesMap={p.categoriesMap}
                                    rootCategoryIds={p.rootCategoryIds}
                                    onAdd={(categoryId) => self.setFilterFields({innerCategoryIds: Utils.arr.put(p.searchRequest.innerCategoryIds, categoryId)}) }
                                    onRemove={(categoryId) => self.setFilterFields({innerCategoryIds: Utils.arr.remove(p.searchRequest.innerCategoryIds, categoryId)}) }
                                    onRemoveAll={() => self.setFilterFields({innerCategoryIds: []})}/>
    },

    renderCategoryCheckMark(id){
        const self = this, p = self.props;
        if (!p.searchRequest.categoryIds || Utils.arr.contains(p.searchRequest.categoryIds || [], id)) {
            return Icons.glyph.ok('icon-btn-success icon-btn-rpad')
        }
    },

    renderAllCategoriesCheckMark(){
        const self = this, p = self.props;
        if (self.allCategoriesSelected()) {
            return Icons.glyph.ok('icon-btn-success icon-btn-rpad')
        }
    },

    allCategoriesSelected(){
        const self = this, p = self.props;
        return !p.searchRequest.categoryIds || p.rootCategoryIds.length === (p.searchRequest.categoryIds || []).length && p.rootCategoryIds.length !== 0
    },

    toggleAllCategories(){
        const self = this, p = self.props,
            nextCategoryIds = self.allCategoriesSelected() ?
                [] :
                (p.rootCategoryIds || []);
        self.setFilterFields({categoryIds: nextCategoryIds});
    },

    toggleCategory(id){
        const self = this, p = self.props;
        let categoryIds = p.searchRequest.categoryIds || p.rootCategoryIds;
        self.setFilterFields({categoryIds: Utils.arr.toggle(categoryIds, id)});
    },

    renderPeriodsGroup(){
        const self = this, p = self.props;
        return <PeriodsSearchGroup searchRequest={p.searchRequest}
                                   periodFieldId="period"
                                   startDateFieldId="periodStart"
                                   endDateFieldId="periodEnd"
                                   onPeriodChange={self.setFilterFields}/>
    },

    renderViewsGroup(){
        const self = this, p = self.props;
        return <Row>
            {Dictionaries.paymentsSearchView.map(opt => {
                return <ColFormGroup key={opt.id} classes="col-xs-4">
                    <Button classes={Utils.ui.btn.successClassForVal(p.view, opt.id)}
                            onClick={() => p.dispatch(Actions.setPaymentsView(opt.id))}>
                        {opt.title}
                    </Button>
                </ColFormGroup>
            })}
            <ColFormGroup key="export" classes="col-xs-4">
                <div className="btn-group w100pc">
                    <button type="button"
                            data-toggle="dropdown"
                            className="btn btn-wide btn-default dropdown-toggle">
                        {Utils.message('common.payments.search.table.export')}&nbsp;
                        {Icons.caret()}
                    </button>
                    <ul className="dropdown-menu">
                        <li>
                            <a onClick={() => self.exportPaymentsList({skipPhotos: false})}>
                                {Utils.message('common.payments.search.table.export.with.photos')}
                            </a>
                        </li>
                        <li>
                            <a onClick={() => self.exportPaymentsList({skipPhotos: true})}>
                                {Utils.message('common.payments.search.table.export.no.photos')}
                            </a>
                        </li>
                    </ul>
                </div>
            </ColFormGroup>

        </Row>
    },

    exportPaymentsList(options){
        const self = this, p = self.props;
        window.open(DataService.urls.payments.exportList(Utils.extend(p.searchRequest, options)));
    },

    renderChangeSearchMethodBtn(){
        const self = this, p = self.props,
            isFiltersMode = p.searchRequest.searchMethod === 'filters';
        return <button type="button"
                       key="changeSearchMethod"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={() => {
                           self.setFilterFields({searchMethod: Utils.select(isFiltersMode, 'text', 'filters')});
                           p.dispatch(Actions.setPaymentsView('list'))
                       }}>
            {Utils.select(isFiltersMode, Icons.glyph.search(), Icons.glyph.th())}
        </button>
    },

    renderSearchTextGroup(){
        const self = this, p = self.props;
        return <Row>
            <ColFormGroup>
                <TextInput id="text"
                           owner={p.searchRequest}
                           name="text"
                           placeholder={Utils.message('common.payments.search.fields.text')}
                           defaultValue={p.searchRequest.text}
                           onChange={val => self.setFilterFields({text: val})}/>
            </ColFormGroup>
        </Row>
    },

    renderDetailsTable(){
        const self = this, p = self.props,
            payment = p.selectedObj;

        if (payment) {
            return <table className="entities-tbl-details">
                <thead>
                <tr>
                    <td style={{width: '180px'}}>{Utils.message('common.payments.search.table.comment')}</td>
                    <td>{Utils.message('common.payments.search.table.link')}</td>
                    <td>{Utils.message('common.payments.search.table.receiptPhotos')}</td>
                    <td>{Utils.message('common.payments.search.table.productPhotos')}</td>
                    <td>{Utils.message('common.payments.search.table.account')}</td>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>{payment.comment}</td>
                    <td>
                        {self.renderProductUrlBtn(payment)}
                    </td>
                    <td>
                        {self.renderPhotosBtn(payment)}
                    </td>
                    <td>
                        {self.renderReceiptBtn(payment)}
                    </td>
                    <td>{Renderers.account.suffix(payment.account)}</td>
                </tr>
                </tbody>
            </table>
        }
    },

    renderProductUrlBtn(payment){
        const self = this, p = self.props,
            productUrl = payment && payment.productUrl;

        if (productUrl) {
            return <a type="button"
                      className="btn btn-default btn-row"
                      target="_blank"
                      href={productUrl}>
                {Icons.glyph.newWindow()}
            </a>
        }
    },

    renderPhotosBtn(obj){
        const self = this, p = self.props;
        if (obj && obj.productPhotosCount > 0) {
            return <button type="button"
                           className="btn btn-default btn-row"
                           onClick={(e) => {
                               p.dispatch(Actions.ajax.payments.listPhotos(obj, "productPhotos"));
                               e.stopPropagation()
                           }}>
                {Icons.glyph.camera()}
            </button>
        }
    },

    renderReceiptBtn(obj){
        const self = this, p = self.props;
        if (obj && obj.receiptPhotosCount > 0) {
            return <button type="button"
                           className="btn btn-default btn-row"
                           onClick={(e) => {
                               p.dispatch(Actions.ajax.payments.listPhotos(obj, "receiptPhotos"));
                               e.stopPropagation()
                           }}>
                {Icons.glyph.list()}
            </button>
        }
    },

    renderPhotos(){
        const self = this, p = self.props;
        return <div><AttachmentsCarousel
            showCarousel={p.selectedObj && p.photosOwnerField}
            shownAttachments={p.shownPhotos}
            currentAttachmentUrl={(attachment) => DataService.urls.payments.downloadPhoto(p.selectedObj.id, p.photosOwnerField, attachment)}
            onHideFn={() => p.dispatch(Actions.hidePaymentPhotos())}
            {...p}
        /></div>
    },

    renderGroupBySection(){
        const self = this, p = self.props;
        if (p.view === 'chart') {
            return [
                self.renderGroupTitle(Utils.message('common.payments.search.fields.groupBy'), 'groupBy'),
                self.renderGroupByGroup()
            ]
        }
    },

    renderGroupByGroup(){
        const self = this, p = self.props;
        return <Row key="groupBy">
            {Dictionaries.paymentsChartGroupBy.map(opt => {
                return <ColFormGroup key={opt.id} classes="col-xs-2">
                    <Button classes={Utils.ui.btn.successClassForVal(p.searchRequest.groupBy, opt.id)}
                            onClick={() => self.setFilterFields({groupBy: opt.id})}>
                        {opt.title}
                    </Button>
                </ColFormGroup>
            })}
        </Row>
    },

    renderRefreshBtn(){
        const self = this, p = self.props;
        return <Row>
            <ColFormGroup >
                <Button onClick={() => self.fetchData()}>
                    {Icons.glyph.refresh()}&nbsp;
                    {Utils.message('button.refresh')}
                </Button>
            </ColFormGroup>
        </Row>
    },

    renderBalanceSection(){
        const self = this, p = self.props;
        if (p.balanceItems.length > 0) {
            return <table className="entities-tbl-body">
                <thead>
                <tr>
                    <td style={{width: '80px'}}>{Utils.message('common.payments.search.table.balance.account')}</td>
                    <td style={{width: '80px'}}>{Utils.message('common.payments.search.table.balance.school')}</td>
                    <td style={{width: '75px'}}>{Utils.message('common.payments.search.table.balance.start')}</td>
                    <td style={{width: '75px'}}>{Utils.message('common.payments.search.table.balance.income')}</td>
                    <td style={{width: '75px'}}>{Utils.message('common.payments.search.table.balance.expense')}</td>
                    <td style={{width: '75px'}}>{Utils.message('common.payments.search.table.balance.end')}</td>
                </tr>
                </thead>
                <tbody>
                {p.balanceItems.map(item => {
                    const account = Utils.arr.find(p.accounts, {id: +item.accountId}, Utils.obj.id).obj,
                        school = Utils.arr.find(p.schools, {id: +item.schoolId}, Utils.obj.id).obj;
                    return <tr key={item.accountId + '_' + item.schoolId}
                               className="entities-tbl-row">
                        <td>{Renderers.account.name(account)}</td>
                        <td>{Renderers.school.name(school)}</td>
                        <td>{item.startBalance}</td>
                        <td>{item.income}</td>
                        <td>{item.expense}</td>
                        <td>{item.endBalance}</td>
                    </tr>
                })}
                </tbody>
            </table>
        }
    },

    renderView(view){
        const self = this, p = self.props;
        if (view === 'chart') {
            if (!p.outdatedChart) {
                return self.renderChartView()
            } else {
                return <div className="entities-tbl-message">{Utils.message('common.refresh.to.get.data')}</div>
            }
        }
    },

    renderChartView() {
        const self = this, p = self.props,
            chartOptions = {
                title: {
                    text: ' ',
                    x: -20 //center
                },
                xAxis: {
                    categories: Locale.shortMonths()
                },
                yAxis: {
                    title: {
                        text: Utils.message('common.payments.search.table.chart.total')
                    },
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }]
                },
                tooltip: {
                    valueSuffix: '¥'
                },
                legend: {
                    layout: 'vertical',
                    align: 'center',
                    verticalAlign: 'bottom',
                    borderWidth: 0
                },
                series: p.statChart.series
            };

        return <div>
            <div className="payments-stat-chart">
                <Chart container="payments-chart" options={chartOptions}/>
            </div>
        </div>
    },

    setFilterFields(values){
        const self = this, p = self.props;
        const searchRequest = PaymentsMutator.mutateSearchRequestOnFilterChange(p.searchRequest, values);
        p.dispatch(Actions.setPaymentsFilter(searchRequest));
    },

    fetchData(){
        const self = this, p = self.props,
            accountsDefined = Utils.isNotDefinedOrNotEmptyArray(p.searchRequest.accountIds),
            categoriesDefined = Utils.isNotDefinedOrNotEmptyArray(p.searchRequest.useInnerCategories ? p.searchRequest.innerCategoryIds : p.searchRequest.categoryIds),
            isFiltersMode = p.searchRequest.searchMethod === 'filters';

        if (!isFiltersMode || (accountsDefined && categoriesDefined)) {
            self.onSearchRequestChange(p.searchRequest);

            if (isFiltersMode) { //do not show charts and balance for other search modes
                p.dispatch(Actions.ajax.payments.stat(p.searchRequest));
                p.dispatch(Actions.ajax.payments.balance(self.getBalanceRequest()))
            }
        }
    },

    getBalanceRequest(){
        const self = this, p = self.props, r = p.searchRequest,
            origin = r.direction === 'incoming' ? r.target : r.source,
            {period, periodStart, periodEnd} = r;
        return Utils.extend(origin, {period, periodStart, periodEnd});
    }
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.Payments,
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

module.exports = connect(mapStateToProps, mapDispatchToProps)(Payments);
