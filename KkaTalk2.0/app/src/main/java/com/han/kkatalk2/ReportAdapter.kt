package com.han.kkatalk2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class ReportAdapter(private val reports: List<Report>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    override fun getItemCount(): Int = reports.size

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtReporter: TextView = itemView.findViewById(R.id.txtReporter)
        private val txtAccused: TextView = itemView.findViewById(R.id.txtAccused)
        private val txtReason: TextView = itemView.findViewById(R.id.txtReason)
        private val btnMenu: ImageView = itemView.findViewById(R.id.btn_report_menu)

        fun bind(report: Report) {
            txtReporter.text = "신고자: ${report.reporterUid}"
            txtAccused.text = "피신고자: ${report.accusedUid}"
            txtReason.text = "사유: ${report.reason}"

            btnMenu.setOnClickListener {
                val popupMenu = PopupMenu(itemView.context, it)
                popupMenu.menuInflater.inflate(R.menu.report_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_handled -> {
                            val reportRef = FirebaseDatabase.getInstance()
                                .getReference("reports")
                                .child(report.reportId)

                            reportRef.child("isHandled").setValue(true)
                                .addOnSuccessListener {
                                    Toast.makeText(itemView.context, "신고가 처리되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(itemView.context, "처리 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            true
                        }

                        // TODO: 이후에 action_view_detail, action_ban_user 등도 여기에 추가
                        else -> false
                    }
                }

                popupMenu.show()
            }
        }
    }
}
