package jigg.pipeline

sealed trait SystemDic

object SystemDic {
  case object ipadic extends SystemDic
  case object jumandic extends SystemDic
  case object unidic extends SystemDic
}
