"use strict";

require('es6-promise').polyfill();

const React = require("react"),
    ReactDOM = require("react-dom"),
    Utils = require("../Utils");

const ModalWindow = React.createClass({
    displayName: "ModalWindow",

    renderBackdrop() {
        return <div className="modal-backdrop in"></div>
    },

    renderModal() {
        return <div className="modal in"
                    tabIndex="-1"
                    role="dialog"
                    aria-hidden="false"
                    ref="modal"
                    style={{display: 'block'}}>
            <div className="modal-dialog">
                <div className="modal-content">
                    {this.props.children}
                </div>
            </div>
        </div>
    },

    render() {
        return <div>
            {this.renderBackdrop()}
            {this.renderModal()}
        </div>
    }
});

const Confirm = React.createClass({
    displayName: Utils.message('common.dialog.confirm'),
    promise: undefined,

    getDefaultProps() {
        return {
            confirmLabel: Utils.message('button.ok'),
            abortLabel: Utils.message('button.cancel')
        }
    },

    abort() {
        this.promise.reject();
    },

    confirm() {
        this.promise.resolve();
    },

    componentDidMount() {
        this.promise = Promise.defer();
        ReactDOM.findDOMNode(this.refs.confirm).focus()
    },

    render() {
        return <ModalWindow>
            <div className="modal-header">
                <h4 className="modal-title">{this.props.title}</h4>
            </div>
            <div className="modal-body-container">
                <div className="modal-body">
                    {this.props.content}
                </div>
            </div>
            <div className="modal-footer">
                <button type="button"
                        className="btn btn-default"
                        data-dismiss="modal"
                        onClick={this.abort}>
                    {this.props.abortLabel}
                </button>
                <button type="button"
                        className="btn btn-primary"
                        ref="confirm"
                        onClick={this.confirm}>
                    {this.props.confirmLabel}
                </button>
            </div>
        </ModalWindow>
    }
});

const Modal = React.createClass({
    displayName: "Modal",
    promise: undefined,

    getDefaultProps() {
        return {
            buttons: {
                'close': {
                    label: Utils.message('button.close'),
                    reject: null
                }
            }
        }
    },

    reject(result) {
        this.promise.reject(result);
    },

    resolve(result) {
        this.promise.resolve(result);
    },

    componentDidMount() {
        this.promise = Promise.defer();
    },

    render() {
        const self = this;
        return <ModalWindow>
            <div className="modal-header">
                <h4 className="modal-title">{this.props.title}</h4>
            </div>
            <div className="modal-body-container">
                <div className="modal-body">
                    {this.props.content}
                </div>
            </div>
            <div className="modal-footer">
                {Object.keys(self.props.buttons).map(b => {
                    const button = self.props.buttons[b],
                        buttonClasses = 'btn btn-' + (button.className || 'default');
                    return <button key={b}
                                   type="button"
                                   className={buttonClasses}
                                   data-dismiss="modal"
                                   onClick={self.onButtonClick.bind(self, button)}>
                        {button.label}
                    </button>
                })}
            </div>
        </ModalWindow>
    },

    onButtonClick(button){
        if (button.resolve !== undefined || button.reject === undefined) {
            this.promise.resolve(Utils.isFunction(button.resolve) ? button.resolve.call(this) : button.resolve);
        } else {
            this.promise.reject(Utils.isFunction(button.reject) ? button.reject.call(this) : button.reject);
        }
    }
});

module.exports = {
    Modal: Modal,
    Confirm: Confirm
};
