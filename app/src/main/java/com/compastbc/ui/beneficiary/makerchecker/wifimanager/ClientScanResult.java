package com.compastbc.ui.beneficiary.makerchecker.wifimanager;


//class to search available clients nearby
public class ClientScanResult {

    private String IpAddr;

    private String HWAddr;

    private String Device;

    private String deviceName;

    private boolean isReachable;

    public ClientScanResult(String ipAddress, String hWAddress, String device, boolean isReachable, String deviceName) {
        super();
        IpAddr = ipAddress;
        HWAddr = hWAddress;
        Device = device;
        this.deviceName = deviceName;
        this.setReachable(isReachable);
    }

    public String getIpAddress() {
        return IpAddr;
    }


    public String getDevice() {
        return Device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getHWAddr() {
        return HWAddr;
    }

    public boolean isReachable() {
        return isReachable;
    }

    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }
}