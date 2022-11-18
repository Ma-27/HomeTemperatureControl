package com.hometemperature

import android.app.Application
import timber.log.Timber


class HomeTemperatureApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            //引入插件Timber，不用每次都去手动打印log
            Timber.plant(Timber.DebugTree())
            //TODO 以后只需要搜索 成功 tag即可查看自定义log
            Timber.tag("成功")
        }
    }
}