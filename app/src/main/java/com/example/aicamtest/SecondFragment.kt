package com.example.aicamtest

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kcalidentifier.databinding.FragmentSecondBinding
import org.json.JSONObject
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SecondFragment : Fragment() {
    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var module: Module

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load model từ assets
//        module = Module.load(assetFilePath(requireContext(), "best_calorie.pt"))

        val foodName = arguments?.getString("food_name") ?: ""

        // Hiển thị thông tin dinh dưỡng từ JSON
        val nutritionInfo = getNutritionFromJson(requireContext(), foodName)
        binding.textFoodName.text = if (nutritionInfo != null) {
            val formattedName = foodName.replaceFirstChar { it.uppercase() }
            """
            Món: $formattedName
            Năng lượng: ${nutritionInfo.kcal} kcal
            Protein: ${nutritionInfo.protein}g
            Carb: ${nutritionInfo.carbs}g
            Chất béo: ${nutritionInfo.fat}g
            """.trimIndent()
        } else {
            "Không tìm thấy thông tin cho món '${foodName}'"
        }

        // Xử lý ảnh từ Camera/Gallery
        val imageUriString = arguments?.getString("image_uri")
        imageUriString?.let {
            val uri = Uri.parse(it)
            binding.imageFood.setImageURI(uri)

            // Chuyển ảnh thành calories dự đoán từ AI
//            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
//            val caloriesPred = runModel(bitmap)
//
//            // Cập nhật UI với kết quả AI
//            binding.textAiResult.text = "AI dự đoán: ${caloriesPred.toInt()} kcal"
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    // Hàm chạy model AI
//    private fun runModel(bitmap: Bitmap): Float {
//        val inputTensor = Tensor.fromBlob(
//            preprocessImage(bitmap),
//            longArrayOf(1, 3, 224, 224)
//        )
//        val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
//        return outputTensor.dataAsFloatArray[0] // vì model trả về 1 giá trị calories
//    }

    // Tiền xử lý ảnh: resize và normalize về [0,1]
//    private fun preprocessImage(bitmap: Bitmap): FloatArray {
//        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
//        val inputData = FloatArray(3 * 224 * 224)
//        var idx = 0
//        for (y in 0 until 224) {
//            for (x in 0 until 224) {
//                val pixel = resizedBitmap.getPixel(x, y)
//                inputData[idx++] = ((pixel shr 16) and 0xFF) / 255.0f // R
//                inputData[idx++] = ((pixel shr 8) and 0xFF) / 255.0f  // G
//                inputData[idx++] = (pixel and 0xFF) / 255.0f           // B
//            }
//        }
//        return inputData
//    }

    // Hàm copy file model từ assets ra bộ nhớ
//    private fun assetFilePath(context: Context, assetName: String): String {
//        val file = File(context.filesDir, assetName)
//        if (file.exists() && file.length() > 0) {
//            return file.absolutePath
//        }
//        try {
//            context.assets.open(assetName).use { inputStream ->
//                FileOutputStream(file).use { outputStream ->
//                    val buffer = ByteArray(4 * 1024)
//                    var read: Int
//                    while (inputStream.read(buffer).also { read = it } != -1) {
//                        outputStream.write(buffer, 0, read)
//                    }
//                    outputStream.flush()
//                }
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return file.absolutePath
//    }

    private fun getNutritionFromJson(context: Context, food: String): NutritionInfo? {
        return try {
            val inputStream = context.assets.open("nutrition_map.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val matchingKey = jsonObject.keys().asSequence().firstOrNull {
                it.equals(food, ignoreCase = true)
            }

            matchingKey?.let { key ->
                val foodJson = jsonObject.getJSONObject(key)
                NutritionInfo(
                    kcal = foodJson.getInt("kcal"),
                    protein = foodJson.getDouble("protein"),
                    carbs = foodJson.getDouble("carbs"),
                    fat = foodJson.getDouble("fat")
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class NutritionInfo(
        val kcal: Int,
        val protein: Double,
        val carbs: Double,
        val fat: Double
    )
}
