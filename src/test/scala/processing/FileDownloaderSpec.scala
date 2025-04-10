package processing

import utils.BaseSpec

class FileDownloaderSpec extends BaseSpec {

  "FileDownloader.redactKey" should {

    "redact key in text when key is present" in {
      val fileDownloader = new FileDownloader("testAuthKey")
      val text = "https://api.example.com/data?key=testAuthKey"
      val result = fileDownloader.redactKey(text)
      result shouldEqual "https://api.example.com/data?key=****"
    }

    "return original text when key is not present" in {
      val fileDownloader = new FileDownloader("testAuthKey")
      val text = "https://api.example.com/data?key=anotherKey"
      val result = fileDownloader.redactKey(text)
      result shouldEqual text
    }

    "return original text when there is no key parameter" in {
      val fileDownloader = new FileDownloader("testAuthKey")
      val text = "https://api.example.com/data"
      val result = fileDownloader.redactKey(text)
      result shouldEqual text
    }
  }

}
