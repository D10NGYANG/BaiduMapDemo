package com.hailiao.baidumapdemo.bean

import com.baidu.mapapi.map.offline.MKOLSearchRecord
import com.baidu.mapapi.map.offline.MKOLUpdateElement
import java.io.Serializable

data class MapCityInfo(
    var record: MKOLSearchRecord,
    var element: MKOLUpdateElement? = null
): Serializable
