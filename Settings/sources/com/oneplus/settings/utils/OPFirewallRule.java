package com.oneplus.settings.utils;

public class OPFirewallRule {
    private static final String TAG = "FirewallRule";
    private Integer id;
    private Integer mobile;
    private String pkg;
    private Integer wlan;

    public OPFirewallRule(String pkg, Integer wlan, Integer mobile) {
        this.pkg = pkg;
        this.wlan = wlan;
        this.mobile = mobile;
    }

    public OPFirewallRule(Integer id, String pkg, Integer wlan, Integer mobile) {
        this.id = id;
        this.pkg = pkg;
        this.wlan = wlan;
        this.mobile = mobile;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPkg() {
        return this.pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public Integer getWlan() {
        return this.wlan;
    }

    public void setWlan(Integer wlan) {
        this.wlan = wlan;
    }

    public Integer getMobile() {
        return this.mobile;
    }

    public void setMobile(Integer mobile) {
        this.mobile = mobile;
    }
}
