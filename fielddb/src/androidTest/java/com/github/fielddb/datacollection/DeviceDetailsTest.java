package com.github.fielddb.datacollection;

import android.support.test.filters.MediumTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RequiresDevice
@RunWith(AndroidJUnit4.class)
@MediumTest
public class DeviceDetailsTest {
    DeviceDetails underTest;

    public DeviceDetailsTest() {
        super();
    }

    @Test
    public void setDeviceDetails_shouldWork() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                underTest = new DeviceDetails(getTargetContext());
                assertThat(underTest, notNullValue());
                underTest.setDeviceDetails();
                assertThat(underTest.mDeviceDetails, containsString("model: 'Pixel 2'"));
                assertThat(underTest.mDeviceDetails, containsString("sdk: '27'"));
                assertThat(underTest.mDeviceDetails, containsString("identifier: '63bac898cd404a04'"));
                assertThat(underTest.mDeviceDetails, containsString("location:{longitude: '0.0', latitude: '0.0', accuracy: '0.0'}"));
                assertThat(underTest.mDeviceDetails, containsString("wifiMACaddress: '02:00:00:00:00:00'"));
                assertThat(underTest.mDeviceDetails, containsString("telephonyDeviceId:'unknown'"));
                // assertThat(underTest.mDeviceDetails, containsString("{name: 'LG Nexus 5X', model: 'Nexus 5X', product: 'bullhead', manufacturer: 'LG', appversion: 'null', sdk: '27', osversion: '3.10.73-g7d1e90244638(4448085)',device: 'bullhead', screen: {height: '1794', width: '1080', ratio: '1.0', currentOrientation: 'portrait'}, serial: '00e77591894820b4', identifier: 'aad6b1e07b3ec579', wifiMACaddress: 'unknown', timestamp: '1513786005317',location:{longitude: '0.0', latitude: '0.0', accuracy: '0.0'} , telephonyDeviceId:'unknown'}"));
            }
        });
    }

}
