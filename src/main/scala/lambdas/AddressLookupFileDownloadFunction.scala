package lambdas

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import processing.FileDownloader
import processing.FileDownloader.model.{MD5NotMatched, SizeTooSmall, UnexpectedSize}

import java.util
import java.util.{Map => jMap}
import scala.collection.JavaConverters._

/**
 * Returns json like: <br />
 * <pre>
 * {
 * "batchesRootDir":"/tmp/ABP10062022",
 * "downloadFiles":[
 * "/tmp/download/AB76GB_CSV.zip",
 * "/tmp/download/ABPRIS_CSV.zip"
 * ]
 * }
 * </pre>
 */
class AddressLookupFileDownloadFunction extends RequestHandler[java.util.Map[String, String], jMap[String, Object]] {
  override def handleRequest(data: jMap[String, String], context: Context): jMap[String, Object] = {
    val epochOverride: Option[String] = data.asScala.get("epoch")
    doFileDownload(epochOverride)
  }

  def doFileDownload(epochOverride: Option[String]): jMap[String, Object] = {
    epochOverride.fold {
      val downloader = FileDownloader()
      downloader.download() match {
        case Left(UnexpectedSize(fileName)) => throw new IllegalStateException(s"actual size does not match for $fileName")
        case Left(SizeTooSmall(fileName))   => throw new IllegalStateException(s"size condition not met for $fileName")
        case Left(MD5NotMatched(fileName))  => throw new IllegalStateException(s"md5 mismatch for file $fileName")
        case Right((batchesRootDir, downloadedFiles)) =>

          Map(
            "batchesRootDir" -> batchesRootDir,
            "unpack" -> "true",
            "downloadedFiles" -> downloadedFiles.toList.asJava).asJava
      }
    } { epoch =>
      Map(
        "batchesRootDir" -> s"${FileDownloader.outputRoot}/${epoch}",
        "unpack" -> "false",
        "epoch" -> epoch,
        "downloadedFiles" -> new util.ArrayList[Object]().asInstanceOf[Object]).asJava
    }
  }
}
