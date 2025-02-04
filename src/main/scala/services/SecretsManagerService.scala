package services

import com.amazonaws.secretsmanager.caching.SecretCache
import play.api.libs.json.Json

class SecretsManagerService {

  val secretCache: SecretCache = new SecretCache()

  def getSecret(secretId: String): String = {
    val secretString = secretCache.getSecretString(secretId)
    val secretAsJSON = Json.parse(secretString)

    secretAsJSON("secret").as[String]
  }
}