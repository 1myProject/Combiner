package by.morinosenshi.combiner.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import by.morinosenshi.combiner.R
import by.morinosenshi.combiner.toast


class MargerSettingsAlert(activity: Activity) {
    private val context: Context = activity
    private val settings = MargerSettings()
    private val alert: AlertDialog
    private val runBtn: TextView

    init {
        val root = activity.layoutInflater.inflate(R.layout.marger_settings_alert, null)

        crf(root)
        preset(root)

        blur(root)

        alert = AlertDialog.Builder(context).setView(root).create()
        runBtn = root.findViewById(R.id.run_btn)
    }

    private fun crf(root: View) {
        val head = root.findViewById<TextView>(R.id.head_crf)
        val seek = root.findViewById<SeekBar>(R.id.seekBar_crf)

        head.text = context.getString(R.string.crf_head, "${CRF.DEFAULT} (default)")

        seek.max = CRF.MAX
        seek.min = CRF.MIN
        seek.progress = CRF.DEFAULT

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return

                settings.crf = progress

                val inf = if (progress == CRF.DEFAULT) "${CRF.DEFAULT} (default)"
                else progress.toString()

                head.text = context.getString(R.string.crf_head, inf)
            }
        })

    }

    private fun preset(root: View) {
        val head = root.findViewById<TextView>(R.id.head_preset)
        val seek = root.findViewById<SeekBar>(R.id.seekBar_preset)

        head.text = context.getString(R.string.preset_head, "${Preset.DEFAULT_NAME} (default)")

        seek.min = 0
        seek.max = Preset.MAX
        seek.progress = Preset.DEFAULT

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return

                settings.preset = Preset.getPreset(progress)

                val inf = if (progress == Preset.DEFAULT) "${Preset.DEFAULT_NAME} (default)"
                else Preset.getPreset(progress).toString()

                head.text = context.getString(R.string.preset_head, inf)
            }
        })
    }

    private fun blur(root: View) {
        val blurBox = root.findViewById<CheckBox>(R.id.blurBox)
        blurBox.setOnClickListener {
            context.toast("пока не зрабив")
            blurBox.isChecked = false
        }
    }

    fun show(runMerge: (MargerSettings) -> Unit) {
        alert.show()
        runBtn.setOnClickListener {
            alert.dismiss()
            runMerge(settings)
        }
    }
}