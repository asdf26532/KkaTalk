package com.han.kkaTalk

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class KakaoAplication : Application() {

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "native appkey")
    }

}