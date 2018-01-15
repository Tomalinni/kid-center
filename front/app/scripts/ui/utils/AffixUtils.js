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

const preAffixBlockAttr = 'data-pre-affix-block';

const AffixUtils = {
    applyFor(elem){
        const $elem = $(elem),
            scrollTopLimit = $elem.offset().top;
        let $preAffix = $elem.prev('[' + preAffixBlockAttr + ']');

        if ($preAffix.length === 0) {
            $preAffix = $('<div/>').addClass('block-pre-affix')
                .attr(preAffixBlockAttr, 'true');
            $preAffix.insertBefore($elem)
        }

        $preAffix.css({height: $elem.outerHeight(true)});
        AffixUtils.invoke($elem[0], scrollTopLimit);
        AffixUtils.invoke($preAffix[0], scrollTopLimit);
    },
    invoke(elem, calcTopOffsetFn){
        $(elem).affix({
            offset: {
                top: calcTopOffsetFn
            }
        });
    },
    disposeFor(elem){
        const $elem = $(elem),
            $preAffix = $elem.prev('[' + preAffixBlockAttr + ']');

        if ($preAffix.length > 0) {
            $preAffix.remove()
        }
    }
};

module.exports = AffixUtils;