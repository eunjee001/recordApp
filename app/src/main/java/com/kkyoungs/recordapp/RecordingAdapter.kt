package com.kkyoungs.recordapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kkyoungs.recordapp.databinding.ItemRecordingBinding
import java.io.File

class RecordingAdapter: ListAdapter<UriViewModel, RecordingAdapter.RecordingViewHolder>(diffUtil) {
    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<UriViewModel>(){
            override fun areItemsTheSame(oldItem: UriViewModel, newItem: UriViewModel): Boolean {
                return oldItem.uri === newItem.uri
            }

            override fun areContentsTheSame(oldItem: UriViewModel, newItem: UriViewModel): Boolean {
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
        fun bind(item : UriViewModel){
            val file = File(item.uri.toString())
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

