/**
 * 图表控件
 * @class BI.PieChart
 * @extends BI.Widget
 */
BI.PieChart = BI.inherit(BI.Widget, {

    constants: {
        LEFT_AXIS: 0,
        RIGHT_AXIS: 1,
        RIGHT_AXIS_SECOND: 2,
        X_AXIS: 3,
        ROTATION: -90,
        NORMAL: 1,
        LEGEND_BOTTOM: 4,
        ZERO2POINT: 2,
        ONE2POINT: 3,
        TWO2POINT: 4,
        STYLE_NORMAL: 21
    },

    _defaultConfig: function () {
        return BI.extend(BI.PieChart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-pie-chart"
        })
    },

    _init: function () {
        BI.PieChart.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.combineChart = BI.createWidget({
            type: "bi.combine_chart",
            formatConfig: BI.bind(this._formatConfig, this),
            element: this.element
        });
        this.combineChart.on(BI.CombineChart.EVENT_CHANGE, function (obj) {
            self.fireEvent(BI.PieChart.EVENT_CHANGE, obj);
        });
    },

    _formatConfig: function(config, items){
        var self = this, o = this.options;

        config.colors = this.config.chart_color;
        config.style = formatChartStyle();
        formatChartPieStyle();

        switch (this.config.chart_legend){
            case BICst.CHART_LEGENDS.BOTTOM:
                config.legend.enabled = true;
                config.legend.position = "bottom";
                break;
            case BICst.CHART_LEGENDS.RIGHT:
                config.legend.enabled = true;
                config.legend.position = "right";
                break;
            case BICst.CHART_LEGENDS.NOT_SHOW:
            default:
                config.legend.enabled = false;
                break;
        }

        config.plotOptions.dataLabels.enabled = this.config.show_data_label;

        config.chartType = "pie";
        delete config.xAxis;
        delete config.yAxis;
        config.plotOptions.dataLabels.align = "outside";
        config.plotOptions.dataLabels.connectorWidth = "outside";
        config.plotOptions.dataLabels.formatter.identifier = "${VALUE}${PERCENT}";
        return [items, config];

        function formatChartStyle(){
            switch (self.config.chart_style) {
                case BICst.CHART_STYLE.STYLE_GRADUAL:
                    return "gradual";
                case BICst.CHART_STYLE.STYLE_NORMAL:
                default:
                    return "normal";
            }
        }

        function formatChartPieStyle(){
            switch (self.config.chart_pie_type){
                case BICst.CHART_STYLE.EQUAL_ARC_ROSE:
                    config.plotOptions.roseType = "sameArc";
                    break;
                case BICst.CHART_STYLE.NOT_EQUAL_ARC_ROSE:
                    config.plotOptions.roseType = "differentArc";
                    break;
                case BICst.CHART_STYLE.NORMAL:
                default:
                    delete config.plotOptions.roseType;
                    break;
            }
            config.plotOptions.innerRadius = self.config.chart_inner_radius;
            config.plotOptions.endAngle = self.config.chart_total_angle;
        }

    },

    populate: function (items, options) {
        var self = this, c = this.constants;
        this.config = {
            chart_color: options.chart_color || [],
            chart_style: options.chart_style || c.NORMAL,
            chart_pie_type: options.chart_pie_type || c.NORMAL,
            chart_legend: options.chart_legend || c.LEGEND_BOTTOM,
            show_data_label: options.show_data_label || false,
            chart_inner_radius: options.chart_inner_radius || 0,
            chart_total_angle: options.chart_total_angle || BICst.PIE_ANGLES.TOTAL
        };
        this.options.items = items;

        var types = [];
        BI.each(items, function(idx, axisItems){
            var type = [];
            BI.each(axisItems, function(id, item){
                type.push(BICst.WIDGET.PIE);
            });
            types.push(type);
        });

        this.combineChart.populate(items, types);
    },

    resize: function () {
        this.combineChart.resize();
    },

    magnify: function(){
        this.combineChart.magnify();
    }
});
BI.PieChart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.pie_chart', BI.PieChart);