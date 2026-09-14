package com.example.ui.i18n

object Strings {
    // App & Tagline
    fun appName(lang: String): String = "বন্ধু·AI"
    fun splashSub(lang: String): String = if (lang == "bn") "বাংলাদেশের নিজের AI" else "Bangladesh's own AI"

    // Models
    fun modelLight(lang: String): String = if (lang == "bn") "Bondhu Light" else "Bondhu Light"
    fun modelLightBadge(lang: String): String = if (lang == "bn") "ডিফল্ট" else "Default"
    fun modelLightDesc(lang: String): String = if (lang == "bn") "দ্রুত — দৈনন্দিন কথা" else "Fast — for everyday chat"

    fun modelReasoning(lang: String): String = if (lang == "bn") "Bondhu 5.3 Reasoning" else "Bondhu 5.3 Reasoning"
    fun modelReasoningBadge(lang: String): String = if (lang == "bn") "অ্যাডভান্সড" else "Advanced"
    fun modelReasoningDesc(lang: String): String = if (lang == "bn") "গভীর চিন্তা — গণিত, কোড, বিশ্লেষণ" else "Deep thinking — math, code, analysis"

    fun selectModel(lang: String): String = if (lang == "bn") "Select AI Model" else "Select AI Model"

    // Onboarding 4 Steps
    fun onboardingStep1Title(lang: String): String = if (lang == "bn") "ভাষা বেছে নাও" else "Choose your language"
    fun onboardingStep1Sub(lang: String): String = if (lang == "bn") "তোমার পছন্দের ভাষা নির্বাচন করো" else "Select your preferred language"

    fun onboardingStep2Title(lang: String): String = if (lang == "bn") "তোমার নাম কী?" else "What's your name?"
    fun onboardingStep2Placeholder(lang: String): String = if (lang == "bn") "তোমার নাম" else "Your name"
    fun onboardingStep2Validation(lang: String): String = if (lang == "bn") "অনুগ্রহ করে তোমার নাম লেখো" else "Please enter your name"

    fun onboardingStep3Title(lang: String): String = if (lang == "bn") "শুরু করো" else "Let's get started"
    fun googleSignIn(lang: String): String = "Continue with Google"
    fun continueWithEmail(lang: String): String = if (lang == "bn") "ইমেইল দিয়ে প্রবেশ করো" else "Continue with Email"
    fun emailLabel(lang: String): String = if (lang == "bn") "ইমেইল ঠিকানা" else "Email address"
    fun passwordLabel(lang: String): String = if (lang == "bn") "পাসওয়ার্ড" else "Password"
    fun signIn(lang: String): String = if (lang == "bn") "সাইন ইন" else "Sign In"
    fun signUp(lang: String): String = if (lang == "bn") "নতুন অ্যাকাউন্ট তৈরি" else "Create Account"
    fun orDivider(lang: String): String = if (lang == "bn") "অথবা" else "or"
    fun continueAsGuest(lang: String): String = if (lang == "bn") "Continue as guest" else "Continue as guest"
    fun guestModeDisclaimer(lang: String): String = if (lang == "bn") "Guest mode: credits & history stored on this device" else "Guest mode: credits & history stored on this device"

    fun onboardingStep4Title(lang: String): String = if (lang == "bn") "শেষ একটা ধাপ" else "One last step"
    fun onboardingStep4Sub(lang: String): String = if (lang == "bn") "Accept to continue" else "Accept to continue"
    fun checkboxPrivacy(lang: String): String = if (lang == "bn") "I accept the Privacy Policy" else "I accept the Privacy Policy"
    fun checkboxTerms(lang: String): String = if (lang == "bn") "I accept the Terms of Service" else "I accept the Terms of Service"
    fun getStarted(lang: String): String = if (lang == "bn") "শুরু করো" else "Get Started"
    const val PRIVACY_URL = "https://boundhu-ai.vercel.app/privacy.html"
    const val TERMS_URL = "https://boundhu-ai.vercel.app/terms.html"

    fun next(lang: String): String = if (lang == "bn") "পরবর্তী" else "Next"
    fun back(lang: String): String = if (lang == "bn") "পেছনে" else "Back"

