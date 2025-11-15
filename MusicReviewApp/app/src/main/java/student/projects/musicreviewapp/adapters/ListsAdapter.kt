package student.projects.musicreviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.UserList

class ListsAdapter(private var lists: List<UserList>) :
    RecyclerView.Adapter<ListsAdapter.ListViewHolder>() {

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listTitle: TextView = itemView.findViewById(R.id.list_title)
        val listCreator: TextView = itemView.findViewById(R.id.list_creator)
        val listDescription: TextView = itemView.findViewById(R.id.list_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val list = lists[position]
        holder.listTitle.text = list.name
        holder.listCreator.text = list.creator
        holder.listDescription.text = list.description
    }

    override fun getItemCount(): Int = lists.size

    fun updateData(newLists: List<UserList>) {
        lists = newLists
        notifyDataSetChanged()
    }
}