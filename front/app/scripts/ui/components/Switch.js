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
    Utils = require('../Utils');

const Switch = React.createClass({
    getInitialState(){
        const self = this, p = self.props;
        return {
            checked: p.checked
        }
    },

    componentWillReceiveProps(nextProps){
        const self = this;
        self.setState({checked: nextProps.checked})
    },

    render(){
        const self = this, p = self.props, labelFn = p.labelFn || self.defaultLabelFn;

        return <span
            className={'control-switch-outline ' +
            Utils.select(p.checked, 'control-switch-outline-checked ', '') +
            Utils.select(p.disabled, 'control-switch-outline-disabled ', '')}
            style={p.style}
            onClick={self.onClick}>
            <span className="control-switch-knob">
                {labelFn(p.checked)}
            </span>
        </span>
    },

    defaultLabelFn(checked){
        return Utils.message(Utils.select(checked, 'button.yes', 'button.no'))
    },

    onClick(){
        const self = this, p = self.props;

        if (!p.disabled) {
            const nextVal = !p.checked;
            self.setState({checked: nextVal});
            if (Utils.isFunction(p.onChange)) {
                p.onChange(nextVal)
            }
        }
    }
});

module.exports = Switch;
