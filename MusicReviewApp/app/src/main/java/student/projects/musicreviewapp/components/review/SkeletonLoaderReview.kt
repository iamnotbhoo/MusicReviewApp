package student.projects.musicreviewapp.components.review

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import student.projects.musicreviewapp.R

class SkeletonLoaderReview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var shimmerAnimation: Animation? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_skeleton_loader_review, this, true)
    }

    fun startLoading() {
        // Start shimmer animation if you implement it
        shimmerAnimation = AnimationUtils.loadAnimation(context, R.anim.shimmer_animation)
        // You would apply this to specific views for shimmer effect
    }

    fun stopLoading() {
        shimmerAnimation?.cancel()
        shimmerAnimation = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLoading()
    }
}