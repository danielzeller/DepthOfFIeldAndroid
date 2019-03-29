package no.danielzeller.depthoffield

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recycler_view_example.*
import kotlinx.android.synthetic.main.content_recycler_view_example.*
import kotlinx.android.synthetic.main.image_card.view.*

private const val UNSPLASH_RANDOM_URL = "https://source.unsplash.com/960x540?"

class RecyclerViewExampleActivity : AppCompatActivity() {

    private lateinit var picasso: Picasso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        picasso = Picasso.Builder(applicationContext).memoryCache(LruCache(100000000)).build()
        setContentView(R.layout.activity_recycler_view_example)
        setSupportActionBar(toolbar)

        setupRecyclerView()
        makeAHorribleMessThatOnlyICanRead()
    }

    private fun setupRecyclerView() {
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = MyAdapter(createUrls(), picasso)

        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun createUrls(): Array<String> {
        val searchTerm = resources.getStringArray(R.array.image_search_term)
        return Array(searchTerm.size) { i -> UNSPLASH_RANDOM_URL + searchTerm[i] }
    }


    class MyAdapter(private val imageUrls: Array<String>, val picasso: Picasso) :
        RecyclerView.Adapter<MyAdapter.SimpleImageVIewHolder>() {

        class SimpleImageVIewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView = itemView.imageView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.SimpleImageVIewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.image_card, parent, false)
            view.clipToOutline = true
            return SimpleImageVIewHolder(view)
        }

        @SuppressLint("NewApi")
        override fun onBindViewHolder(holder: SimpleImageVIewHolder, position: Int) {
            picasso.load(imageUrls[position]).config(Bitmap.Config.ARGB_8888).into(holder.imageView)
        }

        override fun getItemCount() = imageUrls.size
    }

    private fun makeAHorribleMessThatOnlyICanRead() {
        val interpolator = PathInterpolatorCompat.create(.42f, 0f, .71f, .45f)
        val interpolator2 = PathInterpolatorCompat.create(0f,.99f,.32f,.99f)
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                for (i in 0..recyclerView.childCount) {
                    val view = recyclerView.getChildAt(i)

                    view?.apply {
                        val center = recyclerView.height * 0.6f
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
                        } else if (view.bottom > center) {
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
