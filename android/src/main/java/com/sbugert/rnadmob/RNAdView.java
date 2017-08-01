package com.sbugert.rnadmob;

import android.content.Context;

import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

/**
 * Created by michaelhancock on 2017/07/28.
 */
public class RNAdView extends ReactViewGroup {

   // public final PublisherAdView adView;

    public RNAdView(Context context) {
        super(context);
        //addView(this.adView = new PublisherAdView(context));
    }

}
