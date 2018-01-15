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

const Redux = require('redux'),
    moment = require('moment-timezone'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    Validators = require('../Validators'),
    EntityLifeCycleReducerFactory = require('./EntityLifeCycleReducerFactory'),
    ReducersMap = require('./ReducersMap');

const reducers = new ReducersMap(getInitialState())
    .add({type: 'ajaxFinishedSuccess', entity: 'lessonTemplates', operation: 'findOne'},
        (state, action) => {
            return Utils.extend(state, {lessonTemplate: action.response});
        })
    .add({type: 'ajaxFinishedSuccess', entity: 'lessonTemplates', operation: 'save'},
        (state, action) => {
            return Utils.extend(state, {
                lessonTemplate: Utils.extend(state.lessonTemplate, action.response)
            });
        })
    .add({type: 'initEntity', entity: 'lessonTemplates'},
        (state, action) => {
            return Utils.extend(state, {lessonTemplate: {}});
        })
    .add({type: 'setEntityValue', entity: 'lessonTemplates'},
        (state, action) => {
            const lessonTemplate = Utils.extend(state.lessonTemplate, {[action.fieldId]: action.newValue});
            return Utils.merge(state, {
                lessonTemplate: lessonTemplate,
                validationMessages: {lessonTemplates: Validators.lessonTemplates(lessonTemplate, action.fieldId)}
            });
        })
    .add({type: 'setValidationMessages', entity: 'lessonTemplates'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {lessonTemplates: action.messages}});
        })
    .add({type: 'clearValidationMessages', entity: 'lessonTemplates'},
        (state, action) => {
            return Utils.extend(state, {validationMessages: {lessonTemplates: {}}});
        })
    .add({type: 'selectLesson'},
        (state, action) => {
            return Utils.extend(state, {selectedLesson: action.lesson});
        })
    .add({type: 'setLessonAgeGroup'},
        (state, action) => {
            const lesson = action.lesson,
                templateLessons = state.lessonTemplate.lessons || {},
                lessonsByDay = templateLessons[lesson.day] || {},
                lessonsBySubject = lessonsByDay[lesson.subject] || [],
                nextLesson = Utils.extend({}, lesson, {ageGroup: action.ageGroup});
            let nextLessonsBySubject;

            if (action.ageGroup) {
                nextLessonsBySubject = Utils.arr.put(lessonsBySubject, nextLesson, Utils.obj.id)
            } else {
                nextLessonsBySubject = Utils.arr.remove(lessonsBySubject, {id: lesson.id}, Utils.obj.id)
            }

            return Utils.merge(state, {
                lessonTemplate: {
                    lessons: {
                        [lesson.day]: {
                            [lesson.subject]: nextLessonsBySubject
                        }
                    }
                },
                selectedLesson: nextLesson
            });
        });

function getInitialState() {
    return {
        lessonTemplate: {},
        selectedLesson: null,
        validationMessages: {
            lessonTemplates: {}
        }
    };
}

function LessonTemplateReducer(state = getInitialState(), action) {
    return reducers.reduce(state, action);
}

module.exports = Utils.chainReducers(EntityLifeCycleReducerFactory('lessonTemplates'), LessonTemplateReducer);
