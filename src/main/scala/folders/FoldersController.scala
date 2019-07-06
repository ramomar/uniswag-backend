package folders

import skinny.micro.response._
import skinny.micro.contrib.csrf.CSRFTokenGenerator
import skinny.micro.cookie.Cookie
import common.Constants
import folders.drive.DriveClient

import scala.concurrent.{ExecutionContext, Future}

class FoldersController(client: DriveClient)
                       (implicit ec: ExecutionContext) {

  def oauthRedirect(subjectNames: Seq[String],
                    parentFolderName: String): Future[ActionResult] = {
    val csrf    = CSRFTokenGenerator()
    val request = FoldersRequest(parentFolderName, subjectNames, csrf)

    FoldersRequestCache
      .save(request)
      .map { cacheId =>
        PermanentRedirect(
          location = client.oauthUrl(csrf),
          cookies  = Seq(Cookie(Constants.RequestIdHeader, cacheId)),
          headers  = Map("Access-Control-Allow-Origin" -> "*")
        )
      }
  }

  def oauthCallback(oauthCode: String,
                    csrf: String,
                    requestId: String): Future[ActionResult] = {
    FoldersRequestCache
      .retrieve(requestId)
      .flatMap {
        case Some(FoldersRequest(parentFolder, folders, csrfCode))
          if csrfCode == csrf && folders.nonEmpty =>
          new FoldersServiceClient(client, oauthCode)
            .createFolders(parentFolder, folders)
            .map {
              case true  => PermanentRedirect("https://drive.google.com")
              case false => ServiceUnavailable("Something went wrong.")
            }
        case _ =>
          val response =
            BadRequest("Invalid request sequence/Cache expired/Bad CSRF/No folders to add.")
          Future.successful(response)
    }
  }
}
