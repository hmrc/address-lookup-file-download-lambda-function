import java.util

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.jessecoyle.JCredStash

class AddressLookupFileDownloadFunction extends RequestHandler[String,  util.Map[String, util.List[String]]] with scala.collection.convert.AsJavaConverters {
  override def handleRequest(input: String, context: Context): util.Map[String,  util.List[String]] = {
//    val bucket = System.getenv("BUCKET_NAME") //Do we need this as files should be downloaded to EFS?
//    val region = System.getenv("AWS_REGION") //Ditto - no need to reference this anywhere?
//    val user = AddressLookupFileDownloadFunction.retrieveCredential("address_lookup_file_download_user")
//    val password = AddressLookupFileDownloadFunction.retrieveCredential("address_lookup_file_download_password")

    //TODO: Actually download the file

    mapAsJavaMap(Map("files" -> seqAsJavaList(Seq("file_1", "file_ 2", "file_3"))))
  }
}

object AddressLookupFileDownloadFunction {
  val context: java.util.HashMap[String,String] = {
    val hm = new util.HashMap[String,String]()
    hm.put("role", "address-lookup-file-download")
    hm
  }

  def retrieveCredential(credName: String): String = {
    new JCredStash().getSecret(credName, context)
  }
}
