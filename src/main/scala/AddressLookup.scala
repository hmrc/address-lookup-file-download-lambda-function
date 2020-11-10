import java.io.File
import java.net.URL
import java.util

import com.amazonaws.services.lambda.runtime.Context
import com.jessecoyle.JCredStash
import fetch.{SardineFactory2, SardineWrapper, WebdavFetcher}
import pureconfig._
import pureconfig.generic.auto._

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

  val (hfsUrl, user, password, role) = {
    import appConfig._
    (
      addressLookup.hfs.url,
      retrieveCredential(addressLookup.hfs.userKey),
      retrieveCredential(addressLookup.hfs.passwordKey),
      appConfig.addressLookup.hfs.roleKey
    )
  }

  lazy val context: java.util.HashMap[String, String] = {
    println(s"Setting role to $role")
    val hm = new util.HashMap[String, String]()
    hm.put("role", "bacs_eiscd")
    hm
  }

  def retrieveCredential(credName: String): String = {
    println(s"Trying to get $credName with role $role from credstash ...")
    new JCredStash().getSecret(credName, context)
  }

  def sardineWrapper: SardineWrapper = {
    new SardineWrapper(hfsUrl, user, password, new SardineFactory2)
  }

  def webDavFetcher(ctxt: Context): WebdavFetcher = {
    new WebdavFetcher(sardineWrapper, appConfig.addressLookup.outputPath, ctxt.getLogger)
  }
}
