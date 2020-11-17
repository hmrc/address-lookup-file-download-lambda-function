import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import scala.collection.JavaConverters._

class AddressLookupFileDownloadFunction extends RequestHandler[java.util.Map[String, Any], Int] {
  override def handleRequest(batchInfo: java.util.Map[String, Any], context: Context): Int = {

    batchInfo.get("files").asInstanceOf[java.util.List[String]].asScala.foreach { f =>
      AddressLookup.downloadFileToOutputDirectory(
        batchInfo.get("productName").asInstanceOf[String],
        batchInfo.get("epoch").asInstanceOf[String],
        batchInfo.get("batchIndex").asInstanceOf[String],
        f)
    }

    // What to return here?
    0
  }
}
