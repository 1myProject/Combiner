package by.morinosenshi.combiner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.morinosenshi.combiner.databinding.FragmentItemBinding
import by.morinosenshi.combiner.MyData.VideoRow


class MyItemRecyclerViewAdapter(private val values: List<VideoRow>) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        item.getImg(holder.img)
        holder.path.text = "$position"
    }

    override fun getItemCount(): Int = values.size

    fun addNew() = notifyItemInserted(itemCount)

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        // TODO: добавить кнопки перемещения и удаления строк
        val img: ImageView = binding.img
        val path: TextView = binding.path
    }

}