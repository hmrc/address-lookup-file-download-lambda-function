package processing

import com.amazonaws.secretsmanager.caching.SecretCache
import play.api.libs.json._
import processing.FileDownloader.model._
import services.SecretsManagerService
import sttp.client3.quick._
import sttp.client3.{HttpURLConnectionBackend, Identity, Response, SttpBackend}
import sttp.model.Uri

import java.io.File
import java.time.LocalDate


class FileDownloader(
                      private val authKey: String,
                      private val secretsManagerService: SecretsManagerService,
                      private val backend: SttpBackend[Identity, Any]) extends FileOps {

  import FileDownloader._
  import model.implicits._

  def download(): Either[DownloadError, (String, List[String])] = {
    val dataPackages = listDataPackagesOfInterest()

    for {
      abpDownload <- doDownload(dataPackages, abp)
      abpIslandsDownload <- doDownload(dataPackages, abpIslands)

      (abpBatchesRootDir, downloadedAbpFile) = abpDownload
      (_, downloadedAbpIslandsFile) = abpIslandsDownload

    } yield (abpBatchesRootDir, List(downloadedAbpFile, downloadedAbpIslandsFile))
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

    if (downloadedFile.length() != downloadForLatestVersion.size) {
      println(s">>> actualSize: ${downloadedFile.length()} <> givenSize: ${downloadForLatestVersion.size}")
      Left(UnexpectedSize(downloadForLatestVersion.fileName))
    } else if (!downloadedFile.checkMinSize) {
      Left(SizeTooSmall(downloadForLatestVersion.fileName))
    } else if (!downloadedFile.checkMd5(downloadForLatestVersion.md5)) {
      Left(MD5NotMatched(downloadForLatestVersion.fileName))
    } else {
      Right((s"$outputRoot/${latestVersion.productVersion.dbSafe}", downloadedFile.getAbsolutePath))
    }
  }

  private def listDataPackagesOfInterest(): List[DataPackage] = {
    body[List[DataPackage]](uri"$baseUrl?key=$authKey")
      .filter(dp => packagesOfInterest.contains(dp.name))
  }

  private def body[A](url: Uri)(implicit format: Format[A]): A = {
    val responseBodyText = get(url).body
    println(s">>> result of call to '${redactKey(url.toString)}': ${redactKey(responseBodyText)}")

    val jsRes = Json.fromJson[A](Json.parse(responseBodyText))

    jsRes match {
      case JsSuccess(value, _) => value
      case JsError(errors)     => throw new Exception(errors.mkString)
    }
  }

  protected[processing] def redactKey(text: String): String = {
    text.replaceAll(s"key=$authKey", "key=****")
  }

  private def get(url: Uri): Identity[Response[String]] = {
    quickRequest.get(url).send(backend)
  }

  private def download(url: Uri, fileName: String): File = {
    val downloadedFile = new File(s"$downloadRoot/$fileName")
    quickRequest.get(uri"$url")
      .response(asFile(downloadedFile))
      .send(backend)
    downloadedFile
  }
}

object FileDownloader {
  def apply(): FileDownloader = {
    val syncBackend = HttpURLConnectionBackend()
    val secretsManagerService = new SecretsManagerService(new SecretCache)
    val apiKey = secretsManagerService.getSecret(secretName, secretKey)

    new FileDownloader(apiKey, secretsManagerService, syncBackend)
  }

  val secretName = "attrep-secret/address_lookup_file_download/address_lookup_osdatahub_auth_key"
  val secretKey = "secret"
  val baseUrl = "https://api.os.uk/downloads/v1/dataPackages"
  val abp = "AB Prem (Full)"
  val abpIslands = "AB Prem Islands (Full)"
  val packagesOfInterest: Set[String] = Set(abp, abpIslands)
  val outputRoot = "/mnt/efs"
  val downloadRoot = s"$outputRoot/download"

  object model {
    case class DataPackage(id: String, name: String, url: String, versions: List[DataPackageVersion])

    case class DataPackageVersion(id: String, createdOn: LocalDate, url: String, productVersion: String, downloads: Option[List[DataPackageVersionDownload]])

    case class DataPackageVersionDownload(fileName: String, url: String, size: Long, md5: String)

    object implicits {
      implicit val dataPackageVersionDownloadFormat: Format[DataPackageVersionDownload] = Json.format[DataPackageVersionDownload]
      implicit val dataPackageVersionFormat: Format[DataPackageVersion] = Json.format[DataPackageVersion]
      implicit val dataPackageFormat: Format[DataPackage] = Json.format[DataPackage]
    }

    sealed trait DownloadError
    case class MD5NotMatched(fileName: String) extends DownloadError
    case class UnexpectedSize(fileName: String) extends DownloadError
    case class SizeTooSmall(fileName: String) extends DownloadError
  }
}
