#import "RNDFPBannerView.h"
#import <React/RCTBridgeModule.h>
#import <React/UIView+React.h>
#import <React/RCTLog.h>

@implementation RNDFPBannerView {
    DFPBannerView  *_bannerView;
    RCTEventDispatcher *_eventDispatcher;
}

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    if ((self = [super initWithFrame:CGRectZero])) {
        _eventDispatcher = eventDispatcher;
    }
    return self;
}

RCT_NOT_IMPLEMENTED(- (instancetype)initWithFrame:(CGRect)frame)
RCT_NOT_IMPLEMENTED(- (instancetype)initWithCoder:coder)

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    RCTLogError(@"AdMob Banner cannot have any subviews");
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    RCTLogError(@"AdMob Banner cannot have any subviews");
    return;
}

- (GADAdSize)getAdSizeFromString:(NSString *)bannerSize
{
    if ([bannerSize isEqualToString:@"banner"]) {
        return kGADAdSizeBanner;
    } else if ([bannerSize isEqualToString:@"largeBanner"]) {
        return kGADAdSizeLargeBanner;
    } else if ([bannerSize isEqualToString:@"mediumRectangle"]) {
        return kGADAdSizeMediumRectangle;
    } else if ([bannerSize isEqualToString:@"fullBanner"]) {
        return kGADAdSizeFullBanner;
    } else if ([bannerSize isEqualToString:@"leaderboard"]) {
        return kGADAdSizeLeaderboard;
    } else if ([bannerSize isEqualToString:@"smartBannerPortrait"]) {
        return kGADAdSizeSmartBannerPortrait;
    } else if ([bannerSize isEqualToString:@"smartBannerLandscape"]) {
        return kGADAdSizeSmartBannerLandscape;
    }
    else {
        return [self parseCustomAdSize:bannerSize];;
    }
}

- (GADAdSize)parseCustomAdSize:(NSString *)bannerSizeString
{
    GADAdSize _localSize = kGADAdSizeBanner;
    @try {
        //try parse width x height string to GADAdSize
        if ([bannerSizeString rangeOfString:@"x"].location != NSNotFound)
        {
            NSArray *array = [bannerSizeString componentsSeparatedByString:@"x"];
            int width = [ array[0] intValue];
            int height = [ array[1] intValue];
            _localSize = GADAdSizeFromCGSize(CGSizeMake(width, height));
        }
    }
    @catch (NSException *exception) {
        NSLog(@"%@", exception.reason);
    }
    return _localSize;
}


-(void)loadBanner {
    
    if (_adUnitID && _bannerSizes && _keywords) {
        _bannerView = [[DFPBannerView alloc] init];
        
        NSMutableArray *validSizes = [[NSMutableArray alloc] init];
        
        for(id currentSize in _bannerSizes)
        {
            GADAdSize size = [self getAdSizeFromString:currentSize];
            [validSizes addObject: NSValueFromGADAdSize(size)];
           //  NSLog(@"Parsing adsize(%@)", size);
        }
        
        
        NSArray *adSizes = [validSizes copy];
        //added Admob event dispatch listener
        [_bannerView setAppEventDelegate:self];
        
        _bannerView.delegate = self;
        _bannerView.validAdSizes = adSizes;
        _bannerView.adUnitID = _adUnitID;
        _bannerView.rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
        GADRequest *request = [GADRequest request];
        if(_testDeviceID) {
            if([_testDeviceID isEqualToString:@"EMULATOR"]) {
                request.testDevices = @[kGADSimulatorID];
            } else {
                request.testDevices = @[_testDeviceID];
            }
        }
        
        if(_keywords.count > 0)
        {
            request.keywords = [[NSMutableArray alloc] init];
            //ad keyword params to request
            GADExtras *extras = [[GADExtras alloc] init];
            
            // [ ["key1,"val1",
            //   ["key2",["val2"]
            // ]
            
            NSMutableDictionary *kwDict = [[NSMutableDictionary alloc] init];
            NSMutableArray *kwArr = [[NSMutableArray alloc] init];
            for(id kvp in _keywords)
            {
                NSString* key = kvp[0];
                NSString* val = kvp[1];
              //  NSLog(@"Parsing kvp (%@, %@)", key, val);
                [kwDict setObject:val forKey:key];
                [kwArr addObject: val];
            }
            request.keywords = kwArr;
            extras.additionalParameters = kwDict; //["posno": page];
            [request registerAdNetworkExtras:extras];
        }
        
        [_bannerView loadRequest:request];
    }
}

