import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class AddressLookupFileDownloadFunction extends RequestHandler[java.util.Map[String, Any], Int] {
  override def handleRequest(fileInfo: java.util.Map[String, Any], context: Context): Int = {
    AddressLookup.downloadFileToOutputDirectory(
      fileInfo.get("productName").asInstanceOf[String],
      fileInfo.get("epoch").asInstanceOf[String],
      fileInfo.get("fileUrl").asInstanceOf[String])

    // What to return here?
    0
  }
}
