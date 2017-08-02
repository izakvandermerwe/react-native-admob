#import "RNDFPAdView.h"
#import <React/RCTBridgeModule.h>
#import <React/UIView+React.h>
#import <React/RCTLog.h>

#import "RNAdListener.h"

@implementation RNDFPAdView {
    DFPBannerView  *_bannerView;
    NSMutableDictionary *_viewCache;
    RCTEventDispatcher *_eventDispatcher;
}

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher viewCache:(NSMutableDictionary *) viewCache
{
    if ((self = [super initWithFrame:CGRectZero])) {
        _eventDispatcher = eventDispatcher;
        _viewCache = viewCache;
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

- (void)setAd:(NSDictionary *) ad {
    NSString *cacheKey =nil;
    @try {
        
         cacheKey = [ad valueForKey:@"cacheKey"];
        NSString *adUnit = [ad valueForKey:@"adUnit"];
        NSLog(@"ADS: adUnit: %@",[ad valueForKey:@"adUnit"]);
        NSLog(@"ADS: cacheKey: %@",[ad valueForKey:@"cacheKey"]);
        
        
        NSArray *sizes = [ad valueForKey:@"adSizes"];
        NSMutableArray *adSizes = [[NSMutableArray alloc] init];
        for(id currentSize in sizes)
        {
            GADAdSize size = [self getAdSizeFromString:currentSize];
            [adSizes addObject: NSValueFromGADAdSize(size)];
            NSLog(@"ADS: Parsing adsize(%@)", NSValueFromGADAdSize(size));
        }

        
    
        GADRequest *request = [GADRequest request];
        
        NSArray *keywords = [ad valueForKey:@"keywords"];
        if(keywords.count > 0)
        {
            request.keywords = [[NSMutableArray alloc] init];
            //ad keyword params to request
            GADExtras *extras = [[GADExtras alloc] init];

            // [ ["key1,"val1",
            //   ["key2",["val2"]
            // ]

            NSMutableDictionary *kwDict = [[NSMutableDictionary alloc] init];
            NSMutableArray *kwArr = [[NSMutableArray alloc] init];
            for(id kvp in keywords)
            {
                NSString* key = kvp[0];
                NSString* val = kvp[1];
                NSLog(@"Parsing kvp (%@, %@)", key, val);
                [kwDict setObject:val forKey:key];
                [kwArr addObject: val];
            }
            request.keywords = kwArr;
            extras.additionalParameters = kwDict; //["posno": page];
            [request registerAdNetworkExtras:extras];
        }
        
        
        bool refire = false;
        bool load=false;
        
        if(_bannerView == nil){
            if(cacheKey!=nil){
                self.listener = [_viewCache valueForKey:cacheKey];
                if(self.listener !=nil){
                    _bannerView = self.listener.adView;
                }
                
            }
            if(_bannerView == nil){
                NSLog(@"ADS: creating adview %@",cacheKey);
                _bannerView = [[DFPBannerView alloc] init];
                _bannerView.adUnitID = adUnit;
                _bannerView.validAdSizes = [adSizes copy];
                self.listener = [[RNAdListener alloc] init];
              
                [_bannerView setAppEventDelegate: (id)self.listener];
                _bannerView.delegate = (id)self.listener;
              
                _bannerView.rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
                self.listener.adView = _bannerView;
                self.listener.view = self;
                
                if(cacheKey!=nil)
                    [_viewCache setObject:self.listener forKey:cacheKey];
                load=true;
            }else{
                self.listener.view = self;
                NSLog(@"ADS: using cached adview %@",cacheKey);
                if(_bannerView.superview != nil){
                    [_bannerView removeFromSuperview];
                }
                refire=true;
            }
            
            [self addSubview: _bannerView];
            
            if(load){
            
                [_bannerView loadRequest:request];
            }
            if(refire)
                [self.listener refireEvents];
        }else{
            NSLog(@"ADS: reusing adview %@",cacheKey);
        }
    }@catch (NSException *exception) {
        NSLog(@"ADS: ERROR %@", exception.reason);
    }
    
  
}

-(void)layoutSubviews
{
    NSLog(@"ADS: layoutSubviews %@ %@",self.listener.adView.superview,self);
    
    UIView *  _bannerView = self.listener.adView;
    
    _bannerView.frame =CGRectMake(
          self.bounds.origin.x,
          self.bounds.origin.x,
          _bannerView.frame.size.width,
        _bannerView.frame.size.height);
    [super layoutSubviews ];
}

- (void)removeFromSuperview
{
    NSLog(@"ADS: removeFromSuperview");
    _eventDispatcher = nil;
    [super removeFromSuperview];
}

@end
