package io.tolgee.api.v2.controllers.suggestion

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.api.v2.hateoas.invitation.TranslationMemoryItemModelAssembler
import io.tolgee.constants.Message
import io.tolgee.dtos.request.SuggestRequestDto
import io.tolgee.exceptions.NotFoundException
import io.tolgee.hateoas.machineTranslation.SuggestResultModel
import io.tolgee.hateoas.translationMemory.TranslationMemoryItemModel
import io.tolgee.model.enums.Scope
import io.tolgee.model.key.Key
import io.tolgee.model.views.TranslationMemoryItemView
import io.tolgee.security.ProjectHolder
import io.tolgee.security.authentication.AllowApiAccess
import io.tolgee.security.authorization.RequiresProjectPermissions
import io.tolgee.service.LanguageService
import io.tolgee.service.key.KeyService
import io.tolgee.service.security.SecurityService
import io.tolgee.service.translation.TranslationMemoryService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(value = ["/v2/projects/{projectId:[0-9]+}/suggest", "/v2/projects/suggest"])
@Tag(name = "Translation suggestion")
@Suppress("SpringJavaInjectionPointsAutowiringInspection", "MVCPathVariableInspection")
class TranslationSuggestionController(
  private val projectHolder: ProjectHolder,
  private val languageService: LanguageService,
  private val keyService: KeyService,
  private val translationMemoryService: TranslationMemoryService,
  private val translationMemoryItemModelAssembler: TranslationMemoryItemModelAssembler,
  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  private val arraytranslationMemoryItemModelAssembler: PagedResourcesAssembler<TranslationMemoryItemView>,
  private val securityService: SecurityService,
  private val machineTranslationSuggestionFacade: MachineTranslationSuggestionFacade
) {
  @PostMapping("/machine-translations")
  @Operation(summary = "Suggests machine translations from enabled services")
  @RequiresProjectPermissions([ Scope.TRANSLATIONS_EDIT ])
  @AllowApiAccess
  fun suggestMachineTranslations(@RequestBody @Valid dto: SuggestRequestDto): SuggestResultModel {
    return machineTranslationSuggestionFacade.suggestSync(dto)
  }

  @PostMapping("/machine-translations-streaming", produces = ["application/x-ndjson"])
  @Operation(
    summary = "Suggests machine translations from enabled services (streaming).\n" +
      "If an error occurs when any of the services is used," +
      " the error information is returned as a part of the result item, while the response has 200 status code."
  )

  @RequiresProjectPermissions([ Scope.TRANSLATIONS_EDIT ])
  @AllowApiAccess
  fun suggestMachineTranslationsStreaming(
    @RequestBody @Valid dto: SuggestRequestDto
  ): ResponseEntity<StreamingResponseBody> {
    return ResponseEntity.ok().headers {
      it.add("X-Accel-Buffering", "no")
    }.body(
      machineTranslationSuggestionFacade.suggestStreaming(dto)
    )
  }

  @PostMapping("/translation-memory")
  @Operation(
    summary = "Suggests machine translations from translation memory." +
      "\n\nThe result is always sorted by similarity, so sorting is not supported."
  )
  @RequiresProjectPermissions([ Scope.TRANSLATIONS_EDIT ])
  @AllowApiAccess
  fun suggestTranslationMemory(
    @RequestBody @Valid dto: SuggestRequestDto,
    @ParameterObject pageable: Pageable
  ): PagedModel<TranslationMemoryItemModel> {
    val targetLanguage = languageService.get(dto.targetLanguageId)

    securityService.checkLanguageTranslatePermission(projectHolder.project.id, listOf(targetLanguage.id))

    val data = dto.baseText?.let { baseText -> translationMemoryService.suggest(baseText, targetLanguage, pageable) }
      ?: let {
        val key = keyService.findOptional(dto.keyId).orElseThrow { NotFoundException(Message.KEY_NOT_FOUND) }
        key.checkInProject()
        translationMemoryService.suggest(key, targetLanguage, pageable)
      }
    return arraytranslationMemoryItemModelAssembler.toModel(data, translationMemoryItemModelAssembler)
  }

  private fun Key.checkInProject() {
    keyService.checkInProject(this, projectHolder.project.id)
  }
}
