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
    Permissions = require('../Permissions'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    {Row, Col, FormGroup, ColFormGroup, Button} = require('../components/CompactGrid'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    Icons = require('../components/Icons');

const LessonTemplates = React.createClass({
    render(){
        const self = this, p = self.props;

        return <ListScreen entity='lessonTemplates'
                           ajaxResource={Actions.ajax.lessonTemplates}
                           entityModifyPermssion={Permissions.lessonTemplatesModify}
                           entityRouteFn={Navigator.routes.lessonTemplate}
                           pageTitle={Utils.message('common.pages.lessonTemplates')}
                           filterElementFn={self.renderFilterElement}
                           entitiesTblCols={self.columns()}
                           {...p}
        />
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;

        return <Row>
            <ColFormGroup>
                <TextInput id="searchText"
                           name="searchText"
                           placeholder={Utils.message('common.search.text')}
                           defaultValue={p.searchRequest.text}
                           onChange={(text) => {
                               onSearchRequestChange({text})
                           }}/>
            </ColFormGroup>
        </Row>
    },

    columns(){
        return [
            {
                headerCell: {
                    children: Utils.message('common.lessonTemplates.search.table.name')
                },
                bodyCell: {
                    childrenFn: (obj) => obj.name
                }
            }
        ]
    }
});

function mapStateToProps(state) {
    return state.pages.LessonTemplates;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(LessonTemplates);
