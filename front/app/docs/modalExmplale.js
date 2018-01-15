'use strict';

const React = require('react'),
    {Modal} = require('../components/Dialogs');

const ModalExample = ({})=> {
    return <Modal>
        <div className="modal-header">
            <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span
                aria-hidden="true">&times;</span></button>
            <h4 className="modal-title">Modal title</h4>
        </div>
        <div className="modal-body-container">
            <div className="modal-body">
                <p>
                    <nobr>Long long long long long long long long long long long long long long long long long long long
                        long long long long long long long long long long Line
                    </nobr>
                </p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
                <p>One fine body&hellip;</p>
            </div>
        </div>
        <div className="modal-footer">
            <button type="button" className="btn btn-default" data-dismiss="modal">Close</button>
            <button type="button" className="btn btn-primary">Save changes</button>
        </div>
    </Modal>
};

module.exports = ModalExample;
