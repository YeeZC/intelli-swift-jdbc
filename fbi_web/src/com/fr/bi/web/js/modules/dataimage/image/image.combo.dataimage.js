/**
 * Created by fay on 2016/9/1.
 */
BI.DataImagePane = BI.inherit(BI.Widget, {

    _defaultConfig: function () {
        var conf = BI.DataImagePane.superclass._defaultConfig.apply(this, arguments);
        return BI.extend(conf, {});
    },

    _init: function () {
        BI.DataImagePane.superclass._init.apply(this, arguments);
        this._createImage();

        BI.createWidget({
                type: "bi.vertical",
                items: [BI.createWidget({
                    type: "bi.horizontal",
                    element: this.element,
                    items: [this.chart, this.imageSet],
                    scrollable: false,
                    scrolly: false,
                    scrollx: false
                })]
            }
        );
    },

    _createImage: function () {
        var self = this, o = this.options;
        this.imageSet = BI.createWidget({
            type: "bi.data_image_set",
            dId: o.dId
        });
        this.imageSet.on(BI.DataImageImageSet.EVENT_CHANGE, function () {
            self.chart.populate(FR.servletURL + "?op=fr_bi&cmd=get_uploaded_image&image_id=" + self.imageSet.getValue().src);
            self.fireEvent(BI.DataImagePane.IMG_CHANGE, arguments);
        });
        this.chart = BI.createWidget({
            type: "bi.data_label_chart",
            chartType: o.chartType,
            showType: "data_image"
        });
    },

    setValue: function (v) {
        this.imageSet.setValue(v)
    },

    getValue: function () {
        return this.imageSet.getValue();
    },

    populate: function (src) {
        this.chart.populate(FR.servletURL + "?op=fr_bi&cmd=get_uploaded_image&image_id=" + src);
        this.imageSet.populate();
    }
});
BI.DataImagePane.IMG_CHANGE = "BI.DataImageTab.IMG_CHANGE";
$.shortcut("bi.data_image_pane", BI.DataImagePane);