package com.template.app.update

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.template.app.databinding.DialogAppUpdateBinding

class UpdateDialog(
    context: Context,
    private val updateInfo: com.template.app.data.model.UpdateInfo,
    private val onUpdate: () -> Unit,
    private val onCancel: () -> Unit
) : Dialog(context, com.template.core.R.style.CoreTheme) {

    private lateinit var binding: DialogAppUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAppUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(!updateInfo.isForce)
        setCanceledOnTouchOutside(!updateInfo.isForce)

        binding.tvVersion.text = "发现新版本 ${updateInfo.versionName}"
        binding.tvContent.text = updateInfo.updateLog
        binding.btnUpdate.setOnClickListener {
            onUpdate()
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            onCancel()
            dismiss()
        }
        binding.btnCancel.visibility = if (updateInfo.isForce) android.view.View.GONE else android.view.View.VISIBLE
    }

    fun setProgress(progress: Int) {
        binding.progressBar.progress = progress
        binding.tvProgress.text = "$progress%"
    }
}
