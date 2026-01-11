package com.example.chatmessengerapp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.databinding.FragmentSettingsBinding
import com.example.chatmessengerapp.mvvm.ChatAppViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.UUID

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: ChatAppViewModel

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    // --------- ACTIVITY RESULT LAUNCHERS ---------

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    it
                )
                uploadImage(bitmap)
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            uploadImage(bitmap)
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                takePhotoLauncher.launch(null)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // --------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.person)
                    .into(binding.settingUpdateImage)
            }
        }

        binding.settingBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.settingUpdateButton.setOnClickListener {
            viewModel.updateProfile()
        }

        binding.settingUpdateImage.setOnClickListener {
            showImagePickerDialog()
        }
    }

    // --------- IMAGE PICKER ---------

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(requireContext())
            .setTitle("Choose profile picture")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Take Photo" -> checkCameraPermissionAndOpen()
                    "Choose from Gallery" -> pickImageLauncher.launch("image/*")
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        val permission = Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePhotoLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    // --------- FIREBASE UPLOAD ---------

    private fun uploadImage(bitmap: Bitmap?) {
        if (bitmap == null) return

        binding.settingUpdateImage.setImageBitmap(bitmap)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()

        val imageRef =
            storageRef.child("Photos/${UUID.randomUUID()}.jpg")

        imageRef.putBytes(data)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    viewModel.imageUrl.value = uri.toString()
                    Toast.makeText(
                        requireContext(),
                        "Image uploaded",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Upload failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}