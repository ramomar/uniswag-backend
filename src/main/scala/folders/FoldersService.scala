package folders

import folders.drive.DriveClient
import folders.drive.commands.CreateFolderCommand
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.HttpResponse

trait FoldersService {
  def createFolders(parentFolderName: String,
                    folders: Seq[String]): Future[Boolean]
}

class FoldersServiceClient(driveClient: DriveClient,
                           oauthCode: String)
                          (implicit ec: ExecutionContext) extends FoldersService {

  def createFolders(parentFolderName: String,
                    folders: Seq[String]): Future[Boolean] =
    for {
      tokenRequestResponse              <- driveClient.accessToken(oauthCode)
      token                             = extractToken(tokenRequestResponse)
      drive                             = driveClient.submitCommand(token) _
      driveBatch                        = driveClient.submitCommandBatch(token) _
      createParentFolderRequestResponse <- drive(CreateFolderCommand(parentFolderName))
      parentFolderId                    = extractFolderId(createParentFolderRequestResponse)
      foldersToCreate                   = makeFoldersToCreate(folders, parentFolderId)
      createFoldersRequestResponse      <- driveBatch(foldersToCreate)
    } yield createFoldersRequestResponse.isSuccess

  private def makeFoldersToCreate(folders: Seq[String],
                                  parentFolderId: String): Seq[CreateFolderCommand] =
    folders.map(f => CreateFolderCommand(f, Seq(parentFolderId)))

  private def extractFolderId(response: HttpResponse[String]): String =
    (Json.parse(response.body) \ "id").as[String]

  private def extractToken(response: HttpResponse[String]): String =
    (Json.parse(response.body) \ "access_token").as[String]
}
