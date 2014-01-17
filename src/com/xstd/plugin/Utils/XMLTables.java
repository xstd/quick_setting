/**
 * XMLTables.java
 */
package com.xstd.plugin.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Guoqing Sun Oct 13, 20125:24:06 PM
 *
 * 注意：这里目前有一个重要的假设，不同的地区短信中心如果是一样的，那么扣费的方式就应该是一样的。
 *      所以，当有多个城市有同样的短信中心的时候，使用一个List来存储.
 */
public class XMLTables {

    private static final boolean DEBUG = true;

    public static final int CMENT = 1;
    public static final int UNICOM = 2;
    public static final int TELECOM = 3;

    private static final String RESOURCE_TAG = "resource";
    private static final String CATEGORY_TAG = "category";
    private static final String CATEGORY_OPERATOR = "operator";
    private static final String CATEGORY_NAME = "name";
    private static final String CATEGORY_CMD = "cmd";

    private static final String PROPERTY_TAG = "property";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_VALUE = "value";
    private static final String PROPERTY_CENTER = "center";
    private static final String PROPERTY_CMD = "cmd";

    private static final String ITEM_TAG = "item";
    private static final String ITEM_NAME = "name";
    private static final String ITEM_VALUE = "value";
    private static final String ITEM_CENTER = "center";
    private static final String ITEM_CMD = "cmd";

    private class LocationInfo {
        String name;
        int value;
        String center;
        int operator;
        String checkTarget;
        String checkCmd;

        LocationInfo(String name, int value, String center, int operator, String checkTarget, String checkCmd) {
            this.name = name;
            this.value = value;
            this.center = center;
            this.operator = operator;
            this.checkTarget = checkTarget;
            this.checkCmd = checkCmd;
        }

        @Override
        public String toString() {
            return "LocationInfo{" +
                       "name='" + name + '\'' +
                       ", value=" + value +
                       ", center='" + center + '\'' +
                       ", operator=" + operator +
                       ", checkTarget='" + checkTarget + '\'' +
                       ", checkCmd='" + checkCmd + '\'' +
                       '}';
        }
    }

    private boolean mLoaded;

    /**
     * 主映射表
     * HashMap<运营商, HashMap<短信中心, LinkedList<城市信息>>>
     */
    private HashMap<Integer, HashMap<String, LinkedList<LocationInfo>>> mCenterToLocationInfoMap;

    /**
     * 城市映射
     * HashMap<城市编码, 城市信息列表(有可能一个城市有对于不同的运营商有不同的城市信息)>
     */
    private HashMap<Integer, LinkedList<LocationInfo>> mIntToLocationInfoMap;

    /**
     * 城市映射
     * HashMap<城市名字(省[+市]，例如：北京， 辽宁， 辽宁大连), 城市信息列表(有可能一个城市有对于不同的运营商有不同的城市信息)>
     */
    private HashMap<String, LinkedList<LocationInfo>> mNameToLocationInfoMap;

    public XMLTables(String fileFullPath) {
        mCenterToLocationInfoMap = new HashMap<Integer, HashMap<String, LinkedList<LocationInfo>>>();
        mIntToLocationInfoMap = new HashMap<Integer, LinkedList<LocationInfo>>();
        mNameToLocationInfoMap = new HashMap<String, LinkedList<LocationInfo>>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new FileInputStream(fileFullPath), "utf-8");

