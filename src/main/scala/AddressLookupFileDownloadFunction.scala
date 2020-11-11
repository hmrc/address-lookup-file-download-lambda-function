import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class AddressLookupFileDownloadFunction extends RequestHandler[String, Int] {
  override def handleRequest(fileToDownload: String, context: Context): Int = {
    AddressLookup.downloadFileToOutputDirectory(fileToDownload)

    // What to return here?
    0
  }
}
