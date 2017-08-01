#import <React/RCTEventDispatcher.h>
#import <React/RCTComponent.h>
@import GoogleMobileAds;
#import "RNAdListener.h"

@class RCTEventDispatcher;
@class RNAdListener;

@interface RNDFPAdView : UIView 

@property (nonatomic, copy) NSDictionary *ad;

@property (nonatomic, copy) RCTBubblingEventBlock onSizeChange;
@property (nonatomic, copy) RCTBubblingEventBlock onAdmobDispatchAppEvent;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewDidReceiveAd;
@property (nonatomic, copy) RCTBubblingEventBlock onDidFailToReceiveAdWithError;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewWillPresentScreen;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewWillDismissScreen;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewDidDismissScreen;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewWillLeaveApplication;

@property (nonatomic, retain) RNAdListener * listener;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher viewCache:(NSMutableDictionary *) viewCache NS_DESIGNATED_INITIALIZER;
- (GADAdSize)getAdSizeFromString:(NSString *)bannerSize;
- (GADAdSize)parseCustomAdSize:(NSString *)bannerSize;

@end
