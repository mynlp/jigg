package jigg.util

case class IDGenerator(prefix: String) {
  private[this] val stream = Stream.from(0).iterator
  def next() = prefix + stream.next
}
