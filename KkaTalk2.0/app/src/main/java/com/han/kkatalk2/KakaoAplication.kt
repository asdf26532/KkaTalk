package com.han.kkatalk2

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk

class KakaoAplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }

}