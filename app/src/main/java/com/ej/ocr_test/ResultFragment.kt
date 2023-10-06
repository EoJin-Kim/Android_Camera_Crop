package com.ej.ocr_test

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ej.ocr_test.databinding.FragmentResultBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

class ResultFragment : Fragment() {
    lateinit var binding : FragmentResultBinding

    private val viewModel : ImageViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_result, container,false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setImageBitmap( viewModel.cropImage)

        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        val inputImage = InputImage.fromBitmap(viewModel.cropImage!!, 0)
        recognizer.process(inputImage)
            .addOnSuccessListener {
                for (textBlock in it.textBlocks) {
                    textBlock.cornerPoints
                }
                val strList = it.text.split("\n")
                if (strList.size < 3) {
                    return@addOnSuccessListener
                }
                binding.textView.text = strList[0]
                binding.textView2.text = strList[1].split("(")[0]
                binding.textView3.text = strList[2]
                for (str in it.text.split("\n")) {

                    Log.d("EJTAG", str)
                }
            }
            .addOnFailureListener {
                Log.d("EJTAG", it.message.toString())
            }
    }
    companion object{

        val TAG = this::class.java.simpleName
        fun newInstance() = ResultFragment()
    }
}