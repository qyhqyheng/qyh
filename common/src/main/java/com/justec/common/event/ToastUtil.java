package com.justec.common.event;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static Toast toast;

    /**
     * 显示toast，若多次触发，按最后一次的显示时间显示
     *
     * @param context
     * @param msg
     *            显示的信息
     * @param duration
     *            显示的时长
     */
    public static void showToast(Context context, String msg, int duration) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, duration);
        } else {
            toast.cancel();
            toast = Toast.makeText(context, msg, duration);
        }
        toast.show();
    }

    /**
     * 显示toast，若多次触发，按最后一次的显示时间显示
     *
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg) {
        showToast(context, msg, 1000);
    }

}
