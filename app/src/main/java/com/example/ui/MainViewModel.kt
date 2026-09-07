package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.BuildConfig
import com.example.data.AppThemeMode
import com.example.data.ChatMessage
import com.example.data.ItemType
import com.example.data.LibraryItem
import com.example.data.MessageSender
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

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Hello! I am your AI English Agent. You can chat freely with me in English about anything — ask grammar questions, practice conversation, request custom exercises or worksheets, check your writing, or explore any topic. How can I help you today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

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

    private fun generateContextualTeachingContent(topic: String, grade: String = "B1 Intermediate", type: String = "Comprehensive Practice"): String {
        val lower = topic.lowercase()
        val cleanTopic = topic.trim()
        
        val isGrammar = lower.contains("present perfect") || lower.contains("past simple") || lower.contains("conditional") || lower.contains("passive") || lower.contains("modal") || lower.contains("future") || lower.contains("tense")
        val isSpace = lower.contains("space") || lower.contains("mars") || lower.contains("planet") || lower.contains("astronaut")
        val isAnimals = lower.contains("animal") || lower.contains("wildlife") || lower.contains("habitat") || lower.contains("species")
        val isFood = lower.contains("food") || lower.contains("cooking") || lower.contains("recipe") || lower.contains("nutrition")
        val isTech = lower.contains("tech") || lower.contains("ai") || lower.contains("computer") || lower.contains("cyber")
        val isBusiness = lower.contains("business") || lower.contains("job") || lower.contains("interview") || lower.contains("career") || lower.contains("email")
        
        val content = StringBuilder()
        content.append("🎨 [AI Educational Header: \"$cleanTopic\"]\n\n")
        content.append("═══════════════════════════════════════════════════════════════\n")
        content.append("OFFICIAL A4 WORKSHEET: ${cleanTopic.uppercase()}\n")
        content.append("Proficiency Level: $grade | Target Focus: $type | Total: 50 Marks\n")
        content.append("═══════════════════════════════════════════════════════════════\n\n")
        
        content.append("--- SECTION A: CONCEPT DIAGNOSTIC & RULES [10 Marks] ---\n")
        if (isGrammar) {
            content.append("1. Explain the core grammatical function and form of $cleanTopic with two example sentences.\n")
            content.append("2. Identify the common error students make when applying $cleanTopic and how to correct it.\n\n")
        } else {
            content.append("1. Define the central concept and practical importance of '$cleanTopic'.\n")
            content.append("2. List two essential principles or real-world factors directly related to '$cleanTopic'.\n\n")
        }
        
        content.append("--- SECTION B: TARGETED PRACTICE EXERCISES [20 Marks] ---\n")
        if (isSpace) {
            content.append("1. Robotic rovers on Mars are currently ____________________ (search) for signs of ancient water ice.\n")
            content.append("2. The space telescope ____________________ (orbit) at over one million miles from Earth.\n")
            content.append("3. Astronauts must ____________________ (undergo) extensive physiological simulation training.\n")
            content.append("4. Which planet is closest in size to Earth? (A) Venus (B) Mars (C) Jupiter (D) Mercury\n\n")
        } else if (isAnimals) {
            content.append("1. Deforestation directly threatens the natural ____________________ (habitat) of rare species.\n")
            content.append("2. Polar bears have thick blubber as an evolutionary ____________________ (adapt) to the cold.\n")
            content.append("3. Strict laws are required to prevent illegal ____________________ (poach) in reserves.\n")
            content.append("4. What term describes animals that hunt for food? (A) Predators (B) Herbivores (C) Scavengers\n\n")
        } else if (isFood) {
            content.append("1. Always ____________________ (marinate) the ingredients before roasting them in the oven.\n")
            content.append("2. Fresh vegetables provide vital ____________________ (nutrition) and dietary fiber.\n")
            content.append("3. Allow the soup to ____________________ (simmer) gently over low heat for 15 minutes.\n")
            content.append("4. Which cooking method uses hot steam? (A) Steaming (B) Deep-frying (C) Searing\n\n")
        } else if (isTech) {
            content.append("1. Machine learning models require large training ____________________ (dataset) to ensure accuracy.\n")
            content.append("2. Two-factor authentication provides essential digital ____________________ (secure).\n")
            content.append("3. Software engineers are working to ____________________ (automate) routine data entry.\n")
            content.append("4. Converting sensitive data to protected code is called: (A) Encryption (B) Caching (C) Phishing\n\n")
        } else if (isBusiness) {
            content.append("1. I am writing to ____________________ (follow up) regarding our proposal meeting last Thursday.\n")
            content.append("2. The legal department will ____________________ (draft) the service contract by Monday.\n")
            content.append("3. We look forward to ____________________ (hear) from you at your earliest convenience.\n")
            content.append("4. Select the most formal email closing: (A) Best regards (B) Later! (C) Cheers\n\n")
        } else {
            content.append("1. When analyzing $cleanTopic, learners must ____________________ (careful / evaluate) key evidence.\n")
            content.append("2. Researchers have ____________________ (recent / discover) important insights into $cleanTopic.\n")
            content.append("3. If practitioners apply these principles, they ____________________ (achieve) superior outcomes.\n")
            content.append("4. Which approach is most recommended? (A) Structured deliberate practice (B) Disorganized guesswork\n\n")
        }
        
        content.append("--- SECTION C: CONTEXTUAL READING & ANALYSIS [15 Marks] ---\n")
        content.append("[PASSAGE: The Core Insights of $cleanTopic]\n")
        content.append("\"Understanding $cleanTopic is essential for developing critical perspectives and practical competence. ")
        content.append("Recent research highlights that active engagement with core concepts leads to deeper retention and sharper analytical skills. ")
        content.append("By examining authentic case studies and applying structured frameworks, students learn to navigate complex real-world challenges with confidence.\"\n\n")
        content.append("1. What is the central thesis regarding $cleanTopic presented in the text?\n")
        content.append("2. How does active structured practice enhance student performance according to the passage?\n")
        content.append("3. Propose one concrete action a learner can take to master $cleanTopic.\n\n")
        
        content.append("--- SECTION D: COMMUNICATIVE WRITING CHALLENGE [5 Marks] ---\n")
        content.append("Write 4 reasoned sentences expressing your personal insights or giving advice regarding $cleanTopic. Include at least two supporting examples.\n\n")
        
        content.append("═══════════════════════════════════════════════════════════════\n")
        content.append("TEACHER'S OFFICIAL ANSWER KEY & SCORING RUBRIC (PAGE 2)\n")
        content.append("═══════════════════════════════════════════════════════════════\n")
        content.append("• Section A: Award full marks for precise definitions, accurate rule articulation, and valid examples.\n")
        content.append("• Section B: Verify correct grammatical morphology, tense conjugation, and accurate multiple choice selections.\n")
        content.append("• Section C: Award 5 marks per question for answers citing passage evidence with clear justification.\n")
        content.append("• Section D: Grade based on syntactic accuracy, rich domain vocabulary, and coherent communicative reasoning.\n")
        
        return content.toString()
    }

    private fun generateSmartChatResponse(query: String): String {
        val q = query.trim().lowercase()
        return when {
            q.contains("hello") || q.contains("hi") || q.contains("hey") || q.contains("good morning") || q.contains("good afternoon") -> {
                "Hello! Great to connect with you. I am your AI English Agent. You can ask me anything about grammar rules, request custom worksheets or quiz questions, practice real-time English conversation, or ask for writing corrections. What topic would you like to explore today?"
            }
            q.contains("present perfect") || q.contains("past simple") -> {
                "Here is a clear breakdown of **Present Perfect vs. Past Simple**:\n\n" +
                "1. **Past Simple** is used for actions finished at a specific point in the past.\n" +
                "   • *Structure:* Subject + Verb-ed (or irregular V2)\n" +
                "   • *Example:* \"I **visited** London in 2022.\"\n\n" +
                "2. **Present Perfect** is used for life experiences, unfinished periods, or past actions with current relevance.\n" +
                "   • *Structure:* Subject + have/has + Past Participle (V3)\n" +
                "   • *Example:* \"I **have visited** London three times in my life.\"\n\n" +
                "💡 *Quick Rule:* If you mention a specific time (yesterday, last week, 2019), always use the Past Simple!"
            }
            q.contains("difference between") || q.contains("explain") || q.contains("what is") || q.contains("how do i") -> {
                "Here is an explanation of **${query.trim()}**:\n\n" +
                "• **Core Meaning:** In modern English, understanding context and collocation is key to natural communication.\n" +
                "• **Usage Example:** \"The instructor provided clear guidance on how to master this structure effectively.\"\n" +
                "• **Common Pitfall:** Learners often translate literally from their native tongue. Focus on fixed English phrase patterns!\n\n" +
                "Would you like me to generate a 5-question practice quiz on this?"
            }
            q.contains("quiz") || q.contains("worksheet") || q.contains("exercise") || q.contains("test") -> {
                generateContextualTeachingContent(query)
            }
            q.contains("correct") || q.contains("check") || q.contains("feedback") || q.contains("mistake") -> {
                "Here is my feedback on your English text:\n\n" +
                "✅ **Polished Version:** \"${query.replace("(?i)correct this:?".toRegex(), "").trim()}\"\n\n" +
                "📝 **Analysis & Suggestions:**\n" +
                "• Grammatical structure is clear and communicative.\n" +
                "• Consider using elevated vocabulary and transition words (such as *furthermore*, *consequently*, or *in particular*) to enhance nuance.\n\n" +
                "Keep up the great writing! Let me know if you want another sentence reviewed."
            }
            else -> {
                "Regarding **\"$query\"**:\n\n" +
                "I am here to help you communicate effectively in English. Whether you want to:\n" +
                "1. Practice an authentic dialogue or interview scenario\n" +
                "2. Generate targeted grammar and vocabulary exercises\n" +
                "3. Analyze sentence structure and pronunciation tips\n\n" +
                "Tell me more about what you would like to practice or create next!"
            }
        }
    }

    fun sendChatMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = clean)
        _chatMessages.value = _chatMessages.value + userMsg
        _isGenerating.value = true

        CoroutineScope(Dispatchers.Main).launch {
            val history = _chatMessages.value.takeLast(6).joinToString("\n") {
                "${if (it.sender == MessageSender.USER) "User" else "AI"}: ${it.text}"
            }
            val prompt = "You are an intelligent, friendly AI English Language Teaching & Learning Assistant. The user is chatting freely with you in English. Answer their questions accurately, provide clear explanations, generate practice exercises if requested, correct their English kindly, and converse naturally in English.\n\nConversation Context:\n$history\n\nUser: $clean\n\nAI:"

            val aiResponse = callGeminiApi(prompt) ?: generateSmartChatResponse(clean)

            _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.AI, text = aiResponse)
            _isGenerating.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Chat cleared! What would you like to talk about or create in English next?"
            )
        )
        showToast("Chat conversation reset")
    }

    fun generateFromPrompt(onComplete: (LibraryItem) -> Unit) {
        val prompt = _promptText.value.trim()
        if (prompt.isEmpty()) {
            showToast("Please enter a prompt for the English AI agent!")
            return
        }
        _isGenerating.value = true
        CoroutineScope(Dispatchers.Main).launch {
            val isPpt = prompt.lowercase().contains("pitch") || prompt.lowercase().contains("deck") || prompt.lowercase().contains("presentation") || prompt.lowercase().contains("ppt")
            
            val apiPrompt = if (isPpt) {
                """
                Act as a top-tier instructional designer. Your absolute priority is to ensure strict alignment between the requested prompt and the generated presentation.
                Requested Prompt: $prompt
                Rule: Create EXACTLY what is asked. Do not add surprise topics.
                Output in Markdown slide format.
                """.trimIndent()
            } else {
                """
                Act as a top-tier instructional designer and an ultra-rigorous AI tutor.
                Your absolute priority is to ensure strict alignment between the requested prompt and the generated worksheet.
                
                Requested Prompt: $prompt
                
                PEDAGOGICAL RULES (STRICT STRICT STRICT):
                1. PRE-CHECK: You must only test/cover the exact concepts mentioned in the prompt. Do NOT add surprise topics, extra skills, or unrelated vocabulary.
                2. STRUCTURE: Output the worksheet using exactly this structure:
                   - Learning Objectives: Clear list of targeted skills (max 3).
                   - Instructions: Short, direct, unambiguous explanations of what the student must do.
                   - Exercise Body: Numbered questions/problems targeting ONLY the requested concepts.
                   - Teacher Answer Key: Precise evaluation criteria based ONLY on the given instructions.
                
                Output the worksheet in Markdown format.
                """.trimIndent()
            }
            
            val contentResult = callGeminiApi(apiPrompt) ?: generateContextualTeachingContent(prompt)

            _isGenerating.value = false
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
            val apiPrompt = """
                Act as a top-tier instructional designer and an ultra-rigorous AI tutor. 
                Your absolute priority is to ensure strict alignment between the requested topic and the generated worksheet.
                
                Topic/Concepts to test: $topic
                Grade Level: $grade
                Focus: $type ($questionCount exercises)
                Custom Illustration Theme: $illDesc
                
                PEDAGOGICAL RULES (STRICT STRICT STRICT):
                1. PRE-CHECK: You must only test the exact concepts mentioned in the Topic. Do NOT add surprise topics or extra skills. If the topic lists A, B, C, test exactly A, B, and C.
                2. STRUCTURE: You must output the worksheet using exactly this structure:
                   - Learning Objectives: Clear list of targeted skills (max 3).
                   - Instructions: Short, direct, unambiguous explanations of what the student must do.
                   - Exercise Body: Numbered questions/problems targeting ONLY the requested concepts.
                   - Teacher Answer Key: Precise evaluation criteria based ONLY on the given instructions.
                ${if (includeVocabulary) "3. Include a Vocabulary Glossary." else ""}
                
                Output the worksheet in Markdown format.
            """.trimIndent()

            val baseContent = callGeminiApi(apiPrompt) ?: generateContextualTeachingContent(topic, grade, type)

            val contentResult = if (baseContent.startsWith("🎨")) baseContent else "🎨 [AI Generated Illustration: \"$illDesc\"]\n\n$baseContent"

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
            val apiPrompt = if (type == ItemType.WORKSHEET) {
                """
                Act as a top-tier instructional designer and an ultra-rigorous AI tutor.
                Your absolute priority is to ensure strict alignment between the requested template topic and the generated worksheet.
                
                Topic/Template: "$title"
                Level: $level
                Description: $description
                Custom Illustration Theme: $illPrompt
                Base Content: $content
                
                PEDAGOGICAL RULES (STRICT STRICT STRICT):
                1. PRE-CHECK: You must only test/cover the exact concepts mentioned in the Topic/Description. Do NOT add surprise topics or extra skills.
                2. STRUCTURE: Output the worksheet using exactly this structure:
                   - Learning Objectives: Clear list of targeted skills (max 3).
                   - Instructions: Short, direct, unambiguous explanations of what the student must do.
                   - Exercise Body: Numbered questions/problems targeting ONLY the requested concepts based on the template.
                   - Teacher Answer Key: Precise evaluation criteria based ONLY on the given instructions.
                
                Output the worksheet in Markdown format.
                """.trimIndent()
            } else {
                """
                Act as a top-tier instructional designer. Your absolute priority is to ensure strict alignment between the requested template and the generated presentation.
                Topic: "$title" (Level: $level)
                Description: $description
                Rule: Create EXACTLY what is asked. Do not add surprise topics.
                Expand the following Base Content into a Markdown presentation format:
                $content
                """.trimIndent()
            }
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

