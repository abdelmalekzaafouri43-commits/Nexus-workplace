package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ItemType
import com.example.ui.components.AnimatedButton
import com.example.ui.components.GlassCard

data class TeachingTemplate(
    val title: String,
    val type: ItemType,
    val level: String,
    val format: String,
    val description: String,
    val illustrationPrompt: String,
    val sampleContent: String
)

@Composable
fun TemplatesGalleryScreen(
    onUseTemplate: (title: String, type: ItemType, level: String, format: String, description: String, illPrompt: String, content: String) -> Unit
) {
    val templates = listOf(
        TeachingTemplate(
            title = "Present Perfect vs Past Simple Masterclass",
            type = ItemType.WORKSHEET,
            level = "B1 Intermediate",
            format = "pdf",
            description = "Comprehensive gap-fill exercises, timeline charts, and error-correction tasks.",
            illustrationPrompt = "Grammar timeline chart with dialogue bubbles",
            sampleContent = "English A4 Worksheet: Present Perfect vs Past Simple. Level: B1 Intermediate. Exercises: Gap-fills, timeline analysis, and teacher answer key included."
        ),
        TeachingTemplate(
            title = "Business English Email Etiquette & Tone",
            type = ItemType.WORKSHEET,
            level = "B2 Upper-Intermediate",
            format = "docx",
            description = "Formal sign-offs, polite requests, and professional vocabulary matching exercises.",
            illustrationPrompt = "Professional workplace and formal correspondence icon",
            sampleContent = "English A4 Worksheet: Business Email Etiquette. Level: B2 Upper-Intermediate. Formal sign-offs, polite requests, and professional tone rewriting tasks."
        ),
        TeachingTemplate(
            title = "Exam Speaking Part 2: Travel & Culture",
            type = ItemType.POWERPOINT,
            level = "C1 Advanced",
            format = "pptx",
            description = "8-slide interactive deck with cue cards, advanced vocabulary, and band score rubric.",
            illustrationPrompt = "Global travel imagery and speaking cue card graphic",
            sampleContent = "English Presentation Deck: Exam Speaking Part 2. Total Slides: 8. Includes cue cards, advanced travel vocabulary, and examiner scoring rubric."
        ),
        TeachingTemplate(
            title = "Phonics & Pronunciation: Silent Letters",
            type = ItemType.WORKSHEET,
            level = "A2 Elementary",
            format = "pdf",
            description = "Engaging phonics exercises highlighting silent consonants in common English words.",
            illustrationPrompt = "Playful phonetic alphabet symbols and speaking mouths",
            sampleContent = "English A4 Worksheet: Phonics & Silent Letters. Level: A2 Elementary. Consonant identification, silent letter circles, and pronunciation pairing."
        ),
        TeachingTemplate(
            title = "Tech Innovation & AI Pitch Deck",
            type = ItemType.POWERPOINT,
            level = "B2/C1",
            format = "pptx",
            description = "Interactive presentation slides exploring artificial intelligence vocabulary and debates.",
            illustrationPrompt = "Futuristic AI neural network and glowing data streams",
            sampleContent = "English Presentation Deck: Tech Innovation & AI. Total Slides: 10. AI concepts, ethics debate prompts, and vocabulary glossary."
        ),
        TeachingTemplate(
            title = "Travel Phrasal Verbs & Airport Situations",
            type = ItemType.WORKSHEET,
            level = "B1 Intermediate",
            format = "pdf",
            description = "Roleplay dialogues and matching tasks for check-in, security, and boarding.",
            illustrationPrompt = "Airport terminal boarding gate and travel luggage vector",
            sampleContent = "English A4 Worksheet: Travel Phrasal Verbs. Level: B1 Intermediate. Check-in dialogues, security phrasal verb matching, and roleplay exercises."
        )
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.tertiary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = "Templates Gallery",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select pre-made professional worksheet & presentation layouts for instant use",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(templates) { template ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (template.type == ItemType.WORKSHEET) accentColor.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (template.type == ItemType.WORKSHEET) "A4 Worksheet" else "Presentation Deck",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (template.type == ItemType.WORKSHEET) accentColor else primaryColor
                                    )
                                )
                            }
                            Text(
                                text = template.level,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }

                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            ),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        AnimatedButton(
                            onClick = {
                                onUseTemplate(
                                    template.title,
                                    template.type,
                                    template.level,
                                    template.format,
                                    template.description,
                                    template.illustrationPrompt,
                                    template.sampleContent
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            containerColor = primaryColor
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Template & Generate", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
