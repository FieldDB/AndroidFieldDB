package com.github.fielddb;

import android.content.Context;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertThat;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ApplicationTest extends ApplicationTestCase<FieldDBApplication> {
    private Context mContext;

    public ApplicationTest() {
        super(FieldDBApplication.class);
    }

    @Before
    public void initTargetContext() {
        mContext = getTargetContext();
        assertThat(mContext, notNullValue());
    }

    @Test
    public void mUserIsDefined() {
        assertThat(mContext, notNullValue());
    }

}
