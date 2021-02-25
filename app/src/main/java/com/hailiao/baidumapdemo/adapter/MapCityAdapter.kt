package com.hailiao.baidumapdemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.map.offline.MKOLUpdateElement
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.bean.MapCityInfo
import com.hailiao.baidumapdemo.databinding.ItemOfflineMapBinding

class MapCityAdapter constructor(
    val mList: MutableList<MapCityInfo> = mutableListOf(),
    val selectIdLive: MutableLiveData<Int> = MutableLiveData(-1)
): RecyclerView.Adapter<MapCityAdapter.ViewHolder>() {

    private var listener: RecyclerViewClick? = null

    /** 设置点击监听器 */
    fun setListener(listener: (RecyclerViewClick.() -> Unit)) {
        val viewClick = RecyclerViewClick()
        viewClick.listener()
        this.listener = viewClick
    }

    @Synchronized
    fun update(list: List<MapCityInfo>) {
        this.mList.clear()
        this.mList.addAll(list)
        notifyDataSetChanged()
    }

    @Synchronized
    fun update(cityId: Int, status: MKOLUpdateElement) {
        val index = this.mList.indexOfFirst { it.record.cityID == cityId }
        val info = this.mList[index]
        info.element = status
        this.mList[index] = info
        //notifyItemChanged(index)
        notifyItemChanged(index, R.id.txt_tips)
    }

    class ViewHolder constructor(
        val binding: ItemOfflineMapBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MapCityInfo, selectIdLive: MutableLiveData<Int>) {
            binding.cityNameText = item.record.cityName
            binding.sizeText = item.record.dataSize.formatDataSize()
            binding.progressBar.visibility = View.GONE
            if (item.element == null) {
                binding.tipsText = ""
            } else {
                binding.tipsText = when(item.element?.status) {
                    MKOLUpdateElement.DOWNLOADING -> "正在下载"
                    MKOLUpdateElement.FINISHED -> "已下载"
                    MKOLUpdateElement.WAITING -> "等待下载"
                    else -> ""
                }
            }
            selectIdLive.observeForever {
                binding.isSelect = it == item.record.cityID
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemOfflineMapBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_offline_map,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = this.mList[position]
        holder.bind(item, selectIdLive)

        // 点击
        holder.binding.ctlItem.setOnClickListener {
            if (item.record.childCities.isNullOrEmpty()) {
                selectIdLive.postValue(-1)
            } else {
                selectIdLive.postValue(item.record.cityID)
            }
            this.listener?.click(it, position, item)
        }
    }

    override fun getItemCount(): Int = this.mList.size
}