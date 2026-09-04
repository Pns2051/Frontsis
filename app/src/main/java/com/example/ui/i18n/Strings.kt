package com.example.ui.i18n

object Strings {
    fun appName(lang: String): String = if (lang == "bn") "বন্ধু AI" else "Bondhu AI"
    fun tagline(lang: String): String = if (lang == "bn") "বাংলাদেশের নিজস্ব প্রথম AI" else "Bangladesh's first own AI"
    fun welcomeGreeting(lang: String): String = if (lang == "bn") "আজ আপনার জন্য কী বানাতে পারি?" else "What can I build for you?"
    fun welcomeToast(lang: String): String = if (lang == "bn") "স্বাগতম, বন্ধু! 👋" else "Welcome to Bondhu AI"

    fun typingNormal(lang: String): String = if (lang == "bn") "বন্ধু চিন্তা করছে…" else "Thinking…"
    fun wakingUp(lang: String): String = if (lang == "bn") "বন্ধু জেগে উঠছে… এক মিনিট পর্যন্ত সময় লাগতে পারে ☕" else "Waking up server… this can take up to a minute ☕"

    fun composerPlaceholder(lang: String): String = if (lang == "bn") "আজ আপনাকে কীভাবে সাহায্য করতে পারি?" else "How can I help you today?"
    fun fileUploadComingSoon(lang: String): String = if (lang == "bn") "📎 ফাইল আপলোড শীঘ্রই আসছে!" else "📎 Attachments coming soon"

    fun newChat(lang: String): String = if (lang == "bn") "নতুন চ্যাট" else "New Chat"
    fun historyTitle(lang: String): String = if (lang == "bn") "কথোপকথনের ইতিহাস" else "Chat History"
    fun noHistory(lang: String): String = if (lang == "bn") "এখনও কোনো অতীত চ্যাট নেই" else "No past chats yet"
    fun messagesCount(count: Int, lang: String): String {
        return if (lang == "bn") {
            "${toBanglaDigits(count)}টি মেসেজ"
        } else {
            "$count messages"
        }
    }

    fun deleteConfirmTitle(lang: String): String = if (lang == "bn") "চ্যাট মুছবেন?" else "Delete chat?"
    fun deleteConfirmMessage(lang: String): String = if (lang == "bn") "এই আলাপটি মুছে ফেলতে চান? এটি আর ফিরিয়ে আনা যাবে না।" else "Are you sure you want to delete this chat? This cannot be undone."
    fun deleteButton(lang: String): String = if (lang == "bn") "মুছে ফেলুন" else "Delete"
    fun cancelButton(lang: String): String = if (lang == "bn") "বাতিল" else "Cancel"

    fun copy(lang: String): String = if (lang == "bn") "কপি করুন" else "Copy"
    fun copied(lang: String): String = if (lang == "bn") "কপি করা হয়েছে!" else "Copied to clipboard!"
    fun sendAgain(lang: String): String = if (lang == "bn") "আবার পাঠান" else "Send again"
    fun stopGenerating(lang: String): String = if (lang == "bn") "বন্ধ করুন" else "Stop"

    fun speak(lang: String): String = if (lang == "bn") "শুনুন" else "Read Aloud"
    fun stopSpeaking(lang: String): String = if (lang == "bn") "পড়া থামান" else "Stop"
    fun share(lang: String): String = if (lang == "bn") "শেয়ার করুন" else "Share"
    fun shareConversation(lang: String): String = if (lang == "bn") "পুরো আলাপ শেয়ার করুন" else "Share Conversation"
    fun regenerate(lang: String): String = if (lang == "bn") "পুনরায় তৈরি করুন" else "Regenerate"
    fun voiceInput(lang: String): String = if (lang == "bn") "কথা বলুন" else "Voice Input"
    fun listening(lang: String): String = if (lang == "bn") "শুনছি..." else "Listening..."
    fun speechNotAvailable(lang: String): String = if (lang == "bn") "ভয়েস ইনপুট সমর্থিত নয়" else "Voice input not supported"

    fun categoryAll(lang: String): String = if (lang == "bn") "সব" else "All"
    fun categoryCoding(lang: String): String = if (lang == "bn") "প্রোগ্রামিং" else "Coding"
    fun categoryBengali(lang: String): String = if (lang == "bn") "বাংলাদেশ ও সংস্কৃতি" else "Bangla & Culture"
    fun categoryWriting(lang: String): String = if (lang == "bn") "লেখালেখি" else "Writing"
    fun categoryLearning(lang: String): String = if (lang == "bn") "জ্ঞান ও শিক্ষা" else "Learning"

    fun selectLanguage(lang: String): String = if (lang == "bn") "ভাষা নির্বাচন করুন" else "Select Language"
    fun confirm(lang: String): String = if (lang == "bn") "শুরু করুন" else "Get Started"

