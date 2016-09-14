package com.fr.bi.conf.report.map;

import com.fr.file.XMLFileManager;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLReadable;
import com.fr.stable.xml.XMLableReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 2016/8/31.
 */
public class BIWMSManager extends XMLFileManager {
    private static final String XML_TAG = "BIWMSManager";
    private Map<String, JSONObject> gisMap =  new HashMap<String, JSONObject>();
    private static BIWMSManager manager;

    public static BIWMSManager getInstance() {
        synchronized (BIWMSManager.class) {
            if (manager == null) {
                manager = new BIWMSManager();
                manager.readXMLFile();
            }
            return manager;
        }
    }

    @Override
    public String fileName() {
        return "bi_wms.xml";
    }

    @Override
    public void readXML(final XMLableReader reader) {
        if (reader.isChildNode()) {
            if (ComparatorUtils.equals(reader.getTagName(), "gis")) {
                String name = reader.getAttrAsString("name", StringUtils.EMPTY);
                String type = reader.getAttrAsString("type", StringUtils.EMPTY);
                final JSONObject info = new JSONObject();
                final JSONArray ja = new JSONArray();
                try {
                    info.put("type", type).put("wmsLayer", ja);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                gisMap.put(name, info);
                reader.readXMLObject(new XMLReadable() {
                    @Override
                    public void readXML(XMLableReader xmLableReader) {
                        if(xmLableReader.isChildNode()){
                            if(ComparatorUtils.equals(xmLableReader.getTagName(), "wmsLayer")){
                                ja.put(xmLableReader.getElementValue());
                            }
                            if(ComparatorUtils.equals(xmLableReader.getTagName(), "url")){
                                try {
                                    info.put("url", xmLableReader.getElementValue());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void writeXML(XMLPrintWriter xmlPrintWriter) {

    }

    public void clear(){
        gisMap.clear();
    }

    public Map<String, JSONObject> getWMSInfo(){
        return gisMap;
    }
}
