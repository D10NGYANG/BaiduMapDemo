package com.hailiao.baidumapdemo.activity.offline

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.databinding.FragmentOfflineMainBinding
import com.hailiao.baidumapdemo.model.OfflineDataModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 离线地图首页
 * @Author: D10NG
 * @Time: 2021/2/22 9:38 上午
 */
@AndroidEntryPoint
class OfflineMainFragment : Fragment() {

    private lateinit var binding: FragmentOfflineMainBinding

    @Inject
    lateinit var dataModel: OfflineDataModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offline_main, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 点击返回
        binding.toolbar.setNavigationOnClickListener { activity?.finish() }

        // 点击下载管理
        binding.llDownload.setOnClickListener {
            findNavController().navigate(R.id.action_offlineMainFragment_to_offlineDownloadingFragment)
        }

        // 点击热门城市
        binding.llHotCity.setOnClickListener {
            findNavController().navigate(R.id.action_offlineMainFragment_to_offlineHotCityFragment)
        }

        // 点击全部城市
        binding.llAllCity.setOnClickListener {
            findNavController().navigate(R.id.action_offlineMainFragment_to_offlineAllCityFragment)
        }
    }
}