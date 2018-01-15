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

const React = require('react'),
    Highcharts = require('highcharts');

const Chart = React.createClass({

    componentDidMount: function () {
        const self = this, p = self.props;

        // Extend Highcharts with modules
        if (p.modules) {
            p.modules.forEach(function (module) {
                module(Highcharts);
            });
        }
        // Set container which the chart should render to.
        this.chart = new Highcharts[p.type || "Chart"](
            p.container,
            p.options
        );
    },

    componentWillUnmount: function () {
        this.chart.destroy();
    },

    //Create the div which the chart will be rendered to.
    render: function () {
        const self = this, p = self.props;

        return <div id={p.container}></div>
    }
});

module.exports = Chart;
