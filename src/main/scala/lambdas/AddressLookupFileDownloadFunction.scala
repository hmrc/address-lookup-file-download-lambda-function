package lambdas

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import processing.FileDownloader

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
class AddressLookupFileDownloadFunction extends RequestHandler[java.util.Map[String, Object], jMap[String, Object]] {
  override def handleRequest(unused: jMap[String, Object], context: Context): jMap[String, Object] = {
    doFileDownload()
  }

  def doFileDownload(): jMap[String, Object] = {
    val downloader = FileDownloader()
    val (batchesRootDir, downloadedFiles) = downloader.download()

    Map("batchesRootDir" -> batchesRootDir, "downloadedFiles" -> downloadedFiles.toList.asJava).asJava
  }
}
