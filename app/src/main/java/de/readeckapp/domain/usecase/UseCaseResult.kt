package de.readeckapp.domain.usecase

sealed class UseCaseResult<out DataType : Any> {
    data class Success<out DataType : Any>(val  dataType: DataType) : UseCaseResult<DataType>()
    data class Error(val exception: Throwable) : UseCaseResult<Nothing>()
}
