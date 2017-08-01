package com.sbugert.rnadmob;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

/**
 * Created by michaelhancock on 2017/08/01.
 */

class AdCacheModule extends ReactContextBaseJavaModule {
    public AdCacheModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }


    @ReactMethod
    public void clearGroup(String name) {
        ViewCache.instance.clearGroup(name);
    }

    @Override
    public String getName() {
        return "AdCacheModule";
    }

}

