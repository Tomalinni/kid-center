// @if ENV='dev'
Raven && Raven.config('https://bae7c7129a464837b12797a130e7501b@sentry.io/133479').install();
// @endif

require('./Polyfills');

const React = window.React = require('react'),
    ReactDOM = require("react-dom"),
    {Provider} = require('react-redux'),
    thunkMiddleware = require('redux-thunk').default,

    {createStore, applyMiddleware} = require('redux'),
    {Router, Route, browserHistory} = require('react-router'),

    Api = require('./ui/Api'),
    AuthService = require('./ui/services/AuthService'),
    Permissions = require('./ui/Permissions'),
    Actions = require('./ui/actions/Actions'),
    Reducers = require('./ui/reducers/Reducers'),
    Config = require('./ui/Config'),
    Utils = require('./ui/Utils'),
    Locale = require("./ui/Locale"),
    {pageFactory, securePageEnterCb} = require("./ui/pages/Pages"),
    NoMatch = require("./ui/pages/NoMatch"),
    Login = require("./ui/pages/Login"),
    Lessons = require("./ui/pages/Lessons"),
    Profile = require("./ui/pages/Profile"),
    RegisterForm = require("./ui/pages/RegisterForm"),
    Homeworks = require("./ui/pages/Homeworks"),
    Homework = require("./ui/pages/Homework"),
    RelativeChildrenForm = require("./ui/pages/RelativeChildrenForm");

// @if ENV='dev'
const createLogger = require('redux-logger');
// @endif

window.kidCenter = Api;

Config.registrationAvailable = true;
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
    <Provider store={store}>
        <Router history={browserHistory}>
            <Route path="/login" component={Login}/>
            <Route path="/register" component={RegisterForm}/>
            <Route path="/children" component={RelativeChildrenForm}
                   onEnter={securePageEnterCb(store)}/>

            <Route path="/profile" component={Profile}
                   onEnter={securePageEnterCb(store)}/>
            <Route path="/lessons" component={pageFactory(Lessons, Permissions.lessonsRead)}
                   onEnter={securePageEnterCb(store)}/>

            <Route path="homework" component={pageFactory(Homeworks, Permissions.homeworkRead)}
                   onEnter={securePageEnterCb(store)}/>
            <Route path="homework/:id" component={pageFactory(Homework, Permissions.homeworkRead)}
                   onEnter={securePageEnterCb(store)}/>

            <Route path="*" component={NoMatch}/>
        </Router>
    </Provider>), appContainer);
