import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import scala.collection.JavaConverters.{mapAsJavaMapConverter, seqAsJavaListConverter}

class AddressLookupFileListFunction extends RequestHandler[String, java.util.Map[String, java.util.List[String]]] {
  override def handleRequest(input: String, context: Context): java.util.Map[String, java.util.List[String]] = {
    val remoteTree = AddressLookup.sardineWrapper.exploreRemoteTree

    val fileUrls = {
      AddressLookup.productTypes.flatMap(p => remoteTree.findLatestFor(p))
        .flatMap(p => p.zips)
        .map(_.url.toString)
    }

    Map("files" -> fileUrls.asJava).asJava
  }
}
