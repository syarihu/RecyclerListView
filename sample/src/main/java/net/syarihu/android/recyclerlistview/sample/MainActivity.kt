package net.syarihu.android.recyclerlistview.sample

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import net.syarihu.android.recyclerlistview.sample.databinding.ActivityMainBinding
import net.syarihu.android.recyclerlistview.sample.databinding.ViewholderSampleBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val list = (1..10).map {
            SampleData("Hoge$it", it * 1000)
        }.toList()

        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = Adapter(this@MainActivity, list)
        }
    }

    class Adapter(
            private val context: Context,
            private val list: List<SampleData>
    ) : RecyclerView.Adapter<BindingHolder<ViewholderSampleBinding>>() {
        override fun onCreateViewHolder(
                parent: ViewGroup?,
                viewType: Int
        ): BindingHolder<ViewholderSampleBinding> {
            return BindingHolder(context, parent, R.layout.viewholder_sample)
        }

        override fun onBindViewHolder(
                holder: BindingHolder<ViewholderSampleBinding>,
                position: Int
        ) {
            val adapter = SampleListAdapter(
                    LayoutInflater.from(context),
                    list,
                    { listPosition, adapter ->
                        Toast.makeText(context, adapter.getItem(listPosition).toString(), Toast.LENGTH_SHORT).show()
                    }
            )
            holder.binding.run {
                recyclerListView.adapter = adapter
                loadMore.setOnClickListener {
                    recyclerListView.adapter?.run {
                        listLimit = 0
                        notifyDataSetChanged()
                        this@Adapter.notifyDataSetChanged()
                    }
                }

                val visibility = if (recyclerListView.isLimit()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                dividerEnd.visibility = visibility
                loadMore.visibility = visibility
            }
        }

        override fun getItemCount(): Int {
            return 1
        }

    }
}
