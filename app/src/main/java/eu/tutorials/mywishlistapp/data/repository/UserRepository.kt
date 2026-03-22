package eu.tutorials.mywishlistapp.data.repository

import eu.tutorials.mywishlistapp.data.local.dao.UserDao
import eu.tutorials.mywishlistapp.data.local.entity.UserEntity
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    suspend fun register(username: String, password: String): Result<UserEntity> {
        val existing = userDao.getUserByUsername(username)
        if (existing != null) return Result.failure(Exception("Користувач вже існує"))
        val user = UserEntity(username = username, passwordHash = hashPassword(password))
        val id = userDao.insertUser(user)
        return Result.success(user.copy(id = id.toInt()))
    }

    suspend fun login(username: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByUsername(username)
            ?: return Result.failure(Exception("Користувача не знайдено"))
        if (user.passwordHash != hashPassword(password))
            return Result.failure(Exception("Невірний пароль"))
        return Result.success(user)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}