package com.sbugert.rnadmob;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFPAdViewManager extends SimpleViewManager<RNAdView>{
    public static final String REACT_CLASS = "DFPAdView";
    private static final String LOGTAG = "DFPAdView";

    public static final String PROP_AD = "ad";


    ViewCache viewCache = ViewCache.instance;

    private RCTEventEmitter mEventEmitter;

    @Override
    public String getName() {
        return REACT_CLASS;
    }


    @ReactProp(name = PROP_AD)
    public void setAd(final RNAdView view, final ReadableMap map)
    {
        Log.d(LOGTAG,"SET AD");
        Map<String, Object> json = Utils.toMap(map);

        String adUnit = (String) json.get("adUnit");
        String cacheKey = (String) json.get("cacheKey");
        boolean enableCache = cacheKey!=null&&cacheKey!="";
        if(cacheKey==null)
            cacheKey="";
        String cacheGroup = (String) json.get("cacheGroup");
        Log.d(LOGTAG,String.format("cacheKey: %s cacheEnabled:%s", cacheKey,enableCache));
        Log.d(LOGTAG,String.format("cacheGroup: %s", cacheGroup));


        List sizes = (List)json.get("adSizes");
        AdSize[] adSizes = new AdSize[sizes.size()];
        int i=0;
        for(Object o:sizes){
            adSizes[i++] = Utils.getAdSizeFromString(o+"");
        }

        PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

        if(json.containsKey("keywords")){
            ArrayList<Object> keywords = (ArrayList<Object>) json.get("keywords");
            if(keywords.size() > 0)
            {
                try
                {
                    Bundle adBundle = new Bundle();
                    for(Object kwArr : keywords)
                    {

                        Log.d(LOGTAG,String.format("KW:RAW: %S",kwArr));
                        ArrayList<Object> innerArr = (ArrayList<Object>) kwArr;
                        String kw = (String) innerArr.get(0);
                        String value = (String) innerArr.get(1);
                        Log.d(LOGTAG,"KW:"+ kw + " - " + value);
                        cacheKey+="("+kw + " - " + value+")";
                        adBundle.putString(kw,value);
                    }
                    AdMobExtras extras = new AdMobExtras (adBundle);
                    adRequestBuilder.addNetworkExtras(extras);
                }
                catch(Exception ex)
                {
                    Log.e(LOGTAG,ex.getMessage());
                }
            }
        }

        if(json.containsKey("contentUrl")) {
            String contentUrl = (String) json.get("contentUrl");
            adRequestBuilder.setContentUrl(contentUrl);
            Log.d(LOGTAG, "setting contentUrl: " +contentUrl);
        }

        PublisherAdRequest adRequest = adRequestBuilder.build();
        PublisherAdView adView = (PublisherAdView) view.getChildAt(0);
        boolean refire = false;
        if(adView == null){
            if(enableCache)
                adView = viewCache.getView(cacheGroup,cacheKey,PublisherAdView.class);
            if(adView == null){
                Log.d(LOGTAG, "creating adview" +cacheKey);
                adView = new PublisherAdView(view.getContext());
                adView.setAdUnitId(adUnit);
                adView.setAdSizes(adSizes);
                RNAdListener adListener = new RNAdListener(adView,mEventEmitter,cacheKey);
                adView.setAppEventListener(adListener);
                adView.setAdListener(adListener);
                if(enableCache)
                    viewCache.addView(cacheGroup,cacheKey,adView);
                adView.loadAd(adRequest);
            }else{
                Log.d(LOGTAG, "using cached adview" +cacheKey);
                ((ViewGroup)adView.getParent()).removeView(adView);
                refire=true;
            }
            view.addView(adView);
            if(refire)
                ((RNAdListener)adView.getAdListener()).refireEvents();
        }else{
            // todo re create adview
            Log.d(LOGTAG, "reusing adview" +adUnit);
        }
    }

    @Override
    protected RNAdView createViewInstance(ThemedReactContext themedReactContext) {
        mEventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);
        return new RNAdView(themedReactContext);
    }

    @Override
    @Nullable
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (Events event : Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }

}
