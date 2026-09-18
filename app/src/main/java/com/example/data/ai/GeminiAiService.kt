package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.game.model.SkillId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {
    private val TAG = "GeminiAiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun generateChronicleStory(
        timeSurvivedSec: Int,
        level: Int,
        kills: Int,
        isVictory: Boolean,
        skills: Map<SkillId, Int>
    ): String = withContext(Dispatchers.IO) {
        val skillsDesc = skills.entries.joinToString(", ") { "${it.key.name} (Lv.${it.value})" }
        val prompt = """
            Bạn là người kể sử thi của vương quốc Slime cổ đại trong tựa game "Slime Survivor: The Chronicles".
            Hãy viết một đoạn sử thi ngắn (khoảng 3-4 câu) bằng tiếng Việt thật hào hùng, đậm chất RPG viễn tưởng về trận chiến vừa qua:
            - Kết quả: ${if (isVictory) "Chiến Thắng Vang Dội, sống sót qua đêm đen và tiêu diệt Thần Cây Già!" else "Hi sinh anh dũng giữa vòng vây quái vật"}
            - Thời gian sống sót: ${timeSurvivedSec / 60} phút ${timeSurvivedSec % 60} giây
            - Cấp độ đạt được: Cấp $level
            - Số lượng quái đã diệt: $kills quái vật
            - Các kỹ năng trang bị: $skillsDesc
            Hãy dùng giọng văn cuốn hút, tôn vinh lòng quả cảm của Slime Hiệp Sĩ mọng nước!
        """.trimIndent()

        try {
            val response = generateContentWithGemini(prompt)
            if (response.isNotBlank()) response else getFallbackChronicle(isVictory, level, kills)
        } catch (e: Exception) {
            Log.w(TAG, "Gemini call failed, using fallback lore: ${e.message}")
            getFallbackChronicle(isVictory, level, kills)
        }
    }

    suspend fun getSageStrategyAdvice(
        currentLevel: Int,
        timeSec: Int,
        skills: Map<SkillId, Int>
    ): String = withContext(Dispatchers.IO) {
        val skillsDesc = skills.entries.joinToString(", ") { "${it.key.name} Lv.${it.value}" }
        val prompt = """
            Bạn là Thần Rừng Cổ Đại (Forest Sage) trong tựa game "Slime Survivor".
            Hãy đưa ra 2 lời khuyên chiến thuật ngắn gọn, dí dỏm và hữu ích cho Slime Hiệp Sĩ:
            - Thời gian hiện tại trong trận: ${timeSec / 60}m ${timeSec % 60}s (Lưu ý: phút thứ 2 có Dơi Lửa bay nhanh, phút thứ 4 có Boss Thần Cây Già khổng lồ)
            - Cấp độ hiện tại: $currentLevel
            - Kỹ năng đang có: $skillsDesc
            Trả lời bằng tiếng Việt, súc tích, gạch đầu dòng rõ ràng.
        """.trimIndent()

        try {
            val response = generateContentWithGemini(prompt)
            if (response.isNotBlank()) response else getFallbackSageAdvice(timeSec)
        } catch (e: Exception) {
            Log.w(TAG, "Gemini call failed, using fallback advice: ${e.message}")
            getFallbackSageAdvice(timeSec)
        }
    }

    private fun generateContentWithGemini(prompt: String): String {
        return try {
            // Check BuildConfig for GEMINI_API_KEY
            val apiKeyField = try {
                val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                field.get(null) as? String ?: ""
            } catch (_: Exception) {
                ""
            }

            if (apiKeyField.isBlank() || apiKeyField.startsWith("MY_")) {
                return ""
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKeyField"
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return ""

            val responseString = response.body?.string() ?: return ""
            val jsonRes = JSONObject(responseString)
            val candidates = jsonRes.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCand = candidates.getJSONObject(0)
            val content = firstCand.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            parts.getJSONObject(0).optString("text", "")
        } catch (e: Exception) {
            Log.d(TAG, "Direct Gemini call exception: ${e.message}")
            ""
        }
    }

    private fun getFallbackChronicle(isVictory: Boolean, level: Int, kills: Int): String {
        return if (isVictory) {
            "Trận chiến huyền thoại đã khép lại! Slime Hiệp Sĩ quả cảm đạt đến Cấp $level, quét sạch $kills quái vật bóng tối và bảo vệ vầng hào quang neon của Khu Rừng Ma Thuật suốt 5 phút trường kỳ!"
        } else {
            "Dẫu ngã xuống trước đợt sóng quái vật hung hãn sau khi diệt $kills kẻ địch, linh hồn kiên cường của Slime Hiệp Sĩ (Cấp $level) vẫn mãi là biểu tượng bất tử được khắc ghi trong biên niên sử rừng già!"
        }
    }

    private fun getFallbackSageAdvice(timeSec: Int): String {
        return when {
            timeSec < 120 -> "• Ưu tiên nâng cấp Tia Sét Định Vị hoặc Vòng Lửa để dọn dẹp Nấm Độc nhanh chóng.\n• Giữ khoảng cách và nhặt thật nhiều Ngọc Xanh để nhanh chóng thăng cấp trước đợt Dơi Lửa!"
            timeSec < 240 -> "• Dơi Lửa bay rất nhanh! Hãy trang bị Vòng Lửa và Giày Tốc Độ để né tránh va chạm.\n• Chuẩn bị sẵn Rìu Xoay và Giáp Thạch trước khi Thần Cây Già xuất trận ở phút thứ 4!"
            else -> "• Thần Cây Già đã thức tỉnh! Hãy liên tục di chuyển vòng quanh boss, để Tia Sét và Vòng Lửa cấu rỉa máu từ từ. Chiến thắng 5 phút đang ở rất gần!"
        }
    }
}
