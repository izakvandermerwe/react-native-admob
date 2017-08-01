#import "DFPAdViewManager.h"
#import "RNDFPAdView.h"

#import <React/RCTBridge.h>

@implementation DFPAdViewManager

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (UIView *)view
{
    return [[RNDFPAdView alloc] initWithEventDispatcher:self.bridge.eventDispatcher viewCache:DFPAdViewManager.viewCache];
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


RCT_EXPORT_VIEW_PROPERTY(ad, NSDictionary);

RCT_EXPORT_VIEW_PROPERTY(onSizeChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdmobDispatchAppEvent, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewDidReceiveAd, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDidFailToReceiveAdWithError, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillPresentScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillDismissScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewDidDismissScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillLeaveApplication, RCTBubblingEventBlock)


+ (NSDictionary *)viewCache {
    static NSDictionary *cache = nil;
    if (cache == nil) {
        cache = [[NSMutableDictionary alloc] init];
    }
    return cache;
}
@end

