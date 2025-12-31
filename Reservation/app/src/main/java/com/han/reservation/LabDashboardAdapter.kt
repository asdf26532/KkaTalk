package com.han.reservation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.han.reservation.databinding.ItemLabDashboardBinding

class LabDashboardAdapter(
    private val experiments: List<LabExperiment>
) : RecyclerView.Adapter<LabDashboardAdapter.VH>() {

    inner class VH(val binding: ItemLabDashboardBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLabDashboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val experiment = experiments[position]
        val context = holder.itemView.context

        val count = LabExperimentHistory.getCount(context, experiment.key)
        val group = ExperimentGroupManager.getGroup(context, experiment.key)

        holder.binding.tvTitle.text = experiment.title
        holder.binding.tvCount.text = "실행 횟수: ${count}"
        holder.binding.tvPhase.text = "상태: ${experiment.phase}"
        holder.binding.tvGroup.text = "그룹: ${group.name}"

        holder.binding.btnGraduate.setOnClickListener {
            experiment.phase = ExperimentPhase.GRADUATED
            notifyItemChanged(position)
        }

        holder.binding.btnRemove.setOnClickListener {
            experiment.phase = ExperimentPhase.REMOVED
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = experiments.size
}