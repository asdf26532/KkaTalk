package com.han.kkatalk2

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
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
        private val txtGuideTitle: TextView = itemView.findViewById(R.id.txtGuideTitle)
        private val txtReason: TextView = itemView.findViewById(R.id.txtReason)
        private val btnMenu: ImageView = itemView.findViewById(R.id.btn_report_menu)

        fun bind(report: Report) {
            txtReporter.text = "신고자: ${report.reporterUid}"
            txtAccused.text = "피신고자: ${report.accusedUid}"
            txtGuideTitle.text = "게시물 제목: ${report.guideTitle}"
            txtReason.text = "사유: ${report.reason}"

            btnMenu.setOnClickListener {
                val popupMenu = PopupMenu(itemView.context, it)
                popupMenu.menuInflater.inflate(R.menu.report_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_handled -> {
                            val context = itemView.context
                            val reportRef = FirebaseDatabase.getInstance()
                                .getReference("reports")
                                .child(report.reportId)

                            reportRef.child("isHandled").setValue(true)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "신고가 처리되었습니다.", Toast.LENGTH_SHORT).show()

                                    (context as? ReportManagementActivity)?.fetchReports()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "처리 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            true
                        }

                        R.id.action_view_detail -> {
                            val context = itemView.context

                            if (report.accusedUid.isNotEmpty()) {
                                Log.d("ReportAdapter", "accusedUid 확인됨: ${report.accusedUid}")

                                val intent = Intent(context, GuideDetailActivity::class.java).apply {
                                    putExtra("guideId", report.accusedUid)
                                }

                                Log.d("ReportAdapter", "GuideDetailActivity 인텐트 시작")
                                context.startActivity(intent)
                            } else {
                                Log.w("ReportAdapter", "guideId가 null 또는 빈 문자열임 → 게시물 없음 메시지 표시")
                                Toast.makeText(context, "신고된 게시물을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }

                        R.id.action_ban_user -> {
                            val context = itemView.context
                            val options = arrayOf("1일", "3일", "7일", "30일", "영구 정지")
                            val durations = arrayOf(1, 3, 7, 30, -1)

                            AlertDialog.Builder(context)
                                .setTitle("계정 정지 기간 선택")
                                .setItems(options) { _, which ->
                                    val suspendDays = durations[which]
                                    val bannedUntil = if (suspendDays == -1) {
                                        Long.MAX_VALUE // 영구정지
                                    } else {
                                        System.currentTimeMillis() + suspendDays * 24 * 60 * 60 * 1000
                                    }

                                    FirebaseDatabase.getInstance().getReference("user")
                                        .child(report.accusedUid)
                                        .child("banUntil")
                                        .setValue(bannedUntil)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "계정이 정지되었습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "정지 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .setNegativeButton("취소", null)
                                .show()
                            true
                        }
                        else -> false
                    }
                }

                popupMenu.show()
            }
        }
    }
}
