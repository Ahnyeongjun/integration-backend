package com.msa.wedding.application

import com.msa.common.exception.NotFoundException
import com.msa.wedding.api.controller.WeddingPlanResponse
import com.msa.wedding.domain.entity.WeddingPlan
import com.msa.wedding.domain.repository.DressShopRepository
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
    private val dressShopRepository: DressShopRepository,
    private val makeupShopRepository: MakeupShopRepository
) {
    fun getMyPlans(userId: Long, pageable: Pageable): Page<WeddingPlan> =
        weddingPlanRepository.findByUserId(userId, pageable)

    fun getPlan(userId: Long, planId: Long): WeddingPlan =
        weddingPlanRepository.findByUserIdAndId(userId, planId)
            ?: throw NotFoundException("WeddingPlan", planId)

    @Transactional
    fun createPlan(userId: Long, request: PlanCreateRequest): WeddingPlan {
        val plan = WeddingPlan(
            userId = userId,
            title = request.title,
            weddingDate = request.weddingDate,
            budget = request.budget,
            expectedGuests = request.expectedGuests
        )

        request.hallId?.let { hallId ->
            plan.selectedHall = weddingHallRepository.findById(hallId)
                .orElseThrow { NotFoundException("WeddingHall", hallId) }
        }

        request.dressShopId?.let { dressShopId ->
            plan.selectedDressShop = dressShopRepository.findById(dressShopId)
                .orElseThrow { NotFoundException("DressShop", dressShopId) }
        }

        request.makeupShopId?.let { makeupShopId ->
            plan.selectedMakeupShop = makeupShopRepository.findById(makeupShopId)
                .orElseThrow { NotFoundException("MakeupShop", makeupShopId) }
        }

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
            plan.selectedHall = weddingHallRepository.findById(hallId)
                .orElseThrow { NotFoundException("WeddingHall", hallId) }
        }

        request.dressShopId?.let { dressShopId ->
            plan.selectedDressShop = dressShopRepository.findById(dressShopId)
                .orElseThrow { NotFoundException("DressShop", dressShopId) }
        }

        request.makeupShopId?.let { makeupShopId ->
            plan.selectedMakeupShop = makeupShopRepository.findById(makeupShopId)
                .orElseThrow { NotFoundException("MakeupShop", makeupShopId) }
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
    val expectedGuests: Int? = null,
    val hallId: Long? = null,
    val dressShopId: Long? = null,
    val makeupShopId: Long? = null
)

data class PlanUpdateRequest(
    val title: String? = null,
    val weddingDate: LocalDate? = null,
    val budget: Long? = null,
    val expectedGuests: Int? = null,
    val hallId: Long? = null,
    val dressShopId: Long? = null,
    val makeupShopId: Long? = null
)
