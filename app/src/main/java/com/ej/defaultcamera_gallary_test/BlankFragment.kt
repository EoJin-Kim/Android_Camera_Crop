package com.ej.defaultcamera_gallary_test

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.ej.defaultcamera_gallary_test.databinding.FragmentBlankBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class BlankFragment : Fragment() {
    lateinit var binding : FragmentBlankBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val viewModel : ImageViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_blank,container,false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            takePhoto()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireActivity(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val callback = object : OnImageCapturedCallback (){
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val bitmap = image.convertImageProxyToBitmap()
                val rotationsDegrees = image.imageInfo.rotationDegrees

                val matrix = Matrix()
                matrix.postRotate(rotationsDegrees.toFloat())

                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                activity?.let {
                    it.runOnUiThread {
                        viewModel.bitmap = rotatedBitmap
                        val fragment = CropFragment.newInstance()
                        val tag = CropFragment.TAG
                        addFragment(fragment, tag, R.id.fragmentContainerView)
                    }
                }
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }
        }

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireActivity()), callback)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
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

    companion object {

        private const val TAG = "CameraXApp"
        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        @JvmStatic
        fun newInstance() =
            BlankFragment()
    }
}

fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}