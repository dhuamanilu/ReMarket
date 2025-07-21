package com.example.remarket.data.repository

import com.example.remarket.data.model.Report
import com.example.remarket.data.model.toDomain
import com.example.remarket.data.network.ApiService
import com.example.remarket.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val api: ApiService
) : IReportRepository {

    override fun getReports(): Flow<Resource<List<Report>>> = flow {
        emit(Resource.Loading)
        try {
            val list = api.getReports().map { it.toDomain() }
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(parseError(e)))
        }
    }

    override suspend fun deleteReport(reportId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                api.deleteReport(reportId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(parseError(e))
            }
        }

    private fun parseError(e: Exception) = when (e) {
        is HttpException -> "Error ${e.code()}"
        is IOException   -> "Sin conexión"
        else             -> e.message ?: "Error desconocido"
    }
}
