/**
 * Application state object format
 */
const appState = {
    pages: {
        Lessons: {
            entities: {
                //list of entity collections
                users: {//Users entity collection
                    _load: { //extra object to define load status of any entity collection.
                        // Absence of this object means that loading of entity collection was not started
                        status: 'loading'// 'loaded', 'invalid'
                    },
                    1: { //User entity
                        id: 1,
                        name: 'Sergey'
                    }//,..
                },
                templates: {
                    //template used to describe lessons timetable on a week basis
                    1: {
                        id: 1,
                        title: '2016', //any string title
                        startDate: '01.01.2016',
                        //date that this template is used from, end date of template usage is defined by nearest startDate in other templates
                        lessons: {
                            monday: {
                                fitness: [ //subject id
                                    {
                                        id: 't1-fitness-monday-1445', //templateId, subjectId, day, time of lesson
                                        ageGroup: '2-3', //3-5, 5-7
                                        fromMins: 885 //start time in minutes from midnight
                                    }//,..
                                ]//,ballet,english,art,cooking,lego
                            }//,tuesday,wednesday...sunday
                        }
                    }
                },
                lessonPlans: {
                    'f29.05.16-1445': {
                        id: 'f29.05.2016-1445',
                        //date: 29.05.2016 storage field, used in requests
                        status: 'planned',
                        //'closed' lesson is finished and attendance is checked
                        //'revoked' only for rare cases when teacher is unavailable. No other fields are filled in in that case;
                        //'removed' lesson is removed by templates change
                        teacherIds: [1, 2, 3], //teacher and assistant ids
                        visitsSummary: { //brief info about student planned visits
                            total: 9,
                            regular: 3, //no more than 8 regulars in one class, and not more than 10 total visits
                            trial: 4,
                            bonus: 1
                        },
                        studentSlots: { //detailed info about student planned and made visits
                            100: { //student slot id
                                studentId: 1,
                                cardId: 2,
                                visitType: 'regular', //'trial','bonus'
                                status: 'planned' //'visited', 'missed', 'revoked'
                                //Canceled status will be preserved for any visit type
                            }
                        }
                    }//,.. other lesson ids
                },
                students: {
                    1: {
                        id: 1,
                        businessId: 'A0001', //A0001..A9999..Z9999 for regular students, $0001..$9999 for new students
                        nameEn: 'John Doe',
                        nameCn: '李四',
                        gender: 'boy',//'girl'
                        birthDate: '21.12.2012', //dd.mm.yyyy
                        ageGroup: '2-3', //'3-5', '5-7', null (if group should be calculated from age)
                        kinderGardenName: 'Youth',
                        passCardId: '123', //format is not defined
                        cards: { //Cards to visit lessons
                            1: {
                                id: 1,
                                visitType: 'regular', //one of visit types
                                endDate: '01.09.2016',
                                lessonsLimit: 20,
                                lessonsAvailable: 15, //number of unplanned lessons
                                cancelsLimit: 5,
                                cancelsAvailable: 3,
                                suspendsLimit: 2,
                                suspendsAvailable: 2,
                                changesLimit: 2, //total number of provided 'change' actions in student timetable
                                changesAvailable: 2
                            }
                        },
                        lessons: {//student related lessons
                            planned: { //planned lessons for several weeks time range (not all, only currently meaningful)
                                'f29.05.16-1445': {
                                    cardId: '1'
                                }
                            }//,'visited', 'missed', 'revoked'
                        },
                        relatives: {
                            1: {
                                id: 1,
                                role: 'father', //'mother','grandfather','grandmother','sister','brother'
                                mail: 'johndoefather@gmail.com',
                                phone: '123456',
                                passportNumber: '123456',
                                driverLicenseNumber: '123456',
                                passCardId: '123' //format is not defined
                            }//,..
                        }
                    }//,..
                }
            },


            currentDate: '03.06.2016', //date that used in timetable
            visitsViewMode: 'regular',
            lessonProcedure: 'view', //one of Dictionaries lessonProcedure
            planLessonFilter: {
                student: {},//student object
                card: {},//card object from student
                day: {},//day object from dictionary
                time: {},//time object from dictionary
                subject: {},//subject object from dictionary

                _result: {
                    lessons: {
                        //Same structure as lessons in template
                        monday: {
                            fitness: [ //subject id
                                {
                                    id: 't1-fitness-monday-1445', //templateId, subjectId, day, time of lesson
                                    ageGroup: '2-3', //3-5, 5-7
                                    fromMins: 885 //start time in minutes from midnight
                                }//,..
                            ]//,ballet,english,art,cooking,lego
                        }//,tuesday,wednesday...sunday
                    },
                    lessonStatuses: {
                        'f29.05.2016-1445': 'available',
                        'f29.05.2016-1600': 'occupied'
                    }
                }
            }
        },
        Students: {
            entities: {
                students: {//look at Lessons page

                }
            }
        }
    }
};

/**
 * Request to data service
 */
const dataServiceFetchRequest = {
    users: {},
    templates: {
        filter: {
            startDate: {from: '29.05.2016'} // field filter from specified date
        },
        props: [] //optional parameter to request only specified props

    },
    lessonPlans: {
        date: {from: '29.05.2016', to: '03.06.2016'} // field filter from and to specified dates
    },
    students: {
        filter: {
            id: {any: [1, 2, 3]} // field filter, by ids
            //id: {eq: 1} // field filter, by ids
        },
        single: true //fetch only the first available result
    }
};

const dataServiceFetchSingleRequest = {
    students: 1
};


const paymentSearchOptions = {
    cities: [
        {
            id: '1',
            name: 'City1',
            schools: [
                {
                    id: '1',
                    name: 'School1',
                    accounts: [
                        {
                            id: '1',
                            number: '123456'
                        }
                    ]
                }
            ]
        }//,...
    ],
    categories: {
        '1': {
            id: '1',
            name: 'Category1'
        }//,...
    }
};