'use strict';

const React = require('react'),
    {connect} = require('react-redux'),
    Permissions = require('../Permissions'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    {Row, Col, FormGroup, ColFormGroup, Button} = require('../components/CompactGrid'),
    Icons = require('../components/Icons');

const Cards = React.createClass({
    render(){
        const self = this, p = self.props;
        let detailsBlockClass = 'details-block-main';
        if (p.detailsCollapsed) {
            detailsBlockClass += ' details-block-main-collapsed'
        }

        return <div className={detailsBlockClass}>
            <ListScreen entity='cards'
                        ajaxResource={Actions.ajax.cards}
                        renderToolbarBtns={self.renderToggleDetailsBtn}
                        listClasses={Utils.select(!p.detailsCollapsed, 'with-details-block-main')}
                        tableBodyClasses="entities-tbl-body-high"
                        entityModifyPermssion={Permissions.cardsModify}
                        entityRouteFn={Navigator.routes.card}
                        pageTitle={Utils.message('common.pages.cards')}
                        filterElementFn={self.renderFilterElement}
                        entitiesTblCols={self.columns()}
                        {...p}
            />
            {self.renderDetailsSection()}
        </div>
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;

        return <Row>
            {Dictionaries.cardStateFilter.map(opt => {
                return <ColFormGroup key={'cardState-' + opt.id} classes="col-xs-4">
                    <Button type="button"
                            onClick={() => onSearchRequestChange({activeState: opt.id})}>
                        {p.searchRequest.activeState === opt.id ? Icons.glyph.ok('icon-btn-success icon-btn-rpad') : undefined}
                        {opt.title}
                    </Button>
                </ColFormGroup>
            })}

            <ColFormGroup>
                <table className="period-btn-table">
                    <tbody>
                    <tr>
                        {Dictionaries.studentDashboardLessonVisitTypeFilter.withAll.map(opt => {
                            return <td key={opt.id}>
                                <Button type="button"
                                        onClick={() => onSearchRequestChange({visitType: opt.id})}>
                                    {p.searchRequest.visitType === opt.id ? Icons.glyph.ok('icon-btn-success icon-btn-rpad') : undefined}
                                    {opt.title}
                                </Button>
                            </td>
                        })}
                    </tr>
                    </tbody>
                </table>
            </ColFormGroup>

        </Row>
    },

    renderToggleDetailsBtn(){
        const self = this, p = self.props;
        return <button type="button"
                       key="details"
                       className="btn btn-default btn-lg nav-toolbar-btn"
                       onClick={() => p.dispatch(Actions.setPageMode('cards', 'list', 'card', Utils.select(p.detailsCollapsed, 'withDetails', 'noDetails')))}>
            {Icons.glyph.listAlt()}
        </button>
    },

    renderDetailsSection(){
        const self = this, p = self.props,
            card = p.selectedObj;

        if (!p.detailsCollapsed) {
            if (card) {
                return self.renderCardInfo()
            } else {
                const message = Utils.select(card && card.id, Utils.message('common.search.table.loading'), Utils.message('common.cards.card.not.selected'));
                return <div className="entities-block-details student-block-details">
                    <div className="entities-tbl-message">{message}</div>
                </div>
            }
        }
    },

    renderCardInfo(){
        const self = this, p = self.props,
            card = p.selectedObj;

        return <div className="entities-block-details student-block-details">
            <div className="container">
                <Row>
                    <ColFormGroup classes="col-xs-5">
                        <label>{Utils.message('common.cards.search.table.validity')}</label>
                        <div>{Renderers.cardValidityRange(card)}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-3">
                        <label>{Utils.message('common.cards.search.table.maxSalesCount')}</label>
                        <div>{card.maxSalesCount}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-4">
                        <label>{Utils.message('common.cards.search.table.allowedSubjects')}</label>
                        <div>{Renderers.arr(Utils.bitmaskToArrayItems(card.allowedSubjectsMask, Dictionaries.lessonSubject), ', ', Utils.obj.key('title'))}</div>
                    </ColFormGroup>
                </Row>
                <Row>
                    <ColFormGroup classes="col-xs-3">
                        <label>{Utils.message('common.cards.search.table.lessonsLimit')}</label>
                        <div>{card.lessonsLimit}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-3">
                        <label>{Utils.message('common.cards.search.table.cancelsLimit')}</label>
                        <div>{card.cancelsLimit + ';' + card.lateCancelsLimit + ';' + card.lastMomentCancelsLimit + ';' + card.undueCancelsLimit}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-3">
                        <label>{Utils.message('common.cards.search.table.missLimit')}</label>
                        <div>{card.missLimit}</div>
                    </ColFormGroup>
                    <ColFormGroup classes="col-xs-3">
                        <label>{Utils.message('common.cards.search.table.suspendsLimit')}</label>
                        <div>{card.suspendsLimit}</div>
                    </ColFormGroup>
                </Row>
            </div>
        </div>
    },

    columns(){
        return [
            Utils.cols.col(Utils.message('common.cards.search.table.ageRange'), obj => Dictionaries.ageRange.byId(obj.ageRange).title),
            Utils.cols.col(Utils.message('common.cards.search.table.visitType'), obj => Dictionaries.visitType.byId(obj.visitType).title),
            Utils.cols.col(Utils.message('common.cards.search.table.price'), Utils.obj.key('price'), null, null, 'price'),
            Utils.cols.col(Utils.message('common.cards.search.table.lessonsLimit'), Utils.obj.key('lessonsLimit'), null, null, 'lessonsLimit'),
            Utils.cols.col(Utils.message('common.cards.search.table.maxDiscount'), Utils.obj.key('maxDiscount')),
            Utils.cols.col(Utils.message('common.cards.search.table.duration'), obj => Renderers.cardDuration(obj)),
            Utils.cols.col(Utils.message('common.cards.search.table.validity'), obj =>
                <span>{Renderers.cardActiveState(obj, 'shortTitle')}&nbsp;{Renderers.cardValidity(obj)}</span>, '105px'),
            Utils.cols.col(Utils.message('common.cards.search.table.sold'), Utils.obj.key('soldCount'))
        ]
    }
});

function mapStateToProps(state) {
    return state.pages.Cards;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Cards);
