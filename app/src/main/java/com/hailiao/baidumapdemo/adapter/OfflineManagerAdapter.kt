package com.hailiao.baidumapdemo.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.map.offline.MKOLUpdateElement
import com.hailiao.baidumapdemo.R
import com.hailiao.baidumapdemo.databinding.ItemOfflineMapBinding

class OfflineManagerAdapter constructor(
    val mList: MutableList<MKOLUpdateElement> = mutableListOf()
): RecyclerView.Adapter<OfflineManagerAdapter.ViewHolder>() {

    private var listener: RecyclerViewClick? = null

    /** 设置点击监听器 */
    fun setListener(listener: (RecyclerViewClick.() -> Unit)) {
        val viewClick = RecyclerViewClick()
        viewClick.listener()
        this.listener = viewClick
    }

    @Synchronized
    fun update(list: List<MKOLUpdateElement>) {
        this.mList.clear()
        this.mList.addAll(list)
        notifyDataSetChanged()
    }

    @Synchronized
    fun update(status: MKOLUpdateElement) {
        val index = this.mList.indexOfFirst { it.cityID == status.cityID }
        if (index < 0) {
            this.mList.add(status)
            notifyItemInserted(this.mList.size -1)
        } else {
            val old = this.mList[index]
            if (old.isSame(status)) return
            this.mList[index] = status
            //notifyItemChanged(index)
            notifyItemChanged(index, R.id.txt_tips)
            notifyItemChanged(index, R.id.progress_bar)
        }
    }

    class ViewHolder constructor(
        val binding: ItemOfflineMapBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MKOLUpdateElement) {
            binding.cityNameText = item.cityName
            binding.sizeText = item.serversize.toLong().formatDataSize()
            binding.tipsText = when(item.status) {
                MKOLUpdateElement.DOWNLOADING -> "${item.ratio}%"
                MKOLUpdateElement.eOLDSFormatError -> "数据错误，需重新下载"
                MKOLUpdateElement.eOLDSIOError -> "读写异常"
                MKOLUpdateElement.eOLDSMd5Error -> "校验失败"
                MKOLUpdateElement.eOLDSNetError -> "网络异常"
                MKOLUpdateElement.eOLDSWifiError -> "wifi网络异常"
                MKOLUpdateElement.FINISHED -> "完成"
                MKOLUpdateElement.WAITING -> "等待下载"
                MKOLUpdateElement.SUSPENDED -> "已暂停"
                else -> ""
            }
            binding.progressBar.progress = item.ratio
            binding.isSelect = false
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
        val context = holder.binding.root.context
        val item = this.mList[position]
        holder.bind(item)

        // 点击
        holder.binding.ctlItem.setOnClickListener {
            this.listener?.click(it, position, item)
        }

        // 长按
        holder.binding.ctlItem.setOnLongClickListener {
            val pop = PopupMenu(context, it, Gravity.END)
            pop.menuInflater.inflate(R.menu.menu_offline_map, pop.menu)
            pop.setOnMenuItemClickListener { menu ->
                menu?.let { m ->
                    this.listener?.click(m.itemId, position, item)
                }
                true
            }
            pop.show()
            true
        }
    }

    override fun getItemCount(): Int = this.mList.size
}

fun MKOLUpdateElement.isSame(other: MKOLUpdateElement): Boolean {
    return this.update == other.update &&
            this.ratio == other.ratio &&
            this.status == other.status &&
            this.cityID == other.cityID &&
            this.cityName == other.cityName
}

/**
 * 数据转换
 * @receiver Long
 * @return String
 */
fun Long.formatDataSize(): String {
    return if (this < 1024 * 1024) {
        String.format("%dK", this / 1024)
    } else {
        String.format("%.1fM", this / (1024 * 1024.0))
    }
}