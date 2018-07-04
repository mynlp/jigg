import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestDcoref(BaseTest):

    def setUp(self):
        self.input_text = 'Joe Smith was born in California. In 2017, he went to Paris, France in the summer. His flight left at 3:00pm on July 10th, 2017. After eating some escargot for the first time, Joe said, "That was delicious!"'

        self.expected_text = r"""<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="33" characterOffsetBegin="0" id="s0">
        Joe Smith was born in California.
        <NEs annotators="corenlp">
          <NE tokens="t0 t1" label="PERSON" id="s0_corene0"/>
          <NE tokens="t5" label="LOCATION" id="s0_corene1"/>
        </NEs>
        <tokens annotators="corenlp">
          <token form="Joe" id="t0" characterOffsetBegin="0" characterOffsetEnd="3" lemma="Joe" pos="NNP"/>
          <token form="Smith" id="t1" characterOffsetBegin="4" characterOffsetEnd="9" lemma="Smith" pos="NNP"/>
          <token form="was" id="t2" characterOffsetBegin="10" characterOffsetEnd="13" lemma="be" pos="VBD"/>
          <token form="born" id="t3" characterOffsetBegin="14" characterOffsetEnd="18" lemma="bear" pos="VBN"/>
          <token form="in" id="t4" characterOffsetBegin="19" characterOffsetEnd="21" lemma="in" pos="IN"/>
          <token form="California" id="t5" characterOffsetBegin="22" characterOffsetEnd="32" lemma="California" pos="NNP"/>
          <token form="." id="t6" characterOffsetBegin="32" characterOffsetEnd="33" lemma="." pos="."/>
        </tokens>
        <parse annotators="corenlp" root="sp5">
          <span children="t0 t1" symbol="NP" id="sp0"/>
          <span children="t5" symbol="NP" id="sp1"/>
          <span children="t4 sp1" symbol="PP" id="sp2"/>
          <span children="t3 sp2" symbol="VP" id="sp3"/>
          <span children="t2 sp3" symbol="VP" id="sp4"/>
          <span children="sp0 sp4 t6" symbol="S" id="sp5"/>
        </parse>
        <dependencies annotators="corenlp" type="basic">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep0"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep1"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep2"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep3"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep4"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep5"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep6"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep50"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep51"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep52"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep53"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep54"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep55"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep56"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep100"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep101"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep102"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep103"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep104"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep105"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep106"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep150"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep151"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep152"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep153"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep154"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep155"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep156"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep200"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep201"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep202"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep203"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep204"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep205"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep206"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="82" characterOffsetBegin="34" id="s1">
        In 2017, he went to Paris, France in the summer.
        <NEs annotators="corenlp">
          <NE tokens="t8" normalizedLabel="2017" label="DATE" id="s1_corene0"/>
          <NE tokens="t13" label="LOCATION" id="s1_corene1"/>
          <NE tokens="t15" label="LOCATION" id="s1_corene2"/>
          <NE tokens="t18" normalizedLabel="XXXX-SU" label="DATE" id="s1_corene3"/>
        </NEs>
        <tokens annotators="corenlp">
          <token form="In" id="t7" characterOffsetBegin="0" characterOffsetEnd="2" lemma="in" pos="IN"/>
          <token form="2017" id="t8" characterOffsetBegin="3" characterOffsetEnd="7" lemma="2017" pos="CD"/>
          <token form="," id="t9" characterOffsetBegin="7" characterOffsetEnd="8" lemma="," pos=","/>
          <token form="he" id="t10" characterOffsetBegin="9" characterOffsetEnd="11" lemma="he" pos="PRP"/>
          <token form="went" id="t11" characterOffsetBegin="12" characterOffsetEnd="16" lemma="go" pos="VBD"/>
          <token form="to" id="t12" characterOffsetBegin="17" characterOffsetEnd="19" lemma="to" pos="TO"/>
          <token form="Paris" id="t13" characterOffsetBegin="20" characterOffsetEnd="25" lemma="Paris" pos="NNP"/>
          <token form="," id="t14" characterOffsetBegin="25" characterOffsetEnd="26" lemma="," pos=","/>
          <token form="France" id="t15" characterOffsetBegin="27" characterOffsetEnd="33" lemma="France" pos="NNP"/>
          <token form="in" id="t16" characterOffsetBegin="34" characterOffsetEnd="36" lemma="in" pos="IN"/>
          <token form="the" id="t17" characterOffsetBegin="37" characterOffsetEnd="40" lemma="the" pos="DT"/>
          <token form="summer" id="t18" characterOffsetBegin="41" characterOffsetEnd="47" lemma="summer" pos="NN"/>
          <token form="." id="t19" characterOffsetBegin="47" characterOffsetEnd="48" lemma="." pos="."/>
        </tokens>
        <parse annotators="corenlp" root="sp14">
          <span children="t8" symbol="NP" id="sp6"/>
          <span children="t7 sp6" symbol="PP" id="sp7"/>
          <span children="t10" symbol="NP" id="sp8"/>
          <span children="t13 t14 t15" symbol="NP" id="sp9"/>
          <span children="t12 sp9" symbol="PP" id="sp10"/>
          <span children="t17 t18" symbol="NP" id="sp11"/>
          <span children="t16 sp11" symbol="PP" id="sp12"/>
          <span children="t11 sp10 sp12" symbol="VP" id="sp13"/>
          <span children="sp7 t9 sp8 sp13 t19" symbol="S" id="sp14"/>
        </parse>
        <dependencies annotators="corenlp" type="basic">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep7"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep8"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep9"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep10"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep11"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep12"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep13"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep14"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep15"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep16"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep17"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep18"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep19"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep57"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep58"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep59"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep60"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep61"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep62"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep63"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep64"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep65"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep66"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep67"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep68"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep69"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep107"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep108"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep109"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep110"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep111"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep112"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep113"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep114"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep115"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep116"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep117"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep118"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep119"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep157"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep158"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep159"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep160"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep161"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep162"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep163"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep164"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep165"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep166"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep167"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep168"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep169"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep207"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep208"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep209"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep210"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep211"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep212"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep213"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep214"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep215"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep216"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep217"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep218"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep219"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="128" characterOffsetBegin="83" id="s2">
        His flight left at 3:00pm on July 10th, 2017.
        <NEs annotators="corenlp">
          <NE tokens="t24 t25 t26" normalizedLabel="2017-07-10T15:00" label="TIME" id="s2_corene0"/>
          <NE tokens="t27 t28 t29 t30" normalizedLabel="2017-07-10T15:00" label="DATE" id="s2_corene1"/>
        </NEs>
        <tokens annotators="corenlp">
          <token form="His" id="t20" characterOffsetBegin="0" characterOffsetEnd="3" lemma="he" pos="PRP$"/>
          <token form="flight" id="t21" characterOffsetBegin="4" characterOffsetEnd="10" lemma="flight" pos="NN"/>
          <token form="left" id="t22" characterOffsetBegin="11" characterOffsetEnd="15" lemma="leave" pos="VBD"/>
          <token form="at" id="t23" characterOffsetBegin="16" characterOffsetEnd="18" lemma="at" pos="IN"/>
          <token form="3:00" id="t24" characterOffsetBegin="19" characterOffsetEnd="23" lemma="3:00" pos="CD"/>
          <token form="pm" id="t25" characterOffsetBegin="23" characterOffsetEnd="25" lemma="pm" pos="NN"/>
          <token form="on" id="t26" characterOffsetBegin="26" characterOffsetEnd="28" lemma="on" pos="IN"/>
          <token form="July" id="t27" characterOffsetBegin="29" characterOffsetEnd="33" lemma="July" pos="NNP"/>
          <token form="10th" id="t28" characterOffsetBegin="34" characterOffsetEnd="38" lemma="10th" pos="CD"/>
          <token form="," id="t29" characterOffsetBegin="38" characterOffsetEnd="39" lemma="," pos=","/>
          <token form="2017" id="t30" characterOffsetBegin="40" characterOffsetEnd="44" lemma="2017" pos="CD"/>
          <token form="." id="t31" characterOffsetBegin="44" characterOffsetEnd="45" lemma="." pos="."/>
        </tokens>
        <parse annotators="corenlp" root="sp21">
          <span children="t20 t21" symbol="NP" id="sp15"/>
          <span children="t24 t25" symbol="NP" id="sp16"/>
          <span children="t23 sp16" symbol="PP" id="sp17"/>
          <span children="t27 t28 t29 t30" symbol="NP" id="sp18"/>
          <span children="t26 sp18" symbol="PP" id="sp19"/>
          <span children="t22 sp17 sp19" symbol="VP" id="sp20"/>
          <span children="sp15 sp20 t31" symbol="S" id="sp21"/>
        </parse>
        <dependencies annotators="corenlp" type="basic">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep20"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep21"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep22"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep23"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep24"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep25"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep26"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep27"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep28"/>
          <dependency deprel="nmod" dependent="t27" head="t22" id="dep29"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep30"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep31"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep70"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep71"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep72"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep73"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep74"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep75"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep76"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep77"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep78"/>
          <dependency deprel="nmod" dependent="t27" head="t22" id="dep79"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep80"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep81"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep120"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep121"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep122"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep123"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep124"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep125"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep126"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep127"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep128"/>
          <dependency deprel="nmod" dependent="t27" head="t22" id="dep129"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep130"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep131"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep170"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep171"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep172"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep173"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep174"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep175"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep176"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep177"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep178"/>
          <dependency deprel="nmod" dependent="t27" head="t22" id="dep179"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep180"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep181"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep220"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep221"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep222"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep223"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep224"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep225"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep226"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep227"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep228"/>
          <dependency deprel="nmod" dependent="t27" head="t22" id="dep229"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep230"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep231"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="207" characterOffsetBegin="129" id="s3">
        After eating some escargot for the first time, Joe said, &quot;That was delicious!&quot;
        <NEs annotators="corenlp">
          <NE tokens="t38" normalizedLabel="1.0" label="ORDINAL" id="s3_corene0"/>
          <NE tokens="t41" label="PERSON" id="s3_corene1"/>
        </NEs>
        <tokens annotators="corenlp">
          <token form="After" id="t32" characterOffsetBegin="0" characterOffsetEnd="5" lemma="after" pos="IN"/>
          <token form="eating" id="t33" characterOffsetBegin="6" characterOffsetEnd="12" lemma="eat" pos="VBG"/>
          <token form="some" id="t34" characterOffsetBegin="13" characterOffsetEnd="17" lemma="some" pos="DT"/>
          <token form="escargot" id="t35" characterOffsetBegin="18" characterOffsetEnd="26" lemma="escargot" pos="NN"/>
          <token form="for" id="t36" characterOffsetBegin="27" characterOffsetEnd="30" lemma="for" pos="IN"/>
          <token form="the" id="t37" characterOffsetBegin="31" characterOffsetEnd="34" lemma="the" pos="DT"/>
          <token form="first" id="t38" characterOffsetBegin="35" characterOffsetEnd="40" lemma="first" pos="JJ"/>
          <token form="time" id="t39" characterOffsetBegin="41" characterOffsetEnd="45" lemma="time" pos="NN"/>
          <token form="," id="t40" characterOffsetBegin="45" characterOffsetEnd="46" lemma="," pos=","/>
          <token form="Joe" id="t41" characterOffsetBegin="47" characterOffsetEnd="50" lemma="Joe" pos="NNP"/>
          <token form="said" id="t42" characterOffsetBegin="51" characterOffsetEnd="55" lemma="say" pos="VBD"/>
          <token form="," id="t43" characterOffsetBegin="55" characterOffsetEnd="56" lemma="," pos=","/>
          <token form="``" id="t44" characterOffsetBegin="57" characterOffsetEnd="58" lemma="``" pos="``"/>
          <token form="That" id="t45" characterOffsetBegin="58" characterOffsetEnd="62" lemma="that" pos="DT"/>
          <token form="was" id="t46" characterOffsetBegin="63" characterOffsetEnd="66" lemma="be" pos="VBD"/>
          <token form="delicious" id="t47" characterOffsetBegin="67" characterOffsetEnd="76" lemma="delicious" pos="JJ"/>
          <token form="!" id="t48" characterOffsetBegin="76" characterOffsetEnd="77" lemma="!" pos="."/>
          <token form="''" id="t49" characterOffsetBegin="77" characterOffsetEnd="78" lemma="''" pos="''"/>
        </tokens>
        <parse annotators="corenlp" root="sp35">
          <span children="t34 t35" symbol="NP" id="sp22"/>
          <span children="t37 t38 t39" symbol="NP" id="sp23"/>
          <span children="t36 sp23" symbol="PP" id="sp24"/>
          <span children="sp22 sp24" symbol="NP" id="sp25"/>
          <span children="t33 sp25" symbol="VP" id="sp26"/>
          <span children="sp26" symbol="S" id="sp27"/>
          <span children="t32 sp27" symbol="PP" id="sp28"/>
          <span children="t41" symbol="NP" id="sp29"/>
          <span children="t42" symbol="VP" id="sp30"/>
          <span children="t40 sp29 sp30 t43" symbol="PRN" id="sp31"/>
          <span children="t45" symbol="NP" id="sp32"/>
          <span children="t47" symbol="ADJP" id="sp33"/>
          <span children="t46 sp33" symbol="VP" id="sp34"/>
          <span children="sp28 sp31 t44 sp32 sp34 t48 t49" symbol="S" id="sp35"/>
        </parse>
        <dependencies annotators="corenlp" type="basic">
          <dependency deprel="root" dependent="t47" head="ROOT" id="dep32"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep33"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep34"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep35"/>
          <dependency deprel="parataxis" dependent="t42" head="t47" id="dep36"/>
          <dependency deprel="punct" dependent="t44" head="t47" id="dep37"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep38"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep39"/>
          <dependency deprel="punct" dependent="t48" head="t47" id="dep40"/>
          <dependency deprel="advcl" dependent="t33" head="t47" id="dep41"/>
          <dependency deprel="punct" dependent="t49" head="t47" id="dep42"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep43"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep44"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep45"/>
          <dependency deprel="nmod" dependent="t39" head="t35" id="dep46"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep47"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep48"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep49"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed">
          <dependency deprel="root" dependent="t47" head="ROOT" id="dep82"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep83"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep84"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep85"/>
          <dependency deprel="parataxis" dependent="t42" head="t47" id="dep86"/>
          <dependency deprel="punct" dependent="t44" head="t47" id="dep87"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep88"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep89"/>
          <dependency deprel="punct" dependent="t48" head="t47" id="dep90"/>
          <dependency deprel="advcl" dependent="t33" head="t47" id="dep91"/>
          <dependency deprel="punct" dependent="t49" head="t47" id="dep92"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep93"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep94"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep95"/>
          <dependency deprel="nmod" dependent="t39" head="t35" id="dep96"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep97"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep98"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep99"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t47" head="ROOT" id="dep132"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep133"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep134"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep135"/>
          <dependency deprel="parataxis" dependent="t42" head="t47" id="dep136"/>
          <dependency deprel="punct" dependent="t44" head="t47" id="dep137"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep138"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep139"/>
          <dependency deprel="punct" dependent="t48" head="t47" id="dep140"/>
          <dependency deprel="advcl" dependent="t33" head="t47" id="dep141"/>
          <dependency deprel="punct" dependent="t49" head="t47" id="dep142"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep143"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep144"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep145"/>
          <dependency deprel="nmod" dependent="t39" head="t35" id="dep146"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep147"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep148"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep149"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced">
          <dependency deprel="root" dependent="t47" head="ROOT" id="dep182"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep183"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep184"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep185"/>
          <dependency deprel="parataxis" dependent="t42" head="t47" id="dep186"/>
          <dependency deprel="punct" dependent="t44" head="t47" id="dep187"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep188"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep189"/>
          <dependency deprel="punct" dependent="t48" head="t47" id="dep190"/>
          <dependency deprel="advcl" dependent="t33" head="t47" id="dep191"/>
          <dependency deprel="punct" dependent="t49" head="t47" id="dep192"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep193"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep194"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep195"/>
          <dependency deprel="nmod" dependent="t39" head="t35" id="dep196"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep197"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep198"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep199"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t47" head="ROOT" id="dep232"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep233"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep234"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep235"/>
          <dependency deprel="parataxis" dependent="t42" head="t47" id="dep236"/>
          <dependency deprel="punct" dependent="t44" head="t47" id="dep237"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep238"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep239"/>
          <dependency deprel="punct" dependent="t48" head="t47" id="dep240"/>
          <dependency deprel="advcl" dependent="t33" head="t47" id="dep241"/>
          <dependency deprel="punct" dependent="t49" head="t47" id="dep242"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep243"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep244"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep245"/>
          <dependency deprel="nmod" dependent="t39" head="t35" id="dep246"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep247"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep248"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep249"/>
        </dependencies>
      </sentence>
    </sentences>
    <mentions annotators="corenlp">
      <mention head="t1" tokens="t0 t1" id="me0"/>
      <mention head="t5" tokens="t5" id="me1"/>
      <mention head="t13" tokens="t13" id="me2"/>
      <mention head="t8" tokens="t8" id="me3"/>
      <mention head="t10" tokens="t10" id="me4"/>
      <mention head="t15" tokens="t13 t14 t15" id="me5"/>
      <mention head="t18" tokens="t17 t18" id="me6"/>
      <mention head="t21" tokens="t20 t21" id="me7"/>
      <mention head="t20" tokens="t20" id="me8"/>
      <mention head="t25" tokens="t24 t25" id="me9"/>
      <mention head="t27" tokens="t27 t28 t29 t30" id="me10"/>
      <mention head="t38" tokens="t38" id="me11"/>
      <mention head="t41" tokens="t41" id="me12"/>
      <mention head="t35" tokens="t34 t35 t36 t37 t38 t39" id="me13"/>
      <mention head="t39" tokens="t37 t38 t39" id="me14"/>
      <mention head="t45" tokens="t45" id="me15"/>
    </mentions>
    <coreferences annotators="corenlp">
      <coreference representative="me0" mentions="me0 me4 me8 me12" id="cr0"/>
      <coreference representative="me1" mentions="me1" id="cr1"/>
      <coreference representative="me2" mentions="me2" id="cr2"/>
      <coreference representative="me3" mentions="me3" id="cr3"/>
      <coreference representative="me5" mentions="me5" id="cr4"/>
      <coreference representative="me6" mentions="me6" id="cr5"/>
      <coreference representative="me7" mentions="me7" id="cr6"/>
      <coreference representative="me9" mentions="me9" id="cr7"/>
      <coreference representative="me10" mentions="me10" id="cr8"/>
      <coreference representative="me11" mentions="me11" id="cr9"/>
      <coreference representative="me13" mentions="me13 me15" id="cr10"/>
      <coreference representative="me14" mentions="me14" id="cr11"/>
    </coreferences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline ' \
                   + '-annotators corenlp[tokenize,ssplit,parse,lemma,ner,dcoref] '

    def test_dcoref(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
