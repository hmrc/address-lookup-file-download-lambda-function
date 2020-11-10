import java.io.File
import java.net.URL
import java.util

import com.amazonaws.services.lambda.runtime.Context
import com.jessecoyle.JCredStash
import fetch.{SardineFactory2, SardineWrapper, WebdavFetcher}
import pureconfig._
import pureconfig.generic.auto._

import scala.collection.JavaConverters.mapAsJavaMapConverter

object AddressLookup {

  case class HFSConfig(url: URL, userKey: String, passwordKey: String, roleKey: String, productTypes: Seq[String])

  case class AddressLookupConfig(hfs: HFSConfig, outputPath: File)

  case class AppConfig(addressLookup: AddressLookupConfig)

  val appConfig: AppConfig = ConfigSource.default.load[AppConfig].fold(
    e => {
      println(s"Error reading config: $e")
      throw new IllegalStateException(e.prettyPrint())
    },
    cfg => cfg
  )

  def hfsUrl = new URL("https://hfs.os.uk/")
  def user = retrieveCredential("address_lookup_user")
  def password = retrieveCredential("address_lookup_password")
  def role = "address_lookup_file_download"

  val context: java.util.Map[String, String] = {
    println(s"Setting role to $role")
    Map("role" -> role).asJava
  }

  def retrieveCredential(credName: String): String = {
    println(s"Getting secret $credName in role $role")
    new JCredStash().getSecret(credName, context)
  }

  def sardineWrapper: SardineWrapper = {
    new SardineWrapper(hfsUrl, user, password, new SardineFactory2)
  }

  def webDavFetcher(ctxt: Context): WebdavFetcher = {
    new WebdavFetcher(sardineWrapper, appConfig.addressLookup.outputPath, ctxt.getLogger)
  }
}
