package Retrieve

/**
 * Created by chenguanghe on 6/30/15.
 */
trait RetrieveInfo {
  def CompanyName(): String

  def RetrieveURL(): String

  def UserNameInUser(username: String, processID:Long)
}
