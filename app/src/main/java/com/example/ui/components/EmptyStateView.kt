package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.i18n.Strings
import com.example.ui.theme.BengalGreen
import com.example.ui.theme.BengalGreenBright
import com.example.ui.theme.SunriseRed
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyStateView(
    language: String,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val headingAlpha = remember { Animatable(0f) }
    val headingOffset = remember { Animatable(16f) }
    val scrollState = rememberScrollState()

    // Halo pulse around the brand badge
    val transition = rememberInfiniteTransition(label = "badge_halo")
    val haloPulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )
    val haloAlpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    LaunchedEffect(Unit) {
        delay(60)
        headingAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(60)
        headingOffset.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("all") }

    val categories = listOf(
        "all" to Strings.categoryAll(language),
        "coding" to Strings.categoryCoding(language),
        "bengali" to Strings.categoryBengali(language),
        "writing" to Strings.categoryWriting(language),
        "learning" to Strings.categoryLearning(language)
    )

    val suggestions = when (selectedCategory) {
        "coding" -> if (language == "bn") {
            listOf(
                "একটি রেসপন্সিভ কম্পোনেন্ট লিখে দাও",
                "Python দিয়ে API কল করার কোড দাও",
                "কোড অপটিমাইজ ও বাগ ফিক্স করো",
                "SQL ডেটাবেস ডিজাইন শেখাও"
            )
        } else {
            listOf(
                "Write a clean Jetpack Compose UI",
                "Debug this Python API call",
                "Explain REST vs GraphQL",
                "Write a regex for email validation"
            )
        }
        "bengali" -> if (language == "bn") {
            listOf(
                "বাংলাদেশের ইতিহাস সংক্ষেপে বলো",
                "রবীন্দ্রনাথ ও নজরুলের সৃষ্টি নিয়ে লিখো",
                "ঢাকার সেরা ঐতিহ্যবাহী খাবার কী?",
                "সুন্দরবন সম্পর্কে কিছু তথ্য দাও"
            )
        } else {
            listOf(
                "Brief history of Bangladesh",
                "Beauty of Bengali literature",
                "Famous traditional foods of Dhaka",
                "Fascinating facts about Sundarbans"
            )
        }
        "writing" -> if (language == "bn") {
            listOf(
                "বৃষ্টিভেজা ঢাকা নিয়ে একটি কবিতা",
                "ছুটির আবেদনের জন্য একটি ফর্মাল ইমেইল",
                "একটি রোমাঞ্চকর ছোট গল্পের প্লট",
                "একটি স্টার্টআপ প্রপোজাল ড্রাফট করো"
            )
        } else {
            listOf(
                "Write a poem about rainy evening",
                "Draft a formal leave request email",
                "Outline a mystery thriller story",
                "Create a startup pitch outline"
            )
        }
        "learning" -> if (language == "bn") {
            listOf(
                "কোয়ান্টাম কম্পিউটিং সহজ বাংলায় বুঝাও",
                "কৃত্রিম বুদ্ধিমত্তা কীভাবে কাজ করে?",
                "সময় ব্যবস্থাপনার ৫টি দারুণ কৌশল",
                "কৃষ্ণগহ্বর (Black Hole) কী?"
            )
        } else {
            listOf(
                "Explain Quantum Computing simply",
                "How do Large Language Models work?",
                "5 proven time management tips",
                "What happens inside a Black Hole?"
            )
        }
        else -> if (language == "bn") {
            listOf(
                "একটি প্রজেক্ট প্ল্যান করো",
                "কোড কীভাবে কাজ করে বুঝাও",
                "বাংলায় কবিতা লিখো",
                "একটি ব্যবসায়িক ইমেইল ড্রাফট করো"
            )
        } else {
            listOf(
                "Plan a project",
                "Explain how code works",
                "Draft a proposal",
                "Summarize text"
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("empty_state_view"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon Squircle Badge with gentle halo glow
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Ambient glow halo
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(haloPulse)
                        .alpha(haloAlpha)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(BengalGreenBright.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )

                // App Icon Squircle Badge
                Image(
                    painter = painterResource(id = R.drawable.img_app_logo),
                    contentDescription = "Bondhu App Logo",
                    modifier = Modifier
                        .size(58.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.25f))
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Editorial Serif Heading: "What can I build for you?"
            Text(
                text = Strings.welcomeGreeting(language),
                fontSize = 25.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                letterSpacing = 0.2.sp,
                lineHeight = 32.sp,
                modifier = Modifier
                    .offset(y = headingOffset.value.dp)
                    .alpha(headingAlpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category Filter Row
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = headingOffset.value.dp)
                    .alpha(headingAlpha.value),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (catKey, catLabel) ->
                    val isSelected = selectedCategory == catKey
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) BengalGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) BengalGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedCategory = catKey }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = catLabel,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clean Minimal Suggestion Pills with interactive press feel
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = headingOffset.value.dp)
                    .alpha(headingAlpha.value),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                suggestions.forEachIndexed { index, prompt ->
                    SuggestionPill(
                        prompt = prompt,
                        onClick = { onSuggestionClick(prompt) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionPill(
    prompt: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scaleAnim.animateTo(0.95f, tween(80))
        } else {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .scale(scaleAnim.value)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isPressed) BengalGreen.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = 1.dp,
                color = if (isPressed) BengalGreenBright.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 15.dp, vertical = 9.dp)
    ) {
        Text(
            text = prompt,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isPressed) BengalGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
