import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger, RequestHandler}

import java.util.{Map => JMap}
import scala.collection.JavaConverters._

class AddressLookupFileListFunction extends RequestHandler[JMap[String, Object], JMap[String, Object]] {

  override def handleRequest(requestMap: JMap[String, Object], context: Context): JMap[String, Object] = {
    val scalaMap = requestMap.asScala
    val epoch = scalaMap.get("epoch").map(_.asInstanceOf[String])
    val forceDownload = scalaMap.contains("forceDownload")

    // We should only ever get one epoch back from listAllFileUrlsToDownload
    val addressLookupFileListResponse = AddressLookup.listFiles(epoch, forceDownload)

    addressLookupFileListResponse.asJava()
  }
}