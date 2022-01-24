package com.beam.engine.repository

import com.beam.engine.model.Workflow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @author  KK
 * @version 1.0
 */

@Repository
interface WorkflowRepository : CoroutineCrudRepository<Workflow, Long> {

    @Query(
        "select * from grendel.workflow where partner_loan_id = :partnerLoanId and status <> :status " +
                "and updated_at >= :lastOneDay limit 1"
    )
    suspend fun fetchDuplicateWorkflowIfExist(
        partnerLoanId: String,
        status: String,
        lastOneDay: LocalDateTime
    ): Workflow?

    @Query(
        "select * from grendel.workflow where partner_loan_id = :partnerLoanId order by created_at desc limit 1"
    )
    suspend fun findByLatestPartnerLoanId(partnerLoanId: String): Workflow?

    @Query(
        "select * from grendel.workflow where partner_loan_id = :partnerLoanId order by created_at desc"
    )
    suspend fun findAllByPartnerLoanId(partnerLoanId: String): List<Workflow?>
}