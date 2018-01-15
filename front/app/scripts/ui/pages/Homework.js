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
    DatePicker = require('../components/DateComponents').DatePicker,
    AttachmentsList = require('../components/AttachmentsList'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    {Select} = require('../components/CompactGrid'),
    ConfirmPhoneInput = require('../components/ConfirmPhoneInput'),
    Validators = require('../Validators'),
    ValidationMessage = require('../components/ValidationMessage'),
    FormScreen = require('../components/FormScreen'),
    Icons = require('../components/Icons');


const {PropTypes} = React;

const Homework = React.createClass({
    propTypes: {
        params: PropTypes.object,
        homework: PropTypes.object,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;

        const id = p.params.id,
            isNewId = Utils.isNewId(id);
        if (!isNewId) {
            p.dispatch(Actions.ajax.homework.findOne(id));
        } else {
            p.dispatch(Actions.initEntity('homework'));
        }
        p.dispatch(Actions.clearValidationMessages('homework'));
    },

    render(){
        const self = this, p = self.props;
        return <FormScreen entitiesId='homework'
                           entityId='homework'
                           entity={p.homework}
                           pageTitle={Renderers.homework.info(p.homework)}
                           ajaxResource={Actions.ajax.homework}
                           entitiesRoute={Navigator.routes.homeworks}
                           formElementFn={self.renderFormElement}
                           {...p}
        />
    },

    renderFormElement (onFieldChange) {
        const self = this, p = self.props;
        return <div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.homework.search.table.subject')}</label>
                        <ValidationMessage message={self.getValidationMessage('subject')}/>
                        <Select
                            name="subject"
                            placeholder={Utils.message('common.homework.search.table.subject')}
                            value={p.homework.subject}
                            valueKey="id"
                            labelKey="title"
                            options={Dictionaries.lessonSubject}
                            onChange={(opt) => onFieldChange('subject', opt ? opt.id : null)}
                        />
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <label>{Utils.message('common.homework.search.table.ageGroup')}</label>
                        <ValidationMessage message={self.getValidationMessage('ageGroup')}/>
                        <Select
                            name="ageGroup"
                            placeholder={Utils.message('common.homework.search.table.ageGroup')}
                            value={p.homework.ageGroup}
                            valueKey="id"
                            labelKey="title"
                            options={Dictionaries.studentAge}
                            onChange={(opt) => onFieldChange('ageGroup', opt ? opt.id : null)}
                        />
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-md-6">
                    <div className="form-group">
                        <label>{Utils.message('common.homework.search.table.startDate')}</label>
                        <DatePicker id="startDate"
                                    value={p.homework.startDate}
                                    onChange={(opt) => onFieldChange('startDate', opt)}>
                            <button type="button" className="form-control btn btn-default">
                                {Renderers.valOrNotDefined(p.homework.startDate)}
                                &nbsp;{Icons.caret()} </button>
                        </DatePicker>
                    </div>
                </div>
                <div className="col-md-6">
                    <div className="form-group">
                        <label>{Utils.message('common.homework.search.table.endDate')}</label>
                        <DatePicker id="endDate"
                                    value={p.homework.endDate}
                                    onChange={(opt) => onFieldChange('endDate', opt)}>
                            <button type="button" className="form-control btn btn-default">
                                {Renderers.valOrNotDefined(p.homework.endDate)}
                                &nbsp;{Icons.caret()} </button>
                        </DatePicker>
                    </div>
                </div>
            </div>
            <div className="row">
                {self.tryRenderAttachmentRow()}
            </div>
        </div>
    },

    tryRenderAttachmentRow () {
        const self = this, p = self.props,
            isNew = !p.homework.id;
        if (!isNew) {
            return <div className="col-md-12">
                <label>Attachments</label>
                <AttachmentsList uploadUrl={DataService.urls.homework.uploadHomework(p.homework.id)}
                                 downloadUrlFn={(name) => {
                                     return DataService.urls.homework.downloadHomework(p.homework.id, name)
                                 }}
                                 onFileUploaded={(fileNames) => {
                                     p.dispatch(Actions.filesUploaded(fileNames, 'homework'))
                                 }}
                                 onRemoveFile={(fileName) => {
                                     p.dispatch(Actions.ajax.homework.removeFile(p.homework.id, fileName))
                                 }}
                                 fileNames={p.homework.files || []}/>
            </div>
        }
    },

    getValidationMessage(fieldId){
        const self = this, p = self.props;
        return p.validationMessages['homework'][fieldId];
    }
});

function mapStateToProps(state) {
    return Utils.extend(state.pages.Homework,
        {
            roles: state.pages.Roles.entities,
        });
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Homework);