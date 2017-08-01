package com.sbugert.rnadmob;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by michaelhancock on 2017/08/01.
 */
public class ViewCache {

     private ViewCache(){

    }

    static ViewCache instance = new ViewCache();

    Map<String,Map<String, View>> cache = new HashMap<>();

    public <T> T getView(String group,String key, Class<T> clazz) {

        Map groupCache = cache.get(group);
        if(groupCache == null)
            return null;

        return (T) groupCache.get(key);
    }

    public void addView(String group,String key, View v) {

        Map groupCache = cache.get(group);
        if(groupCache == null)
        {
            groupCache = new HashMap<String,View>();
            this.cache.put(group, groupCache);
        }
        groupCache.put(key,v);

    }

    public void clearGroup(String group) {
        cache.remove(group);
    }
}
