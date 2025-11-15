package student.projects.musicreviewapp.components.filter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import student.projects.musicreviewapp.R

class Filter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var showDropdown = false
    private lateinit var dropdownLayout: LinearLayout
    private lateinit var dropdownList: ListView
    private lateinit var titleText: TextView
    private lateinit var arrowIcon: ImageView

    var onSelect: ((value: String, title: String) -> Unit)? = null

    private var filterTitle = ""
    private var filterValues = emptyList<String>()
    private var currentValues = emptyList<String>()

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_filter, this, true)
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        dropdownLayout = findViewById(R.id.dropdown_layout)
        dropdownList = findViewById(R.id.dropdown_list)
        titleText = findViewById(R.id.filter_title)
        arrowIcon = findViewById(R.id.arrow_icon)
    }

    private fun setupClickListeners() {
        // Toggle dropdown on click
        setOnClickListener {
            showDropdown = !showDropdown
            updateDropdownVisibility()
        }

        // Handle item selection
        dropdownList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedValue = filterValues[position]
            onSelect?.invoke(selectedValue, filterTitle)
            showDropdown = false
            updateDropdownVisibility()
        }
    }

    fun setFilterData(title: String, values: List<String>, currentSelections: List<String>) {
        filterTitle = title
        filterValues = values
        currentValues = currentSelections
        titleText.text = title.uppercase()
        updateDropdownList()
    }

    private fun updateDropdownList() {
        val adapter = ArrayAdapter(context, R.layout.list_item_filter, filterValues)
        dropdownList.adapter = adapter
    }

    private fun updateDropdownVisibility() {
        dropdownLayout.visibility = if (showDropdown) View.VISIBLE else View.GONE
        arrowIcon.rotation = if (showDropdown) 180f else 0f
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        showDropdown = false
        updateDropdownVisibility()
    }
}