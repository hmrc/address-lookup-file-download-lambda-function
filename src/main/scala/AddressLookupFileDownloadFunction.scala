import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import scala.collection.JavaConverters._

class AddressLookupFileDownloadFunction extends RequestHandler[java.util.Map[String, Any], String] {
  override def handleRequest(batchInfo: java.util.Map[String, Any], context: Context): String = {
    val files = batchInfo.get("files").asInstanceOf[java.util.List[String]].asScala
    val batchDir = batchInfo.get("batchDir").asInstanceOf[String]

    files.foreach { f =>
      println(s"Downloading $f to $batchDir")
      AddressLookup.downloadFileToOutputDirectory(batchDir, f)
    }

    batchDir
  }
}
