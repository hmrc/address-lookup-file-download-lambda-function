import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger, RequestHandler}

import java.util.{Map => JMap}

class AddressLookupFileListFunction extends RequestHandler[JMap[String, Object], JMap[String, Object]] {

  override def handleRequest(requestMap: JMap[String, Object], context: Context): JMap[String, Object] = {
    val request = AddressLookupFileListRequest(requestMap)

    // We should only ever get one epoch back from listAllFileUrlsToDownload
    val addressLookupFileListResponse = AddressLookup.listFiles(request)

    addressLookupFileListResponse.asJava()
  }
}