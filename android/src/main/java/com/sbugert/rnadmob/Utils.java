package com.sbugert.rnadmob;

import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.ads.AdSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by michaelhancock on 2017/07/28.
 */
public class Utils {

    public static final String LOGTAG="AdUtils";

    public static WritableMap toWritableMap(Map<String, Object> map) {
        WritableMap writableMap = Arguments.createMap();
        Iterator iterator = map.entrySet().iterator();

        for (Map.Entry pair : map.entrySet()) {

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
            } else {
                Log.d(LOGTAG, "towriteablemap value dropped" + value);
            }

            //iterator.remove();
        }

        return writableMap;
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

    public static AdSize parseCustomAdSize(String sizeString)
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

    public static AdSize getAdSizeFromString(String adSize) {
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


    public Utils() {
    }

    public static Object toObject(@Nullable ReadableMap readableMap, String key) {
        if (readableMap == null) {
            return null;
        }

        Object result;
        ReadableType readableType = readableMap.getType(key);
        switch (readableType) {
            case Null:
                result = null;
                break;
            case Boolean:
                result = readableMap.getBoolean(key);
                break;
            case Number:
                // Can be int or double.
                double tmp = readableMap.getDouble(key);

                if (tmp == (int) tmp) {
                    result = (int) tmp;
                } else {
                    result = tmp;
                }
                break;
            case String:
                result = readableMap.getString(key);
                break;
            case Map:
                result = toMap(readableMap.getMap(key));
                break;
            case Array:
                Log.d("DFPAdView", "MAP-ARRAY PROPS:"+key);
                result = toList(readableMap.getArray(key));
                break;
            default:
                throw new IllegalArgumentException("Could not convert object with key: " + key + ".");
        }

        return result;
    }

    public static Map<String, Object> toMap(@Nullable ReadableMap readableMap) {
        if (readableMap == null) {
            return null;
        }

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        if (!iterator.hasNextKey()) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            result.put(key, toObject(readableMap, key));
        }

        return result;
    }

    public static List<Object> toList(@Nullable ReadableArray readableArray) {
        if (readableArray == null) {
            return null;
        }

        List<Object> result = new ArrayList<>(readableArray.size());
        for (int index = 0; index < readableArray.size(); index++) {
            ReadableType readableType = readableArray.getType(index);
            switch (readableType) {
                case Null:
                    result.add(null);
                    break;
                case Boolean:
                    result.add(readableArray.getBoolean(index));
                    break;
                case Number:
                    // Can be int or double.
                    double tmp = readableArray.getDouble(index);
                    if (tmp == (int) tmp) {
                        result.add((int) tmp);
                    } else {
                        result.add(tmp);
                    }
                    break;
                case String:
                    result.add(readableArray.getString(index));
                    break;
                case Map:
                    result.add(toMap(readableArray.getMap(index)));
                    break;
                case Array:
                    Log.d("DFPAdView", "ARRAY-ARRAY PROPS:"+index);
                    result.add(toList(readableArray.getArray(index)));
                    break;
                default:
                    throw new IllegalArgumentException("Could not convert object with index: " + index + ".");
            }
        }

        return result;
    }



}
