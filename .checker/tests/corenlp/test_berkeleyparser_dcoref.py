import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestBerkeleyParserAndDcoref(BaseTest):
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
        <parse root="s0_berksp0" annotators="berkeleyparser">
          <span children="s0_berksp1 s0_berksp2 t6" symbol="S" id="s0_berksp0"/>
          <span children="t0 t1" symbol="NP" id="s0_berksp1"/>
          <span children="t2 s0_berksp3" symbol="VP" id="s0_berksp2"/>
          <span children="t3 s0_berksp4" symbol="VP" id="s0_berksp3"/>
          <span children="t4 s0_berksp5" symbol="PP" id="s0_berksp4"/>
          <span children="t5" symbol="NP" id="s0_berksp5"/>
        </parse>
        <dependencies annotators="stanfordtypeddep" type="basic">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep0"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep1"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep2"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep3"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep4"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep5"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep6"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="collapsed">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep7"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep8"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep9"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep10"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep11"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep12"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep13"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep14"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep15"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep16"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep17"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep18"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep19"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep20"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="enhanced">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep21"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep22"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep23"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep24"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep25"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep26"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep27"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t3" head="ROOT" id="dep28"/>
          <dependency deprel="compound" dependent="t0" head="t1" id="dep29"/>
          <dependency deprel="nsubjpass" dependent="t1" head="t3" id="dep30"/>
          <dependency deprel="auxpass" dependent="t2" head="t3" id="dep31"/>
          <dependency deprel="nmod" dependent="t5" head="t3" id="dep32"/>
          <dependency deprel="punct" dependent="t6" head="t3" id="dep33"/>
          <dependency deprel="case" dependent="t4" head="t5" id="dep34"/>
        </dependencies>
        <tokens annotators="corenlp berkeleyparser">
          <token form="Joe" id="t0" characterOffsetBegin="0" characterOffsetEnd="3" pos="NNP" lemma="Joe"/>
          <token form="Smith" id="t1" characterOffsetBegin="4" characterOffsetEnd="9" pos="NNP" lemma="Smith"/>
          <token form="was" id="t2" characterOffsetBegin="10" characterOffsetEnd="13" pos="VBD" lemma="be"/>
          <token form="born" id="t3" characterOffsetBegin="14" characterOffsetEnd="18" pos="VBN" lemma="bear"/>
          <token form="in" id="t4" characterOffsetBegin="19" characterOffsetEnd="21" pos="IN" lemma="in"/>
          <token form="California" id="t5" characterOffsetBegin="22" characterOffsetEnd="32" pos="NNP" lemma="California"/>
          <token form="." id="t6" characterOffsetBegin="32" characterOffsetEnd="33" pos="." lemma="."/>
        </tokens>
        <NEs annotators="corenlp">
          <NE tokens="t0 t1" label="PERSON" id="s0_corene0"/>
          <NE tokens="t5" label="STATE_OR_PROVINCE" id="s0_corene1"/>
        </NEs>
      </sentence>
      <sentence characterOffsetEnd="82" characterOffsetBegin="34" id="s1">
        In 2017, he went to Paris, France in the summer.
        <parse root="s1_berksp0" annotators="berkeleyparser">
          <span children="s1_berksp1 t9 s1_berksp3 s1_berksp4 t19" symbol="S" id="s1_berksp0"/>
          <span children="t7 s1_berksp2" symbol="PP" id="s1_berksp1"/>
          <span children="t8" symbol="NP" id="s1_berksp2"/>
          <span children="t10" symbol="NP" id="s1_berksp3"/>
          <span children="t11 s1_berksp5 s1_berksp8" symbol="VP" id="s1_berksp4"/>
          <span children="t12 s1_berksp6" symbol="PP" id="s1_berksp5"/>
          <span children="s1_berksp7 t14 t15" symbol="NP" id="s1_berksp6"/>
          <span children="t13" symbol="NP" id="s1_berksp7"/>
          <span children="t16 s1_berksp9" symbol="PP" id="s1_berksp8"/>
          <span children="t17 t18" symbol="NP" id="s1_berksp9"/>
        </parse>
        <dependencies annotators="stanfordtypeddep" type="basic">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep95"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep96"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep97"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep98"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep99"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep100"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep101"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep102"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep103"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep104"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep105"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep106"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep107"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="collapsed">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep108"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep109"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep110"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep111"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep112"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep113"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep114"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep115"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep116"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep117"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep118"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep119"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep120"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep121"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep122"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep123"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep124"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep125"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep126"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep127"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep128"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep129"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep130"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep131"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep132"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep133"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="enhanced">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep134"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep135"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep136"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep137"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep138"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep139"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep140"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep141"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep142"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep143"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep144"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep145"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep146"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t11" head="ROOT" id="dep147"/>
          <dependency deprel="case" dependent="t7" head="t8" id="dep148"/>
          <dependency deprel="nmod" dependent="t8" head="t11" id="dep149"/>
          <dependency deprel="punct" dependent="t9" head="t11" id="dep150"/>
          <dependency deprel="nsubj" dependent="t10" head="t11" id="dep151"/>
          <dependency deprel="nmod" dependent="t15" head="t11" id="dep152"/>
          <dependency deprel="nmod" dependent="t18" head="t11" id="dep153"/>
          <dependency deprel="punct" dependent="t19" head="t11" id="dep154"/>
          <dependency deprel="case" dependent="t12" head="t15" id="dep155"/>
          <dependency deprel="compound" dependent="t13" head="t15" id="dep156"/>
          <dependency deprel="punct" dependent="t14" head="t15" id="dep157"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep158"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep159"/>
        </dependencies>
        <tokens annotators="corenlp berkeleyparser">
          <token form="In" id="t7" characterOffsetBegin="0" characterOffsetEnd="2" pos="IN" lemma="in"/>
          <token form="2017" id="t8" characterOffsetBegin="3" characterOffsetEnd="7" pos="CD" lemma="2017"/>
          <token form="," id="t9" characterOffsetBegin="7" characterOffsetEnd="8" pos="," lemma=","/>
          <token form="he" id="t10" characterOffsetBegin="9" characterOffsetEnd="11" pos="PRP" lemma="he"/>
          <token form="went" id="t11" characterOffsetBegin="12" characterOffsetEnd="16" pos="VBD" lemma="go"/>
          <token form="to" id="t12" characterOffsetBegin="17" characterOffsetEnd="19" pos="TO" lemma="to"/>
          <token form="Paris" id="t13" characterOffsetBegin="20" characterOffsetEnd="25" pos="NNP" lemma="Paris"/>
          <token form="," id="t14" characterOffsetBegin="25" characterOffsetEnd="26" pos="," lemma=","/>
          <token form="France" id="t15" characterOffsetBegin="27" characterOffsetEnd="33" pos="NNP" lemma="France"/>
          <token form="in" id="t16" characterOffsetBegin="34" characterOffsetEnd="36" pos="IN" lemma="in"/>
          <token form="the" id="t17" characterOffsetBegin="37" characterOffsetEnd="40" pos="DT" lemma="the"/>
          <token form="summer" id="t18" characterOffsetBegin="41" characterOffsetEnd="47" pos="NN" lemma="summer"/>
          <token form="." id="t19" characterOffsetBegin="47" characterOffsetEnd="48" pos="." lemma="."/>
        </tokens>
        <NEs annotators="corenlp">
          <NE tokens="t8" normalizedLabel="2017" label="DATE" id="s1_corene0"/>
          <NE tokens="t13" label="CITY" id="s1_corene1"/>
          <NE tokens="t15" label="COUNTRY" id="s1_corene2"/>
          <NE tokens="t18" normalizedLabel="XXXX-SU" label="DATE" id="s1_corene3"/>
        </NEs>
      </sentence>
      <sentence characterOffsetEnd="128" characterOffsetBegin="83" id="s2">
        His flight left at 3:00pm on July 10th, 2017.
        <parse root="s2_berksp0" annotators="berkeleyparser">
          <span children="s2_berksp1 s2_berksp2 t31" symbol="S" id="s2_berksp0"/>
          <span children="t20 t21" symbol="NP" id="s2_berksp1"/>
          <span children="t22 s2_berksp3" symbol="VP" id="s2_berksp2"/>
          <span children="t23 s2_berksp4" symbol="PP" id="s2_berksp3"/>
          <span children="s2_berksp5 s2_berksp6" symbol="NP" id="s2_berksp4"/>
          <span children="t24 t25" symbol="NP" id="s2_berksp5"/>
          <span children="t26 s2_berksp7" symbol="PP" id="s2_berksp6"/>
          <span children="t27 t28 t29 t30" symbol="NP" id="s2_berksp7"/>
        </parse>
        <dependencies annotators="stanfordtypeddep" type="basic">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep35"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep36"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep37"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep38"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep39"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep40"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep41"/>
          <dependency deprel="nmod" dependent="t27" head="t25" id="dep42"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep43"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep44"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep45"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep46"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="collapsed">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep47"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep48"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep49"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep50"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep51"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep52"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep53"/>
          <dependency deprel="nmod" dependent="t27" head="t25" id="dep54"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep55"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep56"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep57"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep58"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep59"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep60"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep61"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep62"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep63"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep64"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep65"/>
          <dependency deprel="nmod" dependent="t27" head="t25" id="dep66"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep67"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep68"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep69"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep70"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="enhanced">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep71"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep72"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep73"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep74"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep75"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep76"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep77"/>
          <dependency deprel="nmod" dependent="t27" head="t25" id="dep78"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep79"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep80"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep81"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep82"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t22" head="ROOT" id="dep83"/>
          <dependency deprel="nmod:poss" dependent="t20" head="t21" id="dep84"/>
          <dependency deprel="nsubj" dependent="t21" head="t22" id="dep85"/>
          <dependency deprel="nmod" dependent="t25" head="t22" id="dep86"/>
          <dependency deprel="punct" dependent="t31" head="t22" id="dep87"/>
          <dependency deprel="case" dependent="t23" head="t25" id="dep88"/>
          <dependency deprel="nummod" dependent="t24" head="t25" id="dep89"/>
          <dependency deprel="nmod" dependent="t27" head="t25" id="dep90"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep91"/>
          <dependency deprel="nummod" dependent="t28" head="t27" id="dep92"/>
          <dependency deprel="punct" dependent="t29" head="t27" id="dep93"/>
          <dependency deprel="nummod" dependent="t30" head="t27" id="dep94"/>
        </dependencies>
        <tokens annotators="corenlp berkeleyparser">
          <token form="His" id="t20" characterOffsetBegin="0" characterOffsetEnd="3" pos="PRP$" lemma="he"/>
          <token form="flight" id="t21" characterOffsetBegin="4" characterOffsetEnd="10" pos="NN" lemma="flight"/>
          <token form="left" id="t22" characterOffsetBegin="11" characterOffsetEnd="15" pos="VBD" lemma="leave"/>
          <token form="at" id="t23" characterOffsetBegin="16" characterOffsetEnd="18" pos="IN" lemma="at"/>
          <token form="3:00" id="t24" characterOffsetBegin="19" characterOffsetEnd="23" pos="CD" lemma="3:00"/>
          <token form="pm" id="t25" characterOffsetBegin="23" characterOffsetEnd="25" pos="NN" lemma="pm"/>
          <token form="on" id="t26" characterOffsetBegin="26" characterOffsetEnd="28" pos="IN" lemma="on"/>
          <token form="July" id="t27" characterOffsetBegin="29" characterOffsetEnd="33" pos="NNP" lemma="July"/>
          <token form="10th" id="t28" characterOffsetBegin="34" characterOffsetEnd="38" pos="CD" lemma="10th"/>
          <token form="," id="t29" characterOffsetBegin="38" characterOffsetEnd="39" pos="," lemma=","/>
          <token form="2017" id="t30" characterOffsetBegin="40" characterOffsetEnd="44" pos="CD" lemma="2017"/>
          <token form="." id="t31" characterOffsetBegin="44" characterOffsetEnd="45" pos="." lemma="."/>
        </tokens>
        <NEs annotators="corenlp">
          <NE tokens="t24 t25 t26" normalizedLabel="2017-07-10T15:00" label="TIME" id="s2_corene0"/>
          <NE tokens="t27 t28 t29 t30" normalizedLabel="2017-07-10T15:00" label="DATE" id="s2_corene1"/>
        </NEs>
      </sentence>
      <sentence characterOffsetEnd="207" characterOffsetBegin="129" id="s3">
        After eating some escargot for the first time, Joe said, &quot;That was delicious!&quot;
        <parse root="s3_berksp0" annotators="berkeleyparser">
          <span children="s3_berksp1 t40 s3_berksp7 s3_berksp8 t48 t49" symbol="S" id="s3_berksp0"/>
          <span children="t32 s3_berksp2" symbol="PP" id="s3_berksp1"/>
          <span children="s3_berksp3" symbol="S" id="s3_berksp2"/>
          <span children="t33 s3_berksp4 s3_berksp5" symbol="VP" id="s3_berksp3"/>
          <span children="t34 t35" symbol="NP" id="s3_berksp4"/>
          <span children="t36 s3_berksp6" symbol="PP" id="s3_berksp5"/>
          <span children="t37 t38 t39" symbol="NP" id="s3_berksp6"/>
          <span children="t41" symbol="NP" id="s3_berksp7"/>
          <span children="t42 t43 t44 s3_berksp9" symbol="VP" id="s3_berksp8"/>
          <span children="s3_berksp10 s3_berksp11" symbol="S" id="s3_berksp9"/>
          <span children="t45" symbol="NP" id="s3_berksp10"/>
          <span children="t46 s3_berksp12" symbol="VP" id="s3_berksp11"/>
          <span children="t47" symbol="ADJP" id="s3_berksp12"/>
        </parse>
        <dependencies annotators="stanfordtypeddep" type="basic">
          <dependency deprel="root" dependent="t42" head="ROOT" id="dep160"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep161"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep162"/>
          <dependency deprel="nmod" dependent="t39" head="t33" id="dep163"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep164"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep165"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep166"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep167"/>
          <dependency deprel="ccomp" dependent="t47" head="t42" id="dep168"/>
          <dependency deprel="punct" dependent="t48" head="t42" id="dep169"/>
          <dependency deprel="advcl" dependent="t33" head="t42" id="dep170"/>
          <dependency deprel="punct" dependent="t49" head="t42" id="dep171"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep172"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep173"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep174"/>
          <dependency deprel="punct" dependent="t44" head="t42" id="dep175"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep176"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep177"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="collapsed">
          <dependency deprel="root" dependent="t42" head="ROOT" id="dep178"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep179"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep180"/>
          <dependency deprel="nmod" dependent="t39" head="t33" id="dep181"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep182"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep183"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep184"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep185"/>
          <dependency deprel="ccomp" dependent="t47" head="t42" id="dep186"/>
          <dependency deprel="punct" dependent="t48" head="t42" id="dep187"/>
          <dependency deprel="advcl" dependent="t33" head="t42" id="dep188"/>
          <dependency deprel="punct" dependent="t49" head="t42" id="dep189"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep190"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep191"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep192"/>
          <dependency deprel="punct" dependent="t44" head="t42" id="dep193"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep194"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep195"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t42" head="ROOT" id="dep196"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep197"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep198"/>
          <dependency deprel="nmod" dependent="t39" head="t33" id="dep199"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep200"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep201"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep202"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep203"/>
          <dependency deprel="ccomp" dependent="t47" head="t42" id="dep204"/>
          <dependency deprel="punct" dependent="t48" head="t42" id="dep205"/>
          <dependency deprel="advcl" dependent="t33" head="t42" id="dep206"/>
          <dependency deprel="punct" dependent="t49" head="t42" id="dep207"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep208"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep209"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep210"/>
          <dependency deprel="punct" dependent="t44" head="t42" id="dep211"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep212"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep213"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="enhanced">
          <dependency deprel="root" dependent="t42" head="ROOT" id="dep214"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep215"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep216"/>
          <dependency deprel="nmod" dependent="t39" head="t33" id="dep217"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep218"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep219"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep220"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep221"/>
          <dependency deprel="ccomp" dependent="t47" head="t42" id="dep222"/>
          <dependency deprel="punct" dependent="t48" head="t42" id="dep223"/>
          <dependency deprel="advcl" dependent="t33" head="t42" id="dep224"/>
          <dependency deprel="punct" dependent="t49" head="t42" id="dep225"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep226"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep227"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep228"/>
          <dependency deprel="punct" dependent="t44" head="t42" id="dep229"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep230"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep231"/>
        </dependencies>
        <dependencies annotators="stanfordtypeddep" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t42" head="ROOT" id="dep232"/>
          <dependency deprel="mark" dependent="t32" head="t33" id="dep233"/>
          <dependency deprel="dobj" dependent="t35" head="t33" id="dep234"/>
          <dependency deprel="nmod" dependent="t39" head="t33" id="dep235"/>
          <dependency deprel="det" dependent="t34" head="t35" id="dep236"/>
          <dependency deprel="case" dependent="t36" head="t39" id="dep237"/>
          <dependency deprel="det" dependent="t37" head="t39" id="dep238"/>
          <dependency deprel="amod" dependent="t38" head="t39" id="dep239"/>
          <dependency deprel="ccomp" dependent="t47" head="t42" id="dep240"/>
          <dependency deprel="punct" dependent="t48" head="t42" id="dep241"/>
          <dependency deprel="advcl" dependent="t33" head="t42" id="dep242"/>
          <dependency deprel="punct" dependent="t49" head="t42" id="dep243"/>
          <dependency deprel="punct" dependent="t40" head="t42" id="dep244"/>
          <dependency deprel="nsubj" dependent="t41" head="t42" id="dep245"/>
          <dependency deprel="punct" dependent="t43" head="t42" id="dep246"/>
          <dependency deprel="punct" dependent="t44" head="t42" id="dep247"/>
          <dependency deprel="nsubj" dependent="t45" head="t47" id="dep248"/>
          <dependency deprel="cop" dependent="t46" head="t47" id="dep249"/>
        </dependencies>
        <tokens annotators="corenlp berkeleyparser">
          <token form="After" id="t32" characterOffsetBegin="0" characterOffsetEnd="5" pos="IN" lemma="after"/>
          <token form="eating" id="t33" characterOffsetBegin="6" characterOffsetEnd="12" pos="VBG" lemma="eat"/>
          <token form="some" id="t34" characterOffsetBegin="13" characterOffsetEnd="17" pos="DT" lemma="some"/>
          <token form="escargot" id="t35" characterOffsetBegin="18" characterOffsetEnd="26" pos="NN" lemma="escargot"/>
          <token form="for" id="t36" characterOffsetBegin="27" characterOffsetEnd="30" pos="IN" lemma="for"/>
          <token form="the" id="t37" characterOffsetBegin="31" characterOffsetEnd="34" pos="DT" lemma="the"/>
          <token form="first" id="t38" characterOffsetBegin="35" characterOffsetEnd="40" pos="JJ" lemma="first"/>
          <token form="time" id="t39" characterOffsetBegin="41" characterOffsetEnd="45" pos="NN" lemma="time"/>
          <token form="," id="t40" characterOffsetBegin="45" characterOffsetEnd="46" pos="," lemma=","/>
          <token form="Joe" id="t41" characterOffsetBegin="47" characterOffsetEnd="50" pos="NNP" lemma="Joe"/>
          <token form="said" id="t42" characterOffsetBegin="51" characterOffsetEnd="55" pos="VBD" lemma="say"/>
          <token form="," id="t43" characterOffsetBegin="55" characterOffsetEnd="56" pos="," lemma=","/>
          <token form="``" id="t44" characterOffsetBegin="57" characterOffsetEnd="58" pos="``" lemma="``"/>
          <token form="That" id="t45" characterOffsetBegin="58" characterOffsetEnd="62" pos="DT" lemma="that"/>
          <token form="was" id="t46" characterOffsetBegin="63" characterOffsetEnd="66" pos="VBD" lemma="be"/>
          <token form="delicious" id="t47" characterOffsetBegin="67" characterOffsetEnd="76" pos="JJ" lemma="delicious"/>
          <token form="!" id="t48" characterOffsetBegin="76" characterOffsetEnd="77" pos="." lemma="!"/>
          <token form="''" id="t49" characterOffsetBegin="77" characterOffsetEnd="78" pos="''" lemma="''"/>
        </tokens>
        <NEs annotators="corenlp">
          <NE tokens="t38" normalizedLabel="1.0" label="ORDINAL" id="s3_corene0"/>
          <NE tokens="t41" label="PERSON" id="s3_corene1"/>
        </NEs>
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
      <mention head="t25" tokens="t24 t25 t26 t27 t28 t29 t30" id="me9"/>
      <mention head="t27" tokens="t27 t28 t29 t30" id="me10"/>
      <mention head="t38" tokens="t38" id="me11"/>
      <mention head="t41" tokens="t41" id="me12"/>
      <mention head="t35" tokens="t34 t35" id="me13"/>
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

        self.exe = 'java -cp "jar/*" -Xmx8g jigg.pipeline.Pipeline ' \
                   + '-annotators corenlp[tokenize,ssplit],berkeleyparser,stanfordtypeddep,corenlp[lemma,ner,dcoref] '

    def test_berkerleyparser_and_dcoref(self):
        self.check_equal_with_java(self.exe, self.input_text, self.expected_text)
