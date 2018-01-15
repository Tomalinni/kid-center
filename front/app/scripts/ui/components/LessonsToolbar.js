'use strict';

const React = require('react'),
    moment = require('moment-timezone'),
    DialogService = require('../services/DialogService'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    Dictionaries = require('../Dictionaries'),
    DatePicker = require('./DateComponents').DatePicker,
    DropdownActionButton = require('./DropdownActionButton'),
    {Row, Col, FormGroup, ColFormGroup, Button, InlineButton} = require('../components/CompactGrid'),
    Icons = require('./Icons');


const LessonsToolbar = ({startDate, endDate, onSetDate}) => {
    const setDayActions = getSetAllDaysActions(onSetDate),
        onDatePickerChangeDate = (value) => {
            return onSetDate(value)
        };

    return <Row classes="high2-inputs">
        <ColFormGroup classes="col-xs-3">
            <div className="btn-toolbar">
                <div className="btn-group port-visible">
                    <InlineButton onClick={() => onSetDate(Utils.momentToString(moment()))}>
                        {Utils.message('common.calendar.today')}
                    </InlineButton>
                    <DropdownActionButton
                        title={Icons.caret()}
                        inline={true}
                        dropdownMenuClasses="dropdown-menu-left"
                        actions={getSetExtraDaysActions(onSetDate)}/>
                </div>
                <div className="btn-group land-visible">
                    {Object.keys(setDayActions).map(k => {
                        const action = setDayActions[k];
                        return <InlineButton onClick={action.fn}>
                            {action.title}
                        </InlineButton>
                    })}
                </div>
            </div>
        </ColFormGroup>
        <ColFormGroup classes="col-xs-6"
                      style={{textAlign: 'center'}}>
            <DatePicker
                value={startDate}
                onChange={onDatePickerChangeDate}>
                <InlineButton>
                    {renderWeekPeriod(startDate, endDate)}&nbsp;{Icons.caret()}
                </InlineButton>
            </DatePicker>
        </ColFormGroup>
        <ColFormGroup classes="col-xs-3">
            <div className="btn-toolbar pull-right">
                <div className="btn-group">
                    <InlineButton
                        onClick={() => onSetDate(Utils.momentToString(Utils.momentFromString(startDate).subtract(1, 'w')))}>
                        {Icons.glyph.backward()}
                    </InlineButton>
                    <InlineButton
                        onClick={() => onSetDate(Utils.momentToString(Utils.momentFromString(startDate).add(1, 'w')))}>
                        {Icons.glyph.forward()}
                    </InlineButton>
                </div>
            </div>
        </ColFormGroup>
    </Row>
};

function getSetTodayAction(onSetDate) {
    return {
        title: Utils.message('common.calendar.today'),
        fn: dispatch => onSetDate(Utils.momentToString(moment()))
    }
}

function getSetYesterdayAction(onSetDate) {
    return {
        title: Utils.message('common.calendar.yesterday'),
        fn: dispatch => onSetDate(Utils.momentToString(moment().subtract(1, 'd')))
    }
}

function getSetTomorrowAction(onSetDate) {
    return {
        title: Utils.message('common.calendar.tomorrow'),
        fn: dispatch => onSetDate(Utils.momentToString(moment().add(1, 'd')))
    }
}

function getSetAllDaysActions(onSetDate) {
    return {
        yesterday: getSetYesterdayAction(onSetDate),
        today: getSetTodayAction(onSetDate),
        tomorrow: getSetTomorrowAction(onSetDate)
    }
}

function getSetExtraDaysActions(onSetDate) {
    return {
        yesterday: getSetYesterdayAction(onSetDate),
        tomorrow: getSetTomorrowAction(onSetDate)
    }
}

function renderWeekPeriod(startDate, endDate) {
    return Utils.momentToString(Utils.momentFromString(startDate), Config.shortDateFormat) + ' \u2014 ' + Utils.momentToString(Utils.momentFromString(endDate), Config.shortDateFormat);
}

module.exports = LessonsToolbar;
