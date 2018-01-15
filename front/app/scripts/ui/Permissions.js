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

const Permissions = {
    lessonsRead: '',                         //View lessons information
    lessonsPlan: '',                         //Plan student lessons (suspend)
    lessonsClose: '',                        //Mark student presence and absence, close lessons when finished
    lessonsRevoke: '',                       //Revoke lessons
    lessonsModify: '',                       //Modify lesson templates
    studentsRead: '',                        //View student information
    studentsModify: '',                      //Create, modify, delete students and related info (student cards, relative, kinder gardens)
    cardsRead: '',                           //View card information
    cardsModify: '',                         //Create, modify, delete cards
    teachersRead: '',                        //View teachers information
    teachersModify: '',                      //Create, modify, delete teachers
    paymentsRead: '',                        //View payments information
    paymentsModify: '',                      //Create, modify, delete payments and related info (accounts, schools, categories)
    saveNotConfirmedMobileNumber: '',        //Save mobile phones without sending confirmation SMS
    manageUsers: '',                         //Create, edit, delete users, manage their roles and permissions
    hasChildren: '',                         //Determines if user can have children and see children page.
    homeworkRead: '',                        //View homework information
    homeworkModify: '',                      //Create, modify, delete homework
    lessonTemplatesRead: '',                 //View lesson template (schedule) information
    lessonTemplatesModify: '',               //Modify lesson template (schedule) information
    studentCardsRead: '',                    //View student cards information
    studentCardsModify: '',                  //Modify student cards information
    studentCallsRead: '',                    //View student calls information
    studentCallsModify: '',                  //Modify student calls information
    studentCardPaymentPrefModify: ''         //Modify student card payment mapping preferences
}, pm = Permissions;
Object.keys(pm).forEach(k => pm[k] = k);

module.exports = Permissions;
