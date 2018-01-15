'use strict';

const {browserHistory} = require('react-router'),
    Utils = require('./Utils');

const Navigator = {
    routes: {
        login: '/login',
        register: '/register',
        children: '/children',
        profile: '/profile',
        index: '/',
        lessons: '/lessons',
        students: '/students',
        student: id => ('/students/' + id),
        studentCard: (id, cardId) => ('/students/' + id + '/cards/' + cardId),
        studentCall: (id, callId) => ('/students/' + id + '/calls/' + callId),
        studentCalls: '/studentCalls',
        cards: '/cards',
        card: id => ('/cards/' + id),
        teachers: '/teachers',
        teacher: id => ('/teachers/' + id),
        lessonTemplates: '/lessonTemplates',
        lessonTemplate: id => ('/lessonTemplates/' + id),
        payments: '/payments',
        payment: id => ('/payments/' + id),
        cities: '/cities',
        city: id => ('/cities/' + id),
        schools: '/schools',
        school: id => ('/schools/' + id),
        accounts: '/accounts',
        account: id => ('/accounts/' + id),
        categories: '/categories',
        category: id => ('/categories/' + id),
        byPageId: id => Navigator.routes[id],
        users: '/users',
        user: id => ('/users/' + id),
        roles: '/roles',
        role: id => ('/roles/' + id),
        homeworks: '/homework',
        homework: id => ('/homework/' + id),
        preferences: '/preferences'
    },

    navigate(route, query){
        browserHistory.push(route + Utils.objToQueryString(query))
    },

    query(location, parameter){
        return location && location.query && location.query[parameter]
    },

    back(){
        browserHistory.goBack()
    }
};

module.exports = Navigator;
