import React from 'react';
import {
  NativeModules,
  requireNativeComponent,
  View,
  UIManager,
  NativeEventEmitter,
  findNodeHandle,
} from 'react-native';

const RNBanner = requireNativeComponent('DFPAdView', DFPAdView);

export default class DFPAdView extends React.Component {

  constructor() {
    super();
    //this.onSizeChanged = this.onSizeChanged.bind(this);
    this.state = {
      style: {},
    };
  }


  _onSizeChanged = (event) => {
    console.log('ADS: onSizeChange: ----');
    const { height, width } = event.nativeEvent;
    console.log('ADS: onSizeChange: ',height, width);
    this.setState({ style: { width, height } });

    if(this.props.onSizeChange){
        this.props.onSizeChange(event);
    }

}


  render() {
    const { adUnitID, testDeviceID, bannerSizes, keywords,style, didFailToReceiveAdWithError,admobDispatchAppEvent,cacheKey,cacheGroup } = this.props;

    return (
      <View style={this.props.style}>
        <RNBanner
          ref="banner"
          style={this.state.style}

          onAdViewDidReceiveAd={this.props.adViewDidReceiveAd}
          onDidFailToReceiveAdWithError={(event) => didFailToReceiveAdWithError(event.nativeEvent.error)}
          onAdViewWillPresentScreen={this.props.adViewWillPresentScreen}
          onAdViewWillDismissScreen={this.props.adViewWillDismissScreen}
          onAdViewDidDismissScreen={this.props.adViewDidDismissScreen}
          onAdViewWillLeaveApplication={this.props.adViewWillLeaveApplication}
          onAdmobDispatchAppEvent={(event) => admobDispatchAppEvent(event)}
          onSizeChange={this._onSizeChanged}
          ad={{
              adUnit:adUnitID,
              adSizes:bannerSizes,
              keywords,
              cacheKey,
              cacheGroup
          }}
          />
      </View>
    );
  }


}

DFPAdView.propTypes = {
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
   * AdMob events
   */
  adViewDidReceiveAd: React.PropTypes.func,
  didFailToReceiveAdWithError: React.PropTypes.func,
  adViewWillPresentScreen: React.PropTypes.func,
  adViewWillDismissScreen: React.PropTypes.func,
  adViewDidDismissScreen: React.PropTypes.func,
  adViewWillLeaveApplication: React.PropTypes.func,
  admobDispatchAppEvent: React.PropTypes.func,
  sizeChange: React.PropTypes.func,
  ...View.propTypes,
};

DFPAdView.defaultProps = { bannerSizes: ["banner"], didFailToReceiveAdWithError: () => {} ,
admobDispatchAppEvent: () => {}};
