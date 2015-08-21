package br.org.funcate.extended.model;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;

/**
 * Created by Andre Carvalho on 28/05/15.
 */
public class TMConfigEditableLayer {

    private ArrayMap<String,ConfigEditableLayer> config;

    public TMConfigEditableLayer() {
        this.config =new ArrayMap<String, ConfigEditableLayer>();
    }

    public void addConfig(String identify, String JSON) {
        ConfigEditableLayer configEditableLayer=new ConfigEditableLayer(JSON);
        this.config.put(identify, configEditableLayer);
    }

    public void addConfig(String identify, String JSON, String mediaTable) {
        ConfigEditableLayer configEditableLayer=new ConfigEditableLayer(JSON, mediaTable);
        this.config.put(identify, configEditableLayer);
    }

    public String getJSONConfig(String identify) {
        return this.config.get(identify).getJSON();
    }
    public String getMediaTableConfig(String identify) {
        return this.config.get(identify).getMediaTable();
    }

    public boolean isEditable(String identify) {
        return this.config.containsKey(identify);
    }

    private class ConfigEditableLayer {
        private String JSON;
        private String mediaTable;

        public ConfigEditableLayer(String json) {
            this.JSON=json;
            this.mediaTable=null;
        }

        public ConfigEditableLayer(String json, String mediaTable) {
            this.JSON=json;
            this.mediaTable=mediaTable;
        }

        public String getJSON() {
            return JSON;
        }

        public String getMediaTable() {
            return mediaTable;
        }
    }
}
