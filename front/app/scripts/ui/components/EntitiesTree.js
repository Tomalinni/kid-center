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
    ResizeSensor = require('css-element-queries/src/ResizeSensor'),
    AuthService = require('../services/AuthService'),
    DialogService = require('../services/DialogService'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    Actions = require('../actions/Actions'),
    PageToolbar = require('../components/PageToolbar'),
    TextInput = require('../components/TextInput'),
    Icons = require('../components/Icons');

const EntitiesTree = React.createClass({

    render(){
        const self = this, p = self.props;
        return <div className="entities-tree">
            <div className="entities-tree-header">
                {p.header}
            </div>
            <div className="entities-tree-body">
                {self.renderCategoryNodes(p.rootIds)}
            </div>
        </div>
    },

    renderCategoryNodes(nodeIds){
        const self = this, p = self.props;
        return nodeIds.map(nodeId=> {
            const node = p.nodesMap[nodeId];
            if (!p.isNodeVisibleFn || p.isNodeVisibleFn(node)) {
                return <div key={nodeId} className="entities-tree-node">
                    <div className={self.getTreeRowClasses(node)}
                         onClick={()=>p.onSelectRow(node)}>
                        {p.renderRowFn(node)}
                    </div>
                    {self.renderCategoryNodes(node.children.map(Utils.obj.id))}
                </div>
            }
        })
    },

    getTreeRowClasses(node){
        const self = this, p = self.props;
        let isSelected = p.isSelectedRowFn && p.isSelectedRowFn(node);
        return isSelected ? 'entities-tree-row entities-tree-row-selected' : 'entities-tree-row'
    }
});

module.exports = EntitiesTree;
