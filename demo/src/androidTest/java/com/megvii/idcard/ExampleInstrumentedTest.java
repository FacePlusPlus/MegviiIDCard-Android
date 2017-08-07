package com.megvii.idcard;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.megvii.faceppidcardui.util.Util;
import com.megvii.idcard.sdk.IDCard;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    IDCard idCard;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        idCard = new IDCard();
        idCard.init(appContext, Util.readModel(appContext));
    }

    @Test
    public void testConfig() {
        IDCard.IDCardConfig idCardConfig = idCard.getIdCardConfig();
        assertNotNull(idCardConfig);
    }
}
