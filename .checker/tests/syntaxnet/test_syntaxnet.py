import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestSyntaxnet(BaseTest):
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
        <tokens annotators="corenlp syntaxnet">
          <token form="Joe" id="t0" characterOffsetBegin="0" characterOffsetEnd="3" pos="NNP" cpos="NOUN"/>
          <token form="Smith" id="t1" characterOffsetBegin="4" characterOffsetEnd="9" pos="NNP" cpos="NOUN"/>
          <token form="was" id="t2" characterOffsetBegin="10" characterOffsetEnd="13" pos="VBD" cpos="VERB"/>
          <token form="born" id="t3" characterOffsetBegin="14" characterOffsetEnd="18" pos="VBN" cpos="VERB"/>
          <token form="in" id="t4" characterOffsetBegin="19" characterOffsetEnd="21" pos="IN" cpos="ADP"/>
          <token form="California" id="t5" characterOffsetBegin="22" characterOffsetEnd="32" pos="NNP" cpos="NOUN"/>
          <token form="." id="t6" characterOffsetBegin="32" characterOffsetEnd="33" pos="." cpos="."/>
        </tokens>
        <dependencies annotators="syntaxnet" type="basic">
          <dependency deprel="nn" dependent="t0" head="t1" id="dep0"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep1"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep2"/>
          <dependency deprel="ROOT" dependent="t3" head="ROOT" id="dep3"/>
          <dependency deprel="prep" dependent="t4" head="t3" id="dep4"/>
          <dependency deprel="pobj" dependent="t5" head="t4" id="dep5"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep6"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="82" characterOffsetBegin="34" id="s1">
        In 2017, he went to Paris, France in the summer.
        <tokens annotators="corenlp syntaxnet">
          <token form="In" id="t7" characterOffsetBegin="0" characterOffsetEnd="2" pos="IN" cpos="ADP"/>
          <token form="2017" id="t8" characterOffsetBegin="3" characterOffsetEnd="7" pos="CD" cpos="NUM"/>
          <token form="," id="t9" characterOffsetBegin="7" characterOffsetEnd="8" pos="," cpos="."/>
          <token form="he" id="t10" characterOffsetBegin="9" characterOffsetEnd="11" pos="PRP" cpos="PRON"/>
          <token form="went" id="t11" characterOffsetBegin="12" characterOffsetEnd="16" pos="VBD" cpos="VERB"/>
          <token form="to" id="t12" characterOffsetBegin="17" characterOffsetEnd="19" pos="IN" cpos="ADP"/>
          <token form="Paris" id="t13" characterOffsetBegin="20" characterOffsetEnd="25" pos="NNP" cpos="NOUN"/>
          <token form="," id="t14" characterOffsetBegin="25" characterOffsetEnd="26" pos="," cpos="."/>
          <token form="France" id="t15" characterOffsetBegin="27" characterOffsetEnd="33" pos="NNP" cpos="NOUN"/>
          <token form="in" id="t16" characterOffsetBegin="34" characterOffsetEnd="36" pos="IN" cpos="ADP"/>
          <token form="the" id="t17" characterOffsetBegin="37" characterOffsetEnd="40" pos="DT" cpos="DET"/>
          <token form="summer" id="t18" characterOffsetBegin="41" characterOffsetEnd="47" pos="NN" cpos="NOUN"/>
          <token form="." id="t19" characterOffsetBegin="47" characterOffsetEnd="48" pos="." cpos="."/>
        </tokens>
        <dependencies annotators="syntaxnet" type="basic">
          <dependency deprel="prep" dependent="t7" head="t11" id="dep7"/>
          <dependency deprel="pobj" dependent="t8" head="t7" id="dep8"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep9"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep10"/>
          <dependency deprel="ROOT" dependent="t11" head="ROOT" id="dep11"/>
          <dependency deprel="prep" dependent="t12" head="t11" id="dep12"/>
          <dependency deprel="pobj" dependent="t13" head="t12" id="dep13"/>
          <dependency deprel="punct" dependent="t14" head="t13" id="dep14"/>
          <dependency deprel="appos" dependent="t15" head="t13" id="dep15"/>
          <dependency deprel="prep" dependent="t16" head="t11" id="dep16"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep17"/>
          <dependency deprel="pobj" dependent="t18" head="t16" id="dep18"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep19"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="128" characterOffsetBegin="83" id="s2">
        His flight left at 3:00pm on July 10th, 2017.
        <tokens annotators="corenlp syntaxnet">
          <token form="His" id="t20" characterOffsetBegin="0" characterOffsetEnd="3" pos="PRP$" cpos="PRON"/>
          <token form="flight" id="t21" characterOffsetBegin="4" characterOffsetEnd="10" pos="NN" cpos="NOUN"/>
          <token form="left" id="t22" characterOffsetBegin="11" characterOffsetEnd="15" pos="VBD" cpos="VERB"/>
          <token form="at" id="t23" characterOffsetBegin="16" characterOffsetEnd="18" pos="IN" cpos="ADP"/>
          <token form="3:00" id="t24" characterOffsetBegin="19" characterOffsetEnd="23" pos="CD" cpos="NUM"/>
          <token form="pm" id="t25" characterOffsetBegin="23" characterOffsetEnd="25" pos="NN" cpos="NOUN"/>
          <token form="on" id="t26" characterOffsetBegin="26" characterOffsetEnd="28" pos="IN" cpos="ADP"/>
          <token form="July" id="t27" characterOffsetBegin="29" characterOffsetEnd="33" pos="NNP" cpos="NOUN"/>
          <token form="10th" id="t28" characterOffsetBegin="34" characterOffsetEnd="38" pos="NN" cpos="NOUN"/>
          <token form="," id="t29" characterOffsetBegin="38" characterOffsetEnd="39" pos="," cpos="."/>
          <token form="2017" id="t30" characterOffsetBegin="40" characterOffsetEnd="44" pos="CD" cpos="NUM"/>
          <token form="." id="t31" characterOffsetBegin="44" characterOffsetEnd="45" pos="." cpos="."/>
        </tokens>
        <dependencies annotators="syntaxnet" type="basic">
          <dependency deprel="poss" dependent="t20" head="t21" id="dep20"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep21"/>
          <dependency deprel="ROOT" dependent="t22" head="ROOT" id="dep22"/>
          <dependency deprel="prep" dependent="t23" head="t22" id="dep23"/>
          <dependency deprel="num" dependent="t24" head="t25" id="dep24"/>
          <dependency deprel="pobj" dependent="t25" head="t23" id="dep25"/>
          <dependency deprel="prep" dependent="t26" head="t22" id="dep26"/>
          <dependency deprel="nn" dependent="t27" head="t28" id="dep27"/>
          <dependency deprel="pobj" dependent="t28" head="t26" id="dep28"/>
          <dependency deprel="punct" dependent="t29" head="t28" id="dep29"/>
          <dependency deprel="amod" dependent="t30" head="t28" id="dep30"/>
          <dependency deprel="ROOT" dependent="t31" head="ROOT" id="dep31"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="207" characterOffsetBegin="129" id="s3">
        After eating some escargot for the first time, Joe said, &quot;That was delicious!&quot;
        <tokens annotators="corenlp syntaxnet">
          <token form="After" id="t32" characterOffsetBegin="0" characterOffsetEnd="5" pos="IN" cpos="ADP"/>
          <token form="eating" id="t33" characterOffsetBegin="6" characterOffsetEnd="12" pos="VBG" cpos="VERB"/>
          <token form="some" id="t34" characterOffsetBegin="13" characterOffsetEnd="17" pos="DT" cpos="DET"/>
          <token form="escargot" id="t35" characterOffsetBegin="18" characterOffsetEnd="26" pos="NN" cpos="NOUN"/>
          <token form="for" id="t36" characterOffsetBegin="27" characterOffsetEnd="30" pos="IN" cpos="ADP"/>
          <token form="the" id="t37" characterOffsetBegin="31" characterOffsetEnd="34" pos="DT" cpos="DET"/>
          <token form="first" id="t38" characterOffsetBegin="35" characterOffsetEnd="40" pos="JJ" cpos="ADJ"/>
          <token form="time" id="t39" characterOffsetBegin="41" characterOffsetEnd="45" pos="NN" cpos="NOUN"/>
          <token form="," id="t40" characterOffsetBegin="45" characterOffsetEnd="46" pos="," cpos="."/>
          <token form="Joe" id="t41" characterOffsetBegin="47" characterOffsetEnd="50" pos="NNP" cpos="NOUN"/>
          <token form="said" id="t42" characterOffsetBegin="51" characterOffsetEnd="55" pos="VBD" cpos="VERB"/>
          <token form="," id="t43" characterOffsetBegin="55" characterOffsetEnd="56" pos="," cpos="."/>
          <token form="``" id="t44" characterOffsetBegin="57" characterOffsetEnd="58" pos="``" cpos="."/>
          <token form="That" id="t45" characterOffsetBegin="58" characterOffsetEnd="62" pos="DT" cpos="DET"/>
          <token form="was" id="t46" characterOffsetBegin="63" characterOffsetEnd="66" pos="VBD" cpos="VERB"/>
          <token form="delicious" id="t47" characterOffsetBegin="67" characterOffsetEnd="76" pos="JJ" cpos="ADJ"/>
          <token form="!" id="t48" characterOffsetBegin="76" characterOffsetEnd="77" pos="." cpos="."/>
          <token form="''" id="t49" characterOffsetBegin="77" characterOffsetEnd="78" pos="''" cpos="."/>
        </tokens>
        <dependencies annotators="syntaxnet" type="basic">
          <dependency deprel="prep" dependent="t32" head="t42" id="dep32"/>
          <dependency deprel="pcomp" dependent="t33" head="t32" id="dep33"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep34"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep35"/>
          <dependency deprel="prep" dependent="t36" head="t33" id="dep36"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep37"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep38"/>
          <dependency deprel="pobj" dependent="t39" head="t36" id="dep39"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep40"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep41"/>
          <dependency deprel="ROOT" dependent="t42" head="ROOT" id="dep42"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep43"/>
          <dependency deprel="punct" dependent="t44" head="t42" id="dep44"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep45"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep46"/>
          <dependency deprel="ccomp" dependent="t47" head="t42" id="dep47"/>
          <dependency deprel="punct" dependent="t48" head="t47" id="dep48"/>
          <dependency deprel="punct" dependent="t49" head="t42" id="dep49"/>
        </dependencies>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'docker run --rm -it ' \
                   + '--mount type=bind,source="$(pwd)"/,target=/mnt/ ' \
                   + 'jigg/jigg:syntaxnet ' \
                   + 'java -cp "jar/*:target/*" jigg.pipeline.Pipeline ' \
                   + '-annotators corenlp[tokenize,ssplit,pos],syntaxnet ' \
                   + '-syntaxnet.path /opt/tensorflow/syntaxnet/ '

    def test_syntaxnet(self):
        self.check_equal_with_docker(self.exe, self.input_text, self.expected_text)
