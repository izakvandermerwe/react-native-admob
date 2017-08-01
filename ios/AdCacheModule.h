#import <React/RCTEventDispatcher.h>
#import <React/RCTComponent.h>
#import <React/RCTBridgeModule.h>

@import GoogleMobileAds;

@interface AdCacheModule :NSObject <RCTBridgeModule>


+(void)clearGroup: (NSString*) group;
@end
