package com.justec.pillowalcohol.media;

import android.content.Context;

public interface MediaInterface {
    void ValibrateChecked(boolean Checked);
    void AlarmChecked(boolean Checked);
    void SonesIndexChecked(int Index);
    void AlarmValue(int Value);
    void getContext(Context context);
    void TestingStatus(boolean status);
    void TestingData(float data);
}
