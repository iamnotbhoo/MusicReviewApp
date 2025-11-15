package student.projects.musicreviewapp.components.searching

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import student.projects.musicreviewapp.R

class SearchInputMobile @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var searchInput: EditText
    private lateinit var searchIcon: ImageView

    var onSearch: ((String) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_search_input_mobile, this, true)
        setupViews()
    }

    private fun setupViews() {
        searchInput = findViewById(R.id.search_input_mobile)
        searchIcon = findViewById(R.id.search_icon_mobile)

        searchIcon.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                onSearch?.invoke(query)
                searchInput.text.clear()
            }
        }

        // Also trigger search on keyboard enter/done
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    onSearch?.invoke(query)
                    searchInput.text.clear()
                }
                true
            } else {
                false
            }
        }
    }

    fun clearQuery() {
        searchInput.text.clear()
    }

    fun setQuery(query: String) {
        searchInput.setText(query)
    }
}