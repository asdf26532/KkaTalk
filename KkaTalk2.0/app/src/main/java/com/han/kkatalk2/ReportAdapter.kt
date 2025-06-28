package com.han.kkatalk2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(private val reports: List<Report>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtReporter = itemView.findViewById<TextView>(R.id.txtReporter)
        val txtAccused = itemView.findViewById<TextView>(R.id.txtAccused)
        val txtReason = itemView.findViewById<TextView>(R.id.txtReason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.txtReporter.text = "신고자: ${report.reporterUid}"
        holder.txtAccused.text = "피신고자: ${report.accusedUid}"
        holder.txtReason.text = "사유: ${report.reason}"
    }

    override fun getItemCount(): Int = reports.size
}
