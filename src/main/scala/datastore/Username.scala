package datastore

import java.io.IOException
import java.security.GeneralSecurityException

import Utils.{myUtils, response, result}
import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.datastore.DatastoreV1._
import com.google.api.services.datastore.client.DatastoreHelper._
import com.google.api.services.datastore.client.{DatastoreFactory, DatastoreHelper, _}
import com.google.protobuf.ByteString
import spray.http.DateTime

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
 * This class is the implementation of username checking database using Google Datastore.
 * It uses the low-level api to achieve the best performance.
 * Created by gaoyike on 7/2/15.
 */
class Username {
  private val datasetId: String = "usernameinuse-993"
  // the id if project, you can find it in your project management page
  private val accountEmail: String = "698296859446-ue0flgsfncj4k30j6k5fc9t99tusd1sc@developer.gserviceaccount.com"
  // the API ID, in project's API page
  private val filename: String = "/Users/xxx/usernameinuse-a45410b7ff47.p12"
  // the p12 file path
  private val Kind = "username"
  private var datastore: Datastore = null

  def init() = {
    try {
      var options: DatastoreOptions.Builder = new DatastoreOptions.Builder
      var credential: Credential = DatastoreHelper.getServiceAccountCredential(accountEmail, filename)
      datastore = DatastoreFactory.get.create(options.credential(credential).dataset(datasetId).build)
    }
    catch {
      case e: GeneralSecurityException => {
        System.err.println("Security error connecting to the datastore: " + e.getMessage)
        System.exit(1)
      }
      case e: IOException => {
        System.err.println("I/O error connecting to the datastore: " + e.getMessage)
        System.exit(1)
      }
    }
  }
  def search(username: String,id:Long): response = {
    var treq = BeginTransactionRequest.newBuilder
    var tres = datastore.beginTransaction(treq.build)
    var tx: ByteString = tres.getTransaction
    var lreq = LookupRequest.newBuilder
    var key = Key.newBuilder.addPathElement(Key.PathElement.newBuilder.setKind(Kind).setName(username))
    lreq.getReadOptionsBuilder.setTransaction(tx)
    lreq.addKey(key)
    var lresp = datastore.lookup(lreq.build)
    var creq = CommitRequest.newBuilder
    creq.setTransaction(tx)
    if (lresp.getFoundCount > 0) {
      var e = lresp.getFound(0).getEntity.toBuilder
      var property = e.getPropertyBuilderList.asScala.toList
      var list: List[result] = List[result]()
      property.foreach {
        p => {
          if (p.getName.equals("result"))
            list = StringtoRes(p.getValue.getStringValue)
          if (p.getName.equals("VisitedTimes"))
            p.setValue(makeValue(p.getValue.getIntegerValue + 1))
          if (p.getName.equals("LastVisited"))
            p.setValue(makeValue(DateTime.now.toIsoDateTimeString))
        }
      }
      creq.getMutationBuilder.addUpdate(e.build())
      datastore.commit(creq.build)
      response(id,username,list)
    }
    else
      response(id,username,List[result]())
  }

  def StringtoRes(s: String): List[result] = {
    var list: ListBuffer[result] = ListBuffer[result]()
    var rev = myUtils.CompanyNameToID.map(_.swap)
    for (i <- 0 to s.length - 1) {
      if (s.reverse.charAt(i) == '0')
        list.append(result(rev(i), "false"))
      else
        list.append(result(rev(i), "true"))
    }
    return list.toList
  }

  def Insert(username: String, res:List[result]): Boolean = {
    var treq = BeginTransactionRequest.newBuilder
    var tres = datastore.beginTransaction(treq.build)
    var tx: ByteString = tres.getTransaction
    var lreq = LookupRequest.newBuilder
    var key = Key.newBuilder.addPathElement(Key.PathElement.newBuilder.setKind(Kind).setName(username))
    lreq.getReadOptionsBuilder.setTransaction(tx)
    lreq.addKey(key)
    var lresp = datastore.lookup(lreq.build)
    var creq = CommitRequest.newBuilder
    creq.setTransaction(tx)
    if (lresp.getFoundCount > 0) {
      return false
    } else {
      var entity: Entity = null
      var entityBuilder = Entity.newBuilder
      entityBuilder.setKey(key)
      entityBuilder.addProperty(0, makeProperty("CreatedTime", makeValue(DateTime.now.toIsoDateTimeString)))
      entityBuilder.addProperty(1, makeProperty("LastModified", makeValue(DateTime.now.toIsoDateTimeString)))
      entityBuilder.addProperty(2, makeProperty("ModifiedTimes", makeValue(1)))
      entityBuilder.addProperty(3, makeProperty("VisitedTimes", makeValue(1)))
      entityBuilder.addProperty(4, makeProperty("LastVisited", makeValue(DateTime.now.toIsoDateTimeString)))
      entityBuilder.addProperty(5, makeProperty("result", makeValue(ResToString(res))))
      entity = entityBuilder.build
      creq.getMutationBuilder.addInsert(entity)
      datastore.commit(creq.build)
      return true;
    }
  }

  def Update(username: String, res:List[result]): Boolean = {
    var treq = BeginTransactionRequest.newBuilder
    var tres = datastore.beginTransaction(treq.build)
    var tx: ByteString = tres.getTransaction
    var lreq = LookupRequest.newBuilder
    var key = Key.newBuilder.addPathElement(Key.PathElement.newBuilder.setKind(Kind).setName(username))
    lreq.getReadOptionsBuilder.setTransaction(tx)
    lreq.addKey(key)
    var lresp = datastore.lookup(lreq.build)
    var creq = CommitRequest.newBuilder
    creq.setTransaction(tx)
    if (lresp.getFoundCount > 0) {
      var entity: Entity = null
      var e = lresp.getFound(0).getEntity.toBuilder
      var list = e.getPropertyBuilderList.asScala
      list.foreach {
        l => {
          if (l.getName.equals("LastModified"))
            l.setValue(makeValue(DateTime.now.toIsoDateTimeString))
          if (l.getName.equals("ModifiedTimes"))
            l.setValue(makeValue(l.getValue.getIntegerValue + 1))
          if (l.getName.equals("LastModified"))
            l.setValue(makeValue(DateTime.now.toIsoDateTimeString))
          if (l.getName.equals("result") && !l.getValue.getStringValue.equals(ResToString(res)))
            l.setValue(makeValue(ResToString(res)))
          if (l.getName.equals("VisitedTimes"))
            l.setValue(makeValue(l.getValue.getIntegerValue + 1))
          if (l.getName.equals("LastVisited"))
            l.setValue(makeValue(DateTime.now.toIsoDateTimeString))
        }
      }
      creq.getMutationBuilder.addUpdate(e.build())
      datastore.commit(creq.build)
      return true
    } else {
      return false
    }
  }

  def ResToString(list: List[result]): String = {
    var b = new StringBuilder
    list.foreach {
      l => if (l.available.equals("true")) b.append("1") else b.append("0")
    }
    b.toString()
  }
}
//
//object Username extends Username {
//  def main(args: Array[String]) {
//    init();
//    val r = response("2015-07-02T18:45:22", 213131, "readmadn", List[result](result("Gogol", "true"), result("Git", "false")))
//    addandupdate(r)
//    print(search("readmadn",2132131))
//  }
//}
object Username extends Username
