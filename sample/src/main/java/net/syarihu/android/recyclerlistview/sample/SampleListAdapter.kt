package net.syarihu.android.recyclerlistview.sample

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.syarihu.android.library.recyclerlistview.RecyclerListView
import net.syarihu.android.recyclerlistview.sample.databinding.RecyclerListRowBinding

class SampleListAdapter(
        private val inflater: LayoutInflater,
        private val list: List<SampleData>,
        private val onItemClickListener: ((position: Int, adapter: RecyclerListView.Adapter) -> Unit)? = null
) : RecyclerListView.Adapter() {
    override fun createView(position: Int, viewGroup: ViewGroup?): View {
        return DataBindingUtil.inflate<RecyclerListRowBinding>(
                inflater,
                R.layout.recycler_list_row,
                viewGroup,
                false
        ).root
    }

    override fun bindView(position: Int, view: View): View {
        val item = getItem(position) as? SampleData ?: return view
        return DataBindingUtil.findBinding<RecyclerListRowBinding>(view).apply {
            name = item.name
            code = item.code
            root.setOnClickListener {
                onItemClickListener?.invoke(position, this@SampleListAdapter)
            }
        }.root
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}