package care.freed.integration.data

import scala.util.{Failure, Success, Try}

sealed trait NetworkResponse[+T] {
  def map[U](f: T => U): NetworkResponse[U] = {
    this match {
      case NetworkSuccess(data, code) =>
        NetworkSuccess(f(data), code)
      case failure: AuthFailure => failure
      case failure: NetworkError => failure
      case failure: DeserializationError => failure
      case failure: ApiError => failure
    }
  }

  def exception: Option[Throwable]

  def toTry: Try[T] = {
    this match {
      case NetworkSuccess(data, _) => Success(data)
      case failure => Failure(failure.exception.get)
    }
  }
}

final case class NetworkSuccess[T](data: T, code: Int) extends NetworkResponse[T] {
  override def exception: Option[Throwable] = None
}

final case class AuthFailure(ex: Throwable) extends NetworkResponse[Nothing] {
  override def exception: Option[Throwable] = Some(ex)
}
final case class NetworkError(ex: Throwable) extends NetworkResponse[Nothing] {
  override def exception: Option[Throwable] = Some(ex)
}
final case class DeserializationError(code: Int, reason: String, rawResponse: String) extends NetworkResponse[Nothing] {
  override def exception: Option[Throwable] = Some(new Exception(reason))
}
final case class ApiError(code: Int, message: String) extends NetworkResponse[Nothing] {
  override def exception: Option[Throwable] = Some(new Exception(message))
}

