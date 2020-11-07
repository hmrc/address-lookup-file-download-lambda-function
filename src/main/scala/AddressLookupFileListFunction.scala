import java.io.File
import java.net.URL
import java.util

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.jessecoyle.JCredStash
import fetch.{OSGBProduct, SardineFactory2, SardineWrapper, WebDavTree, WebdavFetcher}

import scala.collection.JavaConverters.{mapAsJavaMapConverter, seqAsJavaListConverter}

class AddressLookupFileListFunction {
  def handler(input: String, context: Context ): java.util.Map[String, java.util.List[String]] = {
    val hfsUrl = "https://hfs.os.uk/"
    val user = AddressLookupFileListFunction.retrieveCredential("address_lookup_user")
    val password = AddressLookupFileListFunction.retrieveCredential("address_lookup_password")
    val products = Seq("abp", "abi")

    //TODO: Actually download the file
    val sardineWrapper = new SardineWrapper(new URL(hfsUrl), user, password, None, new SardineFactory2)
    val webDavFetcher = new WebdavFetcher(sardineWrapper, new File("some_where_to_download_files"), context.getLogger)
    val remoteTree = sardineWrapper.exploreRemoteTree
    val fileUrls =
      products.flatMap(p => remoteTree.findLatestFor(p))
        .flatMap(p => p.zips)
        .map(_.url.toString)

    Map("files" -> fileUrls.asJava).asJava
  }
}

object AddressLookupFileListFunction {
  val context: java.util.HashMap[String,String] = {
    val hm = new util.HashMap[String,String]()
    hm.put("role", "address_lookup")
    hm
  }

  def retrieveCredential(credName: String): String = {
    new JCredStash().getSecret(credName, context)
  }
}
