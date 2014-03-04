package com.xdtd.qm.api.active;

import com.plugin.internet.core.ResponseBase;
import com.plugin.internet.core.json.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: AM10:50
 * To change this template use File | Settings | File Templates.
 */
public class LanuchResponse extends ResponseBase {

    @JsonProperty("activeDelay")
    public int activeDelay;

    @JsonProperty("subAppName")
    public String subAppName;

    @JsonProperty("url")
    public String url;

    @JsonProperty("subExtAppName")
    public String subExtAppName;

    @JsonProperty("urlExt")
    public String urlExt;

    public LanuchResponse() {
    }

    @Override
    public String toString() {
        return "LanuchResponse{" +
                   "activeDelay=" + activeDelay +
                   ", subAppName='" + subAppName + '\'' +
                   ", url='" + url + '\'' +
                   ", subExtAppName='" + subExtAppName + '\'' +
                   ", urlExt='" + urlExt + '\'' +
                   '}';
    }
}
