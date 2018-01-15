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
    moment = require('moment-timezone'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    TemplateDayLessonSlots = require('./TemplateDayLessonSlots');

const TemplateLessonSlots = React.createClass({

    render(){
        const self = this, p = self.props;

        return <div>
            {Dictionaries.day.map(day => {
                    const lessonTemplate = p.lessonTemplate || {},
                        lessons = lessonTemplate.lessons || {};
                    return <TemplateDayLessonSlots key={day.id}
                                                   day={day}
                                                   lessonTemplate={lessonTemplate}
                                                   lessons={lessons[day.id] || {}}
                                                   onLessonClick={p.onLessonClick}
                                                   selectedLesson={p.selectedLesson}/>
                }
            )}
        </div>;
    }
});

module.exports = TemplateLessonSlots;
