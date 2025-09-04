package com.example.aicamtest

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kcalidentifier.databinding.FragmentSecondBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.aicamtest.api.CalorieApi
import com.example.aicamtest.api.CalorieResponse
import java.io.File
import java.io.FileOutputStream
import kotlin.math.round

class SecondFragment : Fragment() {
    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUriString = arguments?.getString("image_uri")
        imageUriString?.let { uriString ->
            val uri = Uri.parse(uriString)
            binding.imageFood.setImageURI(uri)

            // Gửi ảnh lên server
            sendImageToApi(uri)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun sendImageToApi(uri: Uri) {
        // Chuyển Uri -> File tạm
        val file = File(requireContext().cacheDir, "temp_image.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://ced6f276a6f9.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(CalorieApi::class.java)
        api.predictCalories(body).enqueue(object : Callback<CalorieResponse> {
            override fun onResponse(
                call: Call<CalorieResponse>,
                response: Response<CalorieResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    result?.let {
                        // Làm tròn giá trị predicted_calories
                        val roundedCalories = round(it.predicted_calories).toInt()
                        binding.textAiResult.text = "Dự đoán calories: $roundedCalories kcal"
                    }
                } else {
                    binding.textAiResult.text = "Lỗi response: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<CalorieResponse>, t: Throwable) {
                binding.textAiResult.text = "Lỗi kết nối: ${t.message}"
            }
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