    fun settingsTitle(lang: String): String = if (lang == "bn") "সেটিংস" else "Settings"
    fun languageLabel(lang: String): String = if (lang == "bn") "ভাষা" else "Language"
    fun darkModeLabel(lang: String): String = if (lang == "bn") "ডার্ক মোড" else "Dark Mode"
    fun creditsLabel(lang: String): String = if (lang == "bn") "ক্রেডিট" else "Credits"
    fun creditsExplainer(lang: String): String = if (lang == "bn") "রোজ রাত ১২টায় (ঢাকার সময়) ১০টি ফ্রি মেসেজ যোগ হয়" else "Every day at midnight (Dhaka time) you get 10 free messages"
    fun aboutTitle(lang: String): String = if (lang == "bn") "বন্ধু AI সম্পর্কে" else "About Bondhu AI"
    fun aboutStory(lang: String): String = if (lang == "bn") {
        "বাংলাদেশের নিজস্ব প্রথম AI। তৈরি বাংলাদেশে, বাংলাদেশের জন্য — বাংলা, ইংরেজি এবং বাংলিশে পারদর্শী।\nসংস্করণ ১.০"
    } else {
        "Bangladesh's first own AI. Made in Bangladesh, for Bangladesh — speaking Bangla, English, and Banglish.\nVersion 1.0"
    }
    fun shareApp(lang: String): String = if (lang == "bn") "অ্যাপ শেয়ার করুন" else "Share App"
    fun shareText(lang: String): String = if (lang == "bn") "বাংলাদেশের নিজস্ব প্রথম AI — বন্ধু AI ব্যবহার করে দেখুন: https://bondhu-ai-backed-beta26.onrender.com" else "Check out Bondhu AI — Bangladesh's first own AI: https://bondhu-ai-backed-beta26.onrender.com"

    fun restrictedTitle(lang: String): String = if (lang == "bn") "অ্যাকাউন্ট সীমিত" else "Account Restricted"
    fun restrictedMessage(lang: String): String = if (lang == "bn") "আপনার অ্যাকাউন্টটি সাময়িকভাবে সীমিত করা হয়েছে। সহায়তার জন্য যোগাযোগ করুন।" else "Your account has been temporarily restricted. Please contact support."

    // Suggestions chips (always 4)
    fun suggestions(lang: String): List<String> = listOf(
        "Write a poem about Dhaka",
        "Explain physics simply",
        "রান্নার রেসিপি দাও",
        "Help me with English grammar"
    )

    // Error messages
    fun error400(lang: String): String = if (lang == "bn") "কিছু ভুল হয়েছে — আবার চেষ্টা করুন" else "Something went wrong — please try again"
    fun error402(lang: String): String = if (lang == "bn") "ফ্রি মেসেজ শেষ! রাত ১২টায় আরও ১০টি যোগ হবে 🌙" else "Out of free messages — +10 more at midnight 🌙"
    fun error403(lang: String): String = if (lang == "bn") "আপনার অ্যাকাউন্ট সীমিত হয়েছে" else "Access restricted"
    fun error429(lang: String): String = if (lang == "bn") "একটু দ্রুত বার্তা পাঠাচ্ছেন! এক মিনিট অপেক্ষা করুন" else "Sending too fast! Wait a minute"
    fun error503(lang: String): String = if (lang == "bn") "বন্ধু এখন ব্যস্ত — একটু পরে আবার চেষ্টা করুন" else "Bondhu is busy right now — try again soon"
    fun errorGeneral(lang: String): String = if (lang == "bn") "সংযোগ বিচ্ছিন্ন হয়েছে — আবার চেষ্টা করুন" else "Connection lost — please try again"
    fun charCountLimit(current: Int, max: Int, lang: String): String = "${toBanglaDigitsIfBn(current, lang)} / ${toBanglaDigitsIfBn(max, lang)}"

    fun formatRelativeTime(dateStr: String?, lang: String): String {
        if (dateStr.isNullOrBlank()) return ""
        try {
            // Parses ISO date or timestamps
            val millis: Long = try {
                java.time.Instant.parse(dateStr).toEpochMilli()
            } catch (e: Exception) {
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
                } catch (e2: Exception) {
                    System.currentTimeMillis()
                }
            }
            val diff = System.currentTimeMillis() - millis
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            return if (lang == "bn") {
                when {
                    minutes < 1 -> "এইমাত্র"
                    minutes < 60 -> "${toBanglaDigits(minutes.toInt())} মিনিট আগে"
                    hours < 24 -> "${toBanglaDigits(hours.toInt())} ঘণ্টা আগে"
                    days < 7 -> "${toBanglaDigits(days.toInt())} দিন আগে"
                    else -> "কিছুদিন আগে"
                }
            } else {
                when {
                    minutes < 1 -> "just now"
                    minutes < 60 -> "${minutes}m ago"
                    hours < 24 -> "${hours}h ago"
                    days < 7 -> "${days}d ago"
                    else -> "days ago"
                }
            }
        } catch (e: Exception) {
            return ""
        }
    }

    fun toBanglaDigits(num: Int): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val str = num.toString()
        val sb = java.lang.StringBuilder()
        for (c in str) {
            if (c in '0'..'9') {
                sb.append(bnDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun toBanglaDigitsIfBn(num: Int, lang: String): String {
        return if (lang == "bn") toBanglaDigits(num) else num.toString()
    }
}
