import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestBenepar(BaseTest):
    '''
    '''
    def setUp(self):
        self.input_text = 'Joe Smith was born in California. In 2017, he went to Paris, France in the summer. His flight left at 3:00pm on July 10th, 2017. After eating some escargot for the first time, Joe said, "That was delicious!"'

        self.expected_text = """<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="33" characterOffsetBegin="0" id="s0">
        Joe Smith was born in California.
        <tokens annotators="corenlp">
          <token form="Joe" id="t0" characterOffsetBegin="0" characterOffsetEnd="3" pos="NNP"/>
          <token form="Smith" id="t1" characterOffsetBegin="4" characterOffsetEnd="9" pos="NNP"/>
          <token form="was" id="t2" characterOffsetBegin="10" characterOffsetEnd="13" pos="VBD"/>
          <token form="born" id="t3" characterOffsetBegin="14" characterOffsetEnd="18" pos="VBN"/>
          <token form="in" id="t4" characterOffsetBegin="19" characterOffsetEnd="21" pos="IN"/>
          <token form="California" id="t5" characterOffsetBegin="22" characterOffsetEnd="32" pos="NNP"/>
          <token form="." id="t6" characterOffsetBegin="32" characterOffsetEnd="33" pos="."/>
        </tokens>
        <parse annotators="benepar" root="sp5">
          <span children="t0 t1" symbol="NP" id="sp0"/>
          <span children="t5" symbol="NP" id="sp1"/>
          <span children="t4 sp1" symbol="PP" id="sp2"/>
          <span children="t3 sp2" symbol="VP" id="sp3"/>
          <span children="t2 sp3" symbol="VP" id="sp4"/>
          <span children="sp0 sp4 t6" symbol="S" id="sp5"/>
        </parse>
      </sentence>
      <sentence characterOffsetEnd="82" characterOffsetBegin="34" id="s1">
        In 2017, he went to Paris, France in the summer.
        <tokens annotators="corenlp">
          <token form="In" id="t7" characterOffsetBegin="0" characterOffsetEnd="2" pos="IN"/>
          <token form="2017" id="t8" characterOffsetBegin="3" characterOffsetEnd="7" pos="CD"/>
          <token form="," id="t9" characterOffsetBegin="7" characterOffsetEnd="8" pos=","/>
          <token form="he" id="t10" characterOffsetBegin="9" characterOffsetEnd="11" pos="PRP"/>
          <token form="went" id="t11" characterOffsetBegin="12" characterOffsetEnd="16" pos="VBD"/>
          <token form="to" id="t12" characterOffsetBegin="17" characterOffsetEnd="19" pos="TO"/>
          <token form="Paris" id="t13" characterOffsetBegin="20" characterOffsetEnd="25" pos="NNP"/>
          <token form="," id="t14" characterOffsetBegin="25" characterOffsetEnd="26" pos=","/>
          <token form="France" id="t15" characterOffsetBegin="27" characterOffsetEnd="33" pos="NNP"/>
          <token form="in" id="t16" characterOffsetBegin="34" characterOffsetEnd="36" pos="IN"/>
          <token form="the" id="t17" characterOffsetBegin="37" characterOffsetEnd="40" pos="DT"/>
          <token form="summer" id="t18" characterOffsetBegin="41" characterOffsetEnd="47" pos="NN"/>
          <token form="." id="t19" characterOffsetBegin="47" characterOffsetEnd="48" pos="."/>
        </tokens>
        <parse annotators="benepar" root="sp15">
          <span children="t8" symbol="NP" id="sp6"/>
          <span children="t7 sp6" symbol="PP" id="sp7"/>
          <span children="t10" symbol="NP" id="sp8"/>
          <span children="t15" symbol="NP" id="sp9"/>
          <span children="t13 t14 sp9" symbol="NP" id="sp10"/>
          <span children="t12 sp10" symbol="PP" id="sp11"/>
          <span children="t17 t18" symbol="NP" id="sp12"/>
          <span children="t16 sp12" symbol="PP" id="sp13"/>
          <span children="t11 sp11 sp13" symbol="VP" id="sp14"/>
          <span children="sp7 t9 sp8 sp14 t19" symbol="S" id="sp15"/>
        </parse>
      </sentence>
      <sentence characterOffsetEnd="128" characterOffsetBegin="83" id="s2">
        His flight left at 3:00pm on July 10th, 2017.
        <tokens annotators="corenlp">
          <token form="His" id="t20" characterOffsetBegin="0" characterOffsetEnd="3" pos="PRP$"/>
          <token form="flight" id="t21" characterOffsetBegin="4" characterOffsetEnd="10" pos="NN"/>
          <token form="left" id="t22" characterOffsetBegin="11" characterOffsetEnd="15" pos="VBD"/>
          <token form="at" id="t23" characterOffsetBegin="16" characterOffsetEnd="18" pos="IN"/>
          <token form="3:00" id="t24" characterOffsetBegin="19" characterOffsetEnd="23" pos="CD"/>
          <token form="pm" id="t25" characterOffsetBegin="23" characterOffsetEnd="25" pos="NN"/>
          <token form="on" id="t26" characterOffsetBegin="26" characterOffsetEnd="28" pos="IN"/>
          <token form="July" id="t27" characterOffsetBegin="29" characterOffsetEnd="33" pos="NNP"/>
          <token form="10th" id="t28" characterOffsetBegin="34" characterOffsetEnd="38" pos="JJ"/>
          <token form="," id="t29" characterOffsetBegin="38" characterOffsetEnd="39" pos=","/>
          <token form="2017" id="t30" characterOffsetBegin="40" characterOffsetEnd="44" pos="CD"/>
          <token form="." id="t31" characterOffsetBegin="44" characterOffsetEnd="45" pos="."/>
        </tokens>
        <parse annotators="benepar" root="sp22">
          <span children="t20 t21" symbol="NP" id="sp16"/>
          <span children="t24 t25" symbol="NP" id="sp17"/>
          <span children="t23 sp17" symbol="PP" id="sp18"/>
          <span children="t27 t28 t29 t30" symbol="NP" id="sp19"/>
          <span children="t26 sp19" symbol="PP" id="sp20"/>
          <span children="t22 sp18 sp20" symbol="VP" id="sp21"/>
          <span children="sp16 sp21 t31" symbol="S" id="sp22"/>
        </parse>
      </sentence>
      <sentence characterOffsetEnd="207" characterOffsetBegin="129" id="s3">
        After eating some escargot for the first time, Joe said, &quot;That was delicious!&quot;
        <tokens annotators="corenlp">
          <token form="After" id="t32" characterOffsetBegin="0" characterOffsetEnd="5" pos="IN"/>
          <token form="eating" id="t33" characterOffsetBegin="6" characterOffsetEnd="12" pos="VBG"/>
          <token form="some" id="t34" characterOffsetBegin="13" characterOffsetEnd="17" pos="DT"/>
          <token form="escargot" id="t35" characterOffsetBegin="18" characterOffsetEnd="26" pos="NN"/>
          <token form="for" id="t36" characterOffsetBegin="27" characterOffsetEnd="30" pos="IN"/>
          <token form="the" id="t37" characterOffsetBegin="31" characterOffsetEnd="34" pos="DT"/>
          <token form="first" id="t38" characterOffsetBegin="35" characterOffsetEnd="40" pos="JJ"/>
          <token form="time" id="t39" characterOffsetBegin="41" characterOffsetEnd="45" pos="NN"/>
          <token form="," id="t40" characterOffsetBegin="45" characterOffsetEnd="46" pos=","/>
          <token form="Joe" id="t41" characterOffsetBegin="47" characterOffsetEnd="50" pos="NNP"/>
          <token form="said" id="t42" characterOffsetBegin="51" characterOffsetEnd="55" pos="VBD"/>
          <token form="," id="t43" characterOffsetBegin="55" characterOffsetEnd="56" pos=","/>
          <token form="``" id="t44" characterOffsetBegin="57" characterOffsetEnd="58" pos="``"/>
          <token form="That" id="t45" characterOffsetBegin="58" characterOffsetEnd="62" pos="DT"/>
          <token form="was" id="t46" characterOffsetBegin="63" characterOffsetEnd="66" pos="VBD"/>
          <token form="delicious" id="t47" characterOffsetBegin="67" characterOffsetEnd="76" pos="JJ"/>
          <token form="!" id="t48" characterOffsetBegin="76" characterOffsetEnd="77" pos="."/>
          <token form="''" id="t49" characterOffsetBegin="77" characterOffsetEnd="78" pos="''"/>
        </tokens>
        <parse annotators="benepar" root="sp35">
          <span children="t34 t35" symbol="NP" id="sp23"/>
          <span children="t37 t38 t39" symbol="NP" id="sp24"/>
          <span children="t36 sp24" symbol="PP" id="sp25"/>
          <span children="t33 sp23 sp25" symbol="VP" id="sp26"/>
          <span children="sp26" symbol="S" id="sp27"/>
          <span children="t32 sp27" symbol="PP" id="sp28"/>
          <span children="t41" symbol="NP" id="sp29"/>
          <span children="t45" symbol="NP" id="sp30"/>
          <span children="t47" symbol="ADJP" id="sp31"/>
          <span children="t46 sp31" symbol="VP" id="sp32"/>
          <span children="sp30 sp32" symbol="S" id="sp33"/>
          <span children="t42 t43 t44 sp33" symbol="VP" id="sp34"/>
          <span children="sp28 t40 sp29 sp34 t48 t49" symbol="S" id="sp35"/>
        </parse>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline -annotators corenlp[tokenize,ssplit,pos],benepar'

    def test_benepar(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
