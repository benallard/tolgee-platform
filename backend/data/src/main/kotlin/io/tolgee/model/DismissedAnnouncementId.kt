package io.tolgee.model

import io.tolgee.model.enums.Announcement
import java.io.Serializable

data class DismissedAnnouncementId(
  val user: Long? = null,
  val announcement: Announcement? = null
) : Serializable
