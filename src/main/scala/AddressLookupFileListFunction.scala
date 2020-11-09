import com.amazonaws.services.lambda.runtime.Context

import scala.collection.JavaConverters.{mapAsJavaMapConverter, seqAsJavaListConverter}

class AddressLookupFileListFunction {
  def handler(notUsed: String, context: Context ): java.util.Map[String, java.util.List[String]] = {
    val remoteTree = AddressLookup.sardineWrapper.exploreRemoteTree
    val currentEpoch = AddressLookup.currentEpoch

    val fileUrls = {
      import AddressLookup.appConfig.addressLookup.hfs
      hfs.productTypes.flatMap(p => remoteTree.findLatestFor(p))
        .filterNot(p => p.epoch <= currentEpoch)
        .flatMap(p => p.zips)
        .map(_.url.toString)
    }

    Map("files" -> fileUrls.asJava).asJava
  }
}