    // Main Chat - Top Bar
    fun creditsTooltip(lang: String): String = if (lang == "bn") "রোজ রাত ১২টায় +১০ রিফিল" else "+10 daily refill at midnight"
    fun newChat(lang: String): String = if (lang == "bn") "নতুন চ্যাট" else "New Chat"

    // Empty State (Strictly Gemini-style & user-requested: "Hi, [name]! I'm Bondhu — your AI friend. Ask away.")
    fun greeting(name: String, lang: String): String {
        val cleanName = name.trim().ifBlank { if (lang == "bn") "Friend" else "Friend" }
        return "Hi, $cleanName!"
    }

    fun emptySub(lang: String): String = "I'm Bondhu — your AI friend. Ask away."

    // 4 Suggestion Chips
    fun suggestions(lang: String): List<String> = listOf(
        "Today's news",
        "Math help",
        "Write a story",
        "Learn English"
    )

    fun suggestionPrompt(chip: String, lang: String): String {
        return if (lang == "bn") {
            when (chip) {
                "Today's news" -> "আজকের গুরুত্বপূর্ণ প্রধান খবর ও সমসাময়িক বিষয়গুলো সংক্ষেপে জানাও।"
                "Math help" -> "গণিতের একটি দরকারি সমস্যা সহজে সমাধানের কৌশল ও উদাহরণ ব্যাখ্যা করো।"
                "Write a story" -> "বাংলার স্নিগ্ধ প্রকৃতি ও নদীর পাড় নিয়ে চমৎকার একটি ছোট গল্প লেখো।"
                "Learn English" -> "দৈনন্দিন কথোপকথনে বহুল ব্যবহৃত ১০টি দরকারি ইংরেজি বাক্য ও অর্থ শেখাও।"
                else -> chip
            }
        } else {
            when (chip) {
                "Today's news" -> "Summarize today's top news and current events briefly."
                "Math help" -> "Explain a helpful math problem-solving technique with a clear example."
                "Write a story" -> "Write an inspiring short creative story about nature and exploration."
                "Learn English" -> "Share 10 useful conversational English phrases with practical examples."
                else -> chip
            }
        }
    }

    // Composer
    fun composerPlaceholder(lang: String): String = "Type a message…"
    fun send(lang: String): String = if (lang == "bn") "পাঠাও" else "Send"
    fun stop(lang: String): String = if (lang == "bn") "থামাও" else "Stop"
    fun attachFile(lang: String): String = if (lang == "bn") "ফাইল যুক্ত করো" else "Attach file"
    fun removeAttachment(lang: String): String = if (lang == "bn") "মুছে ফেলো" else "Remove"
    fun fileSizeLimit(lang: String): String = if (lang == "bn") "ফাইলের সাইজ ৫MB এর বেশি হতে পারবে না" else "File must be under 5MB"

    // Streaming & Typing
    fun typing(lang: String): String = "Bondhu is thinking…"
    fun thinking(lang: String): String = "Bondhu is thinking…"
    fun thinkingProcess(lang: String): String = "Thinking Process"
    fun thinkingDone(lang: String): String = "Thinking complete"
    fun generating(lang: String): String = "Writing…"
    fun coldStart(lang: String): String = "Bondhu is waking up… ☕"

    // Drawer
    fun history(lang: String): String = "History"
    fun noHistory(lang: String): String = "No chats yet"
    fun guest(lang: String): String = "Guest"
    fun deleteChatTitle(lang: String): String = if (lang == "bn") "চ্যাট মুছবেন?" else "Delete chat?"
    fun deleteChatMsg(lang: String): String = if (lang == "bn") "এই কথোপকথনটি মুছে ফেলা হবে।" else "This conversation will be permanently deleted."
    fun cancel(lang: String): String = if (lang == "bn") "বাতিল" else "Cancel"
    fun delete(lang: String): String = if (lang == "bn") "মুছুন" else "Delete"

