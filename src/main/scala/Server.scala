import skinny.micro._
import skinny.logging.Logger
import com.typesafe.config.{ConfigFactory, Config => TypesafeConfig}
import assignments.AssignmentsController
import assignments.todoist.{TodoistClient, Client => Todoist}
import folders.FoldersController
import folders.drive.{DriveClient, Client => Drive}
import common.Constants

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.Random

object Server extends AsyncWebApp {

  override val logger: Logger = Logger("uniswag")
  val conf: TypesafeConfig    = ConfigFactory.load
  val random: Random          = new Random()

  get("/") { implicit ctx =>
    val msg = Constants.Messages(random.nextInt(Constants.Messages.size))
    Future.successful(Ok("uniswag\n" + msg))
  }

  post("/assignments") { implicit ctx =>
    val todoistClient: TodoistClient                 = Todoist(conf)
    val assignmentsController: AssignmentsController = new AssignmentsController(todoistClient)
    val assignmentsParam: Option[Seq[String]]        = formMultiParams.getAs[String]("assignments")
    val subjectNameParam: Option[String]             = formParams.getAs[String]("subject")

    (assignmentsParam, subjectNameParam) match {
      case (Some(assignments), Some(subjectName)) =>
        assignmentsController.oauthRedirect(assignments, subjectName)
      case _ =>
        Future.successful(BadRequest)
    }
  }

  get("/assignments/callback") { implicit ctx =>
    val todoistClient: TodoistClient                 = Todoist(conf)
    val assignmentsController: AssignmentsController = new AssignmentsController(todoistClient)
    val codeParam: Option[String]                    = params.get("code")
    val stateParam: Option[String]                   = params.get("state")
    val requestIdParam: Option[String]               = cookies.get(Constants.RequestIdHeader)

    (codeParam, stateParam, requestIdParam) match {
      case (Some(code), Some(state), Some(requestId)) =>
        assignmentsController.oauthCallback(code, state, requestId)
      case _ =>
        Future.successful(BadRequest)
    }
  }

  post("/folders") { implicit ctx =>
    val driveClient: DriveClient                      = Drive(conf)
    val foldersController: FoldersController          = new FoldersController(driveClient)
    val subjectsNamesSourceParam: Option[Seq[String]] = formMultiParams.getAs[String]("subjects")
    val parentFolderNameParam: Option[String]         = formParams.getAs[String]("parentFolderName")

    (subjectsNamesSourceParam, parentFolderNameParam) match {
      case (Some(subjectsNamesSource), Some(parentFolderName)) =>
        foldersController.oauthRedirect(subjectsNamesSource, parentFolderName)
      case _ =>
        Future.successful(BadRequest)
      }
  }

  get("/folders/callback") { implicit ctx =>
    val driveClient: DriveClient             = Drive(conf)
    val foldersController: FoldersController = new FoldersController(driveClient)
    val codeParam: Option[String]            = params.get("code")
    val stateParam: Option[String]           = params.get("state")
    val requestIdParam: Option[String]       = cookies.get(Constants.RequestIdHeader)

    (codeParam, stateParam, requestIdParam) match {
      case (Some(oauthCode), Some(csrf), Some(requestId)) =>
        foldersController.oauthCallback(oauthCode, csrf, requestId)
      case _ =>
        Future.successful(BadRequest)
    }
  }
}

object Main extends App {
  import scala.util.Properties

  val port = Properties.envOrElse("PORT", "8080").toInt

  WebServer.mount(Server).port(port).start()
}
