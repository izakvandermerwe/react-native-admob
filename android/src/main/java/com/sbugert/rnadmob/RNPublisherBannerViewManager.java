package com.sbugert.rnadmob;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RNPublisherBannerViewManager extends SimpleViewManager<ReactViewGroup>{
    private static final String LOGTAG = "RNAdMobDFP";

    ViewCache viewCache = ViewCache.instance;


    public static final String REACT_CLASS = "RNAdMobDFP";

    public static final String PROP_BANNER_SIZE = "bannerSizes";
    public static final String PROP_KEYWORDS = "keywords";
    public static final String PROP_AD_UNIT_ID = "adUnitID";
    public static final String PROP_TEST_DEVICE_ID = "testDeviceID";

    private String testDeviceID = null;
    private ArrayList<Object> keywords = null;

    public enum Events {
        EVENT_SIZE_CHANGE("onSizeChange"),
        EVENT_RECEIVE_AD("onAdViewDidReceiveAd"),
        EVENT_ERROR("onDidFailToReceiveAdWithError"),
        EVENT_WILL_PRESENT("onAdViewWillPresentScreen"),
        EVENT_WILL_DISMISS("onAdViewWillDismissScreen"),
        EVENT_DID_DISMISS("onAdViewDidDismissScreen"),
        EVENT_WILL_LEAVE_APP("onAdViewWillLeaveApplication"),
        EVENT_ADMOB_EVENT_RECEIVED("onAdmobDispatchAppEvent");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private ThemedReactContext mThemedReactContext;
    private RCTEventEmitter mEventEmitter;

    @Override
    public String getName() {
        return REACT_CLASS;
    }



    @Override
    protected ReactViewGroup createViewInstance(ThemedReactContext themedReactContext) {
        mThemedReactContext = themedReactContext;
        mEventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);
        ReactViewGroup view = new ReactViewGroup(themedReactContext);
        return view;
    }


    public static WritableMap toWritableMap(Map<String, Object> map) {
        WritableMap writableMap = Arguments.createMap();
        Iterator iterator = map.entrySet().iterator();

        for(Map.Entry pair: map.entrySet()){

            Object value = pair.getValue();

            if (value == null) {
                writableMap.putNull((String) pair.getKey());
            } else if (value instanceof Boolean) {
                writableMap.putBoolean((String) pair.getKey(), (Boolean) value);
            } else if (value instanceof Double) {
                writableMap.putDouble((String) pair.getKey(), (Double) value);
            } else if (value instanceof Integer) {
                writableMap.putInt((String) pair.getKey(), (Integer) value);
            } else if (value instanceof String) {
                writableMap.putString((String) pair.getKey(), (String) value);
            }else{
                Log.d(LOGTAG, "towriteablemap value dropped"+value);
            }

            //iterator.remove();
        }

        return writableMap;
    }


    public static class RNAdListener extends AdListener implements AppEventListener{


        private final PublisherAdView adView;
        private final RCTEventEmitter mEventEmitter;

        public RNAdListener(PublisherAdView adView , RCTEventEmitter mEventEmitter){
            this.adView = adView;
            this.mEventEmitter=mEventEmitter;
        }

        @Override
        public void onAppEvent(String name, String info) {
            int id = ((View)adView.getParent()).getId();
            String message = String.format("Received app event (%s, %s) viewID:%s", name, info,id);
            Log.d(LOGTAG, "onAppEvent: "+ message);
            Map<String,Object> event = new HashMap<>();
            event.put(name, info);
            emit(Events.EVENT_ADMOB_EVENT_RECEIVED,event);
        }

        void emit( Events event){
            emit(event,null);
        }

        static class EventEntry{
            public EventEntry(Events e,Map<String,Object> args){
                this.name =e;
                this.args=args;
            }
            RNPublisherBannerViewManager.Events name;
            Map<String,Object> args;

        }
        List<EventEntry> events = new ArrayList<>();



        void emit( Events event,Map<String,Object> data ){
            Log.d(LOGTAG, "Emit Event: "+event.toString());

            WritableMap m= null;
            if(data!=null) {
                m = toWritableMap(data);

                for(Map.Entry<String, Object> entry: data.entrySet()){
                    Log.d(LOGTAG, String.format("emit - ARG:  - %s %s",entry.getKey(),entry.getValue()));
                }
            }
            mEventEmitter.receiveEvent(((View) adView.getParent()).getId(), event.toString(), m);

            events.add(new EventEntry(event,data));
        }

        public void refireEvents( ){


            Log.d(LOGTAG, "refireEvents");
            for (EventEntry e:events) {
                WritableMap data = null;

                Log.d(LOGTAG, "refireEvents - EVENT: "+e.name.toString());
                if(e.args!=null) {
                    data = toWritableMap(e.args);
                    for(Map.Entry<String, Object> entry: e.args.entrySet()){
                        Log.d(LOGTAG, String.format("refireEvents - EVENT:  - %s %s",entry.getKey(),entry.getValue()));
                    }
                    //data = new WritableMap();
                }



                mEventEmitter.receiveEvent(((View) adView.getParent()).getId(), e.name.toString(),data);
            }
        }

        @Override
        public void onAdLoaded() {
            Log.d(LOGTAG, "onAdLoaded");
            int width = adView.getAdSize().getWidthInPixels(adView.getContext());
            int height = adView.getAdSize().getHeightInPixels(adView.getContext());
            int left = adView.getLeft();
            int top = adView.getTop();
            adView.measure(width, height);
            adView.layout(left, top, left + width, top + height);

            emit(Events.EVENT_RECEIVE_AD);



            if (adView.getAdSize() == AdSize.SMART_BANNER) {
                width = (int) PixelUtil.toDIPFromPixel(adView.getAdSize().getWidthInPixels(adView.getContext()));
                height = (int) PixelUtil.toDIPFromPixel(adView.getAdSize().getHeightInPixels(adView.getContext()));
            } else {
                width = adView.getAdSize().getWidth();
                height = adView.getAdSize().getHeight();
            }

            Map<String,Object> event = new HashMap<>();
            event.put("width", width);
            event.put("height", height);
            Log.d(LOGTAG, "ADSIZE: " + width + "x" + height);

            for(Map.Entry<String, Object> entry: event.entrySet()){
                Log.d(LOGTAG, String.format("sizechange - EVENT:  - %s %s",entry.getKey(),entry.getValue()));
            }

            emit( Events.EVENT_SIZE_CHANGE, event);

        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            Map<String,Object> event = new HashMap<>();
            switch (errorCode) {
                case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                    Log.d(LOGTAG, "onAdFailedToLoad - ERROR_CODE_INTERNAL_ERROR");
                    event.put("error", "ERROR_CODE_INTERNAL_ERROR");
                    break;
                case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                    Log.d(LOGTAG, "onAdFailedToLoad - ERROR_CODE_INVALID_REQUEST");
                    event.put("error", "ERROR_CODE_INVALID_REQUEST");
                    break;
                case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                    Log.d(LOGTAG, "onAdFailedToLoad - ERROR_CODE_NETWORK_ERROR");
                    event.put("error", "ERROR_CODE_NETWORK_ERROR");
                    break;
                case PublisherAdRequest.ERROR_CODE_NO_FILL:
                    Log.d(LOGTAG, "onAdFailedToLoad - ERROR_CODE_NO_FILL");
                    event.put("error", "ERROR_CODE_NO_FILL");
                    break;
            }
            emit( Events.EVENT_ERROR, event);
        }

        @Override
        public void onAdOpened() {
            Log.d(LOGTAG, "onAdOpened");
            emit( Events.EVENT_WILL_PRESENT);
        }

        @Override
        public void onAdClosed() {
            Log.d(LOGTAG, "onAdClosed");
            emit( Events.EVENT_WILL_DISMISS);
        }

        @Override
        public void onAdLeftApplication() {
            Log.d(LOGTAG, "onAdLeftApplication");
            emit( Events.EVENT_WILL_LEAVE_APP);
        }
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


    private static ArrayList<Object> convertArrayToArrayList(ReadableArray readableArray) {
        ArrayList<Object> array = new ArrayList<Object>();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.add(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.add(readableArray.getDouble(i));
                    break;
                case String:
                    array.add(readableArray.getString(i));
                    break;
           /* case Map:
                array.put(convertMapToJson(readableArray.getMap(i)));
                break;*/
                case Array:
                    array.add(convertArrayToArrayList(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }

    void setTag(View view, String name, Object value){
        Object o =view.getTag();
        if(o==null){
            o = new HashMap<String,Object>();
            view.setTag(o);
        }
        if(o!=null && o instanceof  Map){
            Map tag = (Map)o;
            tag.put(name,value);
            view.setTag(tag);
        }else{
            Log.w(REACT_CLASS, "failed to set tag "+name+" tag obj was already set and not a map");
        }

    }

    @ReactProp(name = PROP_KEYWORDS)
    public void setKeywords(final ReactViewGroup view, final ReadableArray arr)
    {
        Log.d(LOGTAG,"setKeyWords");
        keywords = convertArrayToArrayList(arr);

        setTag( view, "keywords", keywords );
    }

    @ReactProp(name = PROP_BANNER_SIZE)
    public void setBannerSizes(final ReactViewGroup view, final ReadableArray arr) {
        ArrayList<Object> objArr = convertArrayToArrayList(arr);

        AdSize[] adSizes = new AdSize[objArr.size()];
        int index = 0;
        for(Object size : objArr)
        {
            Log.d(LOGTAG,"size:"+ size);
            AdSize adSize = getAdSizeFromString(size.toString());
            adSizes[index] = adSize;
            index ++;
        }

        setTag(view,"adSizes",adSizes);
    }

    @ReactProp(name = PROP_AD_UNIT_ID)
    public void setAdUnitID(final ReactViewGroup view, final String adUnitID) {
        setTag(view,"adUnit",adUnitID);
    }

    @ReactProp(name = PROP_TEST_DEVICE_ID)
    public void setPropTestDeviceID(final ReactViewGroup view, final String testDeviceID) {
        this.testDeviceID = testDeviceID;
    }

    @Override
    public Map<String,Integer> getCommandsMap() {
        return MapBuilder.of("loadAd", COMMAND_LOAD_AD);
    }

    private static final int COMMAND_LOAD_AD = 1001;
    @Override
    public void receiveCommand( ReactViewGroup view, int commandType, @javax.annotation.Nullable ReadableArray args) {
        Log.d(LOGTAG, "receiveCommand: "+commandType);
        Assertions.assertNotNull(view);
        Assertions.assertNotNull(args);
        switch (commandType){
            case COMMAND_LOAD_AD:
                loadAd(view);
                break;
        }
    }

    @ReactMethod
    public void loadAd(final ReactViewGroup view) {

        Map tag = (Map)view.getTag();

        String adUnit = (String) tag.get("adUnit");
        String cacheKey = (String) tag.get("cacheKey");
        String cacheGroup = (String) tag.get("cacheGroup");
        AdSize[] adSizes = (AdSize[]) tag.get("adSizes");

        PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

        if(tag.containsKey("keywords")){
            ArrayList<Object> keywords = (ArrayList<Object>) tag.get("keywords");
            if(keywords.size() > 0)
            {
                try
                {
                    Bundle adBundle = new Bundle();
                    for(Object kwArr : keywords)
                    {
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

        PublisherAdRequest adRequest = adRequestBuilder.build();

        PublisherAdView adView = (PublisherAdView) view.getChildAt(0);
        boolean refire = false;
        if(adView == null){

            adView = viewCache.getView(cacheGroup,cacheKey,PublisherAdView.class);
            if(adView == null){
                Log.d(LOGTAG, "creating adview" +cacheKey);
                adView = new PublisherAdView(mThemedReactContext);
                adView.setAdUnitId(adUnit);
                adView.setAdSizes(adSizes);
                RNAdListener adListener = new RNAdListener(adView,mEventEmitter);
                adView.setAppEventListener(adListener);
                adView.setAdListener(adListener);
                viewCache.addView(cacheGroup, cacheKey,adView);
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
            Log.d(LOGTAG, "reusing adview" +adUnit);
        }
    }


    private AdSize getAdSizeFromString(String adSize) {
        switch (adSize) {
            case "banner":
                return AdSize.BANNER;
            case "largeBanner":
                return AdSize.LARGE_BANNER;
            case "mediumRectangle":
                return AdSize.MEDIUM_RECTANGLE;
            case "fullBanner":
                return AdSize.FULL_BANNER;
            case "leaderBoard":
                return AdSize.LEADERBOARD;
            case "smartBannerPortrait":
                return AdSize.SMART_BANNER;
            case "smartBannerLandscape":
                return AdSize.SMART_BANNER;
            case "smartBanner":
                return AdSize.SMART_BANNER;
            default:
                return parseCustomAdSize(adSize);
        }
    }

    private AdSize parseCustomAdSize(String sizeString)
    {
        AdSize adSize = AdSize.BANNER;
        if(sizeString.contains("x")){
            String[] sz = sizeString.split("x");
            try {
                adSize = new AdSize( Integer.parseInt(sz[0]),Integer.parseInt(sz[1]) );
            }catch (Exception e){
                Log.e(LOGTAG,"failed to parse ad size");
            }
        }
        return adSize;
    }
}
