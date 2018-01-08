package net.syarihu.android.library.recyclerlistview

import android.content.Context
import android.database.Observable
import android.graphics.Color
import android.support.v4.util.LongSparseArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout

/**
 * View for displaying the list in RecyclerView.
 */
class RecyclerListView : LinearLayout {
    private val dividerSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics).toInt()
    private val listItemCache = LongSparseArray<View>()
    private val dividerCache = LongSparseArray<View>()

    private var dividerColor = Color.LTGRAY
    private var listLimit = 0
    var addItemAnimation: Int = -1

    var adapter: Adapter? = null
        set(value) {
            value?.let {
                initAdapter(value)
            }
            field = value
        }

    /**
     * When an adapter change is notified, the change is reflected in View.
     */
    fun onAdapterChanged(adapter: Adapter) {
        listLimit = adapter.listLimit
        val diff = adapter.getCount() - listItemCache.size()
        if (diff < 0) {
            for (i in listItemCache.size() - 1 downTo (listItemCache.size() - Math.abs(diff))) {
                listItemCache.removeAt(i)
                dividerCache.removeAt(i)
            }
        } else if (diff > 0) {
            val startPosition = if (listItemCache.size() == 0) 0 else listItemCache.size()
            for (i in startPosition until listItemCache.size() + Math.abs(diff)) {
                listItemCache.put(adapter.getItemId(i), adapter.createView(i, this@RecyclerListView))
                dividerCache.put(i.toLong(), createDividerView())
            }
        }
        removeAllViews()
        for (i in 0 until listItemCache.limit()) {
            addView(adapter.bindView(i, listItemCache.get(adapter.getItemId(i))).apply {
                if (addItemAnimation != -1 && listItemCache.size() >= listLimit && i == listItemCache.limit() - 1) {
                    startAnimation(AnimationUtils.loadAnimation(context, addItemAnimation))
                }
            })
            if (i < listItemCache.limit() - 1) {
                addView(dividerCache.valueAt(i))
            }
        }
    }

    constructor(context: Context?) : super(context) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(attrs)
    }

    /**
     * Initialize RecyclerListView.
     *
     * @param attrs AttributeSet
     */
    private fun initialize(attrs: AttributeSet? = null) {
        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.RecyclerListView).run {
                dividerColor = getColor(R.styleable.RecyclerListView_android_divider, Color.LTGRAY)
                listLimit = getInteger(R.styleable.RecyclerListView_limit, 0)
            }
        }
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    /**
     * Initialize RecyclerListView.Adapter
     *
     * @param adapter Adapter to be initialized
     */
    private fun initAdapter(adapter: Adapter) {
        if (adapter.listLimit < 1 && listLimit > 0) {
            adapter.listLimit = listLimit
        }
        listItemCache.clear()
        dividerCache.clear()
        for (position in 0 until adapter.getCount()) {
            listItemCache.put(adapter.getItemId(position), adapter.createView(position, this))
            dividerCache.put(position.toLong(), createDividerView())
        }
        dividerCache.put(listItemCache.size().toLong(), createDividerView())
        adapter.registerObserver(object : AdapterDataObserver {
            override fun onChanged() {
                onAdapterChanged(adapter)
            }
        })
        adapter.notifyDataSetChanged()
    }

    /**
     * Generates divider view and returns it.
     *
     * @return Divider View
     */
    private fun createDividerView(): View {
        return View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dividerSize)
            setBackgroundColor(dividerColor)
        }
    }

    /**
     * If list size is larger than listLimit, listLimit is returned.
     * If listLimit is 0, the size of the list is returned.
     */
    private fun LongSparseArray<View>.limit(): Int {
        return if (listLimit == 0) {
            size()
        } else {
            if (listLimit < size()) {
                listLimit
            } else {
                size()
            }
        }
    }

    /**
     * Returns true if the size of the list is limited by listLimit.
     */
    fun isLimit(): Boolean {
        return listLimit > 0 && listItemCache.limit() != listItemCache.size()
    }

    /**
     * Adapter for implementing list items of RecyclerListView.
     */
    abstract class Adapter {
        var listLimit: Int = 0
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        private val observable = AdapterDataObservable()
        /**
         * Generate a View of a list item.
         *
         * @param position Position of list item
         * @param viewGroup ViewGroup
         */
        abstract fun createView(position: Int, viewGroup: ViewGroup?): View

        /**
         * Bind the data to the View of the list item.
         *
         * @param position Position of list item
         * @param view View to bind data to
         */
        abstract fun bindView(position: Int, view: View): View

        /**
         * Get the data of the list item.
         *
         * @param position Position of list item
         */
        abstract fun getItem(position: Int): Any

        /**
         * Get the id of the list item.
         *
         * @param position Position of list item
         */
        abstract fun getItemId(position: Int): Long

        /**
         * Gets the number of items to display in the list.
         */
        abstract fun getCount(): Int

        /**
         * Notify Observer of changes.
         */
        fun notifyDataSetChanged() {
            observable.notifyChanged()
        }

        /**
         * Register the Observer to receive change notifications.
         *
         * @param observer AdapterDataObserver
         */
        fun registerObserver(observer: AdapterDataObserver) {
            observable.registerObserver(observer)
        }
    }

    /**
     * Observable for receiving data changes.
     */
    private class AdapterDataObservable : Observable<AdapterDataObserver>() {
        fun notifyChanged() {
            for (i in mObservers.indices.reversed()) {
                mObservers[i].onChanged()
            }
        }
    }

    /**
     * Observer for receiving data changes.
     */
    interface AdapterDataObserver {
        fun onChanged()
    }
}