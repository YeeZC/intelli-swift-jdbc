GridTableView = BI.inherit(BI.View, {
    _defaultConfig: function () {
        return BI.extend(GridTableView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-mvc-grid-table-view bi-mvc-layout"
        })
    },

    _init: function () {
        GridTableView.superclass._init.apply(this, arguments);
    },

    _render: function (vessel) {
        var items = [], header = [], columnSize = [];

        var rowCount = 10, columnCount = 10;
        for (var i = 0; i < 1; i++) {
            header[i] = [];
            for (var j = 0; j < columnCount; j++) {
                header[i][j] = {
                    type: "bi.label",
                    text: "表头" + i + "-" + j
                }
                columnSize[j] = 100;
            }
        }
        for (var i = 0; i < rowCount; i++) {
            items[i] = [];
            for (var j = 0; j < columnCount; j++) {
                items[i][j] = {
                    type: "bi.label",
                    text: i + "-" + j
                }
            }
        }

        var table = BI.createWidget({
            type: "bi.resizable_grid_table",
            width: 600,
            height: 500,
            isResizeAdapt: false,
            isNeedResize: true,
            isNeedMerge: true,
            isNeedFreeze: true,
            freezeCols: [0, 1],
            mergeCols: [0, 1],
            columnSize: columnSize,
            items: items,
            header: header
        });
        BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: {
                    type: "bi.grid",
                    columns: 1,
                    rows: 1,
                    items: [{
                        column: 0,
                        row: 0,
                        el: table
                    }]
                },
                left: 10,
                right: 10,
                top: 10,
                bottom: 10
            }]
        })
    }
});

GridTableModel = BI.inherit(BI.Model, {});