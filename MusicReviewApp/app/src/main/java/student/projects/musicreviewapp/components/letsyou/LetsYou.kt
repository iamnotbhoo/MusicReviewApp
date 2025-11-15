package student.projects.musicreviewapp.components.letsyou

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import student.projects.musicreviewapp.R

class LetsYou @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_lets_you, this, true)
        setupCards()
    }

    private fun setupCards() {
        val cardsContainer = findViewById<LinearLayout>(R.id.lets_you_cards_container)

        LetsYouData.cards.forEach { cardData ->
            val card = LetsYouCard(context).apply {
                setCardData(cardData.iconRes, cardData.text, cardData.bgColor)
            }
            cardsContainer.addView(card)
        }
    }
}