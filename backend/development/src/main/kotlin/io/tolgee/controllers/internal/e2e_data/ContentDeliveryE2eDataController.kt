package io.tolgee.controllers.internal.e2e_data

import io.swagger.v3.oas.annotations.Hidden
import io.tolgee.development.testDataBuilder.builders.TestDataBuilder
import io.tolgee.development.testDataBuilder.data.ContentDeliveryConfigTestData
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@Hidden
@RequestMapping(value = ["internal/e2e-data/content-delivery"])
@Transactional
class ContentDeliveryE2eDataController : AbstractE2eDataController() {
  override val testData: TestDataBuilder
    get() {
      val data = ContentDeliveryConfigTestData()
      return data.root
    }
}
