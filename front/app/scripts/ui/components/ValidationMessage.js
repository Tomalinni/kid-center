/*
 * (C) Copyright ${YEAR} Legohuman (https://github.com/Legohuman).
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

const React = require('react'),
    Utils = require('../Utils');

const ValidationMessage = React.createClass({

    render() {
        const p = this.props;
        if (p.message) {
            return <div className={'alert-field-warning ' + Utils.select(p.centered, 'text-centered', '')}>
                {p.message}
            </div>
        }
        return null
    }
});

module.exports = ValidationMessage;
