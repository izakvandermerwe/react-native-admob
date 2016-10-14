#import "RCTEventDispatcher.h"
@import GoogleMobileAds;

@class RCTEventDispatcher;

@interface RNDFPBannerView : UIView <GADBannerViewDelegate>

@property (nonatomic, copy) NSArray *bannerSizes;
@property (nonatomic, copy) NSString *adUnitID;
@property (nonatomic, copy) NSString *testDeviceID;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;
- (GADAdSize)getAdSizeFromString:(NSString *)bannerSize;
- (void)loadBanner;

@end
