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
    DataService = require('../services/DataService'),
    AuthService = require('../services/AuthService'),
    DialogService = require('../services/DialogService'),
    Navigator = require('../Navigator'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    Actions = require('../actions/Actions'),
    PageToolbar = require('../components/PageToolbar'),
    {ProgressButton} = require('../components/CompactGrid'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const ListScreen = React.createClass({
    filter: null,
    tableHeader: null,
    menuUpBtn: null,
    searchRequestDefined: true,

    getDefaultProps(){
        return {
            instantSearch: true,
            fetchOnMount: true,
            renderToolbarBtns: () => [],
            renderMenuUp: true,
            renderModifyBtns: true
        }
    },

    componentDidMount(){
        const self = this, p = self.props,
            selectionId = Navigator.query(p.location, 'selection'); //list view with specific object selected
        self.searchRequestDefined = Utils.isDefined(p.searchRequest);

        let searchRequest = self.searchRequestDefined ? Utils.extend(p.searchRequest,
                {
                    firstRecord: 1,
                    appendResults: false,
                    selection: selectionId
                }) : {}; //do not append special parameters if request is not defined in reducer state, return all entities at once

        if (p.ajaxResource && p.fetchOnMount) { //Not defined for cases with preloaded entities
            const findAllPromise = p.dispatch(p.ajaxResource.findAll(searchRequest));

            if (selectionId) {
                findAllPromise.then(action => {
                    const foundStudentToSelect = action.response.results[0];
                    if (foundStudentToSelect) {
                        self.onRowClick(foundStudentToSelect)
                    }
                });
            }
        }

        if (self.searchRequestDefined) {
            $(window).on('scroll', self.onScroll);
        }
        Utils.affix.invoke(self.tableHeader, self.calcTopOffset);
        Utils.affix.invoke(self.menuUpBtn, self.calcTopOffset);
    },

    componentWillUnmount(){
        const self = this;
        if (self.searchRequestDefined) {
            $(window).off('scroll')
        }
    },

    calcTopOffset(){
        const self = this;
        return $(self.filter).outerHeight(true) + 42; //header height
    },

    getInitialState(){
        return {
            errorMessage: null,
            selectedObjId: null,
            sortColumn: null,
            sortOrder: 'asc' //desc
        }
    },

    render(){
        const self = this, p = self.props,
            modifyBtns = Utils.select(p.renderModifyBtns, [self.renderDeleteBtn(), self.renderEditBtn(), self.renderCreateBtn()], []);
        return <div>
            {Utils.selectFn(p.renderMenuUp, self.renderBtnMenuUp)}
            <PageToolbar
                leftButtons={modifyBtns.concat(p.renderToolbarBtns() || [])}/>

            <h4 className="page-title">{p.pageTitle}</h4>
            <ValidationMessage message={self.state.errorMessage || ''} centered='true'/>
            <div className={self.listClasses()}>
                {self.renderFilter()}
                {self.renderView()}
            </div>
        </div>
    },

    listClasses(){
        const self = this, p = self.props;
        return 'with-nav-toolbar ' + (p.listClasses || '')
    },

    renderBtnMenuUp(){
        const self = this, p = self.props;

        return <button type="button"
                       className="btn btn-primary btn-menu-up"
                       ref={c => {
                           self.menuUpBtn = c
                       }}
                       onClick={(e) => {
                           window.scrollTo(0, 0);
                           e.target.blur()
                       }}>
            {Icons.glyph.menuUp()}
        </button>
    },

    renderFilter(){
        const self = this, p = self.props;

        if (self.searchRequestDefined) {
            return <div className="container" ref={c => {
                self.filter = c
            }}>
                {p.filterElementFn && p.filterElementFn(self.onSearchRequestChange)}
            </div>
        }
    },

    renderView(){
        const self = this, p = self.props,
            currentView = self.getCurrentView();
        if (currentView === 'list') {
            return self.renderListView()
        } else {
            return p.renderViewFn(currentView)
        }
    },

    getCurrentView(){
        const self = this, p = self.props;
        return p.currentViewFn && p.currentViewFn() || 'list';
    },

    renderListView(){
        const self = this, p = self.props;
        return <div>
            {self.renderTableBlock()}
            {self.renderLoadingMessage()}
        </div>
    },

    renderTableBlock(){
        const self = this, p = self.props,
            colWidth = 100 / p.entitiesTblCols.length;
        if (p.infoPanel) {
            return <div>
                {self.renderTableHeader()}
                {self.renderTableMessages()}
                <div style={{width: 100 - colWidth + "%"}} className="inline-block">
                    {self.renderTableBody()}
                </div>
                <div style={{width: colWidth + "%"}} className="info-list-block inline-block">{p.infoPanel}</div>
            </div>
        }
        return <div>
            {self.renderTableHeader()}
            {self.renderTableMessages()}
            {self.renderTableBody()}
        </div>;
    },


    renderTableHeader(){
        const self = this, p = self.props;

        return <table className="entities-tbl-header" ref={c => {
            self.tableHeader = c
        }}>
            <thead>
            <tr>
                {p.entitiesTblCols.map((col, i) => {
                    const cell = col.headerCell;
                    if (cell) {
                        let styleObj = col.width ? {width: col.width} : {};
                        return <td className={col.className}
                                   key={i}
                                   onClick={() => self.onHeaderCellClick(col)}
                                   style={styleObj}>
                            {cell.children}
                            {self.renderHeaderCellSortMark(col)}
                        </td>
                    }
                    return undefined
                })}
            </tr>
            </thead>
        </table>
    },

    renderHeaderCellSortMark(col){
        const self = this, p = self.props, s = self.state;
        if (col.sortId === s.sortColumn) {
            if (s.sortOrder === 'asc') {
                return Icons.glyph.triangleTop('entities-tbl-header-sort-mark')
            } else {
                return Icons.glyph.triangleBottom('entities-tbl-header-sort-mark')
            }
        }
    },

    renderTableBody(){
        const self = this, p = self.props, isFilterRowsFnDefined = Utils.isFunction(p.filterRowsFn);

        return <table className={Utils.joinDefined(['entities-tbl-body', p.tableBodyClasses], ' ')}>
            <tbody>
            {p.entities.map((obj, objIndex) => {
                let row = null;
                if (!isFilterRowsFnDefined || p.filterRowsFn(obj)) {
                    row = <tr key={obj.id + '-1'}
                              className={self.getTableRowClasses(obj)}
                              onClick={() => self.onRowClick(obj)}>
                        {p.entitiesTblCols.map((col, i) => {
                            const cell = col.bodyCell;
                            if (cell) {
                                let styleObj = col.width ? {width: col.width} : {};
                                const cellClassName = Utils.isFunction(col.className) ? col.className(obj) : col.className;
                                return <td className={cellClassName}
                                           key={i}
                                           style={styleObj}>{Utils.isFunction(cell.childrenFn) ? cell.childrenFn(obj, objIndex) : undefined}</td>
                            }
                            return undefined
                        })}
                    </tr>;
                }

                let secondRow = null;
                if (p.entitiesTblSecondRow) {
                    secondRow = self.renderSecondRow(obj);
                }

                let spacingRow = null;
                if (p.entitiesTblSpacingRow && p.entities.length - 1 > objIndex) {
                    spacingRow = self.renderSpacingRow(obj);
                }

                return Utils.degradeArray([row, secondRow, spacingRow]);
            })}
            </tbody>
        </table>
    },

    renderSecondRow(obj, objIndex){
        const self = this, p = self.props, s = self.state,
            row = p.entitiesTblSecondRow,
            cellContent = Utils.isFunction(row.childrenFn) ? row.childrenFn(obj, objIndex) : undefined;

        return Utils.select(cellContent,
            <tr key={obj.id + '-2'}
                className={self.getTableRowClasses(obj)}
                onClick={() => self.onRowClick(obj)}>
                <td className={row.className}
                    colSpan={p.entitiesTblCols.length}>
                    {cellContent}
                </td>
            </tr>
        )
    },

    renderSpacingRow(obj, objIndex){
        const self = this, p = self.props, s = self.state, row = p.entitiesTblSpacingRow;

        return <tr key={obj.id + '-3'}
                   className={self.getTableRowClasses(obj)}
                   onClick={() => self.onRowClick(obj)}>
            <td className={row.className}
                colSpan={p.entitiesTblCols.length}>
            </td>
        </tr>
    },

    onRowClick(obj){
        const self = this, p = self.props, s = self.state;

        if (s.selectedObjId !== obj.id) {
            self.setState({selectedObjId: obj.id});
            self.onItemSelected(obj)
        } else if (p.onClickSelected) {
            p.onClickSelected(obj)
        }
    },

    onItemSelected(obj) {
        const self = this, p = self.props;
        p.dispatch(Actions.toggleSelectedObject(p.entity, obj));
        self.clearErrorMessage();
        if (Utils.isFunction(p.onItemSelectedFn)) {
            p.onItemSelectedFn(obj)
        }
    },

    onHeaderCellClick(col){
        const self = this, p = self.props, s = self.state;
        let sortColumn = col.sortId;
        if (sortColumn) {
            const sortOrder = Utils.select(s.sortColumn === sortColumn,
                Utils.select(s.sortOrder === 'asc', 'desc', 'asc'),
                'asc'),
                sortOptions = {sortColumn, sortOrder};

            self.setState(sortOptions);
            self.onSearchRequestChange(sortOptions);
        }
    },

    getTableRowClasses(obj){
        const self = this, p = self.props;
        return (p.selectedObj && obj && p.selectedObj.id === obj.id) ? 'entities-tbl-row-selected' : 'entities-tbl-row'
    },

    renderTableMessages(){
        const self = this, p = self.props;
        if (Utils.isArray(p.tableMessages)) {
            if (Utils.isEmptyArray(p.entities) && !Utils.isEmptyArray(p.tableMessages)) {
                return p.tableMessages.map((message, i) => {
                    return <div key={i} className="entities-tbl-message">{message}</div>
                })
            }
        }
    },

    renderLoadingMessage(){
        const self = this, p = self.props;
        if (p._loading) {
            return <div className="entities-tbl-message">{Utils.message('common.search.table.loading')}</div>
        }
    },

    renderCreateBtn(){
        const self = this, p = self.props;
        if (AuthService.hasPermission(p.entityModifyPermssion)) {
            const route = p.entityRouteFn('new');
            return <button type="button"
                           key="create"
                           disabled={!route}
                           className="btn btn-default btn-lg nav-toolbar-btn"
                           onClick={() => {
                               if (route) {
                                   Navigator.navigate(route)
                               }
                           }}>
                {Icons.glyph.plus()}
            </button>
        }
    },

    renderEditBtn(){
        const self = this, p = self.props;
        if (p.selectedObj && AuthService.hasPermission(p.entityModifyPermssion)) {
            const route = p.entityRouteFn(p.selectedObj.id);
            return <button type="button"
                           key="edit"
                           disabled={!route}
                           className="btn btn-default btn-lg nav-toolbar-btn"
                           onClick={() => {
                               if (route) {
                                   Navigator.navigate(route)
                               }
                           }}>
                {Icons.glyph.pencil()}
            </button>
        }
    },

    renderDeleteBtn(){
        const self = this, p = self.props;
        if (p.selectedObj && AuthService.hasPermission(p.entityModifyPermssion)) {
            return <ProgressButton key="delete"
                                   className="btn btn-default btn-lg nav-toolbar-btn"
                                   onClick={self.onDelete}>
                {Icons.glyph.remove()}
            </ProgressButton>
        }
    },

    onDelete(){
        const self = this, p = self.props;
        return DialogService.confirmDelete()
            .then(() => p.dispatch(p.ajaxResource.delete(p.selectedObj.id)))
            .then(
                () => {
                    self.clearErrorMessage()
                },
                (error) => {
                    if (error.status === DataService.status.conflict) {
                        self.setState({errorMessage: Utils.messages(error)})
                    }
                }
            );
    },

    clearErrorMessage() {
        const self = this;
        if (self.state.errorMessage) {
            self.setState({errorMessage: null})
        }
    },

    onSearchRequestChange(newSearchRequest) {
        const self = this, p = self.props;

        p.dispatch(Actions.setSearchRequest(p.entity, newSearchRequest));
        if (p.instantSearch) {
            p.dispatch(p.ajaxResource.findAll(Utils.extend(p.searchRequest, newSearchRequest, {
                firstRecord: 1,
                appendResults: false
            })));
        }
    },

    onScroll(){
        const self = this, p = self.props;
        let documentScrollBottom = Utils.documentScrollBottom();
        if (documentScrollBottom < 80 && !p._loading && self.getCurrentView() === 'list') {
            self.tryLoadNextPage();
        }
    },

    tryLoadNextPage(){
        const self = this, p = self.props;

        let nextFirstRecord = p.searchRequest.firstRecord + Config.pageRecordsCount;
        if (nextFirstRecord <= p.totalRecords) {
            p.dispatch(p.ajaxResource.findAll(Utils.extend(p.searchRequest, {
                firstRecord: nextFirstRecord,
                appendResults: true
            })));
        }
    }
});

module.exports = ListScreen;
