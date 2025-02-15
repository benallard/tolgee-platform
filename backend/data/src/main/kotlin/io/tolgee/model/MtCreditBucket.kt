package io.tolgee.model

import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.ColumnDefault
import java.util.*

@Entity
@Table(
  uniqueConstraints = [
    UniqueConstraint(columnNames = ["organization_id"], name = "mt_credit_bucket_organization_unique"),
  ]
)
class MtCreditBucket(
  @OneToOne
  @Deprecated("Only organization can own a credit bucket...")
  var userAccount: UserAccount? = null,

  @OneToOne
  var organization: Organization? = null
) : StandardAuditModel() {

  var credits: Long = 0

  /**
   * These credits are not refilled or reset every period.
   * It's consumed when user is out of their standard credits.
   *
   * (In Tolgee Cloud users can buy these Extra credits)
   */
  @ColumnDefault("0")
  var extraCredits: Long = 0

  var bucketSize: Long = 0

  var refilled: Date = Date()
}
