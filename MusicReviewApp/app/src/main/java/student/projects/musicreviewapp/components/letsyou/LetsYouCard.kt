package student.projects.musicreviewapp.components.letsyou

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import student.projects.musicreviewapp.R

class LetsYouCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var iconImage: ImageView
    private lateinit var descriptionText: TextView

    private var defaultBackground = "#456"
    private var hoverBackground = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_lets_you_card, this, true)
        setupViews()
    }

    private fun setupViews() {
        iconImage = findViewById(R.id.lets_you_icon)
        descriptionText = findViewById(R.id.lets_you_description)

        setBackgroundColor(Color.parseColor(defaultBackground))
    }

    fun setCardData(iconRes: Int, description: String, bgColor: String) {
        iconImage.setImageResource(iconRes)
        descriptionText.text = description
        hoverBackground = bgColor
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener(l)
        // You can add hover effects here if needed
        // For now, we'll just set the background color
    }
}