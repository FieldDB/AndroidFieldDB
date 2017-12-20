package com.github.fielddb.datacollection;

import android.support.test.filters.MediumTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.equalTo;
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

    @Before
    public void setUp() throws Exception {
        underTest = new DeviceDetails(getTargetContext());
        assertThat(underTest, notNullValue());
    }

    @Test
    public void setDeviceDetails_shouldWork() {
        underTest.setDeviceDetails();
        assertThat(underTest.mDeviceDetails, equalTo(" "));
    }

}