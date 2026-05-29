package vn.edu.hcmute.minlish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_progress")
data class StudyProgress(
    @PrimaryKey(autoGenerate = true)
    val progressId: Int = 0,

    val date: String,
    val newWordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0
)