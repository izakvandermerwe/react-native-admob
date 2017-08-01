package com.sbugert.rnadmob;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by michaelhancock on 2017/07/28.
 */
public class AdViewCache {

    Map<String, View> cache = new HashMap<>();

    public <T> T getView(String key, Class<T> clazz) {
        return (T) this.cache.get(key);
    }

    public void addView(String key, View v) {
        this.cache.put(key, v);
    }

}
