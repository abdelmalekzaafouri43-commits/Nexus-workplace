package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.MainViewModel
import com.example.ui.components.A4PreviewDialog
import com.example.ui.components.Sidebar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
    val promptText by viewModel.promptText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val previewItem by viewModel.previewItem.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearToast()
            }
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.85f)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentTab,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSidebar() }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    "Dashboard" -> DashboardScreen(
                        promptText = promptText,
                        isGenerating = isGenerating,
                        libraryItems = libraryItems,
                        onPromptChange = { viewModel.setPromptText(it) },
                        onSubmitPrompt = {
                            viewModel.generateFromPrompt { item -> }
                        },
                        onQuickAction = { prompt ->
                            viewModel.setPromptText(prompt)
                            viewModel.generateFromPrompt {}
                        },
                        onNavigate = { tab -> viewModel.setTab(tab) }
                    )
                    "Worksheet Generator" -> WorksheetScreen(
                        isGenerating = isGenerating,
                        onGenerate = { topic, grade, type, format, answerKey, vocabulary, count, illPrompt ->
                            viewModel.createWorksheet(topic, grade, type, format, answerKey, vocabulary, count, illPrompt)
                        }
                    )
                    "PowerPoint Generator" -> PptScreen(
                        isGenerating = isGenerating,
                        onGenerate = { topic, slides, style, format, speakerNotes, interactiveQnA, illPrompt ->
                            viewModel.createPresentation(topic, slides, style, format, speakerNotes, interactiveQnA, illPrompt)
                        }
                    )
                    "Templates Gallery" -> TemplatesGalleryScreen(
                        onUseTemplate = { title, type, level, format, description, illPrompt, content ->
                            viewModel.useTemplate(title, type, level, format, description, illPrompt, content)
                        }
                    )
                    "My Library" -> LibraryScreen(
                        items = libraryItems,
                        onDelete = { id -> viewModel.deleteItem(id) },
                        onSave = { item -> viewModel.saveItem(item) },
                        onShare = { item -> viewModel.shareItem(item) },
                        previewItem = previewItem,
                        onPreviewDismiss = { viewModel.setPreviewItem(null) },
                        onSelectItem = { item -> viewModel.setPreviewItem(item) }
                    )
                }
            }
        }

        if (previewItem != null && currentTab != "My Library") {
            A4PreviewDialog(
                item = previewItem!!,
                onDismiss = { viewModel.setPreviewItem(null) },
                onSave = { viewModel.saveItem(it) },
                onShare = { viewModel.shareItem(it) }
            )
        }

        if (isSidebarOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.toggleSidebar() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .clickable(enabled = false) {}
                ) {
                    Sidebar(
                        currentTab = currentTab,
                        currentTheme = themeMode,
                        isDarkMode = isDarkMode,
                        onTabSelected = { tab -> viewModel.setTab(tab) },
                        onThemeSelected = { theme -> viewModel.setTheme(theme) },
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onClose = { viewModel.toggleSidebar() }
                    )
                }
            }
        }
    }
}
