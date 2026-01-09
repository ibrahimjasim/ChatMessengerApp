package com.example.chatmessengerapp.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
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
import androidx.lifecycle.Observer
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

    private lateinit var viewModel: ChatAppViewModel
    private lateinit var binding: FragmentSettingsBinding

    private lateinit var storageRef: StorageReference
    private lateinit var storage: FirebaseStorage

    // Modern way to handle activity results for picking an image
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->
                val imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                uploadImageToFirebaseStorage(imageBitmap)
            }
        }
    }

    // Modern way to handle activity results for taking a photo
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            uploadImageToFirebaseStorage(imageBitmap)
        }
    }

    // Modern way to request gallery permission
    private val galleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            pickImageFromGallery()
        } else {
            Toast.makeText(requireContext(), "Permission denied to access gallery", Toast.LENGTH_SHORT).show()
        }
    }

    // Modern way to request camera permission
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takePhotoWithCamera()
        } else {
            Toast.makeText(requireContext(), "Permission denied to use camera", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        viewModel.imageUrl.observe(viewLifecycleOwner, Observer { url ->
            if (!url.isNullOrBlank()) loadImage(url)
        })

        binding.settingBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.settingUpdateButton.setOnClickListener {
            viewModel.updateProfile()
        }

        binding.settingUpdateImage.setOnClickListener {
            showPictureDialog()
        }
    }

    private fun showPictureDialog() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose your profile picture")
            .setItems(options) { dialog, item ->
                when (options[item]) {
                    "Take Photo" -> checkCameraPermissionAndTakePhoto()
                    "Choose from Gallery" -> checkGalleryPermissionAndPickImage()
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkGalleryPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            pickImageFromGallery()
        } else {
            galleryPermissionLauncher.launch(permission)
        }
    }

    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhotoWithCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun pickImageFromGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(pickIntent)
    }

    private fun takePhotoWithCamera() {
        val takeIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takeIntent)
    }

    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap?) {
        if (imageBitmap == null) return

        // Show the selected image immediately
        binding.settingUpdateImage.setImageBitmap(imageBitmap)

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val bytes = baos.toByteArray()

        val storagePath = storageRef.child("Photos/${UUID.randomUUID()}.jpg")
        storagePath.putBytes(bytes)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                    viewModel.imageUrl.value = downloadUri.toString()
                    Toast.makeText(context, "Image uploaded. Press Update to save.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload image!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.person)
            .dontAnimate()
            .into(binding.settingUpdateImage)
    }
}
