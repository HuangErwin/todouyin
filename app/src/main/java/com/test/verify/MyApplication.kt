package com.test.verify

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import cn.jiguang.verifysdk.api.JVerificationInterface
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta

class MyApplication :Application() {
    override fun onCreate() {
        super.onCreate()

        Bugly.init(this, "6d6b7e37a6", true)
        JVerificationInterface.setDebugMode(true)
        val start = System.currentTimeMillis()
        JVerificationInterface.init( this ) { code, result ->
            Log.d(
                "MyApp",
                "[init] code = " + code + " result = " + result + " consists = " + (System.currentTimeMillis() - start)
            )
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        Beta.installTinker()
    }
}
