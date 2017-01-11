/**
 * Created by 小灰灰 on 2016/3/23.
 */
BI.SelectDataLevel8NodeController = BI.inherit(BI.Controller, {

    _defaultConfig: function () {
        return BI.extend(BI.SelectDataLevel8NodeController.superclass._defaultConfig.apply(this, arguments), {})
    },

    getMarkArguments: function (id, text) {
        if (BI.isNotEmptyArray(BI.Utils.getFieldIDsOfTableID(id))) {
            return [];
        } else {
            return [text];
        }
    },
    _init: function () {
        BI.SelectDataLevel8NodeController.superclass._init.apply(this, arguments)
        var self = this;
        self.renameController = new BI.ETLRenamePopoverController();
    },

    _renameChecker: function (v, id) {
        return !BI.Utils.getAllETLTableNames(id).contains(v);
    },

    setWidget: function (widget) {
        this.widget = widget;
    },

    startChecker: function (id) {
        var self = this;
        // BI.ETLReq.reqAddTask(function () {
        //     BI.ETLReq.reqTableStatus({
        //         id: id
        //     })
        // });
        Data.BufferPool.putAnalysisTableStatusFn(id, function(percent) {
            self.widget.showLoading(0.9);
        });
        var checker = setInterval(function () {
            BI.ETLReq.reqTableStatus({
                id: id
            }, function (res) {
                if (res[ETLCst.GENERATED_PERCENT] === 1) {
                    self.widget.showLoading(res[ETLCst.GENERATED_PERCENT]);
                    clearInterval(checker);
                }
                self.widget.showLoading(res[ETLCst.GENERATED_PERCENT]);
            })
        }, 8000);
    },


    _showRenamePop: function (id, text) {
        var self = this;
        var namePopover = BI.createWidget({
            type: "bi.etl_table_name_popover",
            renameChecker: function (v) {
                return self._renameChecker(v, id)
            }
        });
        namePopover.on(BI.PopoverSection.EVENT_CLOSE, function () {
            BI.Layers.hide(ETLCst.ANALYSIS_POPUP_FOLATBOX_LAYER);
        })
        namePopover.on(BI.ETLTableNamePopover.EVENT_CHANGE, function (v, des) {
            BI.ETLReq.reqRenameTable({id: id, name: v, describe: des}, BI.emptyFn);
        });
        BI.Popovers.remove("etlTableName");
        BI.Popovers.create("etlTableName", namePopover, {
            width: 450,
            height: 370,
            container: BI.Layers.create(ETLCst.ANALYSIS_POPUP_FOLATBOX_LAYER)
        }).open("etlTableName");
        BI.Layers.show(ETLCst.ANALYSIS_POPUP_FOLATBOX_LAYER);
        namePopover.populate(text, BI.isNotEmptyArray(BI.Utils.getFieldIDsOfTableID(id)) ? BI.Utils.getDescribe(id) : '');
        namePopover.setTemplateNameFocus();
    },

    afterClickList: function (v, option) {
        var self = this;
        switch (v) {
            case ETLCst.ANALYSIS_TABLE_SET.EDIT :
                BI.ETLReq.reqEditTable({id: option.id}, function (res) {
                    BI.createWidget({
                        type: "bi.analysis_etl_main",
                        element: BI.Layers.create(ETLCst.ANALYSIS_LAYER, "body"),
                        model: res
                    })
                })
                return;
            case ETLCst.ANALYSIS_TABLE_SET.RENAME :
                self._showRenamePop(option.id, option.text);
                return;
            case ETLCst.ANALYSIS_TABLE_SET.DELETE :
                BI.ETLReq.reqDeleteTable({id: option.id}, BI.emptyFn)
                return;
            case ETLCst.ANALYSIS_TABLE_SET.COPY :
                BI.ETLReq.reqSaveTable({
                    id: option.id,
                    new_id: BI.UUID(),
                    name: BI.Utils.createDistinctName(BI.Utils.getAllETLTableNames(), option.text)
                }, BI.emptyFn);
                return;
        }

    }
})