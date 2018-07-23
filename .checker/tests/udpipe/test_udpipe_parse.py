import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestUDpipeParse(BaseTest):
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
        <tokens annotators="udpipe">
          <token offsetEnd="3" offsetBegin="0" form="Joe" id="t0" pos="NNP" upos="PROPN" lemma="Joe" feats="Number=Sing"/>
          <token offsetEnd="9" offsetBegin="4" form="Smith" id="t1" pos="NNP" upos="PROPN" lemma="Smith" feats="Number=Sing"/>
          <token offsetEnd="13" offsetBegin="10" form="was" id="t2" pos="VBD" upos="AUX" lemma="be" feats="Mood=Ind|Number=Sing|Person=3|Tense=Past|VerbForm=Fin"/>
          <token offsetEnd="18" offsetBegin="14" form="born" id="t3" pos="VBN" upos="VERB" lemma="bear" feats="Tense=Past|VerbForm=Part|Voice=Pass"/>
          <token offsetEnd="21" offsetBegin="19" form="in" id="t4" pos="IN" upos="ADP" lemma="in" feats="_"/>
          <token offsetEnd="32" offsetBegin="22" form="California" id="t5" pos="NNP" upos="PROPN" lemma="California" feats="Number=Sing"/>
          <token offsetEnd="33" offsetBegin="32" form="." id="t6" pos="." upos="PUNCT" lemma="." feats="_"/>
        </tokens>
        <dependencies annotators="udpipe">
          <dependency deprel="nsubj:pass" dependent="t0" head="t3" id="dep0"/>
          <dependency deprel="flat" dependent="t1" head="t0" id="dep1"/>
          <dependency deprel="aux:pass" dependent="t2" head="t3" id="dep2"/>
          <dependency deprel="root" dependent="t3" head="root" id="dep3"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep4"/>
          <dependency deprel="obl" dependent="t5" head="t3" id="dep5"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep6"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="82" characterOffsetBegin="34" id="s1">
        In 2017, he went to Paris, France in the summer.
        <tokens annotators="udpipe">
          <token offsetEnd="36" offsetBegin="34" form="In" id="t7" pos="IN" upos="ADP" lemma="in" feats="_"/>
          <token offsetEnd="41" offsetBegin="37" form="2017" id="t8" pos="CD" upos="NUM" lemma="2017" feats="NumType=Card"/>
          <token offsetEnd="42" offsetBegin="41" form="," id="t9" pos="," upos="PUNCT" lemma="," feats="_"/>
          <token offsetEnd="45" offsetBegin="43" form="he" id="t10" pos="PRP" upos="PRON" lemma="he" feats="Case=Nom|Gender=Masc|Number=Sing|Person=3|PronType=Prs"/>
          <token offsetEnd="50" offsetBegin="46" form="went" id="t11" pos="VBD" upos="VERB" lemma="go" feats="Mood=Ind|Tense=Past|VerbForm=Fin"/>
          <token offsetEnd="53" offsetBegin="51" form="to" id="t12" pos="IN" upos="ADP" lemma="to" feats="_"/>
          <token offsetEnd="59" offsetBegin="54" form="Paris" id="t13" pos="NNP" upos="PROPN" lemma="Paris" feats="Number=Sing"/>
          <token offsetEnd="60" offsetBegin="59" form="," id="t14" pos="," upos="PUNCT" lemma="," feats="_"/>
          <token offsetEnd="67" offsetBegin="61" form="France" id="t15" pos="NNP" upos="PROPN" lemma="France" feats="Number=Sing"/>
          <token offsetEnd="70" offsetBegin="68" form="in" id="t16" pos="IN" upos="ADP" lemma="in" feats="_"/>
          <token offsetEnd="74" offsetBegin="71" form="the" id="t17" pos="DT" upos="DET" lemma="the" feats="Definite=Def|PronType=Art"/>
          <token offsetEnd="81" offsetBegin="75" form="summer" id="t18" pos="NN" upos="NOUN" lemma="summer" feats="Number=Sing"/>
          <token offsetEnd="82" offsetBegin="81" form="." id="t19" pos="." upos="PUNCT" lemma="." feats="_"/>
        </tokens>
        <dependencies annotators="udpipe">
          <dependency deprel="case" dependent="t7" head="t8" id="dep7"/>
          <dependency deprel="obl" dependent="t8" head="t11" id="dep8"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep9"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep10"/>
          <dependency deprel="root" dependent="t11" head="root" id="dep11"/>
          <dependency deprel="case" dependent="t12" head="t13" id="dep12"/>
          <dependency deprel="obl" dependent="t13" head="t11" id="dep13"/>
          <dependency deprel="punct" dependent="t14" head="t13" id="dep14"/>
          <dependency deprel="appos" dependent="t15" head="t13" id="dep15"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep16"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep17"/>
          <dependency deprel="nmod" dependent="t18" head="t15" id="dep18"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep19"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="128" characterOffsetBegin="83" id="s2">
        His flight left at 3:00pm on July 10th, 2017.
        <tokens annotators="udpipe">
          <token offsetEnd="86" offsetBegin="83" form="His" id="t20" pos="PRP$" upos="PRON" lemma="he" feats="Gender=Masc|Number=Sing|Person=3|Poss=Yes|PronType=Prs"/>
          <token offsetEnd="93" offsetBegin="87" form="flight" id="t21" pos="NN" upos="NOUN" lemma="flight" feats="Number=Sing"/>
          <token offsetEnd="98" offsetBegin="94" form="left" id="t22" pos="VBN" upos="VERB" lemma="leave" feats="Tense=Past|VerbForm=Part"/>
          <token offsetEnd="101" offsetBegin="99" form="at" id="t23" pos="IN" upos="ADP" lemma="at" feats="_"/>
          <token offsetEnd="106" offsetBegin="102" form="3:00" id="t24" pos="CD" upos="NUM" lemma="3:00" feats="NumType=Card"/>
          <token offsetEnd="108" offsetBegin="106" form="pm" id="t25" pos="NN" upos="NOUN" lemma="pm" feats="Number=Sing"/>
          <token offsetEnd="111" offsetBegin="109" form="on" id="t26" pos="IN" upos="ADP" lemma="on" feats="_"/>
          <token offsetEnd="116" offsetBegin="112" form="July" id="t27" pos="NNP" upos="PROPN" lemma="July" feats="Number=Sing"/>
          <token offsetEnd="121" offsetBegin="117" form="10th" id="t28" pos="NN" upos="NOUN" lemma="10th" feats="Number=Sing"/>
          <token offsetEnd="122" offsetBegin="121" form="," id="t29" pos="," upos="PUNCT" lemma="," feats="_"/>
          <token offsetEnd="127" offsetBegin="123" form="2017" id="t30" pos="CD" upos="NUM" lemma="2017" feats="NumType=Card"/>
          <token offsetEnd="128" offsetBegin="127" form="." id="t31" pos="." upos="PUNCT" lemma="." feats="_"/>
        </tokens>
        <dependencies annotators="udpipe">
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep20"/>
          <dependency deprel="root" dependent="t21" head="root" id="dep21"/>
          <dependency deprel="acl" dependent="t22" head="t21" id="dep22"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep23"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep24"/>
          <dependency deprel="obl" dependent="t25" head="t22" id="dep25"/>
          <dependency deprel="case" dependent="t26" head="t28" id="dep26"/>
          <dependency deprel="compound" dependent="t27" head="t28" id="dep27"/>
          <dependency deprel="nmod" dependent="t28" head="t21" id="dep28"/>
          <dependency deprel="punct" dependent="t29" head="t28" id="dep29"/>
          <dependency deprel="nummod" dependent="t30" head="t28" id="dep30"/>
          <dependency deprel="punct" dependent="t31" head="t21" id="dep31"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="207" characterOffsetBegin="129" id="s3">
        After eating some escargot for the first time, Joe said, &quot;That was delicious!&quot;
        <tokens annotators="udpipe">
          <token offsetEnd="134" offsetBegin="129" form="After" id="t32" pos="IN" upos="SCONJ" lemma="after" feats="_"/>
          <token offsetEnd="141" offsetBegin="135" form="eating" id="t33" pos="VBG" upos="VERB" lemma="eat" feats="VerbForm=Ger"/>
          <token offsetEnd="146" offsetBegin="142" form="some" id="t34" pos="DT" upos="DET" lemma="some" feats="_"/>
          <token offsetEnd="155" offsetBegin="147" form="escargot" id="t35" pos="NN" upos="NOUN" lemma="escargot" feats="Number=Sing"/>
          <token offsetEnd="159" offsetBegin="156" form="for" id="t36" pos="IN" upos="ADP" lemma="for" feats="_"/>
          <token offsetEnd="163" offsetBegin="160" form="the" id="t37" pos="DT" upos="DET" lemma="the" feats="Definite=Def|PronType=Art"/>
          <token offsetEnd="169" offsetBegin="164" form="first" id="t38" pos="JJ" upos="ADJ" lemma="first" feats="Degree=Pos|NumType=Ord"/>
          <token offsetEnd="174" offsetBegin="170" form="time" id="t39" pos="NN" upos="NOUN" lemma="time" feats="Number=Sing"/>
          <token offsetEnd="175" offsetBegin="174" form="," id="t40" pos="," upos="PUNCT" lemma="," feats="_"/>
          <token offsetEnd="179" offsetBegin="176" form="Joe" id="t41" pos="NNP" upos="PROPN" lemma="Joe" feats="Number=Sing"/>
          <token offsetEnd="184" offsetBegin="180" form="said" id="t42" pos="VBD" upos="VERB" lemma="say" feats="Mood=Ind|Tense=Past|VerbForm=Fin"/>
          <token offsetEnd="185" offsetBegin="184" form="," id="t43" pos="," upos="PUNCT" lemma="," feats="_"/>
          <token offsetEnd="187" offsetBegin="186" form="&quot;" id="t44" pos="``" upos="PUNCT" lemma="&quot;" feats="_"/>
          <token offsetEnd="191" offsetBegin="187" form="That" id="t45" pos="DT" upos="PRON" lemma="that" feats="Number=Sing|PronType=Dem"/>
          <token offsetEnd="195" offsetBegin="192" form="was" id="t46" pos="VBD" upos="AUX" lemma="be" feats="Mood=Ind|Number=Sing|Person=3|Tense=Past|VerbForm=Fin"/>
          <token offsetEnd="205" offsetBegin="196" form="delicious" id="t47" pos="JJ" upos="ADJ" lemma="delicious" feats="Degree=Pos"/>
          <token offsetEnd="206" offsetBegin="205" form="!" id="t48" pos="." upos="PUNCT" lemma="!" feats="_"/>
          <token offsetEnd="207" offsetBegin="206" form="&quot;" id="t49" pos="''" upos="PUNCT" lemma="&quot;" feats="_"/>
        </tokens>
        <dependencies annotators="udpipe">
          <dependency deprel="mark" dependent="t32" head="t33" id="dep32"/>
          <dependency deprel="advcl" dependent="t33" head="t42" id="dep33"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep34"/>
          <dependency deprel="obj" dependent="t35" head="t33" id="dep35"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep36"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep37"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep38"/>
          <dependency deprel="obl" dependent="t39" head="t33" id="dep39"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep40"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep41"/>
          <dependency deprel="root" dependent="t42" head="root" id="dep42"/>
          <dependency deprel="punct" dependent="t43" head="t47" id="dep43"/>
          <dependency deprel="punct" dependent="t44" head="t47" id="dep44"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep45"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep46"/>
          <dependency deprel="ccomp" dependent="t47" head="t42" id="dep47"/>
          <dependency deprel="punct" dependent="t48" head="t42" id="dep48"/>
          <dependency deprel="punct" dependent="t49" head="t42" id="dep49"/>
        </dependencies>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline -annotators udpipe[tokenize,pos,parse] ' \
                   + '-udpipe.model udpipe-ud-model/english-ud-2.0-170801.udpipe '

    def test_udpipe_parse(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
