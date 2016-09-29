import PureRenderMixin from 'react-addons-pure-render-mixin'
import mixin from 'react-mixin'
import {findDOMNode} from 'react-dom'
import './Checkbox.css'

import {cn, sc, math, isNil, requestAnimationFrame, emptyFunction, shallowEqual, isEqual, each} from 'core'
import React, {
    Component,
    StyleSheet,
    Text,
    Dimensions,
    PixelRatio,
    ListView,
    View,
    Fetch,
    Promise,
    TouchableHighlight,
    TouchableWithoutFeedback
} from 'lib'


class Checkbox extends Component {
    constructor(props, context) {
        super(props, context);
    }

    static propTypes = {};

    static defaultProps = {
        selected: true,
        half: false
    };

    state = {
        selected: this.props.selected,
        half: this.props.half,
        onSelected: emptyFunction
    };

    componentWillMount() {

    }

    componentDidMount() {

    }

    componentWillReceiveProps(nextProps) {
        const {selected, half} = nextProps;
        this.setState({selected, half});
    }

    render() {
        const {...props} = this.props, {...state} = this.state;
        return <TouchableWithoutFeedback onPress={(e)=> {
            if (this.state.half === true) {
                this.setState({
                    selected: true,
                    half: false
                }, ()=> {
                    props.onSelected(this.state);
                });
            } else {
                this.setState({
                    selected: !this.state.selected
                }, ()=> {
                    props.onSelected(this.state);
                });
            }
            e.stopPropagation();
        }}>
            <View className={cn({
                selected: state.selected,
                half: state.half
            }, props.className, 'Checkbox')}{...props} style={[styles.wrapper, props.style]}>

            </View>
        </TouchableWithoutFeedback>
    }

}
mixin.onClass(Checkbox, PureRenderMixin);
const styles = StyleSheet.create({
    wrapper: {
        width: 28,
        height: 28
    }
});
export default Checkbox
