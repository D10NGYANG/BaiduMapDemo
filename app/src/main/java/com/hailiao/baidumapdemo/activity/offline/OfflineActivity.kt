package com.hailiao.baidumapdemo.activity.offline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.baidu.mapapi.map.offline.MKOfflineMap
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.databinding.ActOfflineBinding
import com.hailiao.baidumapdemo.model.OfflineDataModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OfflineActivity : AppCompatActivity() {

    private lateinit var binding: ActOfflineBinding

    @Inject
    lateinit var dataModel: OfflineDataModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.act_offline)

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

    override fun onDestroy() {
        // 不能destroy，否则会导致定位图标消失
        //dataModel.mkOfflineMap?.destroy()
        super.onDestroy()
    }
}