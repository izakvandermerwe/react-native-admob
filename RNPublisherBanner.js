import React from 'react';
import {
  NativeModules,
  requireNativeComponent,
  View,
  UIManager,
  NativeEventEmitter,
  findNodeHandle,
} from 'react-native';

const RNBanner = requireNativeComponent('RNAdMobDFP', PublisherBanner);

export default class PublisherBanner extends React.Component {

  constructor() {
    super();
    this.onSizeChange = this.onSizeChange.bind(this);
    this.state = {
      style: {},
    };
  }


  onSizeChange(event) {

    const { height, width } = event.nativeEvent;
    console.log('ADS: onSizeChange: ',height, width);
        console.log('ADS: onSizeChange: ',height, width);
    this.setState({ style: { width, height } });
}


  render() {
    const { adUnitID, testDeviceID, bannerSizes, keywords,style, didFailToReceiveAdWithError,admobDispatchAppEvent } = this.props;
    return (
      <View style={this.props.style}>
        <RNBanner
          ref="banner"
          style={this.state.style}
          onSizeChange={this.onSizeChange.bind(this)}
          onAdViewDidReceiveAd={this.props.adViewDidReceiveAd}
          onDidFailToReceiveAdWithError={(event) => didFailToReceiveAdWithError(event.nativeEvent.error)}
          onAdViewWillPresentScreen={this.props.adViewWillPresentScreen}
          onAdViewWillDismissScreen={this.props.adViewWillDismissScreen}
          onAdViewDidDismissScreen={this.props.adViewDidDismissScreen}
          onAdViewWillLeaveApplication={this.props.adViewWillLeaveApplication}
          onAdmobDispatchAppEvent={(event) => admobDispatchAppEvent(event)}
          testDeviceID={testDeviceID}
          adUnitID={adUnitID}
          bannerSizes={bannerSizes}
          keywords={keywords} />
      </View>
    );
  }

  componentDidMount(){
      console.log('ADS: JSADVIEW ad loading...')
      this.loadAd()
  }

  componentWillUnmount(){
      console.log('ADS: JSADVIEW componentWillUnmount...')
  }

  loadAd() {
        UIManager.dispatchViewManagerCommand(
            findNodeHandle(this.refs.banner),
            UIManager.RNAdMobDFP.Commands.loadAd,
            [],
        );
    }
}

PublisherBanner.propTypes = {
  style: View.propTypes.style,

  /**
   * AdMob iOS library banner size constants
   * (https://developers.google.com/admob/ios/banner)
   * banner (320x50, Standard Banner for Phones and Tablets)
   * largeBanner (320x100, Large Banner for Phones and Tablets)
   * mediumRectangle (300x250, IAB Medium Rectangle for Phones and Tablets)
   * fullBanner (468x60, IAB Full-Size Banner for Tablets)
   * leaderboard (728x90, IAB Leaderboard for Tablets)
   * smartBannerPortrait (Screen width x 32|50|90, Smart Banner for Phones and Tablets)
   * smartBannerLandscape (Screen width x 32|50|90, Smart Banner for Phones and Tablets)
   *
   * banner is default
   */
  bannerSizes: React.PropTypes.array,

  /**
   * Admob banner Keywords
   */
  keywords : React.PropTypes.array,

  /**
   * AdMob ad unit ID
   */
  adUnitID: React.PropTypes.string,

  /**
   * Test device ID
   */
  testDeviceID: React.PropTypes.string,

  /**
   * AdMob iOS library events
   */
  adViewDidReceiveAd: React.PropTypes.func,
  didFailToReceiveAdWithError: React.PropTypes.func,
  adViewWillPresentScreen: React.PropTypes.func,
  adViewWillDismissScreen: React.PropTypes.func,
  adViewDidDismissScreen: React.PropTypes.func,
  adViewWillLeaveApplication: React.PropTypes.func,
  admobDispatchAppEvent: React.PropTypes.func,
  ...View.propTypes,
};

PublisherBanner.defaultProps = { bannerSizes: ["banner"], didFailToReceiveAdWithError: () => {} ,
admobDispatchAppEvent: () => {}};
