package com.example.ataraxia.data.local

object DailyPrompts {
    private val prompts = listOf(
        "What did your mind linger on today? What was a moment of quiet?",
        "Describe a pleasant sensation you felt today (e.g. warmth, soft wind, tasty tea).",
        "If today was a color, what color would it be and why?",
        "Name one thing you are ready to let go of before you sleep tonight.",
        "What is a small, quiet victory you achieved today?",
        "Who or what brought a subtle smile to your face today?",
        "Write down three things your senses noticed in your environment just now."
    )

    fun getTodayPrompt(): String {
        return prompts.random()
    }
}
