#import "RNAdMobDFPManager.h"
#import "RNDFPBannerView.h"

#import <React/RCTBridge.h>

@implementation RNAdMobDFPManager

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (UIView *)view
{
  return [[RNDFPBannerView alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}
// this was removed: http://www.reactnative.com/react-native-v0-38-0-rc-0-released/
- (NSArray *) customDirectEventTypes
{
  return @[
           @"onSizeChange",
           @"onAdViewDidReceiveAd",
           @"onDidFailToReceiveAdWithError",
           @"onAdViewWillPresentScreen",
           @"onAdViewWillDismissScreen",
           @"onAdViewDidDismissScreen",
           @"onAdViewWillLeaveApplication",
           @"onAdmobDispatchAppEvent"
           ];
}

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}


RCT_EXPORT_VIEW_PROPERTY(bannerSizes, NSArray);
RCT_EXPORT_VIEW_PROPERTY(keywords, NSArray);
RCT_EXPORT_VIEW_PROPERTY(adUnitID, NSString);
RCT_EXPORT_VIEW_PROPERTY(testDeviceID, NSString);

RCT_EXPORT_VIEW_PROPERTY(onSizeChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdmobDispatchAppEvent, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewDidReceiveAd, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDidFailToReceiveAdWithError, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillPresentScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillDismissScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewDidDismissScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillLeaveApplication, RCTBubblingEventBlock)

@end

