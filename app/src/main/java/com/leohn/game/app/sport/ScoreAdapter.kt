package com.leohn.game.app.sport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.leohn.game.app.sport.databinding.ListItemBinding

class ScoreAdapter(val list: List<Int>): RecyclerView.Adapter<ScoreAdapter.Companion.MyHolder>() {


    companion object {
        class MyHolder(val binding: ListItemBinding): ViewHolder(binding.root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.binding.textView7.text = list[position].toString()
    }
}