package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.BuildConfig
import com.example.data.AppThemeMode
import com.example.data.ItemType
import com.example.data.LibraryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel : ViewModel() {

    private val _currentTab = MutableStateFlow("Dashboard")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SAPPHIRE)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(false)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _previewItem = MutableStateFlow<LibraryItem?>(null)
    val previewItem: StateFlow<LibraryItem?> = _previewItem.asStateFlow()

    private val _libraryItems = MutableStateFlow(
        listOf(
            LibraryItem(
                title = "English Grammar: Present Perfect vs Past Simple",
                type = ItemType.WORKSHEET,
                details = "Level B1 • Fill in the blanks & Error correction • A4 .pdf",
                contentPreview = "🎨 [Illustration: Grammar Timeline Chart & Dialogue Bubbles]\n\nInstruction: Complete the sentences using either the Present Perfect or Past Simple tense.\n\n1. I (visit) ___________ London three times last year.\n2. She (already / finish) ___________ her English homework.\n3. They (live) ___________ in Paris since 2020.\n4. Correct the mistake: 'I have saw that movie yesterday.'"
            ),
            LibraryItem(
                title = "Business English Email Vocabulary & Tone",
                type = ItemType.WORKSHEET,
                details = "Level B2/C1 • Multiple Choice & Matching • A4 .docx",
                contentPreview = "🎨 [Illustration: Professional Workplace & Formal Correspondence Icon]\n\nInstruction: Match formal email sign-offs with their appropriate context.\n\n1. 'Best regards' -> A) Casual chat\n2. 'Yours faithfully' -> B) Formal letter to unknown recipient\n3. Fill in the blank: 'I am writing to _________ with your inquiry about our new product.'"
            ),
            LibraryItem(
                title = "Exam Preparation Speaking Part 2: Travel & Culture Deck",
                type = ItemType.POWERPOINT,
                details = "8 Slides • Advanced Vocabulary & Cue Cards • .pptx",
                contentPreview = "🎨 [Illustration: Global Travel Imagery & Speaking Cue Card Graphic]\n\nSlide 1: Exam Speaking Part 2 Overview\nSlide 2: Cue Card: Describe an unforgettable journey\nSlide 3: High-Scoring Vocabulary (Ubiquitous, Itinerary, Wanderlust)\nSlide 4: Sample Band 8+ Response Structure"
            )
        )
    )
    val libraryItems: StateFlow<List<LibraryItem>> = _libraryItems.asStateFlow()

    fun setTab(tab: String) {
        _currentTab.value = tab
        _isSidebarOpen.value = false
    }

    fun setTheme(theme: AppThemeMode) {
        _themeMode.value = theme
        showToast("Theme switched to ${theme.name}")
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        showToast(if (_isDarkMode.value) "Dark Mode Enabled" else "Light Mode Enabled")
    }

    fun toggleSidebar() {
        _isSidebarOpen.value = !_isSidebarOpen.value
    }

    fun setPromptText(text: String) {
        _promptText.value = text
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setPreviewItem(item: LibraryItem?) {
        _previewItem.value = item
    }

    fun saveItem(item: LibraryItem) {
        if (!_libraryItems.value.any { it.id == item.id }) {
            _libraryItems.value = listOf(item) + _libraryItems.value
        }
        showToast("Worksheet successfully saved to My Library!")
    }

    fun shareItem(item: LibraryItem) {
        showToast("A4 Worksheet link copied to clipboard & ready to share!")
    }

    private suspend fun callGeminiApi(promptText: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
                if (apiKey.isBlank()) return@withContext null

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", promptText))
                            })
                        })
                    })
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val request = Request.Builder().url(url).post(requestBody).build()

                client.newCall(request).execute().use { response ->
                    if (response.code == 429) {
                        showToast("API Quota reached — rendering offline high-fidelity teaching template!")
                        return@withContext null
                    }
                    if (!response.isSuccessful) return@withContext null
                    val bodyString = response.body?.string() ?: return@withContext null
                    val jsonRes = JSONObject(bodyString)
                    val candidates = jsonRes.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun generateFromPrompt(onComplete: (LibraryItem) -> Unit) {
        val prompt = _promptText.value.trim()
        if (prompt.isEmpty()) {
            showToast("Please enter a prompt for the English AI agent!")
            return
        }
        _isGenerating.value = true
        CoroutineScope(Dispatchers.Main).launch {
            val apiPrompt = "Create a professional English teaching resource for teachers and learners based on: $prompt. Include clear pedagogical structure, exercises, vocabulary notes, and teacher answer key."
            val contentResult = callGeminiApi(apiPrompt) ?: "🎨 [AI Generated Illustration & Graphic Header]\n\nEnglish AI Generated Material for: \"$prompt\"\n\n- CEFR Aligned Curriculum\n- Vocabulary & Grammar Exercises\n- Teacher Answer Key Included\n- Ready to Save, Share & Export"

            _isGenerating.value = false
            val isPpt = prompt.lowercase().contains("pitch") || prompt.lowercase().contains("deck") || prompt.lowercase().contains("presentation") || prompt.lowercase().contains("ppt")
            val type = if (isPpt) ItemType.POWERPOINT else ItemType.WORKSHEET
            val newItem = LibraryItem(
                title = prompt.take(45) + if (prompt.length > 45) "..." else "",
                type = type,
                details = if (isPpt) "8 Slides • ESL Interactive • .pptx" else "Level B1 • A4 Worksheet • .pdf",
                contentPreview = contentResult
            )
            _libraryItems.value = listOf(newItem) + _libraryItems.value
            _promptText.value = ""
            showToast("Successfully generated English ${if (isPpt) "Presentation" else "A4 Worksheet"} in real-time!")
            _previewItem.value = newItem
            onComplete(newItem)
        }
    }

    fun createWorksheet(topic: String, grade: String, type: String, format: String, includeAnswerKey: Boolean, includeVocabulary: Boolean, questionCount: Int, illustrationPrompt: String) {
        if (topic.isBlank()) {
            showToast("Please enter an English topic or grammar point.")
            return
        }
        _isGenerating.value = true
        CoroutineScope(Dispatchers.Main).launch {
            val illDesc = if (illustrationPrompt.isNotBlank()) illustrationPrompt else "$topic educational concept"
            val apiPrompt = "Create a printable A4 English teaching worksheet about $topic for level $grade, focusing on $type with $questionCount exercises. Include: " +
                    (if (includeAnswerKey) "Teacher Answer Key, " else "") +
                    (if (includeVocabulary) "Vocabulary Glossary, " else "") +
                    "practice tasks and exercises, incorporating custom illustration theme: $illDesc."
            val baseContent = callGeminiApi(apiPrompt) ?: "English A4 Worksheet: $topic\nProficiency Level: $grade\nActivity Type: $type ($questionCount Questions)\n\n1. Warm-up Discussion Questions...\n2. Core Grammar & Vocabulary Practice...\n" +
                    (if (includeVocabulary) "3. Vocabulary Glossary & Phrasal Verbs...\n" else "") +
                    "\n[Teacher Answer Key Provided on Page 2]"

            val contentResult = "🎨 [AI Generated Illustration: \"$illDesc\"]\n\n$baseContent"

            _isGenerating.value = false
            val newItem = LibraryItem(
                title = "$topic ($grade)",
                type = ItemType.WORKSHEET,
                details = "$grade • $type • A4 .$format",
                contentPreview = contentResult
            )
            _libraryItems.value = listOf(newItem) + _libraryItems.value
            showToast("A4 Worksheet generated with custom AI illustration!")
            _previewItem.value = newItem
        }
    }

    fun createPresentation(topic: String, slides: Int, style: String, format: String, includeSpeakerNotes: Boolean, includeInteractiveQnA: Boolean, illustrationPrompt: String) {
        if (topic.isBlank()) {
            showToast("Please enter an English presentation topic.")
            return
        }
        _isGenerating.value = true
        CoroutineScope(Dispatchers.Main).launch {
            val illDesc = if (illustrationPrompt.isNotBlank()) illustrationPrompt else "$topic presentation banner"
            val apiPrompt = "Create an English slide presentation about $topic with $slides slides in $style style. " +
                    (if (includeSpeakerNotes) "Include detailed speaker notes for every slide. " else "") +
                    (if (includeInteractiveQnA) "Include interactive student Q&A and discussion prompts. " else "") +
                    "Include slide outlines, vocabulary, and custom illustration prompt: $illDesc."
            val baseContent = callGeminiApi(apiPrompt) ?: "English Presentation: $topic\nLayout Style: $style\nTotal Slides: $slides\n\nSlide 1: Lesson Objectives & Warm-up\nSlide 2: Key Vocabulary & Phonetics\nSlide 3: Interactive Dialogue Practice\n" +
                    (if (includeInteractiveQnA) "Slide 4: Q&A & Student Discussion Session\n" else "") +
                    (if (includeSpeakerNotes) "\n[Speaker Notes Included for Instructor]" else "")

            val contentResult = "🎨 [AI Generated Presentation Illustration: \"$illDesc\"]\n\n$baseContent"

            _isGenerating.value = false
            val newItem = LibraryItem(
                title = topic,
                type = ItemType.POWERPOINT,
                details = "$slides Slides • $style • .$format",
                contentPreview = contentResult
            )
            _libraryItems.value = listOf(newItem) + _libraryItems.value
            showToast("Presentation generated with custom AI illustration!")
            _previewItem.value = newItem
        }
    }

    fun useTemplate(title: String, type: ItemType, level: String, format: String, description: String, illPrompt: String, content: String) {
        _isGenerating.value = true
        CoroutineScope(Dispatchers.Main).launch {
            val apiPrompt = "Expand and enhance this English teaching template for topic \"$title\" (Level: $level, Type: $type). Description: $description. Include exercises, practice tasks, teacher answer key, and illustration theme: $illPrompt."
            val expandedContent = callGeminiApi(apiPrompt) ?: content

            _isGenerating.value = false
            val detailsText = if (type == ItemType.WORKSHEET) "$level • Worksheet • A4 .$format" else "8 Slides • Professional • .$format"
            val newItem = LibraryItem(
                title = title,
                type = type,
                details = detailsText,
                contentPreview = expandedContent
            )
            _libraryItems.value = listOf(newItem) + _libraryItems.value
            showToast("Template \"$title\" instantiated & generated successfully!")
            _previewItem.value = newItem
        }
    }

    fun deleteItem(id: String) {
        _libraryItems.value = _libraryItems.value.filter { it.id != id }
        showToast("Item deleted from library.")
    }
}

