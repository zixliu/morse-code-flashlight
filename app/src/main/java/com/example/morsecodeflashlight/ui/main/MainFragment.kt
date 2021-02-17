package com.example.morsecodeflashlight.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.example.morsecodeflashlight.R
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import java.util.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        private const val DEFAULT_LENGTH_ONE_UNIT_MILLISECONDS = 200L
        private const val DEFAULT_LENGTH_DASH = DEFAULT_LENGTH_ONE_UNIT_MILLISECONDS * 3
        private const val DEFAULT_DELAY_LETTERS = DEFAULT_LENGTH_ONE_UNIT_MILLISECONDS * 3
        private const val DEFAULT_DELAY_WORDS = DEFAULT_LENGTH_ONE_UNIT_MILLISECONDS * 7
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var cameraManager: CameraManager
    private var isFlashlightOn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val hasCameraFlash = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
//        val isEnabled = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        lifecycleScope.launch {
            cameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            button.setOnClickListener {
                handleFlashButtonClick()
            }

            button2.setOnClickListener {
                lifecycleScope.launch {
                    flashBinaryMessage("SOS")
                }
            }
        }

    }

    private fun handleFlashButtonClick() {
        lifecycleScope.launch {
            button.text = getString(R.string.flashing_message)
            button.setBackgroundColor(resources.getColor(R.color.teal_700))
            val message = edit_text_enter_message.text.toString()
            Log.d("HAHA","message is $message")
            flashBinaryMessage(message)
            Log.d("HAHA", "it is done")
            button.text = getString(R.string.flash_morse_code)
            button.setBackgroundResource(R.color.purple_500)
        }
    }

    private suspend fun flashBinaryMessage(message: String) {
        val messageInBinary = viewModel.convertToBinary(message.toUpperCase(Locale.ROOT))
        Log.d("HAHA", "Message is $message, in binary is $messageInBinary")
        messageInBinary.forEach {
            when (it) {
                '/' -> pause(DEFAULT_DELAY_WORDS)
                '-' -> pause(DEFAULT_DELAY_LETTERS)
                else -> {
                    when (it) {
                        '0' -> blink(DEFAULT_LENGTH_ONE_UNIT_MILLISECONDS)
                        '1' -> blink(DEFAULT_LENGTH_DASH)
                        else -> print("error")
                    }
                    pause(DEFAULT_LENGTH_ONE_UNIT_MILLISECONDS)
                }
            }
        }
    }

    private suspend fun blink(delay: Long = DEFAULT_LENGTH_ONE_UNIT_MILLISECONDS) {
        flashLightOn()
        pause(delay)
        flashLightOff()
    }

    private suspend fun pause(delayTime: Long) {
        delay(delayTime)
    }

    private fun flashLightOn() {
        cameraManager.setTorchMode(cameraManager.cameraIdList.first(), true)
        isFlashlightOn = true
    }

    private fun flashLightOff() {
        cameraManager.setTorchMode(cameraManager.cameraIdList.first(), false)
        isFlashlightOn = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }
}