import java.io.File
import java.net.URL
import java.util.Base64
import fetch.{OSGBProduct, SardineFactory2, SardineWrapper, WebdavFetcher}
import me.lamouri.JCredStash

import scala.collection.JavaConverters.mapAsJavaMapConverter

object AddressLookup {

  val hfsUrl = new URL("https://hfs.os.uk/")

  def user = retrieveCredential("address_lookup_user")

  def password = new String(
    Base64.getDecoder.decode(retrieveCredential("address_lookup_password"))).trim

  val role = "address_lookup_file_download"
  val outputPath = "/mnt/efs"
  val productTypes = Seq("abp", "abi")

  val context: java.util.Map[String, String] = {
    Map("role" -> role).asJava
  }

  def retrieveCredential(credName: String): String = {
    new JCredStash().getSecret("credential-store", credName, context)
  }

  def sardineWrapper: SardineWrapper = {
    new SardineWrapper(hfsUrl, user, password, new SardineFactory2)
  }

  def webDavFetcher: WebdavFetcher = {
    new WebdavFetcher(sardineWrapper, new File(outputPath))
  }

  def listAllFileUrlsToDownload(epoch: String): Seq[OSGBProduct] =
    AddressLookup.productTypes.flatMap { p =>
      if (epoch.isEmpty)
        sardineWrapper.exploreRemoteTree.findLatestFor(p)
      else
        sardineWrapper.exploreRemoteTree.findAvailableFor(p, epoch.toInt)
    }

  def downloadFileToOutputDirectory(targetDirectory: String, fileUrl: String): Unit = {
    val directory = new File(targetDirectory)
    if (!directory.exists()) {
      println("Directory does not exist, creating...")
      directory.mkdirs()
    }

    println("Beginning fetch...")
    webDavFetcher.fetchFile(new URL(fileUrl), directory)
  }

  def batchTargetDirectory(productName: String, epoch: Int, batchIndex: Int) = {
    s"${AddressLookup.outputPath}/$epoch/$productName/$batchIndex"
  }
}