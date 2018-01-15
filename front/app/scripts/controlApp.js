// @if ENV='prod'
Raven && Raven.config('https://bae7c7129a464837b12797a130e7501b@sentry.io/133479').install();
// @endif

require('./Polyfills');

const React = window.React = require('react'),
    ReactDOM = require("react-dom"),
    {Provider} = require('react-redux'),
    thunkMiddleware = require('redux-thunk').default,

    {createStore, applyMiddleware} = require('redux'),
    {Router, Route, IndexRoute, browserHistory} = require('react-router'),

    Api = require('./ui/Api'),
    AuthService = require('./ui/services/AuthService'),
    Permissions = require('./ui/Permissions'),
    Actions = require('./ui/actions/Actions'),
    Reducers = require('./ui/reducers/Reducers'),
    Utils = require('./ui/Utils'),
    Locale = require("./ui/Locale"),
    AppNotification = require("./ui/components/AppNotification"),
    {pageFactory, securePageEnterCb} = require("./ui/pages/Pages"),
    NoMatch = require("./ui/pages/NoMatch"),
    Login = require("./ui/pages/Login"),
    Index = require("./ui/pages/Index"),
    Lessons = require("./ui/pages/Lessons"),
    Profile = require("./ui/pages/Profile"),
    Students = require("./ui/pages/Students"),
    Student = require("./ui/pages/Student"),
    StudentCard = require("./ui/pages/StudentCard"),
    StudentCall = require("./ui/pages/StudentCall"),
    StudentCalls = require("./ui/pages/StudentCalls"),
    Cards = require("./ui/pages/Cards"),
    Card = require("./ui/pages/Card"),
    Teachers = require("./ui/pages/Teachers"),
    Teacher = require("./ui/pages/Teacher"),
    LessonTemplates = require("./ui/pages/LessonTemplates"),
    LessonTemplate = require("./ui/pages/LessonTemplate"),
    Payments = require("./ui/pages/Payments"),
    Payment = require("./ui/pages/Payment"),
    Cities = require("./ui/pages/Cities"),
    City = require("./ui/pages/City"),
    Schools = require("./ui/pages/Schools"),
    School = require("./ui/pages/School"),
    Accounts = require("./ui/pages/Accounts"),
    Account = require("./ui/pages/Account"),
    Categories = require("./ui/pages/Categories"),
    Category = require("./ui/pages/Category"),
    Users = require("./ui/pages/Users"),
    User = require("./ui/pages/User"),
    Roles = require("./ui/pages/Roles"),
    Role = require("./ui/pages/Role"),
    Homeworks = require("./ui/pages/Homeworks"),
    Homework = require("./ui/pages/Homework"),
    Preferences = require("./ui/pages/Preferences");

// @if ENV='dev'
const createLogger = require('redux-logger');
// @endif

window.kidCenter = Api;

const appContainer = document.getElementById("app");

let store = createStore(Reducers, applyMiddleware(
    thunkMiddleware
    // @if ENV='dev'
    ,
    createLogger()
    // @endif
));
Actions._dispatch = store.dispatch;

ReactDOM.render((
    <div>
        <AppNotification/>
        <Provider store={store}>
            <Router history={browserHistory}>
                <Route path="/login" component={Login}/>
                <Route path="/" component={Index}/>
                <Route path="/lessons" component={pageFactory(Lessons, Permissions.lessonsRead)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="users" component={pageFactory(Users, Permissions.manageUsers)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="users/:id" component={pageFactory(User, Permissions.manageUsers)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="roles" component={pageFactory(Roles, Permissions.manageUsers)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="roles/:id" component={pageFactory(Role, Permissions.manageUsers)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="profile" component={Profile}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="students" component={pageFactory(Students, Permissions.studentsRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="students/:id" component={pageFactory(Student, Permissions.studentsModify)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="students/:studentId/cards/:id"
                       component={pageFactory(StudentCard, Permissions.studentCardsModify)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="students/:studentId/calls/:id"
                       component={pageFactory(StudentCall, Permissions.studentCallsModify)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="studentCalls" component={pageFactory(StudentCalls, Permissions.studentCallsRead)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="cards" component={pageFactory(Cards, Permissions.cardsRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="cards/:id" component={pageFactory(Card, Permissions.cardsModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="teachers" component={pageFactory(Teachers, Permissions.teachersRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="teachers/:id" component={pageFactory(Teacher, Permissions.teachersModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="lessonTemplates" component={pageFactory(LessonTemplates, Permissions.lessonTemplatesRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="lessonTemplates/:id"
                       component={pageFactory(LessonTemplate, Permissions.lessonTemplatesModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="payments" component={pageFactory(Payments, Permissions.paymentsRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="payments/:id" component={pageFactory(Payment, Permissions.paymentsModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="cities" component={pageFactory(Cities, Permissions.paymentsRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="cities/:id" component={pageFactory(City, Permissions.paymentsModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="schools" component={pageFactory(Schools, Permissions.paymentsRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="schools/:id" component={pageFactory(School, Permissions.paymentsModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="accounts" component={pageFactory(Accounts, Permissions.paymentsRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="accounts/:id" component={pageFactory(Account, Permissions.paymentsModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="categories" component={pageFactory(Categories, Permissions.paymentsRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="categories/:id" component={pageFactory(Category, Permissions.paymentsModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="homework" component={pageFactory(Homeworks, Permissions.homeworkRead)}
                       onEnter={securePageEnterCb(store)}/>
                <Route path="homework/:id" component={pageFactory(Homework, Permissions.homeworkModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="preferences" component={pageFactory(Preferences, Permissions.studentCardPaymentPrefModify)}
                       onEnter={securePageEnterCb(store)}/>

                <Route path="*" component={NoMatch}/>
            </Router>
        </Provider>
    </div>), appContainer);
