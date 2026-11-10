package care.freed.integration

import java.time.{Duration, LocalDateTime}

class Cache[T](timeToLive: Duration, refreshMethod: () => T) {
  private var cachedValue: T = _
  private var expiryTime: LocalDateTime = LocalDateTime.now.minusSeconds(1) // Set it to some time in past to immediately invalidate on first call

  def getValue: T = synchronized {
    val currTime = LocalDateTime.now()
    if (currTime.isAfter(expiryTime)) {
      invalidate()
    }

    return cachedValue
  }

  def invalidate(): T = synchronized {
    cachedValue = refreshMethod()
    expiryTime = LocalDateTime.now().plus(timeToLive)

    return cachedValue
  }
}
