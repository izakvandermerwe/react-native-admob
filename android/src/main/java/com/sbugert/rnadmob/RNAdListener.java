package com.sbugert.rnadmob;

import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michaelhancock on 2017/07/28.
 */
public class RNAdListener extends AdListener implements AppEventListener {

    static int adCtr = 0;
    public static final String LOGTAG="RNAdListener";

    private final PublisherAdView adView;
    private final RCTEventEmitter mEventEmitter;
    private final String id;

    public RNAdListener(PublisherAdView adView, RCTEventEmitter mEventEmitter,String id) {
        this.adView = adView;
        this.mEventEmitter = mEventEmitter;

        this.id = id==null? "AD"+(++adCtr) : id;
    }

    @Override
    public void onAppEvent(String name, String info) {
        int id = ((View) adView.getParent()).getId();
        String message = String.format("Received app event (%s, %s) viewID:%s", name, info, id);
        Log.d(LOGTAG, id+" onAppEvent: " + message);
        Map<String, Object> event = new HashMap<>();
        event.put(name, info);
        emit(Events.EVENT_ADMOB_EVENT_RECEIVED, event);
    }

    void emit(Events event) {
        emit(event, null);
    }

    static class EventEntry {
        public EventEntry(Events e, Map<String, Object> args) {
            this.name = e;
            this.args = args;
        }

        Events name;
        Map<String, Object> args;

    }

    List<EventEntry> events = new ArrayList<>();


    void emit(Events event, Map<String, Object> data) {
        Log.d(LOGTAG, id+"Emit Event: " + event.toString());

        WritableMap m = null;
        if (data != null) {
            m = Utils.toWritableMap(data);

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Log.d(LOGTAG, String.format("%s emit - ARG:  - %s %s", id, entry.getKey(), entry.getValue()));
            }
        }
        mEventEmitter.receiveEvent(((View) adView.getParent()).getId(), event.toString(), m);

        events.add(new EventEntry(event, data));
    }

    public void refireEvents() {


        Log.d(LOGTAG, id+" refireEvents");
        for (EventEntry e : events) {
            WritableMap data = null;

            Log.d(LOGTAG,id+" refireEvents - EVENT: " + e.name.toString());
            if (e.args != null) {
                data = Utils.toWritableMap(e.args);
                for (Map.Entry<String, Object> entry : e.args.entrySet()) {
                    Log.d(LOGTAG, String.format("refireEvents - EVENT:  - %s %s", entry.getKey(), entry.getValue()));
                }
                //data = new WritableMap();
            }


            mEventEmitter.receiveEvent(((View) adView.getParent()).getId(), e.name.toString(), data);
        }
    }

    @Override
    public void onAdLoaded() {
        Log.d(LOGTAG, id+" onAdLoaded");
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

        Map<String, Object> event = new HashMap<>();
        event.put("width", width);
        event.put("height", height);
        Log.d(LOGTAG, id+" ADSIZE: " + width + "x" + height);

        for (Map.Entry<String, Object> entry : event.entrySet()) {
            Log.d(LOGTAG, String.format("%s sizechange - EVENT:  - %s %s", id,entry.getKey(), entry.getValue()));
        }

        emit(Events.EVENT_SIZE_CHANGE, event);

    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        Map<String, Object> event = new HashMap<>();
        switch (errorCode) {
            case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                Log.d(LOGTAG,id+" onAdFailedToLoad - ERROR_CODE_INTERNAL_ERROR");
                event.put("error", "ERROR_CODE_INTERNAL_ERROR");
                break;
            case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                Log.d(LOGTAG, id+" onAdFailedToLoad - ERROR_CODE_INVALID_REQUEST");
                event.put("error", "ERROR_CODE_INVALID_REQUEST");
                break;
            case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                Log.d(LOGTAG, id+" onAdFailedToLoad - ERROR_CODE_NETWORK_ERROR");
                event.put("error", "ERROR_CODE_NETWORK_ERROR");
                break;
            case PublisherAdRequest.ERROR_CODE_NO_FILL:
                Log.d(LOGTAG, id+" onAdFailedToLoad - ERROR_CODE_NO_FILL");
                event.put("error", "ERROR_CODE_NO_FILL");
                break;
        }
        emit(Events.EVENT_ERROR, event);
    }

    @Override
    public void onAdOpened() {
        Log.d(LOGTAG, id+" onAdOpened");
        emit(Events.EVENT_WILL_PRESENT);
    }

    @Override
    public void onAdClosed() {
        Log.d(LOGTAG, id+" onAdClosed");
        emit(Events.EVENT_WILL_DISMISS);
    }

    @Override
    public void onAdLeftApplication() {
        Log.d(LOGTAG, id+" onAdLeftApplication");
        emit(Events.EVENT_WILL_LEAVE_APP);
    }
}
