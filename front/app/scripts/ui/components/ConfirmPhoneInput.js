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
    Icons = require('./Icons'),
    TextInput = require('./TextInput'),
    DialogService = require('../services/DialogService');

const ConfirmPhoneInput = React.createClass({

    render() {
        const self = this, p = self.props,
            inputMode = p.confirmationId ? 'confirm' : 'enter';

        return Utils.when(inputMode, {
            enter(){
                return <div className="input-group">
                    {self.renderLeftAddon()}
                    <TextInput id={p.phone.id}
                               key="phone"
                               owner={p.owner}
                               name={p.phone.name}
                               placeholder={p.phone.placeholder}
                               readOnly={p.mobileConfirmed}
                               defaultValue={p.phone.defaultValue}
                               onChange={p.phone.onChange}/>
                    {self.renderInputAddonForEnterMode()}
                </div>
            },
            confirm(){
                return <div className="input-group">
                    {self.renderLeftAddon()}
                    <TextInput id={p.confirm.id}
                               key="owner"
                               owner={p.owner}
                               name={p.confirm.name}
                               placeholder={p.confirm.placeholder}
                               defaultValue={p.confirm.defaultValue}
                               onChange={p.confirm.onChange}/>
                    <span className="input-group-btn">
                        <button className="btn btn-default"
                                type="button"
                                onClick={p.sendSms}>{Icons.glyph.refresh()}</button>
                        <button className="btn btn-default"
                                type="button"
                                onClick={p.goBack}>{Icons.glyph.arrowLeft()}</button>
                    </span>
                </div>
            }
        });
    },

    renderLeftAddon(){
        const self = this, p = self.props;
        if (p.mobileConfirmed) {
            return <span className="input-group-addon">{Icons.glyph.ok('icon-btn-success')}</span>
        } else if (p.validationMessage) {
            return <span className="input-group-addon">{Icons.glyph.warningSign('icon-btn-danger')}</span>
        } else {
            return <span className="input-group-addon">{Icons.glyph.warningSign('icon-btn-warning')}</span>
        }
    },

    renderInputAddonForEnterMode(){
        const self = this, p = self.props;

        if (p.mobileConfirmed) {
            return <span className="input-group-btn">
                <button className="btn btn-default"
                        type="button"
                        onClick={self.onClearNumber}>{Icons.glyph.remove()}</button>
                </span>
        } else if (!p.validationMessage && p.phone.defaultValue) {
            return <span className="input-group-btn">
                <button className="btn btn-default"
                        type="button"
                        onClick={p.sendSms}>{Icons.glyph.send()}</button>
                </span>
        }
    },

    onClearNumber(){
        const self = this, p = self.props;
        DialogService.confirm({
            title: Utils.message('common.dialog.confirm'),
            content: Utils.message('common.mobile.confirmation.confirm.clear.number')

        }).then(() => {
            p.phone.onChange('');
        }, Utils.fn.nop)
    }
});

module.exports = ConfirmPhoneInput;
