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

const Icons = require('../components/Icons'),
    Utils = require('../Utils');

const Buttons = {
    toolbar: {
        back(onClick){
            return renderToolbarBtn('back', Icons.glyph.arrowLeft(), onClick)
        },
        save(onClick){
            return renderToolbarBtn('save', Icons.glyph.save(), onClick)
        }
    }
};

function renderToolbarBtn(key, icon, onClick) {
    return <button type="button"
                   key={key}
                   className="btn btn-default btn-lg nav-toolbar-btn"
                   onClick={onClick}>
        {icon}
    </button>
}

module.exports = Buttons;
