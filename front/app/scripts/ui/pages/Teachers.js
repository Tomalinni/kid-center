'use strict';

const React = require('react'),
    {connect} = require('react-redux'),
    Permissions = require('../Permissions'),
    Dictionaries = require('../Dictionaries'),
    Navigator = require('../Navigator'),
    Renderers = require('../Renderers'),
    Utils = require('../Utils'),
    Actions = require('../actions/Actions'),
    {Row, Col, FormGroup, ColFormGroup, Button} = require('../components/CompactGrid'),
    ListScreen = require('../components/ListScreen'),
    TextInput = require('../components/TextInput'),
    Icons = require('../components/Icons');

const Teachers = React.createClass({
    render(){
        const self = this, p = self.props;

        return <ListScreen entity='teachers'
                           ajaxResource={Actions.ajax.teachers}
                           entityModifyPermssion={Permissions.teachersModify}
                           entityRouteFn={Navigator.routes.teacher}
                           pageTitle={Utils.message('common.pages.teachers')}
                           filterElementFn={self.renderFilterElement}
                           entitiesTblCols={self.columns()}
            {...p}
        />
    },

    renderFilterElement(onSearchRequestChange){
        const self = this, p = self.props;

        return <Row>
            <ColFormGroup>
                <TextInput id="searchText"
                           name="searchText"
                           placeholder={Utils.message('common.search.text')}
                           defaultValue={p.searchRequest.text}
                           onChange={(text) => {
                               onSearchRequestChange({text})
                           }}/>
            </ColFormGroup>
        </Row>
    },

    columns(){
        return [
            {
                headerCell: {
                    children: Utils.message('common.teachers.search.table.name')
                },
                bodyCell: {
                    childrenFn: (obj)=> obj.name
                }
            }
        ]
    }
});

function mapStateToProps(state) {
    return state.pages.Teachers;
}

function mapDispatchToProps(dispatch) {
    return {dispatch};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Teachers);
