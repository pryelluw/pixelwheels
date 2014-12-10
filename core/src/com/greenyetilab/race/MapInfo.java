package com.greenyetilab.race;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;

/**
 * Created by aurelien on 21/11/14.
 */
public class MapInfo {
    private TiledMap mMap;
    private final String mFileName;
    private float mBestTime = 0;

    MapInfo(String filename) {
        mFileName = filename;
        Preferences prefs = Gdx.app.getPreferences("com.greenyetilab.race");
        mBestTime = prefs.getFloat("best/" + mFileName, 0);
    }

    public String getTitle() {
        String title = mFileName.replace(".tmx", "");
        String first = title.substring(0, 1);
        first = first.toUpperCase();
        title = first + title.substring(1);
        return title;
    }

    public TiledMap getMap() {
        if (mMap == null) {
            MapCreator creator = new MapCreator();
            creator.addSourceMap(new AtlasTmxMapLoader().load("maps/straight_single_single.tmx"));
            creator.addSourceMap(new AtlasTmxMapLoader().load("maps/cross_single_single.tmx"));
            mMap = creator.run(4);
        }
        return mMap;
    }

    public float getBestTime() {
        return mBestTime;
    }

    public void setBestTime(float value) {
        mBestTime = value;
        Preferences prefs = RaceGame.getPreferences();
        prefs.putFloat("best/" + mFileName, mBestTime);
        prefs.flush();
    }

    public FileHandle getFile() {
        return Gdx.files.internal("maps/" + mFileName);
    }
}
