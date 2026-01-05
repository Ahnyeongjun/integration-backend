package com.msa.wedding.application

import com.msa.common.exception.ForbiddenException
import com.msa.common.exception.NotFoundException
import com.msa.wedding.domain.entity.WeddingPlan
import com.msa.wedding.domain.repository.DressRepository
import com.msa.wedding.domain.repository.MakeupShopRepository
import com.msa.wedding.domain.repository.WeddingHallRepository
import com.msa.wedding.domain.repository.WeddingPlanRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class WeddingPlanService(
    private val weddingPlanRepository: WeddingPlanRepository,
    private val weddingHallRepository: WeddingHallRepository,
    private val dressRepository: DressRepository,
    private val makeupShopRepository: MakeupShopRepository
) {
    fun getMyPlans(userId: Long, pageable: Pageable): Page<WeddingPlan> =
        weddingPlanRepository.findByUserId(userId, pageable)

    fun getPlan(userId: Long, planId: Long): WeddingPlan {
        val plan = weddingPlanRepository.findByUserIdAndId(userId, planId)
            ?: throw NotFoundException("WeddingPlan", planId)
        return plan
    }

    @Transactional
    fun createPlan(userId: Long, request: PlanCreateRequest): WeddingPlan {
        val plan = WeddingPlan(
            userId = userId,
            title = request.title,
            weddingDate = request.weddingDate,
            budget = request.budget,
            expectedGuests = request.expectedGuests
        )
        return weddingPlanRepository.save(plan)
    }

    @Transactional
    fun updatePlan(userId: Long, planId: Long, request: PlanUpdateRequest): WeddingPlan {
        val plan = getPlan(userId, planId)

        request.title?.let { plan.title = it }
        request.weddingDate?.let { plan.weddingDate = it }
        request.budget?.let { plan.budget = it }
        request.expectedGuests?.let { plan.expectedGuests = it }
        request.hallId?.let { hallId ->
            plan.selectedHall = weddingHallRepository.findById(hallId).orElse(null)
        }
        request.dressId?.let { dressId ->
            plan.selectedDress = dressRepository.findById(dressId).orElse(null)
        }
        request.makeupShopId?.let { shopId ->
            plan.selectedMakeupShop = makeupShopRepository.findById(shopId).orElse(null)
        }

        return weddingPlanRepository.save(plan)
    }

    @Transactional
    fun deletePlan(userId: Long, planId: Long) {
        val plan = getPlan(userId, planId)
        weddingPlanRepository.delete(plan)
    }
}

data class PlanCreateRequest(
    val title: String,
    val weddingDate: LocalDate? = null,
    val budget: Long? = null,
    val expectedGuests: Int? = null
)

data class PlanUpdateRequest(
    val title: String? = null,
    val weddingDate: LocalDate? = null,
    val budget: Long? = null,
    val expectedGuests: Int? = null,
    val hallId: Long? = null,
    val dressId: Long? = null,
    val makeupShopId: Long? = null
)
