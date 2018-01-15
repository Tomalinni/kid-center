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
    PhotosList = require('../components/PhotosList'),
    StudentCardList = require('../components/StudentCardList'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    {Select, ProgressButton} = require('../components/CompactGrid'),
    Validators = require('../Validators'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const {PropTypes} = React;


const Profile = React.createClass({
    propTypes: {
        params: PropTypes.object,
        profile: PropTypes.object,
        newPassword: PropTypes.string,
        newPasswordConfirm: PropTypes.string,
        dispatch: PropTypes.func
    },

    componentDidMount(){
        const self = this, p = self.props;
        p.dispatch(Actions.initEntity('profile'));
        p.dispatch(Actions.clearValidationMessages('profile'));
    },

    render(){
        const self = this, p = self.props;

        return <div>
            <PageToolbar leftButtons={[self.renderSaveBtn()]}/>
            <h4 className="page-title">{Renderers.profile.info(p.profile)}</h4>
            <div className="container with-nav-toolbar">
                { self.renderProfileForm() }
            </div>
        </div>
    },

    renderProfileForm () {
        const self = this, p = self.props;
        return <div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('profile', '_')}/>
                        <ValidationMessage message={self.getValidationMessage('profile', 'oldPass')}/>
                        <TextInput id="oldPass"
                                   owner={p.profile}
                                   name="oldPass"
                                   password={true}
                                   placeholder={Utils.message('common.profile.fields.oldPassword')}
                                   defaultValue={p.profile.oldPass}
                                   onChange={self.onFieldChange.bind(this, 'oldPass')}/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('profile', 'newPass')}/>
                        <TextInput id="newPass"
                                   owner={p.profile}
                                   name="newPass"
                                   password={true}
                                   placeholder={Utils.message('common.profile.fields.newPassword')}
                                   defaultValue={p.profile.newPass}
                                   onChange={self.onFieldChange.bind(this, 'newPass')}/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-md-12">
                    <div className="form-group">
                        <ValidationMessage message={self.getValidationMessage('profile', 'newPassRepeat')}/>
                        <TextInput id="newPassRepeat"
                                   owner={p.profile}
                                   name="newPassRepeat"
                                   password={true}
                                   placeholder={Utils.message('common.profile.fields.repeatNewPassword')}
                                   defaultValue={p.profile.newPassRepeat}
                                   onChange={self.onFieldChange.bind(this, 'newPassRepeat')}/>
                    </div>
                </div>
            </div>
        </div>
    },

    renderSaveBtn(){
        const self = this, p = self.props;
        return <ProgressButton key="save"
                               className="btn btn-default btn-lg nav-toolbar-btn"
                               onClick={() => self.onSave()}>
            {Icons.glyph.save()}
        </ProgressButton>

    },

    onSave(){
        const self = this, p = self.props;

        let validationMessages = Validators.getByEntityId('profile', p.profile);
        if (!validationMessages || Utils.isEmptyArray(Utils.objectValues(validationMessages).filter(Boolean))) {
            return p.dispatch(Actions.ajax.profile.save(p.profile))
                .then(
                    () => p.dispatch(Actions.clearValidationMessages('profile')),
                    (error) => {
                        if (error.status === DataService.status.badRequest) {
                            p.dispatch(Actions.setValidationMessages('profile', error['profile']))
                        }
                    }
                )
        } else {
            p.dispatch(Actions.setValidationMessages('profile', validationMessages))
        }
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('profile', fieldId, newValue))
    },

    getValidationMessage(entity, fieldId){
        const self = this, p = self.props;
        return p.validationMessages[entity][fieldId];
    }
});

function mapStateToProps(state) {
    return state.pages.Profile
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Profile);