- (void)setBannerSizes:(NSArray *)bannerSizes
{
    NSLog(@"setBannerSizes");
    
    if(![bannerSizes isEqual:_bannerSizes]) {
        _bannerSizes = bannerSizes;
        if (_bannerView) {
            [_bannerView removeFromSuperview];
        }
        [self loadBanner];
    }
}

- (void)setKeywords:(NSArray *)keywords
{
    NSLog(@"setKeywords");
    
    if(![keywords isEqual:_keywords]) {
        _keywords = keywords;
        if (_bannerView) {
            [_bannerView removeFromSuperview];
        }
        [self loadBanner];
    }
}


- (void)setAdUnitID:(NSString *)adUnitID
{
    if(![adUnitID isEqual:_adUnitID]) {
        _adUnitID = adUnitID;
        if (_bannerView) {
            [_bannerView removeFromSuperview];
        }
        
        [self loadBanner];
    }
}
- (void)setTestDeviceID:(NSString *)testDeviceID
{
    if(![testDeviceID isEqual:_testDeviceID]) {
        _testDeviceID = testDeviceID;
        if (_bannerView) {
            [_bannerView removeFromSuperview];
        }
        
        [self loadBanner];
    }
}

-(void)layoutSubviews
{
    [super layoutSubviews ];
    
    _bannerView.frame = CGRectMake(
                                   self.bounds.origin.x,
                                   self.bounds.origin.x,
                                   _bannerView.frame.size.width,
                                   _bannerView.frame.size.height);
    [self addSubview:_bannerView];
}

- (void)removeFromSuperview
{
    _eventDispatcher = nil;
    [super removeFromSuperview];
}

- (void)adView:(DFPBannerView *)banner
didReceiveAppEvent:(NSString *)name
      withInfo:(NSString *)info {
    NSLog(@"Received app event (%@, %@)", name, info);
    NSMutableDictionary *myDictionary = [[NSMutableDictionary alloc] init];
    myDictionary[name] = info;
    if (self.onAdmobDispatchAppEvent) {
        self.onAdmobDispatchAppEvent(@{ name: info });
    }
}

/// Tells the delegate an ad request loaded an ad.
- (void)adViewDidReceiveAd:(DFPBannerView *)adView {
    if(!CGRectEqualToRect(self.bounds, _bannerView.bounds)) {
        if (self.onSizeChange) {
            self.onSizeChange(@{
                                @"width": [NSNumber numberWithFloat: _bannerView.bounds.size.width],
                                @"height": [NSNumber numberWithFloat: _bannerView.bounds.size.height]
                                });
        }
    }
    
    if (self.onAdViewDidReceiveAd) {
        self.onAdViewDidReceiveAd(@{});
    }
}

/// Tells the delegate an ad request failed.
- (void)adView:(DFPBannerView *)adView
didFailToReceiveAdWithError:(GADRequestError *)error {
    if (self.onDidFailToReceiveAdWithError) {
        self.onDidFailToReceiveAdWithError(@{ @"error": [error localizedDescription] });
    }
}

/// Tells the delegate that a full screen view will be presented in response
/// to the user clicking on an ad.
- (void)adViewWillPresentScreen:(DFPBannerView *)adView {
    if (self.onAdViewWillPresentScreen) {
        self.onAdViewWillPresentScreen(@{});
    }
}

/// Tells the delegate that the full screen view will be dismissed.
- (void)adViewWillDismissScreen:(DFPBannerView *)adView {
    if (self.onAdViewWillDismissScreen) {
        self.onAdViewWillDismissScreen(@{});
    }
}

/// Tells the delegate that the full screen view has been dismissed.
- (void)adViewDidDismissScreen:(DFPBannerView *)adView {
    if (self.onAdViewDidDismissScreen) {
        self.onAdViewDidDismissScreen(@{});
    }
}

/// Tells the delegate that a user click will open another app (such as
/// the App Store), backgrounding the current app.
- (void)adViewWillLeaveApplication:(DFPBannerView *)adView {
    if (self.onAdViewWillLeaveApplication) {
        self.onAdViewWillLeaveApplication(@{});
    }
}

@end
