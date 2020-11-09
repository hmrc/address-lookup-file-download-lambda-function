import java.io.File
import java.net.URL

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class AddressLookupFileDownloadFunction extends RequestHandler[String, Int] {
  override def handleRequest(fileToDownload: String, context: Context): Int = {
    val sardineWrapper = AddressLookup.sardineWrapper
    val webDavFetcher = AddressLookup.webDavFetcher(context)

    webDavFetcher.fetchFile(new URL(fileToDownload), sardineWrapper.begin, new File("."))

    // What to return here?
    0
  }
}
