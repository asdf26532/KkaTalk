package com.han.reservation

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

        val enabled = prefs.getBoolean(experiment.key, false)
        holder.binding.switchExperiment.isChecked = enabled

        holder.binding.switchExperiment.setOnCheckedChangeListener { _, checked ->
            prefs.edit()
                .putBoolean(experiment.key, checked)
                .putBoolean("${experiment.key}_used", false)
                .apply()
        }
    }

    override fun getItemCount() = experiments.size
}