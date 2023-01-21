package com.alex.ubercloneapp.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.activities.HistoryDetailActivity
import com.alex.ubercloneapp.models.History
import com.alex.ubercloneapp.utils.RelativeTime

class HistoriesAdapter (val context: Activity, val histories:ArrayList<History>): RecyclerView.Adapter<HistoriesAdapter.HistoriesAdapterViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_history, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {
        val history = histories[position]
        holder.tvOrigin.text = history.origin
        holder.tvDestination.text = history.destination
        if (history.timestamp != null){
            holder.tvDate.text = RelativeTime.getTimeAgo(history.timestamp!!, context)
        }

        //Se obtiene el id pero no de firebase si no de la lista que fue convertida a objeto y en esa
        //función se le agrego la obtención de cada id que tiene los documentos
        holder.itemView.setOnClickListener { goToDetail(history?.id!!) }
    }

    private fun goToDetail(idHistory: String){
        val i = Intent(context, HistoryDetailActivity::class.java)
        i.putExtra("id", idHistory)
        context.startActivity(i)
    }

    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvOrigin: TextView
        val tvDestination: TextView
        val tvDate: TextView

        init {
            tvOrigin = view.findViewById(R.id.tvOrigin)
            tvDestination = view.findViewById(R.id.tvDestination)
            tvDate = view.findViewById(R.id.tvDate)
        }
    }
}