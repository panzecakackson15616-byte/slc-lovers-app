package com.slclovers.app.data

import androidx.room.TypeConverter
import com.slclovers.app.data.model.Mood
import com.slclovers.app.data.model.MessageType
import com.slclovers.app.data.model.NoteColor
import com.slclovers.app.data.model.PairingStatus
import com.slclovers.app.data.model.UserRole

/**
 * Room 类型转换器
 */
class Converters {
    @TypeConverter fun userRoleToString(role: UserRole?): String? = role?.name
    @TypeConverter fun stringToUserRole(value: String?): UserRole? = value?.let { UserRole.valueOf(it) }

    @TypeConverter fun moodToString(mood: Mood): String = mood.name
    @TypeConverter fun stringToMood(value: String): Mood = Mood.valueOf(value)

    @TypeConverter fun messageTypeToString(type: MessageType): String = type.name
    @TypeConverter fun stringToMessageType(value: String): MessageType = MessageType.valueOf(value)

    @TypeConverter fun pairingStatusToString(status: PairingStatus): String = status.name
    @TypeConverter fun stringToPairingStatus(value: String): PairingStatus = PairingStatus.valueOf(value)

    @TypeConverter fun noteColorToString(color: NoteColor): String = color.name
    @TypeConverter fun stringToNoteColor(value: String): NoteColor = NoteColor.valueOf(value)
}