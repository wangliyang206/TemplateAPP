package com.template.app.update

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.template.app.R
import com.template.app.databinding.DialogAppUpdateBinding
import com.google.android.material.R as MaterialR

class UpdateDialog(
    context: Context,
    private val updateInfo: com.template.app.data.model.UpdateInfo,
    private val onUpdate: () -> Unit,
    private val onCancel: () -> Unit,
) : Dialog(context, MaterialR.style.Theme_Material3_DayNight_Dialog) {

    private lateinit var binding: DialogAppUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAppUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(!updateInfo.isForce)
        setCanceledOnTouchOutside(!updateInfo.isForce)

        binding.tvVersion.text = context.getString(R.string.update_new_version, updateInfo.versionName)
        binding.tvContent.text = updateInfo.updateLog
        binding.btnUpdate.setOnClickListener {
            onUpdate()
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            onCancel()
            dismiss()
        }
        binding.btnCancel.visibility = if (updateInfo.isForce) View.GONE else View.VISIBLE
    }

    fun setProgress(progress: Int) {
        binding.progressBar.progress = progress
        binding.tvProgress.text = context.getString(R.string.update_progress, progress)
    }
}
