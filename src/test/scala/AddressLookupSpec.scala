import fetch.{OSGBProduct, SardineWrapper, WebDavFile, WebDavTree}
import me.lamouri.JCredStash
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import java.io.File
import java.net.URL
import java.nio.file.{Files, Paths}

class AddressLookupSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "AddressLookup" should {
    val sardineWrapper = mock[SardineWrapper]
    val credstash = mock[JCredStash]

    val fileToTest = "AddressBasePremium_ISL_FULL_2021-09-29_001_csv.zip"
    val fileUrlToTest = new URL("https://hfs.os.uk/abi/88/full/data/" + fileToTest)
    val outputRootPath = createOutputPathAndFile("abi", fileToTest)
    val addressLookup = new AddressLookupBase(outputPath = outputRootPath, () => credstash, (a, b) => sardineWrapper)
    val epoch = 88

    when(sardineWrapper.exploreRemoteTree).thenReturn(
      WebDavTree(
        WebDavFile(new URL("https://hfs.os.uk/"), "", 0, true, false, false, List(
          WebDavFile(new URL("https://hfs.os.uk/abi/"), "abi", 0, true, false, false, List(
            WebDavFile(new URL("https://hfs.os.uk/abi/88/"), "88", 0, true, false, false, List(
              WebDavFile(new URL("https://hfs.os.uk/abi/88/full/"), "full", 0, true, false, false, List(
                WebDavFile(new URL("https://hfs.os.uk/abi/88/full/data/"), "data", 0, true, false, false, List(
                  WebDavFile(new URL("https://hfs.os.uk/abi/88/full/data/AddressBasePremium_ISL_FULL_2021-09-29_001_csv.zip"), "AddressBasePremium_ISL_FULL_2021-09-29_001_csv.zip", 2685, false, false, true, List()),
                  WebDavFile(new URL("https://hfs.os.uk/abi/88/full/data/AddressBasePremium_ISL_FULL_2021-09-29_002_csv.zip"), "AddressBasePremium_ISL_FULL_2021-09-29_002_csv.zip", 32336, false, false, true, List()))),
                WebDavFile(new URL("https://hfs.os.uk/abi/88/full/ready-to-collect.txt"), "ready-to-collect.txt", 0, false, true, false, List()))))))),
          WebDavFile(new URL("https://hfs.os.uk/abp/"), "abp", 0, true, false, false, List(
            WebDavFile(new URL("https://hfs.os.uk/abp/.DS_Store"), ".DS_Store", 6, false, false, false, List()),
            WebDavFile(new URL("https://hfs.os.uk/abp/._.DS_Store"), "._.DS_Store", 4, false, false, false, List()),
            WebDavFile(new URL("https://hfs.os.uk/abp/88/"), "88", 0, true, false, false, List(
              WebDavFile(new URL("https://hfs.os.uk/abp/88/full/"), "full", 0, true, false, false, List(
                WebDavFile(new URL("https://hfs.os.uk/abp/88/full/data/"), "data", 0, true, false, false, List(
                  WebDavFile(new URL("https://hfs.os.uk/abp/88/full/data/AddressBasePremium_FULL_2021-09-29_001_csv.zip"), "AddressBasePremium_FULL_2021-09-29_001_csv.zip", 48430, false, false, true, List()),
                  WebDavFile(new URL("https://hfs.os.uk/abp/88/full/data/AddressBasePremium_FULL_2021-09-29_002_csv.zip"), "AddressBasePremium_FULL_2021-09-29_002_csv.zip", 38471, false, false, true, List()))),
                WebDavFile(new URL("https://hfs.os.uk/abp/88/full/ready-to-collect.txt"), "ready-to-collect.txt", 0, false, true, false, List())))))))))
      ))
    when(credstash.getSecret(any(), any(), any())).thenReturn("secret")

    "return a full list of file to download" when {
      "listFiles is called with forceDownload=true" in {
        val response = addressLookup.listFiles(Some(epoch.toString), true)
        response.batches(1).files should contain(fileUrlToTest)
      }
    }

    "return a partiall list of file to download the don't include already downloaded ones" when {
      "listFiles is called with forceDownload=false" in {
        val response = addressLookup.listFiles(Some(epoch.toString), false)
        response.batches(1).files should not contain fileUrlToTest
      }
    }
  }

  private def createOutputPathAndFile(productType: String, fileName: String): String = {
    val doneFileName = s"${fileName}.done"
    val outputRootPath = sys.props("java.io.tmpdir").replaceAll("/$", "")
    val outputDataPath = outputRootPath + s"88/$productType/0/"
    val doneFilePath = outputDataPath + doneFileName

    new File(outputDataPath).mkdirs()
    if (!Files.exists(Paths.get(doneFilePath))) {
      Files.createFile(Paths.get(doneFilePath))
    }

    outputRootPath
  }
}