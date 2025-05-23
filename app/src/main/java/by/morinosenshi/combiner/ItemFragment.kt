package by.morinosenshi.combiner

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.morinosenshi.combiner.databinding.FragmentItemListBinding
import by.morinosenshi.combiner.settings.MargerSettings
import by.morinosenshi.combiner.settings.MargerSettingsAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ItemFragment : Fragment() {
    private var viewAdapter: MyItemRecyclerViewAdapter? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { code ->
        if (code.resultCode != RESULT_OK) return@registerForActivityResult

        val a = code.data ?: return@registerForActivityResult
        a.data?.let { uri ->
            handleSelectedFile(uri)
        }
        a.clipData?.let { clips ->
            for (i in 0..<clips.itemCount) {
                val uri = clips.getItemAt(i).uri
                handleSelectedFile(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = FragmentItemListBinding.bind(
            inflater.inflate(R.layout.fragment_item_list, container, false)
        )

        with(root.list) {
            layoutManager = LinearLayoutManager(context)
            viewAdapter = MyItemRecyclerViewAdapter(MyData.LIST)
            adapter = viewAdapter
        }

        root.add.setOnClickListener {
            openFilePicker()
        }

        root.run.setOnClickListener {
            MargerSettingsAlert(requireActivity()).show(::startMerge)
        }

        return root.root
    }

    fun startMerge(settings: MargerSettings) {
        val merger = VideoMerger.with(requireActivity()).setVideoFiles(MyData.List_FILES).setOutputPath(getOutputFile())
        CoroutineScope(Dispatchers.IO).launch {
            merger.mergeConcat(settings)
        }
        viewAdapter?.notifyItemRangeRemoved(0, MyData.LIST.size)
        MyData.LIST.clear()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        resultLauncher.launch(intent)
    }

    private fun handleSelectedFile(uri: Uri) {
        MyData.addItem(requireContext(), uri)
        viewAdapter?.addNew()
    }

    private fun getOutputFile(): File {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Combiner")
        if (file.isFile) file.delete()
        if (!file.exists()) file.mkdirs()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")
        val current = LocalDateTime.now().format(formatter)

//        val fileName = "${System.currentTimeMillis()}.mp4"
        val fileName = "${current}.mp4"
        return File(file, fileName)
    }
}
