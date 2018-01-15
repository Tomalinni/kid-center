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

const React = require('react'),
    {connect} = require('react-redux'),
    AuthService = require('../services/AuthService'),
    Permissions = require('../Permissions'),
    DialogService = require('../services/DialogService'),
    Navigator = require('../Navigator'),
    Utils = require('../Utils'),
    Renderers = require('../Renderers'),
    Actions = require('../actions/Actions'),
    PageToolbar = require('../components/PageToolbar'),
    {ProgressButton} = require('../components/CompactGrid'),
    EntitiesTree = require('../components/EntitiesTree'),
    Icons = require('../components/Icons');

'use strict';

const Categories = React.createClass({

    componentDidMount(){
        const self = this, p = self.props;

        p.dispatch(Actions.ajax.categories.findAll());
    },

    render(){
        const self = this, p = self.props;

        return <div>
            <PageToolbar leftButtons={[self.renderDeleteBtn(), self.renderEditBtn(), self.renderCreateBtn()]}/>

            <h4 className="page-title">{Utils.message('common.pages.categories')}</h4>

            <div className="with-nav-toolbar">
                <EntitiesTree
                    header={Utils.message('common.payments.form.category.name')}
                    nodesMap={p.entitiesMap}
                    rootIds={p.rootIds}
                    onSelectRow={(obj) => p.dispatch(Actions.toggleSelectedObject('categories', obj))}
                    renderRowFn={Renderers.category}
                    isSelectedRowFn={(obj) => p.selectedObj && p.selectedObj.id === obj.id}
                />
            </div>
        </div>
    },

    renderCreateBtn(){
        const self = this, p = self.props;
        if (AuthService.hasPermission(Permissions.paymentsModify)) {
            return <button type="button"
                           key="create"
                           className="btn btn-default btn-lg nav-toolbar-btn"
                           onClick={() => Navigator.navigate(Navigator.routes.category('new'))}>
                {Icons.glyph.plus()}
            </button>
        }
    },

    renderEditBtn(){
        const self = this, p = self.props;
        if (p.selectedObj && AuthService.hasPermission(Permissions.paymentsModify)) {
            return <button type="button"
                           key="edit"
                           className="btn btn-default btn-lg nav-toolbar-btn"
                           onClick={() => Navigator.navigate(Navigator.routes.category(p.selectedObj.id))}>
                {Icons.glyph.pencil()}
            </button>
        }
    },

    renderDeleteBtn(){
        const self = this, p = self.props;
        if (p.selectedObj && AuthService.hasPermission(Permissions.paymentsModify)) {
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
            .then(() => p.dispatch(Actions.ajax.categories.delete(p.selectedObj.id)))
    }
});

function mapStateToProps(state) {
    return state.pages.Categories;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Categories);
