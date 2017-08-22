package org.scau.mimi.util;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by 10313 on 2017/8/22.
 */

public class ResponseUtil {

    public static String getString(Response response) {
        String data = null;
        try {
            data = response.body().string();
        } catch (IOException e) {
                  e.printStackTrace();
        }
        return data;
    }
}
