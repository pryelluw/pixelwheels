/*
 * Copyright 2018 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Pixel Wheels.
 *
 * Tiny Wheels is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.agateau.pixelwheels.stats;

import com.agateau.utils.Assert;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Map;

public class JsonGameStatsIO implements GameStats.IO {
    private final FileHandle mHandle;
    private GameStats mGameStats;
    private Gson mGson = new GsonBuilder().setPrettyPrinting().create();

    public JsonGameStatsIO(FileHandle handle) {
        mHandle = handle;
    }

    @Override
    public void setGameStats(GameStats gameStats) {
        mGameStats = gameStats;
    }

    @Override
    public void load() {
        Assert.check(mGameStats != null, "setGameStats() has not been called");
        if (!mHandle.exists()) {
            return;
        }
        mGameStats.mTrackStats.clear();
        String json = mHandle.readString("UTF-8");
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(json).getAsJsonObject();
        JsonObject trackStatsObject = root.getAsJsonObject("trackStats");
        for (Map.Entry<String, JsonElement> kv : trackStatsObject.entrySet()) {
            String trackId = kv.getKey();
            mGameStats.addTrack(trackId);
            loadTrackStats(mGameStats.getTrackStats(trackId), kv.getValue().getAsJsonObject());
        }
    }

    private void loadTrackStats(TrackStats trackStats, JsonObject object) {
        loadResults(trackStats.mLapRecords, object.getAsJsonArray("lap"));
        loadResults(trackStats.mTotalRecords, object.getAsJsonArray("total"));
    }

    private void loadResults(ArrayList<TrackResult> results, JsonArray array) {
        results.clear();
        for (JsonElement element : array) {
            TrackResult result = mGson.fromJson(element, TrackResult.class);
            results.add(result);
        }
    }

    @Override
    public void save() {
        Assert.check(mGameStats != null, "setGameStats() has not been called");
        JsonObject root = new JsonObject();
        JsonObject trackStatsObject = new JsonObject();
        root.add("trackStats", trackStatsObject);
        for (Map.Entry<String, TrackStats> kv : mGameStats.mTrackStats.entrySet()) {
            trackStatsObject.add(kv.getKey(), createJsonForTrack(kv.getValue()));
        }
        String json = mGson.toJson(root);
        mHandle.writeString(json, false /* append */);
    }

    private JsonObject createJsonForTrack(TrackStats trackStats) {
        JsonObject root = new JsonObject();
        root.add("lap", createJsonForResults(trackStats.get(TrackStats.ResultType.LAP)));
        root.add("total", createJsonForResults(trackStats.get(TrackStats.ResultType.TOTAL)));
        return root;
    }

    private JsonArray createJsonForResults(ArrayList<TrackResult> results) {
        JsonArray array = new JsonArray();
        for (TrackResult result : results) {
            array.add(mGson.toJsonTree(result));
        }
        return array;
    }
}