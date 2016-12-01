package com.sbugert.rnadmob;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.ArrayList;
import com.facebook.react.bridge.Arguments;
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
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

import java.util.Map;

public class RNPublisherBannerViewManager extends SimpleViewManager<ReactViewGroup>{

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
    attachNewAdView(view);
    return view;
   }



  protected void attachNewAdView(final ReactViewGroup view) {
    final PublisherAdView adView = new PublisherAdView(mThemedReactContext);
    adView.setAppEventListener(new AppEventDispatcher(view,mEventEmitter));
    // destroy old AdView if present
    PublisherAdView oldAdView = (PublisherAdView) view.getChildAt(0);
    view.removeAllViews();
    if (oldAdView != null) oldAdView.destroy();
    view.addView(adView);
    attachEvents(view);
  }

    static class AppEventDispatcher implements AppEventListener{


        private final ReactViewGroup view;
        private final RCTEventEmitter mEventEmitter;

        public AppEventDispatcher(ReactViewGroup view, RCTEventEmitter mEventEmitter){
            this.view = view;
            this.mEventEmitter=mEventEmitter;
        }

        @Override
        public void onAppEvent(String name, String info) {
            String message = String.format("Received app event (%s, %s) viewID:%s", name, info,view.getId());
            Log.d("PublisherAdBanner", message);
            WritableMap event = Arguments.createMap();
            event.putString(name, info);
            mEventEmitter.receiveEvent(view.getId(), Events.EVENT_ADMOB_EVENT_RECEIVED.toString(), event);
        }
    }

  protected void attachEvents(final ReactViewGroup view) {

    final PublisherAdView adView = (PublisherAdView) view.getChildAt(0);
    adView.setAdListener(new AdListener() {
      @Override
      public void onAdLoaded() {
        int width = adView.getAdSize().getWidthInPixels(mThemedReactContext);
        int height = adView.getAdSize().getHeightInPixels(mThemedReactContext);
        int left = adView.getLeft();
        int top = adView.getTop();
        adView.measure(width, height);
        adView.layout(left, top, left + width, top + height);
        mEventEmitter.receiveEvent(view.getId(), Events.EVENT_RECEIVE_AD.toString(), null);


        if (adView.getAdSize() == AdSize.SMART_BANNER) {
          width = (int) PixelUtil.toDIPFromPixel(adView.getAdSize().getWidthInPixels(mThemedReactContext));
          height = (int) PixelUtil.toDIPFromPixel(adView.getAdSize().getHeightInPixels(mThemedReactContext));
        }
        else {
          width = adView.getAdSize().getWidth();
          height = adView.getAdSize().getHeight();
        }

        WritableMap event = Arguments.createMap();
        event.putDouble("width", width);
        event.putDouble("height", height);
        mEventEmitter.receiveEvent(view.getId(), Events.EVENT_SIZE_CHANGE.toString(), event);

      }

      @Override
      public void onAdFailedToLoad(int errorCode) {
        WritableMap event = Arguments.createMap();
        switch (errorCode) {
          case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
            event.putString("error", "ERROR_CODE_INTERNAL_ERROR");
            break;
          case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
            event.putString("error", "ERROR_CODE_INVALID_REQUEST");
            break;
          case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
            event.putString("error", "ERROR_CODE_NETWORK_ERROR");
            break;
          case PublisherAdRequest.ERROR_CODE_NO_FILL:
            event.putString("error", "ERROR_CODE_NO_FILL");
            break;
        }

        mEventEmitter.receiveEvent(view.getId(), Events.EVENT_ERROR.toString(), event);
      }

      @Override
      public void onAdOpened() {
        mEventEmitter.receiveEvent(view.getId(), Events.EVENT_WILL_PRESENT.toString(), null);
      }

      @Override
      public void onAdClosed() {
        mEventEmitter.receiveEvent(view.getId(), Events.EVENT_WILL_DISMISS.toString(), null);
      }

      @Override
      public void onAdLeftApplication() {
        mEventEmitter.receiveEvent(view.getId(), Events.EVENT_WILL_LEAVE_APP.toString(), null);
      }
    });
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


    @ReactProp(name = PROP_KEYWORDS)
    public void setKeywords(final ReactViewGroup view, final ReadableArray arr)
    {
        Log.d("PublisherAdBanner - kw","");
        keywords = convertArrayToArrayList(arr);

        PublisherAdView oldAdView = (PublisherAdView) view.getChildAt(0);
        String adUnitId = oldAdView.getAdUnitId();

        attachNewAdView(view);
        PublisherAdView newAdView = (PublisherAdView) view.getChildAt(0);
        AdSize[] sizes = oldAdView.getAdSizes();
        if(oldAdView.getAdSizes() != null && oldAdView.getAdSizes().length > 0) {
            newAdView.setAdSizes(oldAdView.getAdSizes());
        }

        newAdView.setAdUnitId(adUnitId);

        loadAd(newAdView);
    }

  @ReactProp(name = PROP_BANNER_SIZE)
  public void setBannerSizes(final ReactViewGroup view, final ReadableArray arr) {
    //Log.d("PublisherAdBanner - setBannerSize", String.valueOf(sizeStringArray));
   ArrayList<Object> objArr = convertArrayToArrayList(arr);

    AdSize[] adSizes = new AdSize[objArr.size()];
    int index = 0;
    for(Object size : objArr)
    {
        Log.d("PublisherBanner - size:", String.valueOf(size.toString()));
        AdSize adSize = getAdSizeFromString(size.toString());
        adSizes[index] = adSize;
        index ++;
    }

    // store old ad unit ID (even if not yet present and thus null)
    PublisherAdView oldAdView = (PublisherAdView) view.getChildAt(0);
    String adUnitId = oldAdView.getAdUnitId();

    attachNewAdView(view);
    PublisherAdView newAdView = (PublisherAdView) view.getChildAt(0);
    newAdView.setAdSizes(adSizes);
    newAdView.setAdUnitId(adUnitId);

    // send measurements to js to style the AdView in react
    //Currently we cannot set a predefined size.as the control takes a list of available sizes...
   /* int width;
    int height;
    WritableMap event = Arguments.createMap();
    if (adSize == AdSize.SMART_BANNER) {
      width = (int) PixelUtil.toDIPFromPixel(adSize.getWidthInPixels(mThemedReactContext));
      height = (int) PixelUtil.toDIPFromPixel(adSize.getHeightInPixels(mThemedReactContext));
    }
    else {
      width = adSize.getWidth();
      height = adSize.getHeight();
    }
    event.putDouble("width", width);
    event.putDouble("height", height);
    mEventEmitter.receiveEvent(view.getId(), Events.EVENT_SIZE_CHANGE.toString(), event);*/

    loadAd(newAdView);
  }

  @ReactProp(name = PROP_AD_UNIT_ID)
  public void setAdUnitID(final ReactViewGroup view, final String adUnitID) {
    // store old banner size (even if not yet present and thus null)
    PublisherAdView oldAdView = (PublisherAdView) view.getChildAt(0);
    AdSize[] adSizes = oldAdView.getAdSizes();

    attachNewAdView(view);
    PublisherAdView newAdView = (PublisherAdView) view.getChildAt(0);
    newAdView.setAdUnitId(adUnitID);
    newAdView.setAdSizes(adSizes);
    loadAd(newAdView);
  }

  @ReactProp(name = PROP_TEST_DEVICE_ID)
  public void setPropTestDeviceID(final ReactViewGroup view, final String testDeviceID) {
    this.testDeviceID = testDeviceID;
  }

  private void loadAd(final PublisherAdView adView) {
    if (adView.getAdSizes() != null && adView.getAdUnitId() != null && keywords != null) {
      PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();
      if (testDeviceID != null){
        if (testDeviceID.equals("EMULATOR")) {
          adRequestBuilder = adRequestBuilder.addTestDevice(PublisherAdRequest.DEVICE_ID_EMULATOR);
        } else {
          adRequestBuilder = adRequestBuilder.addTestDevice(testDeviceID);
        }
      }


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
              Log.e("PublisherBanner - KW:", kw + " - " + value);
              adBundle.putString(kw,value);
          }

          AdMobExtras extras = new AdMobExtras (adBundle);
          adRequestBuilder.addNetworkExtras(extras);
        }
        catch(Exception ex)
        {
          Log.e("PublisherBanner",ex.getMessage());
        }
      }

      PublisherAdRequest adRequest = adRequestBuilder.build();
      adView.loadAd(adRequest);
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
                Log.e("PublisherBanner","failed to parse ad size");
            }
        }
        return adSize;
    }
}
