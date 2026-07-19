package com.example.ataraxia.features.focus.domain

import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFocusIntentionsUseCase(private val repository: FocusRepository) {
    val predefinedIntentions = listOf(
        FocusIntentionEntity("Study", "🎓", "#B9A7D6", "Dedicated time to academic learning.", false),
        FocusIntentionEntity("Programming", "💻", "#6F6CA8", "Coding, debugging, and building software.", false),
        FocusIntentionEntity("Reading", "📖", "#C6D5C0", "Deep focus reading and comprehension.", false),
        FocusIntentionEntity("Writing", "✍️", "#C7D9F1", "Creative writing, scripting, or blogging.", false),
        FocusIntentionEntity("Meditation", "🧘", "#EBC9D8", "Mindfulness and centering session.", false),
        FocusIntentionEntity("Creative Work", "🎨", "#D99EA5", "Design, drawing, music, or crafting.", false),
        FocusIntentionEntity("Homework", "📝", "#B9A7D6", "Completing assignments and tasks.", false),
        FocusIntentionEntity("Meetings", "🤝", "#6F6CA8", "Collaborative work or discussions.", false),
        FocusIntentionEntity("Personal Projects", "🚀", "#C6D5C0", "Building and exploring passion ideas.", false),
        FocusIntentionEntity("Other", "✨", "#EBC9D8", "Centering on any other focal task.", false)
    )

    operator fun invoke(): Flow<List<FocusIntentionEntity>> {
        return repository.getAllCustomIntentions().map { customList ->
            predefinedIntentions + customList
        }
    }
}
