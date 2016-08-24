;
!(function(){
    BI.NumberInRangeFilterValue = function(range){
        this.range = {};
        range = range || {};
        this.range.min = range.min || BI.MIN;
        this.range.max = range.max || BI.MAX;
        this.range.closemin = range.closemin || true;
        this.range.closemax = range.closemax || true;
    };
    BI.NumberInRangeFilterValue.prototype = {
        constructor: BI.NumberInRangeFilterValue,

        isNumberInRange: function(value){
            if(value == null){
                return false;
            }
            return (this.range.closemin ? value >= this.range.min : value > this.range.min) &&
                (this.range.closemax ? value <= this.range.max : value < this.range.max);
        },

        getFilterResult: function(array) {
            return BI.filter(array, function(idx, val){
                return this.isNumberInRange(val);
            });
        }
    }
})();