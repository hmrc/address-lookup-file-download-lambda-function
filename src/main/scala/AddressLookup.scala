import java.io.File
import java.net.URL
import java.util.Base64

import com.amazonaws.services.lambda.runtime.Context
import com.jessecoyle.JCredStash
import fetch.{SardineFactory2, SardineWrapper, WebdavFetcher}

import scala.collection.JavaConverters.mapAsJavaMapConverter

object AddressLookup {

  val hfsUrl = new URL("https://hfs.os.uk/")
  def user = retrieveCredential("address_lookup_user")
  def password = new String(
    Base64.getDecoder.decode(retrieveCredential("address_lookup_password"))).trim

  val role = "address_lookup_file_download"
  val outputPath = new File("/mnt/efs")
  val productTypes = Seq("abp", "abi")

  val context: java.util.Map[String, String] = {
    Map("role" -> role).asJava
  }

  def retrieveCredential(credName: String): String = {
    new JCredStash().getSecret(credName, context)
  }

  def sardineWrapper: SardineWrapper = {
    new SardineWrapper(hfsUrl, user, password, new SardineFactory2)
  }

  def webDavFetcher(ctxt: Context): WebdavFetcher = {
    new WebdavFetcher(sardineWrapper, outputPath, ctxt.getLogger)
  }
}