    // Settings
    fun settings(lang: String): String = "Settings"
    fun account(lang: String): String = "ACCOUNT"
    fun signInMode(lang: String): String = "Sign-in mode"
    fun connectGoogle(lang: String): String = "Connect Google"
    fun nameLabel(lang: String): String = "Name"
    fun save(lang: String): String = if (lang == "bn") "সংরক্ষণ" else "Save"
    fun saved(lang: String): String = if (lang == "bn") "সংরক্ষিত হয়েছে" else "Saved"
    fun profileDesc(lang: String): String = "Profile description"
    fun profileDescPlaceholder(lang: String): String = "Tell Bondhu how you'd like to be addressed..."

    fun aiModel(lang: String): String = "AI MODEL"
    fun appearance(lang: String): String = "APPEARANCE"
    fun theme(lang: String): String = "Theme"
    fun themeLight(lang: String): String = "☀️ Light"
    fun themeDark(lang: String): String = "🌙 Dark"
    fun font(lang: String): String = "Font"
    fun fontSmall(lang: String): String = "Small"
    fun fontNormal(lang: String): String = "Normal"
    fun fontLarge(lang: String): String = "Large"
    fun animations(lang: String): String = "Animations"
    fun animationsOn(lang: String): String = "On"
    fun animationsOff(lang: String): String = "Off"

    fun customApi(lang: String): String = "CUSTOM API"
    fun customApiNote(lang: String): String = "Your key stays on your device only"
    fun endpointLabel(lang: String): String = "Endpoint"
    fun apiKeyLabel(lang: String): String = "API Key"
    fun testButton(lang: String): String = "Test"
    fun removeKeyButton(lang: String): String = "Remove"

    fun legal(lang: String): String = "LEGAL"
    fun privacyPolicy(lang: String): String = "Privacy Policy"
    fun termsOfService(lang: String): String = "Terms of Service"
    fun logoutReset(lang: String): String = "Log out / Reset"

    // Markdown Copy
    fun copy(lang: String): String = "Copy"
    fun copied(lang: String): String = "Copied"

    // Error messages
    fun errorGeneral(lang: String): String = errorGeneric(lang)
    fun apiConfigSaved(lang: String): String = if (lang == "bn") "API কনফিগারেশন সংরক্ষিত হয়েছে" else "API configuration saved"

    fun formatHttpError(err: String, lang: String): String {
        val lower = err.lowercase()
        return when {
            lower.contains("cancel") || lower.contains("standalone") -> ""
            lower.contains("402") || lower.contains("credit") -> errorOutOfCredits(lang)
            lower.contains("429") || lower.contains("rate") -> errorRateLimit(lang)
            lower.contains("503") || lower.contains("busy") -> errorServiceBusy(lang)
            lower.contains("timeout") || lower.contains("timed out") -> if (lang == "bn") "অনুরোধের সময় শেষ হয়ে গেছে। অনুগ্রহ করে আবার চেষ্টা করো।" else "Request timed out. Please try again."
            lower.contains("network") || lower.contains("connect") || lower.contains("unknownhost") -> if (lang == "bn") "ইন্টারনেট সংযোগে সমস্যা হয়েছে। সংযোগ যাচাই করে আবার চেষ্টা করো।" else "Network error. Please check your connection."
            else -> if (err.isNotBlank() && !err.startsWith("HTTP_") && !err.contains("Exception")) err else errorGeneric(lang)
        }
    }

    fun errorOutOfCredits(lang: String): String = "আজকের ফ্রি ক্রেডিট শেষ হয়ে গেছে। রাত ১২টায় আবার ১০টি ক্রেডিট রিফিল হবে।"
    fun errorRateLimit(lang: String): String = "একটু দ্রুত বেশি অনুরোধ পাঠানো হয়েছে। কয়েক সেকেন্ড পর আবার চেষ্টা করো।"
    fun errorServiceBusy(lang: String): String = "সার্ভার এই মুহূর্তে ব্যস্ত আছে। অনুগ্রহ করে কিছুক্ষণ পর চেষ্টা করো।"
    fun errorGeneric(lang: String): String = "দুঃখিত, সংযোগে সমস্যা হয়েছে। আবার চেষ্টা করো।"
    fun retry(lang: String): String = "পুনরায় চেষ্টা"
}
