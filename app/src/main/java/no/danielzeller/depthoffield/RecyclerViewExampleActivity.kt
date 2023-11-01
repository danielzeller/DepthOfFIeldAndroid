package no.danielzeller.depthoffield

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import no.danielzeller.depthoffield.animation.LoaderImageView
import java.lang.ref.WeakReference
import java.util.Locale


private const val UNSPLASH_RANDOM_URL = "https://source.unsplash.com/1080x1080?"

class RecyclerViewExampleActivity : AppCompatActivity() {

    private lateinit var picasso: Picasso
    private val picassoCache = LruCache(100000000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        picasso = Picasso.Builder(applicationContext).memoryCache(picassoCache).build()
        setContentView(R.layout.activity_recycler_view_example)
        setSupportActionBar(findViewById(R.id.toolbar))

        setupRecyclerView()
        makeAHorribleMessThatOnlyICanRead()
    }

    private fun setupRecyclerView() {
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = SimpleCardAdapter(createUrls(),resources.getStringArray(R.array.image_search_term), picasso, picassoCache)

        findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val adapter = parent.adapter!!

                    if (position == 0) {
                        outRect.top = (40f * resources.displayMetrics.density).toInt()
                    }

                    if (position == adapter.itemCount - 1) {
                        outRect.bottom = (140f * resources.displayMetrics.density).toInt()
                    }
                }
            })
        }
    }

    private fun createUrls(): Array<String> {
        val searchTerm = resources.getStringArray(R.array.image_search_term)
        return Array(searchTerm.size) { i -> UNSPLASH_RANDOM_URL + searchTerm[i] }
    }

    class SimpleCardAdapter(
        private val imageUrls: Array<String>,
        private val text: Array<String>,
        val picasso: Picasso,
        val picassoCache: LruCache
    ) :
        RecyclerView.Adapter<SimpleCardAdapter.SimpleImageVIewHolder>() {

        class SimpleImageVIewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView = itemView.findViewById<LoaderImageView>(R.id.imageView)
            val textView = itemView.findViewById<TextView>(R.id.textView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleImageVIewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.image_card_fullscreen, parent, false)
            view.clipToOutline = true
            return SimpleImageVIewHolder(view)
        }

        @SuppressLint("NewApi")
        override fun onBindViewHolder(holder: SimpleImageVIewHolder, position: Int) {

            setupImageView(position, holder)
            holder.itemView.translationY = 0f
            holder.itemView.translationZ = 0f
            holder.itemView.alpha = 1f
            holder.textView.text = text[position].uppercase()
        }

        private fun setupImageView(position: Int, holder: SimpleImageVIewHolder) {
            val bitmap = picassoCache.get(imageUrls[position] + "\n")
            holder.imageView.cancelIntroAnim()

            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap)
                holder.imageView.isLoaderVisible = false
            } else {
                holder.imageView.isLoaderVisible = true
                picasso.load(imageUrls[position]).config(Bitmap.Config.ARGB_8888)
                    .into(holder.imageView, createOnImageLoadFinishedCallback(WeakReference(holder.imageView)))
            }
        }

        override fun getItemCount() = imageUrls.size

        private fun createOnImageLoadFinishedCallback(viewHolderRef: WeakReference<LoaderImageView>): Callback {
            return object : Callback {
                override fun onSuccess() {
                    if (viewHolderRef.get() != null) {
                        val viewHolder = viewHolderRef.get()!!
                        viewHolder.findViewById<LoaderImageView>(R.id.imageView).introAnimate()
                    }
                }
                override fun onError(e: Exception?) {}
            }
        }
    }

    private fun makeAHorribleMessThatOnlyICanRead() {
        val interpolator = PathInterpolatorCompat.create(.42f, 0f, .71f, .45f)
        val interpolator2 = PathInterpolatorCompat.create(0f, .95f, .39f, 1f)
        findViewById<RecyclerView>(R.id.recycler_view).addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                for (i in 0..recyclerView.childCount) {
                    val view = recyclerView.getChildAt(i)

                    view?.apply {
                        val center = (recyclerView.parent as View).height * 0.6f
                        val centerBottom = (recyclerView.parent as View).height * 0.4f
                        if (view.bottom < center) {
                            val percentTilTop = view.bottom.toFloat() / center
                            val offsetAmount = center * 0.8f * interpolator.getInterpolation(1f - percentTilTop)
                            view.translationY = offsetAmount


                            val scale = Math.max(0.75f, 0.75f + 0.25f * interpolator.getInterpolation(percentTilTop))
                            view.scaleX = scale
                            view.scaleY = scale
                            view.pivotY = view.height.toFloat()
                            view.pivotX = view.width.toFloat() / 2f
                            view.translationZ = -(1 - percentTilTop)
                            view.alpha = interpolator2.getInterpolation(percentTilTop)
                        } else if (view.top > centerBottom) {
                            val percentTilBottom =
                                1f - ((recyclerView.parent as View).height - view.top.toFloat()) / ((recyclerView.parent as View).height - centerBottom)
                            val scale = 1f + interpolator.getInterpolation(percentTilBottom) * 0.5f
                            view.scaleX = scale
                            view.scaleY = scale
                            view.pivotY = 0f
                            view.pivotX = view.width.toFloat() / 2f
                            view.translationZ = Math.min(1f, interpolator.getInterpolation(percentTilBottom))
                            view.translationY = interpolator.getInterpolation(percentTilBottom) * (view.height * 0.1f)

                        } else {
                            view.translationY = 0f
                            view.scaleX = 1f
                            view.scaleY = 1f
                            view.translationZ = 0f
                            view.alpha = 1f
                        }
                    }
                }
            }
        })
    }
}
