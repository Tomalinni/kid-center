'use strict';

const React = require('react'),
    Utils = require('../Utils');

const TextInput = React.createClass({
    _debouncedInput: undefined,
    getInitialState() {
        return {value: this.props.defaultValue, changed: false};
    },
    componentDidMount(){
        this._debouncedInput = Utils.debounceInput(this._callOnChange)
    },
    componentWillReceiveProps(nextProps) {
        if (nextProps.owner !== this.props.owner) {
            this.setState({value: Utils.definedValueOrDefault(nextProps.defaultValue, ''), changed: false});
        }
    },
    handleOnChange(event) {
        this.setState({value: Utils.definedValueOrDefault(event.target.value, ''), changed: true});
        this._debouncedInput(event.target.value);
    },
    handleOnBlur(event) {
        this._callOnChange(Utils.definedValueOrDefault(event.target.value, ''));
    },
    handleKeyDown(event) {
        if (event.keyCode === 13) {
            this._callOnChange(Utils.definedValueOrDefault(event.target.value, ''));
        }
    },
    _callOnChange(value){
        if (this.state.changed) {
            this.setState({changed: false});
            this.props.onChange.call(this, value);
        }
    },
    render() {
        const p = this.props,
            classNames = p.classNames || [];
        if (p.multiline && !p.password) {
            return <textarea id={p.id}
                             name={p.name}
                             className={classNames.concat('form-control').join(' ')}
                             rows={p.rows}
                             placeholder={p.placeholder}
                             readOnly={p.readOnly}
                             value={Utils.definedValueOrDefault(this.state.value, '')}
                             onChange={this.handleOnChange}
                             onBlur={this.handleOnBlur}
                             onKeyDown={this.handleKeyDown}></textarea>

        } else {
            const type = p.password ? "password" : "text";
            return <input type={type}
                          id={p.id}
                          name={p.name}
                          className={classNames.concat('form-control').join(' ')}
                          placeholder={p.placeholder}
                          readOnly={p.readOnly}
                          value={Utils.definedValueOrDefault(this.state.value, '')}
                          onChange={this.handleOnChange}
                          onBlur={this.handleOnBlur}
                          onKeyDown={this.handleKeyDown}/>
        }
    }
});

module.exports = TextInput;
