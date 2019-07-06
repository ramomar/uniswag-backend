package common

import java.util.UUID

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scalacache._
import scalacache.caffeine.CaffeineCache

trait RequestsCache[T] {
  protected implicit val scalaCache = ScalaCache(CaffeineCache())
  protected implicit val cache      = typed[T, NoSerialization]

  def save(request: T)(implicit ec: ExecutionContext): Future[String] = {
    val id = UUID.randomUUID().toString
    cache.put(id)(request, Some(10.minutes)).map(_ => id)
  }

  def retrieve(id: String): Future[Option[T]] =
    cache.get(id)
}
