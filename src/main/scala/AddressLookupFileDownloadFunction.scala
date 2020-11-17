import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import scala.collection.JavaConverters._

class AddressLookupFileDownloadFunction extends RequestHandler[java.util.Map[String, Any], String] {
  override def handleRequest(batchInfo: java.util.Map[String, Any], context: Context): String = {

    val files = batchInfo.get("files").asInstanceOf[java.util.List[String]].asScala
    val productName = batchInfo.get("productName").asInstanceOf[String]
    val epoch = batchInfo.get("epoch").asInstanceOf[String]
    val batchIndex = batchInfo.get("batchIndex").asInstanceOf[String]

    val targetDirectory = AddressLookup.batchTargetDirectory(productName, epoch, batchIndex)

    files.foreach { f =>
      println(s"Downloading $productName, $epoch, $batchIndex, $f")
      AddressLookup.downloadFileToOutputDirectory(targetDirectory, f)
    }

    targetDirectory
  }
}
