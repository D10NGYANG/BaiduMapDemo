package com.hailiao.baidumapdemo.activity

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.activity.offline.OfflineActivity
import com.hailiao.baidumapdemo.databinding.ActMainBinding
import com.hailiao.baidumapdemo.utils.goTo
import com.hi.dhl.binding.databind
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {

    private val binding: ActMainBinding by databind(R.layout.act_main)
    private lateinit var mLocationClient: LocationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.executePendingBindings()
        binding.mapView.onCreate(this, savedInstanceState)

        // 修改地图类型
        binding.mapView.map.mapType = BaiduMap.MAP_TYPE_NORMAL
        // 开启地图的定位图层
        binding.mapView.map.isMyLocationEnabled = true

        // 定位初始化
        mLocationClient = LocationClient(this)
        // 通过LocationClientOption设置LocationClient相关参数
        val option = LocationClientOption().apply {
            // 打开gps
            isOpenGps = true
            // 设置坐标类型
            setCoorType("bd09ll")
            setScanSpan(1000)
            locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            isLocationNotify = true
        }
        // 设置locationClientOption
        mLocationClient.locOption = option
        // 注册LocationListener监听器
        val myLocationListener = MyLocationListener(binding.mapView.map)
        mLocationClient.registerLocationListener(myLocationListener)
        mLocationClient.start()

        // 获取定位权限
        GlobalScope.launch {
            checkLocationPermission()
        }

        // 点击定位
        binding.btnLocation.setOnClickListener {
            val map = binding.mapView.map
            val locData = map.locationData
            map.setMapStatus(
                MapStatusUpdateFactory.newLatLng(LatLng(locData.latitude, locData.longitude)))
        }

        // 切换地图样式
        binding.togLayer.setOnCheckedChangeListener { _, isChecked ->
            binding.mapView.map.mapType = if (isChecked) {
                BaiduMap.MAP_TYPE_SATELLITE
            } else {
                BaiduMap.MAP_TYPE_NORMAL
            }
        }

        // 点击离线地图
        binding.btnOffline.setOnClickListener {
            goTo(OfflineActivity::class.java)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        binding.mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mLocationClient.stop()
        binding.mapView.map.isMyLocationEnabled = false
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    /** 定位监听器 */
    class MyLocationListener(val map: BaiduMap): BDAbstractLocationListener() {
        private var isFirst = true
        override fun onReceiveLocation(p0: BDLocation?) {
            p0?: return
            val locData = MyLocationData.Builder()
                .accuracy(p0.radius)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(p0.direction)
                .latitude(p0.latitude)
                .longitude(p0.longitude)
                .build()
            val point = LatLng(p0.latitude, p0.longitude)
            map.setMyLocationData(locData)
            if (isFirst) {
                map.setMapStatus(MapStatusUpdateFactory.newLatLng(point))
                isFirst = false
            }
        }
    }
}

/**
 * 检查定位权限
 * @receiver BaseActivity
 * @return Boolean
 */
suspend fun AppCompatActivity.checkLocationPermission(): Boolean {
    return suspendCoroutine { cont ->
        GlobalScope.launch {
            // 请求定位权限
            val permissionResult = PermissionManager.requestPermissions(
                this@checkLocationPermission,
                1,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permissionResult is PermissionResult.PermissionGranted) {
                // 请求成功
                cont.resume(true)
            } else {
                cont.resume(false)
            }
        }
    }
}