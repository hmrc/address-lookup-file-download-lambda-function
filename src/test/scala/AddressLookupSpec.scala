import fetch.{OSGBProduct, SardineWrapper}
import me.lamouri.JCredStash
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
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

    val filesToTest = Seq(
      "abi" -> "AddressBasePremium_ISL_FULL_2021-09-29_001_csv.zip",
      "abi" -> "AddressBasePremium_ISL_FULL_2021-09-29_002_csv.zip",
      "abp" -> "AddressBasePremium_FULL_2021-09-29_001_csv.zip",
      "abp" -> "AddressBasePremium_FULL_2021-09-29_002_csv.zip"
    )
    val fileUrlToTest = filesToTest.map{ case(p, f) => new URL(s"https://hfs.os.uk/${p}/88/full/data/${f}")}
    val outputRootPath = createOutputPathAndFile(filesToTest)
    val addressLookup = new AddressLookupBase(outputPath = outputRootPath, () => credstash, (a, b) => sardineWrapper)
    val epoch = 88

    when(sardineWrapper.listAllProductsAvailableToDownload(any(), any())).thenReturn(
      Seq(
        OSGBProduct("abi", epoch, List(
          new URL("https://hfs.os.uk/abi/88/full/data/AddressBasePremium_ISL_FULL_2021-09-29_001_csv.zip"),
          new URL("https://hfs.os.uk/abi/88/full/data/AddressBasePremium_ISL_FULL_2021-09-29_002_csv.zip"))),
        OSGBProduct("abp", epoch, List(
          new URL("https://hfs.os.uk/abp/88/full/data/AddressBasePremium_FULL_2021-09-29_001_csv.zip"),
          new URL("https://hfs.os.uk/abp/88/full/data/AddressBasePremium_FULL_2021-09-29_002_csv.zip")))
      ))

    when(credstash.getSecret(any(), any(), any())).thenReturn("secret")

    "return a partiall list of file to download the don't include already downloaded ones" when {
      "listFiles is called with forceDownload=false" in {
        val response = addressLookup.listFiles(Some(epoch.toString), false)
        response.batches.flatMap(_.files) shouldBe empty
      }
    }

    "return a full list of file to download" when {
      "listFiles is called with forceDownload=true" in {
        val response = addressLookup.listFiles(Some(epoch.toString), true)
        response.batches.flatMap(_.files) should contain allElementsOf fileUrlToTest
      }
    }
  }

  private def createOutputPathAndFile(filesToCreate: Seq[(String, String)]): String = {
    val outputRootPath = Paths.get(new File(".").getAbsolutePath, "tmp", "test")
    Files.createDirectories(outputRootPath)
    filesToCreate.foreach {
      case (product, fileName) =>
        val doneFileName = s"${fileName}.done"
        val outputDataPath = Paths.get(outputRootPath.toString, "88", product, "0")
        val doneFilePath = Paths.get(outputDataPath.toString, doneFileName)

        Files.createDirectories(outputDataPath)
        if (!Files.exists(doneFilePath)) {
          Files.createFile(doneFilePath)
        }
    }
    outputRootPath.toFile.getAbsolutePath
  }
}