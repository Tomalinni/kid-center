'use strict';

const React = require('react'),
    {connect} = require('react-redux'),
    AuthService = require('../services/AuthService'),
    Permissions = require('../Permissions'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    DataService = require('../services/DataService'),
    {Row, Col, FormGroup, ColFormGroup, Button, Select} = require('../components/CompactGrid'),
    PeriodsSearchGroup = require('../components/PeriodsSearchGroup'),
    Renderers = require('../Renderers'),
    AttachmentsCarousel = require('../components/AttachmentsCarousel'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const Homeworks = React.createClass({

    render(){
        const self = this, p = self.props;

        return <div><ListScreen entity='homework'
                                ajaxResource={Actions.ajax.homework}
                                instantSearch={false}
                                entityModifyPermssion={Permissions.homeworkModify}
                                entityRouteFn={Navigator.routes.homework}
                                pageTitle={Utils.message('common.pages.homeworks')}
                                filterElementFn={self.renderFilterElement}
                                entitiesTblCols={self.columns()}
                                {...p}
        />
            {self.renderAttachmentsCarousel()}</div>
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;
        self.onSearchRequestChange = onSearchRequestChange;

        return <div>
            {self.renderPeriodsGroup()}
            <Row>
                <ColFormGroup classes="col-xs-5">
                    <Select
                        name="subject"
                        placeholder={Utils.message('common.homework.search.table.subject')}
                        value={Dictionaries.lessonSubject.byId(p.searchRequest.subject)}
                        valueRenderer={Renderers.dictOption}
                        optionRenderer={Renderers.dictOption}
                        options={Dictionaries.lessonSubject}
                        onChange={opt => self.onSearchRequestChange({subject: opt && opt.id}) }/>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-5">
                    <Select
                        name="ageGroup"
                        placeholder={Utils.message('common.homework.search.table.ageGroup')}
                        value={Dictionaries.studentAge.byId(p.searchRequest.ageGroup)}
                        valueRenderer={Renderers.dictOption}
                        optionRenderer={Renderers.dictOption}
                        options={Dictionaries.studentAge}
                        onChange={opt => self.onSearchRequestChange({ageGroup: opt && opt.id}) }/>
                </ColFormGroup>
                <ColFormGroup classes="col-xs-2">
                    <Button onClick={() => p.dispatch(Actions.ajax.homework.findAll(p.searchRequest))}>
                        {Icons.glyph.refresh()}
                    </Button>
                </ColFormGroup>
            </Row>
        </div>
    },

    renderPeriodsGroup(){
        const self = this, p = self.props;
        return <PeriodsSearchGroup searchRequest={p.searchRequest}
                                   periodFieldId="activeDatePeriod"
                                   startDateFieldId="startDate"
                                   endDateFieldId="endDate"
                                   onPeriodChange={self.onSearchRequestChange}/>
    },

    columns(){
        const self = this;
        return [
            {
                headerCell: {
                    children: Utils.message('common.homework.search.table.subject')
                },
                bodyCell: {
                    childrenFn: (obj)=> Dictionaries.lessonSubject.byId(obj.subject).title
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.homework.search.table.ageGroup')
                },
                bodyCell: {
                    childrenFn: (obj)=> Dictionaries.studentAge.byId(obj.ageGroup).title
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.homework.search.table.startDate')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.startDate
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.homework.search.table.endDate')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.endDate
                }
            },
            {
                headerCell: {
                    children: Utils.message('common.homework.search.table.files')
                },
                bodyCell: {
                    childrenFn: (obj)=> self.renderAttachmentsBtn(obj)
                }
            }
        ]
    },


    renderAttachmentsBtn(obj){
        const self = this, p = self.props;
        if (obj && obj.files.length > 0) {
            return <button type="button"
                           className="btn btn-default btn-row"
                           onClick={(e) => {
                               p.dispatch(Actions.showCarouselGallery('homework', obj));
                               e.stopPropagation()
                           }}>
                {Icons.glyph.list()}
            </button>
        }
    },

    renderAttachmentsCarousel(){
        const self = this, p = self.props;
        return <AttachmentsCarousel
            showCarousel={p.selectedObj && p.shownAttachments}
            shownAttachments={p.shownAttachments}
            currentAttachmentUrl={(attachment) => DataService.urls.homework.downloadHomework(p.selectedObj.id, attachment)}
            onHideFn={() => p.dispatch(Actions.hideCarouselGallery())}
            {...p}
        />
    }
});

function mapStateToProps(state) {
    return state.pages.Homeworks
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Homeworks);
