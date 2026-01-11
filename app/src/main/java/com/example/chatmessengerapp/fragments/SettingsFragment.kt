package com.example.chatmessengerapp.fragments

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

    // ---------------- ACTIVITY RESULT LAUNCHERS ----------------

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    it
                )
                uploadImageToFirebaseStorage(bitmap)
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            uploadImageToFirebaseStorage(bitmap)
        }

    // -----------------------------------------------------------

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

        // Observe profile image
        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrBlank()) {
                loadImage(url)
            }
        }

        // Back
        binding.settingBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // Update profile info
        binding.settingUpdateButton.setOnClickListener {
            viewModel.updateProfile()
        }

        // Change profile image
        binding.settingUpdateImage.setOnClickListener {
            showImagePickerDialog()
        }
    }

    // ---------------- IMAGE PICKER DIALOG ----------------

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(requireContext())
            .setTitle("Choose your profile picture")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Take Photo" -> takePhotoWithCamera()
                    "Choose from Gallery" -> pickImageFromGallery()
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    // ---------------- IMAGE ACTIONS ----------------

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun takePhotoWithCamera() {
        takePhotoLauncher.launch(null)
    }

    // ---------------- FIREBASE STORAGE ----------------

    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap?) {
        if (imageBitmap == null) return

        // Preview directly
        binding.settingUpdateImage.setImageBitmap(imageBitmap)

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val bytes = baos.toByteArray()

        val imageRef =
            storageRef.child("Photos/${UUID.randomUUID()}.jpg")

        imageRef.putBytes(bytes)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    viewModel.imageUrl.value = uri.toString()
                    Toast.makeText(
                        requireContext(),
                        "Image uploaded successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Failed to upload image!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // ---------------- IMAGE LOADING ----------------

    private fun loadImage(imageUrl: String) {
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.person)
            .dontAnimate()
            .into(binding.settingUpdateImage)
    }
}