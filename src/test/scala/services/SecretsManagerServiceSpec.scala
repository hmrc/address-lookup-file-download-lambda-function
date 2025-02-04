package services

import com.amazonaws.secretsmanager.caching.SecretCache
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

class SecretsManagerServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "SecretsManagerService" should {

    "return the secret value when secretId is valid" in {
      val secretId = "validSecretId"
      val secretString = """{"secret": "mySecretValue"}"""

      val mockSecretCache = mock[SecretCache]
      when(mockSecretCache.getSecretString(secretId)).thenReturn(secretString)

      val service = new SecretsManagerService {
        override val secretCache: SecretCache = mockSecretCache
      }

      val result = service.getSecret(secretId)
      result shouldEqual "mySecretValue"
    }

    "throw an exception when secret string is not a valid JSON" in {
      val secretId = "validSecretId"
      val secretString = """invalidJson"""

      val mockSecretCache = mock[SecretCache]
      when(mockSecretCache.getSecretString(secretId)).thenReturn(secretString)

      val service = new SecretsManagerService {
        override val secretCache: SecretCache = mockSecretCache
      }

      an[Exception] should be thrownBy service.getSecret(secretId)
    }

    "throw an exception when secret key is missing in JSON" in {
      val secretId = "validSecretId"
      val secretString = """{"someOtherKey": "mySecretValue"}"""

      val mockSecretCache = mock[SecretCache]
      when(mockSecretCache.getSecretString(secretId)).thenReturn(secretString)

      val service = new SecretsManagerService {
        override val secretCache: SecretCache = mockSecretCache
      }

      an[NoSuchElementException] should be thrownBy service.getSecret(secretId)
    }
  }
}
