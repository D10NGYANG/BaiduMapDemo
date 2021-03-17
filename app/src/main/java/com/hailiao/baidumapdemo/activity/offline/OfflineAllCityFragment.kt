package com.hailiao.baidumapdemo.activity.offline

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.mapapi.map.offline.MKOLUpdateElement
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.adapter.MapCityAdapter
import com.hailiao.baidumapdemo.bean.MapCityInfo
import com.hailiao.baidumapdemo.databinding.FragmentOfflineAllCityBinding
import com.hailiao.baidumapdemo.model.OfflineDataModel
import com.hailiao.baidumapdemo.utils.showSnackBar
import com.hi.dhl.binding.databind
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * 全部城市
 * @Author: D10NG
 * @Time: 2021/2/22 2:29 下午
 */
@AndroidEntryPoint
class OfflineAllCityFragment : Fragment(R.layout.fragment_offline_all_city) {

    private val binding: FragmentOfflineAllCityBinding by databind()
    private lateinit var adapter1: MapCityAdapter
    private lateinit var adapter2: MapCityAdapter

    @Inject
    lateinit var dataModel: OfflineDataModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 初始化列表
        adapter1 = MapCityAdapter()
        binding.rcv1.layoutManager = LinearLayoutManager(activity)
        binding.rcv1.adapter = adapter1

        adapter2 = MapCityAdapter()
        binding.rcv2.layoutManager = LinearLayoutManager(activity)
        binding.rcv2.adapter = adapter2

        dataModel.mkOfflineMap?.offlineCityList?.apply {
            val liveList = mutableListOf<MapCityInfo>()
            for (info in this) {
                liveList.add(MapCityInfo(info))
            }
            adapter1.update(liveList)
        }

        adapter1.selectIdLive.observe(this.viewLifecycleOwner, {
            binding.isRcv2Show = it >= 0
        })

        // 点击返回
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        // 点击关闭
        binding.llClose.setOnClickListener {
            binding.isRcv2Show = false
        }

        // 点击
        adapter1.setListener {
            onClick { _, _, data ->
                val item = data as MapCityInfo
                if (item.record.childCities.isNullOrEmpty()) {
                    adapter2.update(listOf())
                    binding.root.clickCity(dataModel, item.record.cityID)
                } else {
                    val liveList = mutableListOf<MapCityInfo>()
                    for (info in item.record.childCities) {
                        liveList.add(MapCityInfo(info))
                    }
                    adapter2.update(liveList)
                }
            }
        }

        adapter2.setListener {
            onClick { _, _, data ->
                val item = data as MapCityInfo
                binding.root.clickCity(dataModel, item.record.cityID)
            }
        }

        startCheckList()
    }

    override fun onDestroy() {
        checkListJob.cancel()
        super.onDestroy()
    }

    private var checkListJob: Job = Job()
    private fun startCheckList() {
        checkListJob = GlobalScope.launch {
            while (isActive) {
                for (info in adapter1.mList) {
                    val downInfo = dataModel.mkOfflineMap?.getUpdateInfo(info.record.cityID)?: continue
                    if (downInfo.status == info.element?.status) continue
                    withContext(Dispatchers.Main) {
                        adapter1.update(info.record.cityID, downInfo)
                    }
                }
                for (info in adapter2.mList) {
                    val downInfo = dataModel.mkOfflineMap?.getUpdateInfo(info.record.cityID)?: continue
                    if (downInfo.status == info.element?.status) continue
                    withContext(Dispatchers.Main) {
                        adapter2.update(info.record.cityID, downInfo)
                    }
                }
                delay(500)
            }
        }
    }
}

/**
 * 点击城市进行下载
 * @receiver View
 * @param dataModel OfflineDataModel
 * @param cityID Int
 */
fun View.clickCity(dataModel: OfflineDataModel, cityID: Int) {
    val downInfo = dataModel.mkOfflineMap?.getUpdateInfo(cityID)
    if (downInfo == null) {
        dataModel.mkOfflineMap?.start(cityID)
    } else {
        when(downInfo.status) {
            MKOLUpdateElement.UNDEFINED,
            MKOLUpdateElement.SUSPENDED -> dataModel.mkOfflineMap?.start(cityID)
            MKOLUpdateElement.FINISHED -> showSnackBar("已下载")
            MKOLUpdateElement.DOWNLOADING,
            MKOLUpdateElement.WAITING -> showSnackBar("正在下载")
            MKOLUpdateElement.eOLDSFormatError,
            MKOLUpdateElement.eOLDSIOError,
            MKOLUpdateElement.eOLDSInstalling,
            MKOLUpdateElement.eOLDSMd5Error,
            MKOLUpdateElement.eOLDSNetError,
            MKOLUpdateElement.eOLDSWifiError -> dataModel.mkOfflineMap?.start(cityID)
        }
    }
}