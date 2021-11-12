import fetch.{OSGBProduct, SardineFactory2, SardineWrapper, WebdavFetcher}
import me.lamouri.JCredStash

import java.io.File
import java.net.URL
import java.nio.file.{Files, Paths}
import java.util.Base64
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
    val epochPath = Paths.get(s"$outputPath/$epoch/")
    if (!epochPath.toFile.exists()) Seq()
    else Files.walk(epochPath)
              .iterator().asScala
              .filter(s => s.toFile.isFile && s.toString.endsWith(".done"))
              .map(x => x.toString)
              .toList
  }

  def listAllProductsAvailableToDownload(requestedEpoch: Option[String]): Seq[OSGBProduct] =
    AddressLookup.productTypes.flatMap { p =>
      if (requestedEpoch.isEmpty)
        sardineWrapper.exploreRemoteTree.findLatestFor(p)
      else
        sardineWrapper.exploreRemoteTree.findAvailableFor(p, requestedEpoch.map(_.toInt).get)
    }

  def listFiles(requested: AddressLookupFileListRequest): AddressLookupFileListResponse = {
    val products = listAllProductsAvailableToDownload(requested.epoch)
    val epoch = requested.epoch.fold(products.head.epoch.toString)(identity)
    val filesAlreadyDownloaded = if (requested.forceDownload) Seq() else AddressLookup.listAllDoneFiles(epoch)

    // If products is empty this means that epoch does not exist on the remote server
    // so we try to reconstruct the batch info by looking at what we've got downloaded
    products match {
      case Seq() => AddressLookupFileListResponse(epoch, batchesFromDoneFiles(filesAlreadyDownloaded))
      case _     => AddressLookupFileListResponse(epoch, products, filesAlreadyDownloaded)
    }
  }

  def batchesFromDoneFiles(doneFiles: Seq[String]): Seq[Batch] =
    doneFiles
        .map { pathAndFileName => pathAndFileName.split("/").init.mkString("/") }.toSet
        .map { path: String => Batch(path, List()) }.toSeq

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