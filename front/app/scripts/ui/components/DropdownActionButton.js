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
    Utils = require('../Utils'),
    Config = require('../Config'),
    Icons = require('../components/Icons');

const DropdownActionButton = React.createClass({

    render(){
        const self = this, p = self.props;

        return <div className={Utils.select(p.inline, 'btn-group', 'dropdown')}>
            <button type="button"
                    data-toggle="dropdown"
                    disabled={Object.keys(p.actions).length === 0}
                    className={'btn dropdown-toggle ' + (p.btnClasses || 'btn-default')}>
                {p.title || Icons.glyph.cog()}
            </button>

            {Utils.selectFn(Object.keys(p.actions).length, () => {
                return <ul className={'dropdown-menu ' + (p.dropdownMenuClasses || 'dropdown-menu-right')}>
                    {Object.keys(p.actions).map(actionId => {
                        const action = p.actions[actionId];

                        return <li key={actionId}>
                            <a onClick={() => Utils.isFunction(action.fn) ? action.fn(p.dispatch) : p.actionFn(actionId)}>
                                {action.title}
                            </a>
                        </li>
                    })}
                </ul>
            })}
        </div>
    }
});

module.exports = DropdownActionButton;
