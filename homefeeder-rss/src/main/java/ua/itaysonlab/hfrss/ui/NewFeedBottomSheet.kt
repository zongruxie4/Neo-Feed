package ua.itaysonlab.hfrss.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import ua.itaysonlab.hfrss.R
import ua.itaysonlab.hfrss.databinding.AddFeedBottomsheetBinding

class NewFeedBottomSheet(private val callback: (String) -> Unit): BottomSheetDialogFragment() {
    private lateinit var binding: AddFeedBottomsheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_feed_bottomsheet, container, false)
    }

    override fun getTheme() = R.style.TransparentBottomSheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AddFeedBottomsheetBinding.inflate(layoutInflater)
        view.findViewById<ImageView>(R.id.close).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.add).setOnClickListener {
            val txt = view.findViewById<TextInputEditText>(R.id.url).text
            if (txt.isNullOrEmpty()) return@setOnClickListener

            callback(txt.toString())
            dismiss()
        }
    }
}