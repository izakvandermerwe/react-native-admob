#import <React/RCTEventDispatcher.h>
#import <React/RCTComponent.h>

@import GoogleMobileAds;

@interface RNAdCache :NSObject 


+(UIView*) getView:(NSString*) group key: (NSString*)key;

+(void)addView:(NSString*) group key:(NSString*) key  view:(UIView*) view;

+(void)clearGroup: (NSString*) group;
@end
