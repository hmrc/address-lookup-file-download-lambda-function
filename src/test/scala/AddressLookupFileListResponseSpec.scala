import com.fasterxml.jackson.databind.ObjectMapper
import fetch.{OSGBProduct, WebDavFile}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.StringWriter
import java.net.URL
import java.nio.file.{Files, Paths}
import scala.io.Source

class AddressLookupFileListResponseSpec extends AnyWordSpec with Matchers {

  "building a response" when {

    "file download list contains multiple products" should {
      val epoch = 99
      val products = Seq(
        OSGBProduct("abp", epoch, List(WebDavFile(new URL("http://example.com/abp.file"), "abp.file"))),
        OSGBProduct("abi", epoch, List(WebDavFile(new URL("http://example.com/abi.file"), "abi.file"))))

      val response = AddressLookupFileListResponse("", products)

      "create a batch for each product" in {
        response.epoch shouldBe s"$epoch"

        response.batches.size shouldBe 2
        response.batches should contain(Batch(s"${AddressLookup.outputPath}/$epoch/abp/0",
          List(new URL("http://example.com/abp.file"))))
        response.batches should contain(Batch(s"${AddressLookup.outputPath}/$epoch/abi/0",
          List(new URL("http://example.com/abi.file"))))
      }

      "create correct structure when 'serialised' to java map" in {
        val javaMapResponse = response.asJava()
        val mapper = new ObjectMapper()
        val responseStr = new StringWriter()
        mapper.writeValue(responseStr, javaMapResponse)
        val expectedResponseStr = """{"epoch":"99","batches":[{"batchDir":"/mnt/efs/99/abp/0","files":["http://example.com/abp.file"]},{"batchDir":"/mnt/efs/99/abi/0","files":["http://example.com/abi.file"]}]}"""
        responseStr.toString shouldBe expectedResponseStr
      }
    }

    "an individual product contains multiple batches" should {
      val epoch = 99
      val files = (1 to 30).toList map { case r =>
        WebDavFile(new URL(s"http://example.com/abp$r.file"), s"abp$r.file")
      }

      val products = Seq(OSGBProduct("abp", epoch, files))
      val response = AddressLookupFileListResponse(epoch.toString, products)

      "create two batches" in {
        response.epoch shouldBe s"$epoch"

        response.batches.size shouldBe 2
        val batch1 :: batch2 :: _ = response.batches

        batch1.files.length shouldBe 25
        batch1.batchDir shouldBe s"${AddressLookup.outputPath}/$epoch/abp/0"
        batch2.files.length shouldBe 5
        batch2.batchDir shouldBe s"${AddressLookup.outputPath}/$epoch/abp/1"
      }

      "create correct structure when 'serialised' to java map" in {
        val javaMapResponse = response.asJava()
        val mapper = new ObjectMapper()
        val responseStr = new StringWriter()
        mapper.writeValue(responseStr, javaMapResponse)
        val expectedResponseStr = """{"epoch":"99","batches":[{"batchDir":"/mnt/efs/99/abp/0","files":["http://example.com/abp1.file","http://example.com/abp2.file","http://example.com/abp3.file","http://example.com/abp4.file","http://example.com/abp5.file","http://example.com/abp6.file","http://example.com/abp7.file","http://example.com/abp8.file","http://example.com/abp9.file","http://example.com/abp10.file","http://example.com/abp11.file","http://example.com/abp12.file","http://example.com/abp13.file","http://example.com/abp14.file","http://example.com/abp15.file","http://example.com/abp16.file","http://example.com/abp17.file","http://example.com/abp18.file","http://example.com/abp19.file","http://example.com/abp20.file","http://example.com/abp21.file","http://example.com/abp22.file","http://example.com/abp23.file","http://example.com/abp24.file","http://example.com/abp25.file"]},{"batchDir":"/mnt/efs/99/abp/1","files":["http://example.com/abp26.file","http://example.com/abp27.file","http://example.com/abp28.file","http://example.com/abp29.file","http://example.com/abp30.file"]}]}"""
        responseStr.toString shouldBe expectedResponseStr
      }
    }

    "some files have already been downloaded" should {
      val epoch = 99
      val files = (1 to 5).toList map { case r =>
        WebDavFile(new URL(s"http://example.com/abp$r.file"), s"abp$r.file")
      }

      val doneFiles = Seq(
        s"${AddressLookup.outputPath}/$epoch/abp/0/abp1.file.done",
        s"${AddressLookup.outputPath}/$epoch/abp/0/abp3.file.done")

      val products = Seq(OSGBProduct("abp", epoch, files))
      val response = AddressLookupFileListResponse(epoch.toString, products, doneFiles)

      "ignore the files that have already been downloaded" in {
        val batch1 = response.batches.head
        batch1.files.length shouldBe 3
      }

      "create correct structure when 'serialised' to java map" in {
        val javaMapResponse = response.asJava()
        val mapper = new ObjectMapper()
        val responseStr = new StringWriter()
        mapper.writeValue(responseStr, javaMapResponse)
        val expectedResponseStr = """{"epoch":"99","batches":[{"batchDir":"/mnt/efs/99/abp/0","files":["http://example.com/abp2.file","http://example.com/abp4.file","http://example.com/abp5.file"]}]}"""
        responseStr.toString shouldBe expectedResponseStr
      }
    }
  }
}
