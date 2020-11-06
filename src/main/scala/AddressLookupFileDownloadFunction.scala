import java.util

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.jessecoyle.JCredStash

class AddressLookupFileDownloadFunction {
  def handler(input: String, context: Context): Int = {
//    val bucket = System.getenv("BUCKET_NAME") //Do we need this as files should be downloaded to EFS?
//    val region = System.getenv("AWS_REGION") //Ditto - no need to reference this anywhere?
    val user = AddressLookupFileDownloadFunction.retrieveCredential("address_lookup_file_download_user")
    val password = AddressLookupFileDownloadFunction.retrieveCredential("address_lookup_file_download_password")

    //TODO: Actually download the file

    0
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
