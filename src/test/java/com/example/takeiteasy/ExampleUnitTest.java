package com.example.takeiteasy;

import org.json.JSONException;
import org.json.JSONStringer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
       // assertEquals(4, 2 + 2);
        String js = null;
        try {
            js = new JSONStringer().object().key("x").value(10).endObject().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(js);
    }
}