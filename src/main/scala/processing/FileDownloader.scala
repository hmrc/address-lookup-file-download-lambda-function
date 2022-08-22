package processing

import play.api.libs.json._
import processing.FileDownloader.model._
import sttp.client3.quick._
import sttp.client3.{HttpURLConnectionBackend, Identity, Response, SttpBackend}
import sttp.model.Uri

import java.io.File
import java.time.LocalDate


class FileDownloader(private val authKey: String, private val backend: SttpBackend[Identity, Any]) extends FileOps {

  import FileDownloader._
  import model.implicits._

  def download(): (String, List[String]) = {
    val dataPackages = listDataPackagesOfInterest()

    val (abpBatchesRootDir, downloadedAbpFile) = doDownload(dataPackages, abp)
    val (_, downloadedAbpIslandsFile) = doDownload(dataPackages, abpIslands)
    val (rootDirForBatches, downloadedFiles) = (abpBatchesRootDir, List(downloadedAbpFile, downloadedAbpIslandsFile))

    (rootDirForBatches, downloadedFiles)
  }


  private def doDownload(dataPackages: List[DataPackage], dataset: String) = {
    import implicits._

    val abpPackage = dataPackages.find(_.name == dataset)

    val latestVersionDescription = abpPackage.get.versions.sortWith { case (a, b) => a.createdOn.isAfter(b.createdOn) }.head
    val latestVersion = body[DataPackageVersion](uri"${latestVersionDescription.url}")

    val downloadForLatestVersion = latestVersion.downloads.flatMap(_.find(_.fileName.endsWith(".zip"))).head

    cleanOldFiles(s"$downloadRoot/${downloadForLatestVersion.fileName}")
    cleanOldFiles(s"$outputRoot/${latestVersion.productVersion}")
    val downloadedFile = download(uri"${downloadForLatestVersion.url}", downloadForLatestVersion.fileName)

    (s"${outputRoot}/${latestVersion.productVersion.dbSafe}", downloadedFile.getAbsolutePath)
  }

  private def listDataPackagesOfInterest(): List[DataPackage] = {
    body[List[DataPackage]](uri"${baseUrl}?key=${authKey}")
      .filter(dp => packagesOfInterest.contains(dp.name))
  }

  private def body[A](url: Uri)(implicit format: Format[A]): A = {
    val jsRes = Json.fromJson[A](
      Json.parse(get(url).body)
    )
    jsRes match {
      case JsSuccess(value, _) => value
      case JsError(errors)     => throw new Exception(errors.mkString)
    }
  }

  private def get(url: Uri): Identity[Response[String]] = {
    quickRequest.get(url).send(backend)
  }

  private def download(url: Uri, fileName: String): File = {
    val downloadedFile = new File(s"${downloadRoot}/${fileName}")
    quickRequest.get(uri"${url}")
      .response(asFile(downloadedFile))
      .send(backend)
    downloadedFile
  }
}

object FileDownloader {
  def apply(): FileDownloader = {
    val creds = new Credentials()
    val apiKey = creds.secret(authKeySecretKey)
    val syncBackend = HttpURLConnectionBackend()

    new FileDownloader(apiKey, syncBackend)
  }

  val authKeySecretKey = "address_lookup_osdatahub_auth_key"
  val baseUrl = "https://api.os.uk/downloads/v1/dataPackages"
  val abp = "AB Prem (Full)"
  val abpIslands = "AB Prem Islands (Full)"
  val packagesOfInterest: Set[String] = Set(abp, abpIslands)
  val outputRoot = "/mnt/efs"
  val downloadRoot = s"${outputRoot}/download"

  object model {
    case class DataPackage(id: String, name: String, url: String, versions: List[DataPackageVersion])

    case class DataPackageVersion(id: String, createdOn: LocalDate, url: String, productVersion: String, downloads: Option[List[DataPackageVersionDownload]])

    case class DataPackageVersionDownload(fileName: String, url: String, size: Long, md5: String)

    object implicits {
      implicit val dataPackageVersionDownloadFormat: Format[DataPackageVersionDownload] = Json.format[DataPackageVersionDownload]
      implicit val dataPackageVersionFormat: Format[DataPackageVersion] = Json.format[DataPackageVersion]
      implicit val dataPackageFormat: Format[DataPackage] = Json.format[DataPackage]
    }
  }
}
