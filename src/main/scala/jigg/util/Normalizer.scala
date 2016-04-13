package jigg.util

/*
 Copyright 2013-2016 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

import com.ibm.icu.text.Transliterator

object Normalizer {

  /** Replace all half space characters in ascii (< 0x7F) to full space characters.
    *
    * Useful for preprocessing in some Japanese software such as JUMAN and KNP.
    *
    * NOTE: We do not touch hankaku kana characters since they make alignment to the
    * original text more involved.
    */
  def hanZenAscii(text: String) = text map {
    case c if c <= 0x7F => hanzenTrans.transliterate(c + "")(0)
    case c => c
  }
  private val hanzenTrans = Transliterator.getInstance("Halfwidth-Fullwidth")
}
