Students:
Code: not editable, not validated
Name cn, Name english: one of fields should be filled in. Max length: 50 characters
Birth date: Required, Max date, 0.5 year ago from current date. Min date 7 years ago.
Gender: required
Mobile: required, 11 digits
Kinder garden: optional
Advised by: optional, Max length: 255 characters
Comment: optional, Max length: no limit


Relative:
Role: required
Name: required, 50 characters
Mail: optional, mail format
Driver license: optional [A-Z0-9], 25 characters (No strict format defined)
Passport: optional [A-Z0-9], 20 characters (Format changes frequently)
Mobile: optional, 11 digits


Student card:
Days: positive number, if activated more than count of days from activation date else any positive number
Keep in mind that for following fields current value and available value will be shifted together on change.
Lessons available: positive number
Cancels available: positive number
Suspends available: positive number
Changes available: positive number

Teacher:
Name: required, 50 characters

Student-relative rules change.
All new students should have at least one relative. This relative should have confirmed mobile number. Mobile is confirmed during user self-registration if he registerd by manager. After entering mobile number and pressing send SMS button, sms with confirmation code will be sent. This code should be entered into Confirmation code field. After that student should be saved or registration process should be finished.
If mobile number is changed new number should also be confirmed by the same rules. For old students with entered mobile confirmation flag would be set to true. The main rule about phone number confirmation is that relative without confirmed mobile number could not register and add students. Manager also could not register student without relative mobile number confirmation. Only users with corresponding role (typically admin) could register relatives without confirmation, confirmation flag is NOT set in that case. 

Lessons scheduling.
Students could schedule lessons either for short period of 7 days or for the whole card duration, but not for some period in between. 
Short period could start from current moment till 3 weeks time window from current moment.
Lessons schedule for long period should have regular pattern that is repeated every week. Student could not plan long period with different lessons (identified by day-time-subject) planned at different weeks.
To plan some extra lessons for specific day student should change one of planned lesson to that specific day-time-subject.
Modifying schedule for specific day is done by the same rules.