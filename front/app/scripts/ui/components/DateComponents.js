'use strict';


const React = require('react'),
    Icons = require('./Icons'),
    Calendar = require('rc-calendar'),
    DatePicker = require('rc-calendar/lib/Picker'),
    MonthCalendar = require('rc-calendar/lib/MonthCalendar'),
    Utils = require('../Utils'),
    Config = require('../Config'),
    Locale = require('../Locale'),
    moment = require('moment-timezone'),
    TimePickerPanel = require('rc-time-picker/lib/Panel'),
    OverlayModal = require('./OverlayModal');

const {PropTypes} = React;
const disabledMinutes = Utils.fillArray(60, Utils.nullifyEvery(10));

function renderDayCalendar(defaultValue, disabledDate, timePickerElement) {
    return <Calendar
        locale={Locale.rcCalendarLocale()}
        style={{zIndex: 1000}}
        dateInputPlaceholder={Utils.message('common.calendar.enter.date')}
        defaultValue={defaultValue}
        showDateInput={false}
        disabledDate={disabledDate}
        timePicker={timePickerElement}
    />
}

function renderMonthCalendar(defaultValue, disabledDate, timePickerElement) {
    return <MonthCalendar
        locale={Locale.rcCalendarLocale()}
        style={{zIndex: 1000}}
        defaultValue={defaultValue}
        disabledDate={disabledDate}
        timePicker={timePickerElement}
    />
}

const AppDatePicker = React.createClass({

    getDefaultProps() {
        const self = this;
        return {
            mode: 'day',
            showTime: false,
            value: Utils.momentToString(moment().startOf('day'))
        };
    },

    setDatePickerValue(value) {
        const self = this, p = self.props;
        p.onChange(Utils.momentToString(value, self.getDateFormat()));
    },

    render () {
        const self = this, p = self.props,
            renderCalendarFn = Utils.select(p.mode === 'month', renderMonthCalendar, renderDayCalendar),
            mt = Utils.momentFromString(p.value, self.getDateFormat());

        return <DatePicker
            animation="slide-down"
            calendar={renderCalendarFn(mt, p.disabledDate, self.renderTimePickerPanel())}
            value={mt}
            disabledTime={true}
            onChange={self.setDatePickerValue}>
            {() => {
                return p.children
            }}
        </DatePicker>
    },

    getDateFormat(){
        return this.props.showTime ? Config.dateTimeFormat : Config.dateFormat
    },

    renderTimePickerPanel(){
        const self = this, p = self.props;
        if (p.showTime) {
            return <TimePickerPanel showSecond={false}
                                    disabledMinutes={() => disabledMinutes}
                                    hideDisabledOptions={true}/>
        }
    },
});

const ButtonDatePicker = React.createClass({

    propTypes: {
        date: PropTypes.string,
        onChange: PropTypes.func
    },

    render () {
        const self = this, p = self.props;

        return <div className="btn-group btn-date-picker" role="group">
            <AppDatePicker value={p.date}
                           onChange={p.onChange}>
                <button type="button"
                        className="btn btn-default">
                    {p.date || Utils.message('common.form.date.not.selected')}
                    &nbsp;{Icons.caret()}
                </button>
            </AppDatePicker>
            <button type="button"
                    className="btn btn-default"
                    onClick={() => p.onChange(null)}>
                {Icons.glyph.remove()}
            </button>
        </div>
    }
});

const AgeSelector = React.createClass({
    getInitialState(){
        return {showModal: false};
    },

    render() {
        const self = this, p = self.props;

        return <div>
            <AppDatePicker value={p.value}
                           onChange={p.onChange}>
                <button type="button"
                        className="btn btn-default btn-right-gap">
                    {Icons.glyph.calendar()}
                </button>
            </AppDatePicker>
            <div className="field-with-right-gap-1">
                <button type="button"
                        className="btn btn-wide btn-default"
                        onClick={() => self.openModal()}>
                    {p.value || '-'}
                </button>
                <OverlayModal isOpened={self.state.showModal}
                              closeModal={self.closeModal}
                              renderModalContent={self.renderModalContent}/>
            </div>
        </div>
    },

    renderModalContent(){
        const self = this, p = self.props, fields = self.getDateFields();

        return <table className='age-selector-options-table'>
            <thead>
            <tr>
                <td>
                    <div className='age-selector-options-table-header'>
                        {Utils.message('common.label.year')}
                    </div>
                </td>
                <td>
                    <div className='age-selector-options-table-header'>
                        {Utils.message('common.label.month')}
                    </div>
                </td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>{self.renderYears(fields.year)}</td>
                <td>{self.renderMonths(fields.month)}</td>
            </tr>
            </tbody>
        </table>
    },

    renderYears(selectedYear){
        const self = this, p = self.props;
        return this.renderOptionsSequence(p.minAge, p.maxAge, selectedYear, 'year')
    },

    renderMonths(selectedMonth){
        return this.renderOptionsSequence(0, 11, selectedMonth, 'month')
    },

    renderOptionsSequence(minVal, maxVal, selectedVal, field){
        const self = this, p = self.props;

        const options = [], diff = maxVal - minVal;
        for (let i = 0; i <= diff; i++) {
            let optVal = i + minVal;
            options[i] = <div key={optVal}
                              className={self.getOptionClassName(optVal, selectedVal)}
                              onClick={() => self.setField(field, optVal)}>
                {optVal}
            </div>
        }
        return options
    },

    getOptionClassName(optVal, selectedVal){
        return 'age-selector-options-table-option ' + Utils.select(optVal === selectedVal, 'age-selector-options-table-option-selected', '')
    },

    getDateFields(){
        const self = this, p = self.props,
            currentMt = Utils.currentMoment(),
            mt = Utils.momentFromString(p.value),
            yearDiff = currentMt.diff(mt, 'years'),
            monthDiff = currentMt.subtract(yearDiff, 'years').diff(mt, 'months');

        return {year: yearDiff, month: monthDiff}
    },

    dateFromFields(fields){
        const currentMt = Utils.currentMoment(),
            mt = currentMt.subtract(fields.year, 'years').subtract(fields.month, 'months');
        return Utils.momentToString(mt)
    },

    setField(field, val){
        const self = this, p = self.props, fields = self.getDateFields();
        fields[field] = val;
        p.onChange(self.dateFromFields(fields))
    },

    closeModal(){
        this.setState({showModal: false})
    },

    openModal(){
        this.setState({showModal: true})
    }
});

module.exports = {
    DatePicker: AppDatePicker,
    ButtonDatePicker: ButtonDatePicker,
    AgeSelector: AgeSelector
};
