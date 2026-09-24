package com.clearspend.data.db

import androidx.room.TypeConverter
import com.clearspend.domain.model.*
import java.time.LocalDate

class Converters {
    @TypeConverter fun fromLocalDate(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter fun fromCategory(cat: Category): String = cat.name
    @TypeConverter fun toCategory(value: String): Category = runCatching { Category.valueOf(value) }.getOrDefault(Category.OTHER)

    @TypeConverter fun fromSource(source: TransactionSource): String = source.name
    @TypeConverter fun toSource(value: String): TransactionSource = runCatching { TransactionSource.valueOf(value) }.getOrDefault(TransactionSource.MANUAL)

    @TypeConverter fun fromReviewStatus(status: ReviewStatus): String = status.name
    @TypeConverter fun toReviewStatus(value: String): ReviewStatus = runCatching { ReviewStatus.valueOf(value) }.getOrDefault(ReviewStatus.APPROVED)

    @TypeConverter fun fromCardNetwork(network: CardNetwork): String = network.name
    @TypeConverter fun toCardNetwork(value: String): CardNetwork = runCatching { CardNetwork.valueOf(value) }.getOrDefault(CardNetwork.VISA)

    @TypeConverter fun fromAgreementRouting(routing: AgreementRouting): String = routing.name
    @TypeConverter fun toAgreementRouting(value: String): AgreementRouting = runCatching { AgreementRouting.valueOf(value) }.getOrDefault(AgreementRouting.AUTO_APPROVE)

    @TypeConverter fun fromAuditEventType(type: AuditEventType): String = type.name
    @TypeConverter fun toAuditEventType(value: String): AuditEventType = runCatching { AuditEventType.valueOf(value) }.getOrDefault(AuditEventType.TRANSACTION_REVIEWED)
}
