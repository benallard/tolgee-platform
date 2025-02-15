package io.tolgee.repository.translation

import io.tolgee.model.translation.Translation
import io.tolgee.model.translation.TranslationComment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TranslationCommentRepository : JpaRepository<TranslationComment, Long> {
  fun deleteAllByIdIn(ids: List<Long>)

  @Query("select tc from TranslationComment tc left join fetch tc.author where tc.translation = :translation")
  fun getPagedByTranslation(translation: Translation, pageable: Pageable): Page<TranslationComment>

  fun deleteAllByTranslationIdIn(translationIds: Collection<Long>)

  fun deleteByTranslationIdIn(ids: Collection<Long>)

  @Query(
    """
    from TranslationComment tc
    join tc.translation t
    join t.key k
    where k.project.id = :projectId
  """
  )
  fun getAllByProjectId(projectId: Long): List<TranslationComment>

  @Query(
    """
    from TranslationComment tc
    left join fetch tc.author
    where tc.id = :id
  """
  )
  fun findWithFetchedAuthor(id: Long): TranslationComment?
}
