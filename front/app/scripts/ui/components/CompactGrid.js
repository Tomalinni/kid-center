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
    Icons = require('../components/Icons'),
    ReactSelect = require('react-select');

function Row({children, style, classes}) {
    return <div className={'row row-compact ' + classes}
                style={style}
    >{children}</div>
}

function Col({children, classes = 'col-xs-12', style}) {
    const classNames = 'col-compact ' + classes;
    return <div className={classNames}
                style={style}>
        {children}
    </div>
}

function FormGroup({children, classes = '', style}) {
    const classNames = 'form-group form-group-compact ' + classes;
    return <div className={classNames}
                style={style}>
        {children}
    </div>
}

function ColFormGroup({children, classes = 'col-xs-12', formGroupClasses = 'form-group-compact', style}) {
    const classNames = 'col-compact ' + classes;
    return <div className={classNames}
                style={style}>
        <div className={'form-group ' + formGroupClasses}>
            {children}
        </div>
    </div>
}

function TableButtonGroup({options, renderBtnFn}) {
    return <ColFormGroup>
        <table className="period-btn-table">
            <tbody>
            <tr>
                {options.map(opt => {
                    return <td key={opt.id}>
                        {renderBtnFn(opt)}
                    </td>
                })}
            </tr>
            </tbody>
        </table>
    </ColFormGroup>

}

function Button({children, classes = 'btn-default', onClick, disabled, style}) {
    const classNames = 'btn btn-wide ' + classes;
    return <button type="button"
                   style={style}
                   disabled={disabled}
                   className={classNames}
                   onClick={onClick}>
        {children}
    </button>
}

function CircleButton({children, classes = 'btn-default', onClick, disabled, style}) {
    const classNames = 'btn btn-circle ' + classes;
    return <button type="button"
                   style={style}
                   disabled={disabled}
                   className={classNames}
                   onClick={onClick}>
        {children}
    </button>
}

function InlineButton({children, classes = 'btn-default', onClick, disabled, style}) {
    const classNames = 'btn ' + classes;
    return <button type="button"
                   style={style}
                   disabled={disabled}
                   className={classNames}
                   onClick={onClick}>
        {children}
    </button>
}

const ProgressButton = React.createClass({
    getInitialState(){
        return {operationInProgress: false}
    },

    render(){
        const self = this, p = self.props, s = self.state;

        return <button {...p}
                       type="button"
                       disabled={Utils.select(s.operationInProgress, true, p.disabled)}
                       onClick={Utils.wrapPromiseFn(p.onClick, self.startProgress, self.stopProgress)}>
            {Utils.selectFn(s.operationInProgress, () => Icons.glyph.refresh('glyphicon-animate-spin'), () => p.children)}
        </button>
    },

    startProgress(){
        this.setState({operationInProgress: true})
    },

    stopProgress(){
        this.setState({operationInProgress: false})
    }
});

function MenuItem({children, onClick}) {
    return <li>
        <a onClick={onClick}>{children}</a>
    </li>
}

const Select = React.createClass({
    commonProps: {
        autosize: false,
        autoBlur: true,
        backspaceRemoves: false,
        deleteRemoves: false,
        noResultsText: Utils.message('common.select.no.results'),
        searchPromptText: Utils.message('common.select.search.prompt'),
        loadingPlaceholder: Utils.message('common.select.loading.placeholder'),
        menuContainerStyle: {zIndex: 5}
    },
    asyncProps: {
        filterOptions: opts => opts, //filtering is done by server
        cache: false //prevent side effects
    },

    render(){
        const self = this, p = self.props;

        if (Utils.isTrue(p.async)) {
            const selectProps = Utils.extend(p, self.commonProps, self.asyncProps);
            return <ReactSelect.Async {...selectProps} ref="inner"/>
        } else {
            const selectProps = Utils.extend(p, self.commonProps);
            return <ReactSelect {...selectProps} ref="inner"/>
        }
    },

    focus(){
        this.refs.inner.focus();
    }
});

module.exports = {
    Row,
    Col,
    FormGroup,
    ColFormGroup,
    TableButtonGroup,
    Button,
    CircleButton,
    InlineButton,
    ProgressButton,
    MenuItem,
    Select
};
