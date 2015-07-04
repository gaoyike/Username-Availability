package Retrieve

/**
 * // the information trait
 * Created by gaoyike on 6/30/15.
 */
trait RetrieveInfo {
  def CompanyName(): String

  def RetrieveURL(): String

  def UserNameInUser(username: String, processID:Long)
}
