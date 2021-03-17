package com.hailiao.baidumapdemo.activity.offline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.map.offline.MKOfflineMap
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.databinding.ActOfflineBinding
import com.hailiao.baidumapdemo.model.OfflineDataModel
import com.hi.dhl.binding.databind
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OfflineActivity : AppCompatActivity() {

    private val binding: ActOfflineBinding by databind(R.layout.act_offline)

    @Inject
    lateinit var dataModel: OfflineDataModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.executePendingBindings()

        dataModel.mkOfflineMap = MKOfflineMap()
        dataModel.mkOfflineMap?.init { type, state ->
            when(type) {
                MKOfflineMap.TYPE_DOWNLOAD_UPDATE,
                MKOfflineMap.TYPE_NEW_OFFLINE,
                MKOfflineMap.TYPE_VER_UPDATE -> {
                    dataModel.statusChangeCityIdLive.postValue(state)
                }
            }
        }
    }
}