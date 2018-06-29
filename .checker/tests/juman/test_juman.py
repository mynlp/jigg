import sys
sys.path.append(".checker/tests")

from basetest import BaseTest
from constant import (
    JIGG_JAR,
    JIGG_MODEL_JAR,
    CORENLP_MODEL_JAR
)


class TestJuman(BaseTest):
    '''
    '''
    def setUp(self):
        self.input_text = "太郎はこの本を二郎を見た女性に渡した。"

        self.expected_text = """<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="19" characterOffsetBegin="0" id="s0">
        太郎はこの本を二郎を見た女性に渡した。
        <tokens normalized="true" annotators="juman">
          <token misc="&quot;人名:日本:名:45:0.00106&quot;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="5" pos1="人名" posId="6" pos="名詞" lemma="太郎" yomi="たろう" characterOffsetEnd="2" characterOffsetBegin="0" form="太郎" id="s0_tok0"/>
          <token misc="NIL" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="2" pos1="副助詞" posId="9" pos="助詞" lemma="は" yomi="は" characterOffsetEnd="3" characterOffsetBegin="2" form="は" id="s0_tok1"/>
          <token misc="NIL" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="2" pos1="連体詞形態指示詞" posId="7" pos="指示詞" lemma="この" yomi="この" characterOffsetEnd="5" characterOffsetBegin="3" form="この" id="s0_tok2"/>
          <token misc="&quot;代表表記:本/ほん 漢字読み:音 カテゴリ:人工物-その他;抽象物&quot;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="普通名詞" posId="6" pos="名詞" lemma="本" yomi="ほん" characterOffsetEnd="6" characterOffsetBegin="5" form="本" id="s0_tok3"/>
          <token misc="NIL" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="格助詞" posId="9" pos="助詞" lemma="を" yomi="を" characterOffsetEnd="7" characterOffsetBegin="6" form="を" id="s0_tok4"/>
          <token misc="&quot;人名:日本:名:150:0.00065&quot;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="5" pos1="人名" posId="6" pos="名詞" lemma="二郎" yomi="じろう" characterOffsetEnd="9" characterOffsetBegin="7" form="二郎" id="s0_tok5"/>
          <token misc="NIL" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="格助詞" posId="9" pos="助詞" lemma="を" yomi="を" characterOffsetEnd="10" characterOffsetBegin="9" form="を" id="s0_tok6"/>
          <token misc="&quot;代表表記:見る/みる 補文ト 自他動詞:自:見える/みえる&quot;" cFormId="10" cForm="タ形" cTypeId="1" cType="母音動詞" pos1Id="0" pos1="*" posId="2" pos="動詞" lemma="見る" yomi="みた" characterOffsetEnd="12" characterOffsetBegin="10" form="見た" id="s0_tok7"/>
          <token misc="&quot;代表表記:女性/じょせい カテゴリ:人&quot;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="普通名詞" posId="6" pos="名詞" lemma="女性" yomi="じょせい" characterOffsetEnd="14" characterOffsetBegin="12" form="女性" id="s0_tok8"/>
          <token misc="NIL" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="格助詞" posId="9" pos="助詞" lemma="に" yomi="に" characterOffsetEnd="15" characterOffsetBegin="14" form="に" id="s0_tok9"/>
          <token misc="&quot;代表表記:渡す/わたす 付属動詞候補（基本） 自他動詞:自:渡る/わたる&quot;" cFormId="10" cForm="タ形" cTypeId="5" cType="子音動詞サ行" pos1Id="0" pos1="*" posId="2" pos="動詞" lemma="渡す" yomi="わたした" characterOffsetEnd="18" characterOffsetBegin="15" form="渡した" id="s0_tok10"/>
          <token misc="NIL" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="句点" posId="1" pos="特殊" lemma="。" yomi="。" characterOffsetEnd="19" characterOffsetBegin="18" form="。" id="s0_tok11"/>
        </tokens>
      </sentence>
    </sentences>
  </document>
</root>
"""

        jar_files = [JIGG_JAR, JIGG_MODEL_JAR, CORENLP_MODEL_JAR]
        self.classpath = ':'.join(jar_files)

        self.exe = 'java -cp ' + self.classpath + ' jigg.pipeline.Pipeline ' \
                   + '-annotators ssplit,juman '

    def test_juman(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
