import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestMecab(BaseTest):
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
        <tokens annotators="mecab">
          <token pron="タロー" yomi="タロウ" lemma="太郎" cForm="*" cType="*" pos3="名" pos2="人名" pos1="固有名詞" pos="名詞" offsetEnd="2" offsetBegin="0" form="太郎" id="s0_tok0"/>
          <token pron="ワ" yomi="ハ" lemma="は" cForm="*" cType="*" pos3="*" pos2="*" pos1="係助詞" pos="助詞" offsetEnd="3" offsetBegin="2" form="は" id="s0_tok1"/>
          <token pron="コノ" yomi="コノ" lemma="この" cForm="*" cType="*" pos3="*" pos2="*" pos1="*" pos="連体詞" offsetEnd="5" offsetBegin="3" form="この" id="s0_tok2"/>
          <token pron="ホン" yomi="ホン" lemma="本" cForm="*" cType="*" pos3="*" pos2="*" pos1="一般" pos="名詞" offsetEnd="6" offsetBegin="5" form="本" id="s0_tok3"/>
          <token pron="ヲ" yomi="ヲ" lemma="を" cForm="*" cType="*" pos3="*" pos2="一般" pos1="格助詞" pos="助詞" offsetEnd="7" offsetBegin="6" form="を" id="s0_tok4"/>
          <token pron="ニ" yomi="ニ" lemma="二" cForm="*" cType="*" pos3="*" pos2="*" pos1="数" pos="名詞" offsetEnd="8" offsetBegin="7" form="二" id="s0_tok5"/>
          <token pron="ロー" yomi="ロウ" lemma="郎" cForm="*" cType="*" pos3="*" pos2="*" pos1="一般" pos="名詞" offsetEnd="9" offsetBegin="8" form="郎" id="s0_tok6"/>
          <token pron="ヲ" yomi="ヲ" lemma="を" cForm="*" cType="*" pos3="*" pos2="一般" pos1="格助詞" pos="助詞" offsetEnd="10" offsetBegin="9" form="を" id="s0_tok7"/>
          <token pron="ミ" yomi="ミ" lemma="見る" cForm="連用形" cType="一段" pos3="*" pos2="*" pos1="自立" pos="動詞" offsetEnd="11" offsetBegin="10" form="見" id="s0_tok8"/>
          <token pron="タ" yomi="タ" lemma="た" cForm="基本形" cType="特殊・タ" pos3="*" pos2="*" pos1="*" pos="助動詞" offsetEnd="12" offsetBegin="11" form="た" id="s0_tok9"/>
          <token pron="ジョセイ" yomi="ジョセイ" lemma="女性" cForm="*" cType="*" pos3="*" pos2="*" pos1="一般" pos="名詞" offsetEnd="14" offsetBegin="12" form="女性" id="s0_tok10"/>
          <token pron="ニ" yomi="ニ" lemma="に" cForm="*" cType="*" pos3="*" pos2="一般" pos1="格助詞" pos="助詞" offsetEnd="15" offsetBegin="14" form="に" id="s0_tok11"/>
          <token pron="ワタシ" yomi="ワタシ" lemma="渡す" cForm="連用形" cType="五段・サ行" pos3="*" pos2="*" pos1="自立" pos="動詞" offsetEnd="17" offsetBegin="15" form="渡し" id="s0_tok12"/>
          <token pron="タ" yomi="タ" lemma="た" cForm="基本形" cType="特殊・タ" pos3="*" pos2="*" pos1="*" pos="助動詞" offsetEnd="18" offsetBegin="17" form="た" id="s0_tok13"/>
          <token pron="。" yomi="。" lemma="。" cForm="*" cType="*" pos3="*" pos2="*" pos1="句点" pos="記号" offsetEnd="19" offsetBegin="18" form="。" id="s0_tok14"/>
        </tokens>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline -annotators ssplit,mecab'

    def test_mecab(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