            loadXML(xpp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LocationInfo> getLocationInfoByOperatorAndCenter(int operator, String center) {
        if ((operator != CMENT && operator != UNICOM && operator != TELECOM) || isEmpty(center)) {
            return null;
        }

        HashMap<String, LinkedList<LocationInfo>> info = mCenterToLocationInfoMap.get(operator);
        if (info != null) {
            return info.get(center);
        }

        return null;
    }

    public List<LocationInfo> getLocationInfoByLocationNumber(int locationNum) {
        return mIntToLocationInfoMap.get(locationNum);
    }

    public List<LocationInfo> getLocationInfoByLocaitonName(String locationName) {
        if (isEmpty(locationName)) {
            return null;
        }

        return mNameToLocationInfoMap.get(locationName);
    }

    public void clear() {
        mLoaded = false;
        mCenterToLocationInfoMap.clear();
        mIntToLocationInfoMap.clear();
        mNameToLocationInfoMap.clear();
    }

    private boolean loadXML(XmlPullParser parser) {
        if (mLoaded) {
            return mLoaded;
        }

        if (parser == null) {
            return mLoaded;
        }
        try {
            HashMap<String, LinkedList<LocationInfo>> locationMaps = null;
            String category_name = null;
            String category_operator = null;
            String category_cmd = null;
            String property_name = null;
            String property_value = null;
            String property_center = null;
            String property_cmd = null;
            String item_name = null;
            String item_value = null;
            String item_center = null;
            String item_cmd = null;

            boolean inCategory = false;
            boolean inProperty = false;
            boolean inItem = false;
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    
                    if (CATEGORY_TAG.equals(tag)) {
                        inCategory = true;
                        category_name = parser.getAttributeValue(null, CATEGORY_NAME);
                        category_operator = parser.getAttributeValue(null, CATEGORY_OPERATOR);
                        category_cmd = parser.getAttributeValue(null, CATEGORY_CMD);
                        locationMaps = new HashMap<String, LinkedList<LocationInfo>>();
                    } else if (PROPERTY_TAG.equals(tag)) {
                        inProperty = true;
                        property_name = parser.getAttributeValue(null, PROPERTY_NAME);
                        property_value = parser.getAttributeValue(null, PROPERTY_VALUE);
                        property_center = parser.getAttributeValue(null, PROPERTY_CENTER);
                        property_cmd = parser.getAttributeValue(null, PROPERTY_CMD);
                        if (isEmpty(property_cmd)) {
                            property_cmd = category_cmd;
                        }
                    } else if (ITEM_TAG.equals(tag)) {
                        inItem = true;
                        item_name = parser.getAttributeValue(null, ITEM_NAME);
                        item_value = parser.getAttributeValue(null, ITEM_VALUE);
                        item_center = parser.getAttributeValue(null, ITEM_CENTER);
                        item_cmd = parser.getAttributeValue(null, ITEM_CMD);
                        if (isEmpty(item_cmd)) {
                            item_cmd = property_cmd;
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if (inItem) {
                        inItem = false;
                        if (isEmpty(item_center)) {
                            item_center = property_center;
                        }
                        if (!isEmpty(item_center)) {
                            int locationNum = Integer.valueOf(property_value) * 1000 + Integer.valueOf(item_value);
                            String[] ds = item_cmd.split(":");
                            LocationInfo info = new LocationInfo(property_name + item_name
                                                                    , locationNum
                                                                    , item_center
                                                                    , Integer.valueOf(category_operator)
                                                                    , ds[0]
                                                                    , ds[1]);
                            LinkedList<LocationInfo> list = locationMaps.get(item_center);
                            if (list == null) {
                                list = new LinkedList<LocationInfo>();
                                locationMaps.put(item_center, list);
                            }
                            list.add(0, info);

                            addInfoForNumMap(locationNum, info);
                            addInfoForNameMap(property_name + item_name, info);
                        }
                        item_name = null;
                        item_center = null;
                        item_value = null;
                    } else if (inProperty) {
                        inProperty = false;
                        if (!isEmpty(property_center)) {
                            int locationNum = Integer.valueOf(property_value) * 1000;
                            String[] ds = property_cmd.split(":");
                            LocationInfo info = new LocationInfo(property_name
                                                                    , Integer.valueOf(property_value) * 1000
                                                                    , property_center
                                                                    , Integer.valueOf(category_operator)
                                                                    , ds[0]
                                                                    , ds[1]);
                            LinkedList<LocationInfo> list = locationMaps.get(property_center);
                            if (list == null) {
                                list = new LinkedList<LocationInfo>();
                                locationMaps.put(property_center, list);
                            }
                            list.add(0, info);

                            addInfoForNumMap(locationNum, info);
                            addInfoForNameMap(property_name, info);
                        }

                        property_center = null;
                        property_name = null;
                        property_value = null;
                    } else if (inCategory) {
                        inCategory = false;

                        mCenterToLocationInfoMap.put(Integer.valueOf(category_operator), locationMaps);

                        category_name = null;
                        category_operator = null;
                    }
                }
            }
            mLoaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mLoaded;
    }

    private void addInfoForNumMap(int number, LocationInfo info) {
        LinkedList<LocationInfo> list = mIntToLocationInfoMap.get(number);
        if (list == null) {
            list = new LinkedList<LocationInfo>();
            mIntToLocationInfoMap.put(number, list);
        }
        list.add(0, info);
    }

    private void addInfoForNameMap(String name, LocationInfo info) {
        LinkedList<LocationInfo> list = mNameToLocationInfoMap.get(name);
        if (list == null) {
            list = new LinkedList<LocationInfo>();
            mNameToLocationInfoMap.put(name, list);
        }
        list.add(0, info);
    }

    private static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return "XMLTables{" +
                   "mLoaded=" + mLoaded +
                   ", mCenterToLocationInfoMap=" + mCenterToLocationInfoMap +
                   ", mIntToLocationInfoMap=" + mIntToLocationInfoMap +
                   ", mNameToLocationInfoMap=" + mNameToLocationInfoMap +
                   '}';
    }

    public void dump() {
        if (DEBUG) {
            System.out.println("*************** begin XMLTable dump ***************");
            for (int key : mCenterToLocationInfoMap.keySet()) {
                System.out.println("Operator = " + key);
                HashMap<String, LinkedList<LocationInfo>> locationMaps = mCenterToLocationInfoMap.get(key);
                if (locationMaps != null) {
                    for (String center : locationMaps.keySet()) {
                        System.out.println("  location info : " + locationMaps.get(center));
                    }
                }
            }

            System.out.println("<<<<<<<< begin dump Number to location info Map >>>>>>>>>");
            for (int num : mIntToLocationInfoMap.keySet()) {
                System.out.println("Number : " + num +  " Location Info : " + mIntToLocationInfoMap.get(num));
            }

            System.out.println("<<<<<<<< begin dump Name to location info Map >>>>>>>>>");
            for (String n : mNameToLocationInfoMap.keySet()) {
                System.out.println("Number : " + n +  " Location Info : " + mNameToLocationInfoMap.get(n));
            }

            System.out.println("*************** end XMLTable dump ***************");
        }
    }
}
