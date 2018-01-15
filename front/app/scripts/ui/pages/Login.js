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
    {connect} = require('react-redux'),
    DataService = require('../services/DataService'),
    AuthService = require('../services/AuthService'),
    Navigator = require('../Navigator'),
    Utils = require('../Utils'),
    Config = require('../Config');

const Login = React.createClass({

    loginInput: null,
    passInput: null,

    getInitialState(){
        return {loginError: null}
    },

    render(){
        const self = this;
        return <div>
            <div className="login-title">
                ANNA SALENKO 儿童培训中心
            </div>
            <div className="login-subtitle">
                Probably the best school in the world.
            </div>
            <form onSubmit={Utils.invokeAndPreventDefaultFactory(self.login)}>
                <div className="login-container">
                    <div className="row">
                        <div className="col-md-12">
                            <div className="form-group">
                                <input type="text"
                                       name="login"
                                       ref={(c) => self.loginInput = c}
                                       className="form-control"
                                       placeholder={Utils.message('common.login.form.fields.login')}/>
                            </div>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-md-12">
                            <div className="form-group">
                                <input type="password"
                                       name="pass"
                                       ref={(c) => self.passInput = c}
                                       className="form-control"
                                       placeholder={Utils.message('common.login.form.fields.pass')}/>
                            </div>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-md-12">
                            <button type="submit"
                                    className="btn btn-primary">
                                {Utils.message('common.login.form.btn.login')}
                            </button>

                            {Utils.select(Config.registrationAvailable,
                                <button className="btn btn-link pull-right"
                                        onClick={() => Navigator.navigate(Navigator.routes.register)}>
                                    {Utils.message('common.login.form.btn.register')}
                                </button>
                            )}
                        </div>
                    </div>
                    {self.renderLoginError()}
                </div>
            </form>
        </div>
    },

    renderLoginError(){
        if (this.state.loginError) {
            return <div className="row mrg-top-10">
                <div className="col-md-12">
                    <div className="alert alert-warning pad-3">{this.state.loginError}</div>
                </div>
            </div>
        }
    },

    login(){
        const self = this, p = self.props;
        const loginRequest = {login: self.loginInput.value, pass: self.passInput.value};
        DataService.operations.auth.login(loginRequest).then((response) => {
            AuthService.applyToken(response.token);
            Navigator.navigate(Navigator.routes.index)
        }, () => {
            self.setState({loginError: Utils.message('common.login.form.incorrect.credentials')})
        })
    }
});

function mapStateToProps(state) {
    return {};
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Login);
