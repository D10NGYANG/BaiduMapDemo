package com.hailiao.baidumapdemo.model

import androidx.lifecycle.MutableLiveData
import com.baidu.mapapi.map.offline.MKOfflineMap
import dagger.hilt.android.scopes.ActivityScoped
import java.io.Serializable
import javax.inject.Inject

@ActivityScoped
data class OfflineDataModel(
    var mkOfflineMap: MKOfflineMap?,
    var statusChangeCityIdLive: MutableLiveData<Int>
): Serializable {

    @Inject
    constructor(): this(
        mkOfflineMap = null,
        statusChangeCityIdLive = MutableLiveData(-1)
    )
}