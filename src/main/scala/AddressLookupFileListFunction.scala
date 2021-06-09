import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import java.util.{Map => JMap}

class AddressLookupFileListFunction extends RequestHandler[String, JMap[String, Object]] {
  override def handleRequest(requestedEpoch: String, context: Context): JMap[String, Object] = {
    // We should only ever get one epoch back from listAllFileUrlsToDownload
    val addressLookupFileListResponse = AddressLookup.listAllNewFileUrlsToDownload(requestedEpoch)

    addressLookupFileListResponse.asJava()
  }
}
