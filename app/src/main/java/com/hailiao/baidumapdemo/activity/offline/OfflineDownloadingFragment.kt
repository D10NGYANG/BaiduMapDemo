package com.hailiao.baidumapdemo.activity.offline

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.mapapi.map.offline.MKOLUpdateElement
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.adapter.OfflineManagerAdapter
import com.hailiao.baidumapdemo.databinding.FragmentOfflineDownloadingBinding
import com.hailiao.baidumapdemo.model.OfflineDataModel
import com.hailiao.baidumapdemo.utils.showSnackBar
import com.hi.dhl.binding.databind
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * 下载管理
 * @Author: D10NG
 * @Time: 2021/2/22 10:08 上午
 */
@AndroidEntryPoint
class OfflineDownloadingFragment : Fragment(R.layout.fragment_offline_downloading) {

    private val binding: FragmentOfflineDownloadingBinding by databind()
    private lateinit var adapter: OfflineManagerAdapter

    @Inject
    lateinit var dataModel: OfflineDataModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 初始化列表
        adapter = OfflineManagerAdapter()
        binding.rcv.layoutManager = LinearLayoutManager(activity)
        binding.rcv.adapter = adapter

        // 监听改变
        dataModel.statusChangeCityIdLive.observe(this.viewLifecycleOwner, {
            //val downInfo = dataModel.mkOfflineMap?.getUpdateInfo(it)?: return@observe
            //adapter.update(downInfo)
        })

        // 点击返回
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        // 点击列表
        adapter.setListener {
            onClick { _, _, data ->
                val item = data as MKOLUpdateElement
                when(item.status) {
                    MKOLUpdateElement.UNDEFINED,
                    MKOLUpdateElement.SUSPENDED -> dataModel.mkOfflineMap?.start(item.cityID)
                    MKOLUpdateElement.FINISHED -> binding.root.showSnackBar("已下载")
                    MKOLUpdateElement.DOWNLOADING,
                    MKOLUpdateElement.WAITING -> dataModel.mkOfflineMap?.pause(item.cityID)
                    MKOLUpdateElement.eOLDSFormatError,
                    MKOLUpdateElement.eOLDSIOError,
                    MKOLUpdateElement.eOLDSInstalling,
                    MKOLUpdateElement.eOLDSMd5Error,
                    MKOLUpdateElement.eOLDSNetError,
                    MKOLUpdateElement.eOLDSWifiError -> dataModel.mkOfflineMap?.start(item.cityID)
                }
            }

            onClickId { id, _, data ->
                val item = data as MKOLUpdateElement
                when(id) {
                    R.id.update -> {
                        if (item.update) {
                            dataModel.mkOfflineMap?.update(item.cityID)
                        } else {
                            binding.root.showSnackBar("暂无更新")
                        }
                    }
                    R.id.delete -> {
                        dataModel.mkOfflineMap?.remove(item.cityID)
                    }
                }
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
                val list = dataModel.mkOfflineMap?.allUpdateInfo?: listOf()
                if (list.size != adapter.itemCount) {
                    withContext(Dispatchers.Main) {
                        adapter.update(list)
                    }
                } else {
                    for (info in list) {
                        withContext(Dispatchers.Main) {
                            adapter.update(info)
                        }
                    }
                }
                delay(500)
            }
        }
    }
}