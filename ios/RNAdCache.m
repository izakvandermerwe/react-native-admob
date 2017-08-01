//
//  RNAdListener.m
//  RNAdMobManager
//
//  Created by michael hancock on 2017/07/31.
//  Copyright © 2017 accosine. All rights reserved.
//

#import "RNAdCache.h"
#import "RNDFPAdView.h"


@implementation RNAdCache



+(UIView*) getView:(NSString*) group key: (NSString*)key{
    
    NSMutableDictionary * groupCache = [RNAdCache.cache valueForKey:group];
    if(groupCache!=nil){
        return [groupCache valueForKey:key];
    }
    
    return nil;
}

+(void)addView:(NSString*) group key:(NSString*) key  view:(UIView*) view{
    NSMutableDictionary * groupCache = [RNAdCache.cache valueForKey:group];
    if(groupCache==nil){
        groupCache = [[NSMutableDictionary alloc] init];
        [RNAdCache.cache setValue:groupCache forKey:group];
    }
    
    [groupCache setValue:view forKey:key];
    
}

+(void)clearGroup: (NSString*) group{
    NSMutableDictionary * groupCache = [RNAdCache.cache valueForKey:group];
    if(groupCache!=nil){
        [RNAdCache.cache setNilValueForKey:group];
    }
}

+ (NSDictionary *) cache {
    static NSDictionary * cache;
    if (cache == nil) {
        cache = [[NSMutableDictionary alloc] init];
    }
    return cache;
}



@end
