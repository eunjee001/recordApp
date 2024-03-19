package com.kkyoungs.recordapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kkyoungs.recordapp.databinding.ItemRecordingBinding
import java.io.File

class RecordingAdapter: ListAdapter<RecordUri, RecordingAdapter.RecordingViewHolder>(diffUtil) {
    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<RecordUri>(){
            override fun areItemsTheSame(oldItem: RecordUri, newItem: RecordUri): Boolean {
                return oldItem.uri === newItem.uri
            }

            override fun areContentsTheSame(oldItem: RecordUri, newItem: RecordUri): Boolean {
                return oldItem.uri == newItem.uri
            }

        }
    }
    private var listener : OnRecordingClickListener ?=null

    fun setOnItemClickListener(listener: OnRecordingClickListener){
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val binding = ItemRecordingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

    }
    inner class RecordingViewHolder(private var binding : ItemRecordingBinding) : ViewHolder(binding.root){
        fun bind(item : RecordUri){
            val file = File(item.uri)
            binding.audioTitleItemAudio.text = file.name

            println(">>>file2 " +file.name)

            binding.playBtnItemAudio.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION){
                    if (listener!=null){
                        listener?.onItemClick(it, pos)
                    }
                }
            }
        }
    }
    interface OnRecordingClickListener{
        fun onItemClick(view: View, position: Int)
    }
}

