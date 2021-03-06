package no.danielzeller.depthoffield

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_select_example.*

class SelectExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_example)
        setSupportActionBar(toolbar)

    }

    fun launchRecycler(view: View) {
        startActivity( Intent(this, RecyclerViewExampleActivity::class.java))
    }

    fun launchFloatingImages(view: View) {
        startActivity( Intent(this, FloatingImagesActivity::class.java))
    }
    fun launchDebug(view: View) {

    }
}
