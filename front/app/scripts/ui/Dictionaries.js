'use strict';

const Utils = require('./Utils'),
    Permissions = require('./Permissions');

const allItem = {
    id: 'all',
    title: Utils.message('common.label.all'),
    shortTitle: Utils.message('common.label.all')
};

const Dictionaries = {
    responseStatus: create(['0', '400', '404', '401', '403', '409', '502']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.error.responseStatus.'))),

    day: create(['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.day.'))),

    studentAge: create([
        {id: 'g2_3', title: '2-3', minAge: 2, maxAge: 3},
        {id: 'g3_5', title: '3-5', minAge: 3, maxAge: 5},
        {id: 'g5_7', title: '5-7', minAge: 5, maxAge: 8},
        {id: 'g1_1y5', title: '1-1,5', minAge: 1, maxAge: 1.5},
        {id: 'g1y5_2', title: '1,5-2', minAge: 1.5, maxAge: 2},
    ]),

    ageRange: create([
        {id: 'r1_2', title: '1-2'},
        {id: 'r2_7', title: '2-7'},
    ]),

    studentGender: create(['boy', 'girl']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.gender.'))),

    studentStatus: create(['registered', 'lessonPlanned', 'lessonVisited', 'trialEnd', 'cardPaid']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.status.'))),

    lessonSubject: indexByFirstLetter(
        create([
            {id: 'cooking', duration: 60, ageRange: 'r2_7'},
            {id: 'art', duration: 60, ageRange: 'r2_7'},
            {id: 'english', duration: 60, ageRange: 'r2_7'},
            {id: 'lego', duration: 60, ageRange: 'r2_7'},
            {id: 'fitness', duration: 60, ageRange: 'r2_7'},
            {id: 'ballet', duration: 60, ageRange: 'r2_7'},
            {id: 'z_sFitness', duration: 30, ageRange: 'r1_2'},
            {id: 'y_sMusic', duration: 30, ageRange: 'r1_2'},
            {id: 'x_sArt', duration: 30, ageRange: 'r1_2'},
        ].map(o => Utils.addLocalizedProp(o, 'title', 'common.lesson.subject.'))
            .map(o => Utils.addLocalizedProp(o, 'shortTitle', 'common.lesson.subject.short.')))
    ),

    lessonTime: create(['0900', '0945', '1015', '1030', '1115', '1130', '1200', '1330', '1445', '1600', '1715', '1830']
        .map(id => ({id, title: Utils.hoursMinutesToTime(id)}))),

    visitType: create([
        {
            id: 'regular',
            planAheadDaysLimit: 21,
            plannedLessonsSpan: 7,
            chargeless: false
        },
        {
            id: 'trial',
            planAheadDaysLimit: 21,
            plannedLessonsSpan: 7,
            chargeless: false
        },
        {
            id: 'bonus',
            planAheadDaysLimit: 21,
            plannedLessonsSpan: 7,
            chargeless: true
        },
        {
            id: 'transfer',
            planAheadDaysLimit: 21,
            plannedLessonsSpan: 7,
            chargeless: false
        },
    ].map(o => Utils.addLocalizedProp(o, 'title', 'common.visit.type.'))),

    lessonsView: create([
        {
            id: 'table',
            icon: 'th'
        },
        {
            id: 'list',
            icon: 'th-list'
        }
    ].map(o => Utils.addLocalizedProp(o, 'title', 'common.lessons.view.'))),

    visitsSummary: create([
        {
            id: 'freeSlots',
            viewMode: 'free',
            sumType: 'total'
        },
        {
            id: 'bookedSlotsTotal',
            viewMode: 'booked',
            sumType: 'total'
        },
        {
            id: 'bookedSlotsRegular',
            viewMode: 'booked',
            sumType: 'regular'
        },
        {
            id: 'bookedSlotsTrial',
            viewMode: 'booked',
            sumType: 'trial'
        },
        {
            id: 'bookedSlotsBonus',
            viewMode: 'booked',
            sumType: 'bonus'
        },
        {
            id: 'advanced',
            viewMode: 'advanced',
            sumType: 'total'
        }
    ].map(o => Utils.addLocalizedProp(o, 'title', 'common.visits.summary.'))),

    lessonProcedure: create([
        {
            id: 'view',
            boundToCard: false,
            blockFurtherDays: false,
            blockSameTime: false,
            usePickedLessons: false,
            useAvailableLessons: false,
            filterLessonsByStudent: true
        },
        {
            id: 'plan',
            boundToCard: true,
            blockFurtherDays: true,
            blockSameTime: true,
            usePickedLessons: true,
            acceptableLessonStatusesToPlan: ['available'],
            useAvailableLessons: true,
            filterLessonsByStudent: false
        },
        {
            id: 'unplan',
            boundToCard: true,
            blockFurtherDays: false,
            blockSameTime: false,
            usePickedLessons: false,
            useAvailableLessons: true,
            filterLessonsByStudent: true
        },
        {
            id: 'suspend',
            boundToCard: true,
            blockFurtherDays: false,
            blockSameTime: false,
            usePickedLessons: false,
            useAvailableLessons: false,
            filterLessonsByStudent: false
        }
    ].map(o => Utils.addLocalizedProp(o, 'title', 'common.lesson.procedure.'))),

    lessonSlotStatus: create(['planned', 'closed', 'revoked', 'removed']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.lesson.slot.status.'))),

    studentSlotStatus: create(['planned', 'visited', 'missed', 'revoked', 'canceled', 'removed']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.slot.status.'))),

    studentDashboardTabs: create(['overview', 'lessons', 'cards', 'calls', 'stat', 'photo', 'notifications']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.lessons.tabs.'))),

    studentDashboardLessonTimeCategory: create(['schedule', 'history']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.lessons.time.category.'))),

    studentDashboardLessonVisitTypeFilter: create(['regular', 'trial', 'bonus', 'transfer']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.lessons.visit.type.filter.'))),

    studentCallMethod: create([
        {
            id: 'chat',
            results: ['replied', 'ignored', 'blocked']
        },
        {
            id: 'phone',
            results: ['replied', 'notResponded', 'aborted', 'turnedOff', 'notExist', 'invalidNumber']
        },
        {
            id: 'reception',
            results: ['replied']
        },
        {
            id: 'mail',
            results: ['replied', 'ignored']
        }]
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.call.method.'))),

    studentCallResult: create(['replied', 'ignored', 'blocked', 'notResponded', 'aborted', 'turnedOff', 'notExist', 'invalidNumber']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.call.result.'))),

    studentImpression: create(['notDefined', 'approvement', 'denial', 'doubt', 'strange', 'spy', 'payPromise', 'play']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.call.impression.'))),

    cardStateFilter: create(['all', 'active', 'inactive']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.card.state.filter.'))),

    cardState: create(['active', 'inactive', 'notStarted', 'expired']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.card.state.'))
        .map(o => Utils.addLocalizedProp(o, 'shortTitle', 'common.card.state.short.'))),

    paymentsSearchPeriod: create(['day', 'week', 'month', 'year', 'custom', 'all']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.payments.period.'))),

    paymentsSearchView: create(['list', 'chart']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.payments.view.'))),

    paymentsChartGroupBy: create(['none', 'category', 'category2', 'category3', 'category4', 'category5']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.payments.groupBy.'))),

    paymentsSearchDirectionType: create(['all', 'outgoing', 'incoming', 'transfer']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.payments.direction.type.'))),

    paymentDirectionType: create(['outgoing', 'incoming', 'transfer']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.payments.direction.type.'))),

    accountType: create(['cashless', 'cash', 'wechat', 'alipay']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.payments.account.type.'))
        .map(o => Utils.addLocalizedProp(o, 'abbr', 'common.payments.account.type.abbr.'))),

    permissions: create(Object.keys(Permissions)
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'name', 'common.permissions.value.'))),

    emailNotifications: create(['homework']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.emailNotifications.'))),

    mobileNotifications: create(['plannedLesson']
        .map(Utils.wrapIdentity)
        .map(o => Utils.addLocalizedProp(o, 'title', 'common.student.mobileNotifications.'))),
};

function create(opts) {
    const optsObj = {};
    opts.forEach(o => {
        optsObj[o.id] = o
    });
    opts.byId = (id) => optsObj[id];
    opts.byIds = (ids) => {
        if (Utils.isArray(ids)) {
            return ids.map(id => optsObj[id])
        }
        return []
    };
    opts.ids = () => opts.map(Utils.obj.id);
    opts.obj = () => optsObj;
    opts.withAll = opts.concat(allItem);
    return opts;
}

function indexByFirstLetter(opts) {
    const optsByLetter = {};
    opts.forEach(o => {
        optsByLetter[o.id.charAt(0)] = o
    });

    opts.byFirstLetter = letter => optsByLetter[letter];
    return opts;
}

module.exports = Dictionaries;
