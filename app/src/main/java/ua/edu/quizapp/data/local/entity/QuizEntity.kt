package ua.edu.quizapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: String? = null,
    /** null = СЃРїС–Р»СЊРЅРёР№ (Supabase, РґРµРјРѕ); С–РЅР°РєС€Рµ Р»РёС€Рµ С†РµР№ Р»РѕРєР°Р»СЊРЅРёР№ РєРѕСЂРёСЃС‚СѓРІР°С‡ */
    val ownerUserId: Int? = null,
    val title: String,
    val description: String,
    val category: String = "Р—Р°РіР°Р»СЊРЅРµ",
    val source: String = "LOCAL",
    val createdAt: Long = System.currentTimeMillis()
)
