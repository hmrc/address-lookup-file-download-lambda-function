import java.io.File
import java.net.URL
import java.util.Base64
import fetch.{OSGBProduct, SardineFactory2, SardineWrapper, WebdavFetcher}
import me.lamouri.JCredStash

import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

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

  def listAllDoneFiles(epoch: String): Seq[String] = {
    Files.walk(Paths.get(s"$outputPath/$epoch/"))
         .iterator().asScala
         .filter(s => s.toFile.isFile && s.toString.endsWith(".done"))
         .map(x => x -> x.getParent)
         .map(x => s"${x._2.getFileName}/${x._1.getFileName}")
         .toList
  }

  def listAllFileUrlsToDownload(epoch: String): Seq[OSGBProduct] =
    AddressLookup.productTypes.flatMap { p =>
      if (epoch.isEmpty)
        sardineWrapper.exploreRemoteTree.findLatestFor(p)
      else
        sardineWrapper.exploreRemoteTree.findAvailableFor(p, epoch.toInt)
    }

  def listAllNewFileUrlsToDownload(epoch: String): AddressLookupFileListResponse = {
    val products = listAllFileUrlsToDownload(epoch)
    val filesAlreadyDownloaded = AddressLookup.listAllDoneFiles(epoch)
    AddressLookupFileListResponse(epoch, products, filesAlreadyDownloaded)
  }

  def downloadFilesToOutputDirectory(targetDirectory: String, fileUrls: Seq[String]): String = {
    fileUrls.foreach { f =>
      println(s"Downloading $f to $targetDirectory")
      AddressLookup.downloadFileToOutputDirectory(targetDirectory, f)
    }
    targetDirectory
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

  def batchTargetDirectory(productName: String, epoch: Int, batchIndex: Int): String = {
    s"${AddressLookup.outputPath}/$epoch/$productName/$batchIndex"
  }
}