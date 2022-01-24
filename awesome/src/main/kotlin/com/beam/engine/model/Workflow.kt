package com.beam.engine.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("grendel.workflow")
data class Workflow(
    @Id
    var id: Long? = null,

    @Column("partner_loan_id")
    var partnerLoanId: String,

    @Column("partner_id")
    var partnerId: String,

    @Column("type")
    var workflowType: String,

    @Column("initial_offer_request")
    var request: String?,

    @Column("message_id")
    var messageId: String,

    @Column("status")
    var status: String?,

    @Column("remarks")
    var remarks: String?,

    @Column("created_at")
    var createdAt: LocalDateTime?,

    @Column("updated_at")
    var updatedAt: LocalDateTime? = LocalDateTime.now()

)