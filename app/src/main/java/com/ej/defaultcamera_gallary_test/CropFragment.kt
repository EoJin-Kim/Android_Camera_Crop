package com.ej.defaultcamera_gallary_test

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ej.defaultcamera_gallary_test.databinding.FragmentCropBinding
import com.theartofdev.edmodo.cropper.CropImageView.Guidelines


class CropFragment : Fragment() {
    lateinit var binding : FragmentCropBinding

    private val viewModel : ImageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_crop,container,false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cropImageView.setImageBitmap(viewModel.bitmap)

        binding.cropImageView.isAutoZoomEnabled = false
        binding.cropImageView.guidelines = Guidelines.OFF


        binding.button2.setOnClickListener {
            val cropped: Bitmap = binding.cropImageView.croppedImage
            viewModel.cropImage = cropped

            val fragment = ResultFragment.newInstance()
            val tag = ResultFragment.TAG
            addFragment(fragment, tag, R.id.fragmentContainerView)
        }
    }

    fun addFragment(fragment: Fragment, tag : String, @IdRes container : Int) {
        activity?.let { act ->
            val transaction = parentFragmentManager.beginTransaction()
            transaction
                .add(container, fragment, tag)
                .addToBackStack(tag)
            transaction.commit()
        }
    }

    companion object{

        val TAG = this::class.java.simpleName
        fun newInstance() = CropFragment()
    }
}