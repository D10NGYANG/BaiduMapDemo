package com.hailiao.baidumapdemo.app

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.hailiao.baidumapdemo.R
import com.simple.spiderman.SpiderMan
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {

    companion object{
        private lateinit var mInstance: MyApp
        /** 获取实例 */
        fun instance() : MyApp = mInstance
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this

        // 调试工具初始化
        SpiderMan.init(this) //设置主题样式，内置了两种主题样式light和dark
            .setTheme(R.style.SpiderManTheme_Dark)

        // 初始化百度地图
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }
}