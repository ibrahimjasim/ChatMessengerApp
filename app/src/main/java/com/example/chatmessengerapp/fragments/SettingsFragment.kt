package com.example.chatmessengerapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.Utils
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
    private var uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ✅ Correct layout name: fragment_settings.xml
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

        // ✅ Observe once (remove onResume observer)
        viewModel.imageUrl.observe(viewLifecycleOwner, Observer { url ->
            if (!url.isNullOrBlank()) loadImage(url)
        })

        binding.settingBackBtn.setOnClickListener {
            // ✅ safest back
            findNavController().popBackStack()
        }

        binding.settingUpdateButton.setOnClickListener {
            viewModel.updateProfile()
        }

        binding.settingUpdateImage.setOnClickListener {
            val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

            AlertDialog.Builder(requireContext())
                .setTitle("Choose your profile picture")
                .setItems(options) { dialog, item ->
                    when (options[item]) {
                        "Take Photo" -> takePhotoWithCamera()
                        "Choose from Gallery" -> pickImageFromGallery()
                        else -> dialog.dismiss()
                    }
                }
                .show()
        }
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.person)
            .dontAnimate()
            .into(binding.settingUpdateImage)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun pickImageFromGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (pickIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(pickIntent, Utils.REQUEST_IMAGE_PICK)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhotoWithCamera() {
        val takeIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takeIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takeIntent, Utils.REQUEST_IMAGE_CAPTURE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            Utils.REQUEST_IMAGE_CAPTURE -> {
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                uploadImageToFirebaseStorage(imageBitmap)
            }

            Utils.REQUEST_IMAGE_PICK -> {
                val imageUri = data?.data ?: return
                val imageBitmap =
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                uploadImageToFirebaseStorage(imageBitmap)
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap?) {
        if (imageBitmap == null) return

        binding.settingUpdateImage.setImageBitmap(imageBitmap)

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val bytes = baos.toByteArray()

        val storagePath = storageRef.child("Photos/${UUID.randomUUID()}.jpg")
        storagePath.putBytes(bytes)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { downloadUri ->
                        uri = downloadUri
                        viewModel.imageUrl.value = downloadUri.toString()
                        Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload image!", Toast.LENGTH_SHORT).show()
            }
    }
}