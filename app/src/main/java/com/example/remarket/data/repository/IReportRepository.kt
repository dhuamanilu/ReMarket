package com.example.remarket.data.repository

import com.example.remarket.data.model.Report
import com.example.remarket.util.Resource
import kotlinx.coroutines.flow.Flow

interface IReportRepository {
    fun getReports(): Flow<Resource<List<Report>>>
    suspend fun deleteReport(reportId: String): Resource<Unit>
}
