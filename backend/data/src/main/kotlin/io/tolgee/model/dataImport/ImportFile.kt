package io.tolgee.model.dataImport

import io.tolgee.model.StandardAuditModel
import io.tolgee.model.dataImport.issues.ImportFileIssue
import io.tolgee.model.dataImport.issues.ImportFileIssueParam
import io.tolgee.model.dataImport.issues.issueTypes.FileIssueType
import io.tolgee.model.dataImport.issues.paramTypes.FileIssueParamType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.Size

@Entity
class ImportFile(
  @field:Size(max = 2000)
  @Column(length = 2000)
  var name: String?,

  @ManyToOne(optional = false)
  val import: Import,
) : StandardAuditModel() {
  @OneToMany(mappedBy = "file", orphanRemoval = true)
  var issues: MutableList<ImportFileIssue> = mutableListOf()

  @OneToMany(mappedBy = "file", orphanRemoval = true)
  var keys: MutableList<ImportKey> = mutableListOf()

  @OneToMany(mappedBy = "file", orphanRemoval = true)
  var languages: MutableList<ImportLanguage> = mutableListOf()

  var namespace: String? = null

  fun addIssue(type: FileIssueType, params: Map<FileIssueParamType, String>) {
    val issue = ImportFileIssue(file = this, type = type).apply {
      this.params = params.map {
        ImportFileIssueParam(this, it.key, it.value.shortenWithEllipsis())
      }.toMutableList()
    }
    this.issues.add(issue)
  }

  fun addKeyIsNotStringIssue(keyName: Any, keyIndex: Int) {
    addIssue(
      FileIssueType.KEY_IS_NOT_STRING,
      mapOf(
        FileIssueParamType.KEY_NAME to keyName.toString(),
        FileIssueParamType.KEY_INDEX to keyIndex.toString()
      )
    )
  }

  fun addValueIsNotStringIssue(keyName: String, keyIndex: Int?, value: Any?) {
    addIssue(
      FileIssueType.VALUE_IS_NOT_STRING,
      mapOf(
        FileIssueParamType.KEY_NAME to keyName,
        FileIssueParamType.KEY_INDEX to keyIndex.toString(),
        FileIssueParamType.VALUE to value.toString()
      )
    )
  }

  fun addKeyIsEmptyIssue(keyIndex: Int) {
    addIssue(
      FileIssueType.KEY_IS_EMPTY,
      mapOf(
        FileIssueParamType.KEY_INDEX to keyIndex.toString(),
      )
    )
  }

  fun addKeyIsBlankIssue(keyIndex: Int) {
    addIssue(
      FileIssueType.KEY_IS_BLANK,
      mapOf(
        FileIssueParamType.KEY_INDEX to keyIndex.toString(),
      )
    )
  }

  fun addValueIsEmptyIssue(keyName: String) {
    addIssue(
      FileIssueType.VALUE_IS_EMPTY,
      mapOf(
        FileIssueParamType.KEY_NAME to keyName,
      )
    )
  }

  private fun String.shortenWithEllipsis(): String {
    if (this.length > 255) {
      return this.substring(0..100) + "..."
    }
    return this
  }
}
