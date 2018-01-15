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
    StudentCardForm = require('../components/StudentCardForm'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    EntityLifeCyclePanel = require('../components/EntityLifeCyclePanel'),
    {ProgressButton} = require('../components/CompactGrid'),
    ConfirmPhoneInput = require('../components/ConfirmPhoneInput'),
    Validators = require('../Validators'),
    ValidationMessage = require('../components/ValidationMessage'),
    Icons = require('../components/Icons');

const {PropTypes} = React;

const StudentCard = React.createClass({
    propTypes: {
        params: PropTypes.object,
        studentCard: PropTypes.object,
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
            p.dispatch(Actions.ajax.studentCards.findOne(id));
        } else if (isNewId) {
            p.dispatch(Actions.initEntity('studentCards'));
        }
        p.dispatch(Actions.ajax.studentCards.findPhotos(id));
        p.dispatch(Actions.clearValidationMessages('studentCards'));
    },

    render(){
        const self = this, p = self.props;

        return <div>
            <PageToolbar leftButtons={[self.renderBackBtn(), self.renderSaveBtn()]}/>
            <h4 className="page-title">{Utils.message('common.student.card.edit.toolbar.title')}</h4>
            <EntityLifeCyclePanel {...p}
                                  entity="studentCards"
                                  renderChildrenFn={self.renderForm}/>
        </div>
    },

    renderForm(){
        const self = this, p = self.props;
        return <StudentCardForm studentCard={p.studentCard}
                                validationMessages={p.validationMessages}
                                onFieldChange={self.onFieldChange}
                                photos={p.cardPhotos}
                                onPhotoUploaded={fileNames => p.dispatch(Actions.filesUploaded(fileNames, 'studentCards'))}
                                onRemovePhoto={fileName => p.dispatch(Actions.ajax.studentCards.removeFile(p.studentCard.id, fileName))}
                                onChangeShownPhoto={fileName => p.dispatch(Actions.changeShownFile(fileName, 'studentCards'))}
                                shownPhotoName={p.shownPhoto}
        />
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

        const validationMessages = Validators.studentCards(p.studentCard);

        if (Utils.isAllValuesEmpty(validationMessages)) {
            return p.dispatch(Actions.ajax.studentCards.save(p.studentCard))
                .then(
                    () => Navigator.navigate(Navigator.routes.students),
                    (error) => {
                        if (error.status === DataService.status.badRequest) {
                            p.dispatch(Actions.setValidationMessages('studentCards', validationMessages))
                        }
                    })
        } else {
            p.dispatch(Actions.setValidationMessages('studentCards', validationMessages))
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

    onBack(){
        const self = this, p = self.props;
        if (!Utils.lifeCycle.entity(p, 'studentCards').saved) {
            DialogService.confirmBack().then(() => {
                Navigator.navigate(Navigator.routes.students)
            }, Utils.fn.nop)
        } else {
            Navigator.navigate(Navigator.routes.students)
        }
    },

    onFieldChange(fieldId, newValue) {
        const self = this, p = self.props;
        p.dispatch(Actions.setEntityValue('studentCards', fieldId, newValue))
    }
});

function mapStateToProps(state) {
    return state.pages.StudentCard
}

function mapDispatchToProps(dispatch) {
    return {dispatch}
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(StudentCard);
