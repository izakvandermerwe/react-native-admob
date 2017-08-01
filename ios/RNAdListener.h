#import <React/RCTEventDispatcher.h>
#import <React/RCTComponent.h>
#import "RNDFPAdView.h";

@import GoogleMobileAds;

@class RCTEventDispatcher;
@class RNDFPAdView;

@interface RNAdListener : NSObject<GADBannerViewDelegate>
@property (nonatomic, retain) RNDFPAdView * view;
@property (nonatomic, retain) GADBannerView * adView;

- (void) refireEvents;
- (NSMutableArray *)events;

- (void) emit:(NSDictionary*) event ;
- (void) dispatchEvent:(NSDictionary*) event ;
@end
