package com.hailiao.baidumapdemo.activity.offline

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.adapter.MapCityAdapter
import com.hailiao.baidumapdemo.bean.MapCityInfo
import com.hailiao.baidumapdemo.databinding.FragmentOfflineHotCityBinding
import com.hailiao.baidumapdemo.model.OfflineDataModel
import com.hi.dhl.binding.databind
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * 热门城市
 * @Author: D10NG
 * @Time: 2021/2/22 11:30 上午
 */
@AndroidEntryPoint
class OfflineHotCityFragment : Fragment(R.layout.fragment_offline_hot_city) {

    private val binding: FragmentOfflineHotCityBinding by databind()
    private lateinit var adapter: MapCityAdapter

    @Inject
    lateinit var dataModel: OfflineDataModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 初始化列表
        adapter = MapCityAdapter()
        binding.rcv.layoutManager = LinearLayoutManager(activity)
        binding.rcv.adapter = adapter

        dataModel.mkOfflineMap?.hotCityList?.apply {
            val liveList = mutableListOf<MapCityInfo>()
            for (info in this) {
                liveList.add(MapCityInfo(info))
            }
            adapter.update(liveList)
        }

        // 点击返回
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        // 点击城市
        adapter.setListener {
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
                for (info in adapter.mList) {
                    val downInfo = dataModel.mkOfflineMap?.getUpdateInfo(info.record.cityID)?: continue
                    if (downInfo.status == info.element?.status) continue
                    withContext(Dispatchers.Main) {
                        adapter.update(info.record.cityID, downInfo)
                    }
                }
                delay(500)
            }
        }
    }
}