//
//  RNAdListener.m
//  RNAdMobManager
//
//  Created by michael hancock on 2017/07/31.
//  Copyright © 2017 accosine. All rights reserved.
//

#import "RNAdListener.h"
#import "RNDFPAdView.h"


@implementation RNAdListener{
    NSMutableArray * _events;

}


- (void)adView:(GADBannerView *)banner
    didReceiveAppEvent:(NSString *)name
      withInfo:(NSString *)info {
    
    NSLog(@"Received app event (%@, %@)", name, info);
    NSMutableDictionary *myDictionary = [[NSMutableDictionary alloc] init];
    myDictionary[name] = info;
    
    
    [self emit:  @{
                   @"event": @"app-event",
                   @"data":@{ name: info }
                   }];
    
}


- (void) emit:(NSDictionary *) event {

    [self.events addObject: event];
    [self dispatchEvent: event];
    
}


- (void) dispatchEvent:(NSDictionary *) event {
    
    NSString * name=[event valueForKey:@"event"];
    NSDictionary * data = [event valueForKey:@"data"];
    NSLog(@"ADS: emit %@",name);
    
    if( [name isEqualToString:@"app-event"]  )
    {
        NSLog(@"ADS: appevnt %@",name);
        self.view.onAdmobDispatchAppEvent(data);
    }else if([name isEqualToString:@"size-change"] ){
        NSLog(@"ADS: sizechange %@",name);
        self.view.onSizeChange(data);
    }else if([name isEqualToString:@"recieve-ad"] ){
        self.view.onAdViewDidReceiveAd(data);
    }else if([name isEqualToString:@"failed-to-recieve-ad"] ){
        self.view.onDidFailToReceiveAdWithError(data);
    }else if([name isEqualToString:@"present-screen"] ){
        self.view.onAdViewWillPresentScreen(data);
    }else if([name isEqualToString:@"will-dismiss-screen"] ){
        self.view.onAdViewWillDismissScreen(data);
    }else if([name isEqualToString:@"did-dismiss-screen"] ){
        self.view.onAdViewDidDismissScreen(data);
    }else if([name isEqualToString:@"will-leave-app"] ){
        self.view.onAdViewWillLeaveApplication(data);
    }
}

/// Tells the delegate an ad request loaded an ad.
- (void)adViewDidReceiveAd:(DFPBannerView *)adView {
    
    NSLog(@"ADS: adViewDidReceiveAd ");
    [self emit: @{
                  @"event": @"size-change",
                  @"data":@{
                             @"width": [NSNumber numberWithFloat: adView.bounds.size.width],
                             @"height": [NSNumber numberWithFloat: adView.bounds.size.height]
                             }
                  } ];
    
    
    [self emit:@{
                 @"event": @"recieve-ad",
                 @"data": @{}
                 }];

}


/// Tells the delegate an ad request failed.
- (void)adView:(DFPBannerView *)adView
didFailToReceiveAdWithError:(GADRequestError *)error {
    NSLog(@"ADS: didFailToReceiveAdWithError");
    
    [self emit:@{
                 @"event": @"failed-to-recieve-ad",
                 @"data": @{ @"error": [error localizedDescription] }
                 }];


}

/// Tells the delegate that a full screen view will be presented in response
/// to the user clicking on an ad.
- (void)adViewWillPresentScreen:(DFPBannerView *)adView {
    NSLog(@"ADS: adViewWillPresentScreen");
    
    
    [self emit:@{
                 @"event": @"present-screen",
                 @"data": @{  }
                 }];
    

}

/// Tells the delegate that the full screen view will be dismissed.
- (void)adViewWillDismissScreen:(DFPBannerView *)adView {
    NSLog(@"ADS: adViewWillDismissScreen");
    
    [self emit:@{
                 @"event": @"will-dismiss-screen",
                 @"data": @{  }
                 }];

}

/// Tells the delegate that the full screen view has been dismissed.
- (void)adViewDidDismissScreen:(DFPBannerView *)adView {
    NSLog(@"ADS: adViewDidDismissScreen");
    
    [self emit:@{
                 @"event": @"did-dismiss-screen",
                 @"data": @{  }
                 }];

}

- (void)adViewWillLeaveApplication:(DFPBannerView *)adView {
    NSLog(@"ADS: adViewWillLeaveApplication");
    
    [self emit:@{
                 @"event": @"will-leave-app",
                 @"data": @{  }
                 }];
}


-(void) refireEvents{
    
    for (NSDictionary* e in self.events) {
        [self dispatchEvent:e];
    }
}

- (NSArray *)events {
    
    if (_events == nil) {
        _events = [[NSMutableArray alloc] init];
    }
    return _events;
}



@end
