package com.nbcamp.tripgo.view.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nbcamp.tripgo.databinding.ItemSearchBinding

class SearchAdapter(private val sContext: Context) :
    RecyclerView.Adapter<SearchAdapter.SearchItemViewHolder>() {
    var items = arrayListOf<String>()
    fun clearItem() {
        items.clear()
        notifyDataSetChanged()
    }

    fun additem(list: List<String>) {
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        val binding = ItemSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class SearchItemViewHolder(binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        var travelerSuggest: TextView = binding.suggest
        var id: View = binding.root // 혹시나 다른 변수를 받아올 때 사용할 수 있음

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(item: String) {
            travelerSuggest.text = item
        }

        override fun onClick(v: View?) {
//            val position = adapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return
//            val clickedItem = items[position] // 클릭한 아이템의 내용 가져오기
//
//            // 아이템 정보를 담는 Bundle을 생성
//            val bundle = Bundle()
//            bundle.putString("clickedItem", clickedItem)
//
//            val nextFragment = AttractionsFragment()
//            nextFragment.arguments = bundle
//
//            val fragmentTransaction = (sContext as AppCompatActivity).supportFragmentManager.beginTransaction()
//            fragmentTransaction.replace(R.id.search_viewpager, nextFragment)
//            fragmentTransaction.addToBackStack(null)
//            fragmentTransaction.commit()
        }
    }
}

// Add a newline character at the end of the file
