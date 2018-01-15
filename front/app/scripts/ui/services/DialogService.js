'use strict';

const React = require("react"),
    ReactDOM = require("react-dom"),

    {Confirm, Modal} = require('../components/Dialogs'),
    Config = require('../Config'),
    Utils = require('../Utils');

const DialogService = {
    confirm(props) {
        return initModalDialog(Confirm, props);
    },
    confirmDelete(){
        return this.confirm({
            title: Utils.message('common.dialog.confirm'),
            content: Utils.message('common.dialog.confirm.delete.message')
        })
    },
    confirmBack(){
        return this.confirm({
            title: Utils.message('common.dialog.confirm'),
            content: Utils.message('common.dialog.confirm.back.message')
        })
    },
    modal(props) {
        return initModalDialog(Modal, props);
    },
    doWithConfirmation(confirmationContent, actionFn){
        return DialogService.confirm({
            title: Utils.message('common.dialog.confirm'),
            content: confirmationContent

        }).then(() => {
            actionFn()
        }, Utils.fn.nop)
    },
    doWithOptionalConfirmation(showConfirmation, confirmationContent, actionFn){
        if (!showConfirmation) {
            actionFn()
        } else {
            return DialogService.doWithConfirmation(confirmationContent, actionFn)
        }
    }
};

function initModalDialog(ModalClass, props) {
    let wrapper = document.body.appendChild(document.createElement('div')),
        component = ReactDOM.render(React.createElement(ModalClass, props), wrapper),
        cleanup = () => {
            ReactDOM.unmountComponentAtNode(wrapper);
            setTimeout(() => {
                wrapper.remove();
            }, 0);
        };

    return component.promise.always(cleanup);
}

module.exports = DialogService;