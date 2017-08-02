//
//  RNAdListener.m
//  RNAdMobManager
//
//  Created by michael hancock on 2017/07/31.
//  Copyright © 2017 accosine. All rights reserved.
//

#import "RNAdCache.h"
#import "AdCacheModule.h"


@implementation AdCacheModule

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(clearGroup:(NSString *)group)
{
    NSLog(@"ADS: clearing ad cache group  %@",group);
    [RNAdCache clearGroup:group];
}


@end
