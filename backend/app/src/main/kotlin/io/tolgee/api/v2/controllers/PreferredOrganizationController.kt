/*
 * Copyright (c) 2020. Tolgee
 */

package io.tolgee.api.v2.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.api.v2.hateoas.organization.OrganizationModel
import io.tolgee.api.v2.hateoas.organization.OrganizationModelAssembler
import io.tolgee.model.Organization
import io.tolgee.model.UserPreferences
import io.tolgee.model.views.OrganizationView
import io.tolgee.security.AuthenticationFacade
import io.tolgee.service.OrganizationRoleService
import io.tolgee.service.OrganizationService
import io.tolgee.service.UserPreferencesService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(value = ["/v2/preferred-organization"])
@Tag(name = "Organizations")
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class PreferredOrganizationController(
  private val authenticationFacade: AuthenticationFacade,
  private val organizationRoleService: OrganizationRoleService,
  private val userPreferencesService: UserPreferencesService,
  private val organizationModelAssembler: OrganizationModelAssembler,
  private val organizationService: OrganizationService
) {
  @GetMapping("")
  @Operation(summary = "Returns preferred organization")
  fun getPreferred(): OrganizationModel? {
    val preferences = userPreferencesService.findOrCreate(authenticationFacade.userAccount.id)
    val preferredOrganization = getPreferredOrganization(preferences)
    val roleType = organizationRoleService.findType(preferredOrganization.id)
    val view = OrganizationView.of(preferredOrganization, roleType)
    return this.organizationModelAssembler.toModel(view)
  }

  private fun getPreferredOrganization(preferences: UserPreferences): Organization {
    return (
      preferences.preferredOrganization
        ?: organizationService.createPreferred(authenticationFacade.userAccountEntity)
          .also { userPreferencesService.setPreferredOrganizationAsync(it, authenticationFacade.userAccountEntity) }
      )
  }
}
