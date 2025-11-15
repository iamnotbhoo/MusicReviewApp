package student.projects.musicreviewapp.components.loader

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import student.projects.musicreviewapp.R

class Loader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var bar1: View
    private lateinit var bar2: View
    private lateinit var bar3: View

    private val loaderAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.loader_animation)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_loader, this, true)
        setupViews()
        startAnimation()
    }

    private fun setupViews() {
        bar1 = findViewById(R.id.loader_bar_1)
        bar2 = findViewById(R.id.loader_bar_2)
        bar3 = findViewById(R.id.loader_bar_3)
    }

    private fun startAnimation() {
        bar1.startAnimation(animation.apply { startOffset = 0 })
        bar2.startAnimation(animation.apply { startOffset = 200 })
        bar3.startAnimation(animation.apply { startOffset = 400 })
    }

    fun stopAnimation() {
        bar1.clearAnimation()
        bar2.clearAnimation()
        bar3.clearAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}