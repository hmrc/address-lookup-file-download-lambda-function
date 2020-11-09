import java.io.File
import java.net.URL
import java.util

import com.amazonaws.services.lambda.runtime.Context
import com.jessecoyle.JCredStash
import fetch.{SardineFactory2, SardineWrapper, WebdavFetcher}
import pureconfig._
import pureconfig.generic.auto._

import scala.io.Source

object AddressLookup {

  case class HFSConfig(url: URL, userKey: String, passwordKey: String, productTypes: Seq[String])

  case class AddressLookupConfig(hfs: HFSConfig, outputPath: File, epochFile: File)

  case class AppConfig(addressLookup: AddressLookupConfig)

  val appConfig: AppConfig = ConfigSource.default.load[AppConfig].fold(
    e => {
      println(s"Error reading config: $e")
      throw new IllegalStateException(e.prettyPrint())
    },
    cfg => cfg
  )

  val (hfsUrl, user, password) = {
    import appConfig._
    (
      addressLookup.hfs.url,
      retrieveCredential(addressLookup.hfs.userKey),
      retrieveCredential(addressLookup.hfs.passwordKey)
    )
  }

  // This tried to read the version file and falls back to 1
  def currentEpoch: Int = {
    import resource._
    managed(Source.fromFile(appConfig.addressLookup.epochFile))
      .map(vf => vf.getLines().take(1).mkString.toInt)
      .opt
      .getOrElse(1)
  }

  val context: java.util.HashMap[String, String] = {
    val hm = new util.HashMap[String, String]()
    hm.put("role", "address-lookup")
    hm
  }

  def retrieveCredential(credName: String): String = {
    new JCredStash().getSecret(credName, context)
  }

  def sardineWrapper: SardineWrapper = {
    new SardineWrapper(hfsUrl, user, password, new SardineFactory2)
  }

  def webDavFetcher(ctxt: Context): WebdavFetcher = {
    new WebdavFetcher(sardineWrapper, appConfig.addressLookup.outputPath, ctxt.getLogger)
  }
}
