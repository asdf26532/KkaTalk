package com.han.reservation

import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.han.reservation.databinding.ItemLabExperimentBinding

class LabAdapter(
    private val experiments: List<LabExperiment>,
    private val prefs: SharedPreferences
) : RecyclerView.Adapter<LabAdapter.LabViewHolder>() {

    inner class LabViewHolder(val binding: ItemLabExperimentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabViewHolder {
        val binding = ItemLabExperimentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LabViewHolder, position: Int) {
        val experiment = experiments[position]

        holder.binding.tvTitle.text = experiment.title
        holder.binding.tvDesc.text = experiment.description

        // 배지 처리
        when (experiment.badge) {
            ExperimentBadge.NEW -> {
                holder.binding.tvBadge.text = "NEW"
                holder.binding.tvBadge.setBackgroundColor(0xFF4CAF50.toInt())
                holder.binding.tvBadge.visibility = View.VISIBLE
            }
            ExperimentBadge.BETA -> {
                holder.binding.tvBadge.text = "BETA"
                holder.binding.tvBadge.setBackgroundColor(0xFFFF9800.toInt())
                holder.binding.tvBadge.visibility = View.VISIBLE
                holder.binding.tvWarning.visibility = View.VISIBLE
            }
            else -> {
                holder.binding.tvBadge.visibility = View.GONE
                holder.binding.tvWarning.visibility = View.GONE
            }
        }

        val enabled = prefs.getBoolean(experiment.key, false)
        holder.binding.switchExperiment.isChecked = enabled

        holder.binding.switchExperiment.setOnCheckedChangeListener { _, checked ->
            prefs.edit()
                .putBoolean(experiment.key, checked)
                .putBoolean("${experiment.key}_used", false)
                .apply()
        }

        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, LabDetailActivity::class.java)
            intent.putExtra("experiment_key", experiment.key)
            context.startActivity(intent)
        }

    }

    override fun getItemCount() = experiments.size


}