package ua.edu.quizapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quizId")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val quizId: Int,
    val text: String,
    val optionsJson: String,    // JSON: ["РІР°СЂС–Р°РЅС‚1","РІР°СЂС–Р°РЅС‚2","РІР°СЂС–Р°РЅС‚3","РІР°СЂС–Р°РЅС‚4"]
    val correctIndex: Int,
    val explanation: String = ""
)
