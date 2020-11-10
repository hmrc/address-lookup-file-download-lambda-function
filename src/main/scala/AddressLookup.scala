import java.io.File
import java.net.URL

import com.amazonaws.services.lambda.runtime.Context
import com.jessecoyle.JCredStash
import fetch.{SardineFactory2, SardineWrapper, WebdavFetcher}

import scala.collection.JavaConverters.mapAsJavaMapConverter

object AddressLookup {

  def hfsUrl = new URL("https://hfs.os.uk/")
  def user = retrieveCredential("address_lookup_user")
  def password = retrieveCredential("address_lookup_password")
  def role = "address_lookup_file_download"
  def outputPath = new File("/mnt/efs")
  def productTypes = Seq("abp", "abi")

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
    new WebdavFetcher(sardineWrapper, outputPath, ctxt.getLogger)
  }
}
