import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestCoreNLPChineseCoref(BaseTest):
    '''
    '''
    def setUp(self):
        # Set an input (sample) text
        self.input_text = """克林顿说，华盛顿将逐步落实对韩国的经济援助。金大中对克林顿的讲话报以掌声：克林顿总统在会谈中重申，他坚定地支持韩国摆脱经济危机。"""

        # Set an expected text
        self.expected_text = """<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="22" characterOffsetBegin="0" id="s0">
        克林顿说，华盛顿将逐步落实对韩国的经济援助。
        <NEs annotators="corenlp">
          <NE tokens="t0" label="PERSON" id="s0_corene0"/>
          <NE tokens="t3" label="STATE_OR_PROVINCE" id="s0_corene1"/>
          <NE tokens="t9" label="COUNTRY" id="s0_corene2"/>
        </NEs>
        <tokens annotators="corenlp">
          <token form="克林顿" id="t0" characterOffsetBegin="0" characterOffsetEnd="3" lemma="克林顿" pos="NR"/>
          <token form="说" id="t1" characterOffsetBegin="3" characterOffsetEnd="4" lemma="说" pos="VV"/>
          <token form="，" id="t2" characterOffsetBegin="4" characterOffsetEnd="5" lemma="，" pos="PU"/>
          <token form="华盛顿" id="t3" characterOffsetBegin="5" characterOffsetEnd="8" lemma="华盛顿" pos="NR"/>
          <token form="将" id="t4" characterOffsetBegin="8" characterOffsetEnd="9" lemma="将" pos="AD"/>
          <token form="逐步" id="t5" characterOffsetBegin="9" characterOffsetEnd="11" lemma="逐步" pos="AD"/>
          <token form="落" id="t6" characterOffsetBegin="11" characterOffsetEnd="12" lemma="落" pos="VV"/>
          <token form="实" id="t7" characterOffsetBegin="12" characterOffsetEnd="13" lemma="实" pos="AD"/>
          <token form="对" id="t8" characterOffsetBegin="13" characterOffsetEnd="14" lemma="对" pos="P"/>
          <token form="韩国" id="t9" characterOffsetBegin="14" characterOffsetEnd="16" lemma="韩国" pos="NR"/>
          <token form="的" id="t10" characterOffsetBegin="16" characterOffsetEnd="17" lemma="的" pos="DEG"/>
          <token form="经济" id="t11" characterOffsetBegin="17" characterOffsetEnd="19" lemma="经济" pos="NN"/>
          <token form="援助" id="t12" characterOffsetBegin="19" characterOffsetEnd="21" lemma="援助" pos="NN"/>
          <token form="。" id="t13" characterOffsetBegin="21" characterOffsetEnd="22" lemma="。" pos="PU"/>
        </tokens>
        <parse annotators="corenlp" root="sp16">
          <span children="t0" symbol="NP" id="sp0"/>
          <span children="t3" symbol="NP" id="sp1"/>
          <span children="t4" symbol="ADVP" id="sp2"/>
          <span children="t5" symbol="ADVP" id="sp3"/>
          <span children="t7" symbol="ADVP" id="sp4"/>
          <span children="t9" symbol="NP" id="sp5"/>
          <span children="sp5 t10" symbol="DNP" id="sp6"/>
          <span children="t11 t12" symbol="NP" id="sp7"/>
          <span children="sp6 sp7" symbol="NP" id="sp8"/>
          <span children="t8 sp8" symbol="PP" id="sp9"/>
          <span children="sp4 sp9" symbol="VP" id="sp10"/>
          <span children="sp10" symbol="IP" id="sp11"/>
          <span children="t6 sp11" symbol="VP" id="sp12"/>
          <span children="sp2 sp3 sp12" symbol="VP" id="sp13"/>
          <span children="sp1 sp13" symbol="IP" id="sp14"/>
          <span children="t1 t2 sp14" symbol="VP" id="sp15"/>
          <span children="sp0 sp15 t13" symbol="IP" id="sp16"/>
        </parse>
        <dependencies annotators="corenlp" type="basic">
          <dependency deprel="root" dependent="t1" head="ROOT" id="dep0"/>
          <dependency deprel="nsubj" dependent="t0" head="t1" id="dep1"/>
          <dependency deprel="punct" dependent="t2" head="t1" id="dep2"/>
          <dependency deprel="ccomp" dependent="t6" head="t1" id="dep3"/>
          <dependency deprel="punct" dependent="t13" head="t1" id="dep4"/>
          <dependency deprel="nsubj" dependent="t3" head="t6" id="dep5"/>
          <dependency deprel="advmod" dependent="t4" head="t6" id="dep6"/>
          <dependency deprel="advmod" dependent="t5" head="t6" id="dep7"/>
          <dependency deprel="ccomp" dependent="t7" head="t6" id="dep8"/>
          <dependency deprel="nmod:prep" dependent="t12" head="t7" id="dep9"/>
          <dependency deprel="case" dependent="t10" head="t9" id="dep10"/>
          <dependency deprel="case" dependent="t8" head="t12" id="dep11"/>
          <dependency deprel="nmod:assmod" dependent="t9" head="t12" id="dep12"/>
          <dependency deprel="compound:nn" dependent="t11" head="t12" id="dep13"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed">
          <dependency deprel="root" dependent="t1" head="ROOT" id="dep40"/>
          <dependency deprel="nsubj" dependent="t0" head="t1" id="dep41"/>
          <dependency deprel="punct" dependent="t2" head="t1" id="dep42"/>
          <dependency deprel="ccomp" dependent="t6" head="t1" id="dep43"/>
          <dependency deprel="punct" dependent="t13" head="t1" id="dep44"/>
          <dependency deprel="nsubj" dependent="t3" head="t6" id="dep45"/>
          <dependency deprel="advmod" dependent="t4" head="t6" id="dep46"/>
          <dependency deprel="advmod" dependent="t5" head="t6" id="dep47"/>
          <dependency deprel="ccomp" dependent="t7" head="t6" id="dep48"/>
          <dependency deprel="nmod:prep" dependent="t12" head="t7" id="dep49"/>
          <dependency deprel="case" dependent="t10" head="t9" id="dep50"/>
          <dependency deprel="case" dependent="t8" head="t12" id="dep51"/>
          <dependency deprel="nmod:assmod" dependent="t9" head="t12" id="dep52"/>
          <dependency deprel="compound:nn" dependent="t11" head="t12" id="dep53"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t1" head="ROOT" id="dep80"/>
          <dependency deprel="nsubj" dependent="t0" head="t1" id="dep81"/>
          <dependency deprel="punct" dependent="t2" head="t1" id="dep82"/>
          <dependency deprel="ccomp" dependent="t6" head="t1" id="dep83"/>
          <dependency deprel="punct" dependent="t13" head="t1" id="dep84"/>
          <dependency deprel="nsubj" dependent="t3" head="t6" id="dep85"/>
          <dependency deprel="advmod" dependent="t4" head="t6" id="dep86"/>
          <dependency deprel="advmod" dependent="t5" head="t6" id="dep87"/>
          <dependency deprel="ccomp" dependent="t7" head="t6" id="dep88"/>
          <dependency deprel="nmod:prep" dependent="t12" head="t7" id="dep89"/>
          <dependency deprel="case" dependent="t10" head="t9" id="dep90"/>
          <dependency deprel="case" dependent="t8" head="t12" id="dep91"/>
          <dependency deprel="nmod:assmod" dependent="t9" head="t12" id="dep92"/>
          <dependency deprel="compound:nn" dependent="t11" head="t12" id="dep93"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced">
          <dependency deprel="root" dependent="t1" head="ROOT" id="dep120"/>
          <dependency deprel="nsubj" dependent="t0" head="t1" id="dep121"/>
          <dependency deprel="punct" dependent="t2" head="t1" id="dep122"/>
          <dependency deprel="ccomp" dependent="t6" head="t1" id="dep123"/>
          <dependency deprel="punct" dependent="t13" head="t1" id="dep124"/>
          <dependency deprel="nsubj" dependent="t3" head="t6" id="dep125"/>
          <dependency deprel="advmod" dependent="t4" head="t6" id="dep126"/>
          <dependency deprel="advmod" dependent="t5" head="t6" id="dep127"/>
          <dependency deprel="ccomp" dependent="t7" head="t6" id="dep128"/>
          <dependency deprel="nmod:prep" dependent="t12" head="t7" id="dep129"/>
          <dependency deprel="case" dependent="t10" head="t9" id="dep130"/>
          <dependency deprel="case" dependent="t8" head="t12" id="dep131"/>
          <dependency deprel="nmod:assmod" dependent="t9" head="t12" id="dep132"/>
          <dependency deprel="compound:nn" dependent="t11" head="t12" id="dep133"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t1" head="ROOT" id="dep160"/>
          <dependency deprel="nsubj" dependent="t0" head="t1" id="dep161"/>
          <dependency deprel="punct" dependent="t2" head="t1" id="dep162"/>
          <dependency deprel="ccomp" dependent="t6" head="t1" id="dep163"/>
          <dependency deprel="punct" dependent="t13" head="t1" id="dep164"/>
          <dependency deprel="nsubj" dependent="t3" head="t6" id="dep165"/>
          <dependency deprel="advmod" dependent="t4" head="t6" id="dep166"/>
          <dependency deprel="advmod" dependent="t5" head="t6" id="dep167"/>
          <dependency deprel="ccomp" dependent="t7" head="t6" id="dep168"/>
          <dependency deprel="nmod:prep" dependent="t12" head="t7" id="dep169"/>
          <dependency deprel="case" dependent="t10" head="t9" id="dep170"/>
          <dependency deprel="case" dependent="t8" head="t12" id="dep171"/>
          <dependency deprel="nmod:assmod" dependent="t9" head="t12" id="dep172"/>
          <dependency deprel="compound:nn" dependent="t11" head="t12" id="dep173"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="64" characterOffsetBegin="22" id="s1">
        金大中对克林顿的讲话报以掌声：克林顿总统在会谈中重申，他坚定地支持韩国摆脱经济危机。
        <NEs annotators="corenlp">
          <NE tokens="t18" label="PERSON" id="s1_corene0"/>
          <NE tokens="t24" label="PERSON" id="s1_corene1"/>
          <NE tokens="t25" label="TITLE" id="s1_corene2"/>
          <NE tokens="t35" label="COUNTRY" id="s1_corene3"/>
        </NEs>
        <tokens annotators="corenlp">
          <token form="金" id="t14" characterOffsetBegin="0" characterOffsetEnd="1" lemma="金" pos="NR"/>
          <token form="大" id="t15" characterOffsetBegin="1" characterOffsetEnd="2" lemma="大" pos="JJ"/>
          <token form="中" id="t16" characterOffsetBegin="2" characterOffsetEnd="3" lemma="中" pos="JJ"/>
          <token form="对" id="t17" characterOffsetBegin="3" characterOffsetEnd="4" lemma="对" pos="P"/>
          <token form="克林顿" id="t18" characterOffsetBegin="4" characterOffsetEnd="7" lemma="克林顿" pos="NR"/>
          <token form="的" id="t19" characterOffsetBegin="7" characterOffsetEnd="8" lemma="的" pos="DEG"/>
          <token form="讲话" id="t20" characterOffsetBegin="8" characterOffsetEnd="10" lemma="讲话" pos="NN"/>
          <token form="报以" id="t21" characterOffsetBegin="10" characterOffsetEnd="12" lemma="报以" pos="VV"/>
          <token form="掌声" id="t22" characterOffsetBegin="12" characterOffsetEnd="14" lemma="掌声" pos="NN"/>
          <token form="：" id="t23" characterOffsetBegin="14" characterOffsetEnd="15" lemma="：" pos="PU"/>
          <token form="克林顿" id="t24" characterOffsetBegin="15" characterOffsetEnd="18" lemma="克林顿" pos="NR"/>
          <token form="总统" id="t25" characterOffsetBegin="18" characterOffsetEnd="20" lemma="总统" pos="NN"/>
          <token form="在" id="t26" characterOffsetBegin="20" characterOffsetEnd="21" lemma="在" pos="P"/>
          <token form="会谈" id="t27" characterOffsetBegin="21" characterOffsetEnd="23" lemma="会谈" pos="NN"/>
          <token form="中" id="t28" characterOffsetBegin="23" characterOffsetEnd="24" lemma="中" pos="LC"/>
          <token form="重申" id="t29" characterOffsetBegin="24" characterOffsetEnd="26" lemma="重申" pos="VV"/>
          <token form="，" id="t30" characterOffsetBegin="26" characterOffsetEnd="27" lemma="，" pos="PU"/>
          <token form="他" id="t31" characterOffsetBegin="27" characterOffsetEnd="28" lemma="他" pos="PN"/>
          <token form="坚定" id="t32" characterOffsetBegin="28" characterOffsetEnd="30" lemma="坚定" pos="VA"/>
          <token form="地" id="t33" characterOffsetBegin="30" characterOffsetEnd="31" lemma="地" pos="DEV"/>
          <token form="支持" id="t34" characterOffsetBegin="31" characterOffsetEnd="33" lemma="支持" pos="VV"/>
          <token form="韩国" id="t35" characterOffsetBegin="33" characterOffsetEnd="35" lemma="韩国" pos="NR"/>
          <token form="摆脱" id="t36" characterOffsetBegin="35" characterOffsetEnd="37" lemma="摆脱" pos="VV"/>
          <token form="经济" id="t37" characterOffsetBegin="37" characterOffsetEnd="39" lemma="经济" pos="NN"/>
          <token form="危机" id="t38" characterOffsetBegin="39" characterOffsetEnd="41" lemma="危机" pos="NN"/>
          <token form="。" id="t39" characterOffsetBegin="41" characterOffsetEnd="42" lemma="。" pos="PU"/>
        </tokens>
        <parse annotators="corenlp" root="sp44">
          <span children="t14" symbol="NP" id="sp17"/>
          <span children="t18" symbol="NP" id="sp18"/>
          <span children="t17 sp18" symbol="PP" id="sp19"/>
          <span children="sp19 t19" symbol="DNP" id="sp20"/>
          <span children="t20" symbol="NP" id="sp21"/>
          <span children="t15 t16 sp20 sp21" symbol="NP" id="sp22"/>
          <span children="sp17 sp22" symbol="NP" id="sp23"/>
          <span children="t22" symbol="NP" id="sp24"/>
          <span children="t21 sp24" symbol="VP" id="sp25"/>
          <span children="sp23 sp25" symbol="IP" id="sp26"/>
          <span children="t24 t25" symbol="NP" id="sp27"/>
          <span children="t27" symbol="NP" id="sp28"/>
          <span children="sp28 t28" symbol="LCP" id="sp29"/>
          <span children="t26 sp29" symbol="PP" id="sp30"/>
          <span children="t31" symbol="NP" id="sp31"/>
          <span children="t32" symbol="VP" id="sp32"/>
          <span children="sp32 t33" symbol="DVP" id="sp33"/>
          <span children="t35" symbol="NP" id="sp34"/>
          <span children="t37 t38" symbol="NP" id="sp35"/>
          <span children="t36 sp35" symbol="VP" id="sp36"/>
          <span children="sp34 sp36" symbol="IP" id="sp37"/>
          <span children="t34 sp37" symbol="VP" id="sp38"/>
          <span children="sp33 sp38" symbol="VP" id="sp39"/>
          <span children="sp31 sp39" symbol="IP" id="sp40"/>
          <span children="t29 t30 sp40" symbol="VP" id="sp41"/>
          <span children="sp30 sp41" symbol="VP" id="sp42"/>
          <span children="sp27 sp42" symbol="IP" id="sp43"/>
          <span children="sp26 t23 sp43 t39" symbol="IP" id="sp44"/>
        </parse>
        <dependencies annotators="corenlp" type="basic">
          <dependency deprel="root" dependent="t21" head="ROOT" id="dep14"/>
          <dependency deprel="case" dependent="t17" head="t18" id="dep15"/>
          <dependency deprel="case" dependent="t19" head="t18" id="dep16"/>
          <dependency deprel="dep" dependent="t16" head="t20" id="dep17"/>
          <dependency deprel="nmod" dependent="t18" head="t20" id="dep18"/>
          <dependency deprel="nmod:assmod" dependent="t14" head="t20" id="dep19"/>
          <dependency deprel="dep" dependent="t15" head="t20" id="dep20"/>
          <dependency deprel="nsubj" dependent="t20" head="t21" id="dep21"/>
          <dependency deprel="dobj" dependent="t22" head="t21" id="dep22"/>
          <dependency deprel="punct" dependent="t23" head="t21" id="dep23"/>
          <dependency deprel="punct" dependent="t39" head="t21" id="dep24"/>
          <dependency deprel="conj" dependent="t29" head="t21" id="dep25"/>
          <dependency deprel="compound:nn" dependent="t24" head="t25" id="dep26"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep27"/>
          <dependency deprel="case" dependent="t28" head="t27" id="dep28"/>
          <dependency deprel="ccomp" dependent="t34" head="t29" id="dep29"/>
          <dependency deprel="nsubj" dependent="t25" head="t29" id="dep30"/>
          <dependency deprel="nmod:prep" dependent="t27" head="t29" id="dep31"/>
          <dependency deprel="punct" dependent="t30" head="t29" id="dep32"/>
          <dependency deprel="mark" dependent="t33" head="t32" id="dep33"/>
          <dependency deprel="advmod:dvp" dependent="t32" head="t34" id="dep34"/>
          <dependency deprel="ccomp" dependent="t36" head="t34" id="dep35"/>
          <dependency deprel="nsubj" dependent="t31" head="t34" id="dep36"/>
          <dependency deprel="nsubj" dependent="t35" head="t36" id="dep37"/>
          <dependency deprel="dobj" dependent="t38" head="t36" id="dep38"/>
          <dependency deprel="compound:nn" dependent="t37" head="t38" id="dep39"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed">
          <dependency deprel="root" dependent="t21" head="ROOT" id="dep54"/>
          <dependency deprel="case" dependent="t17" head="t18" id="dep55"/>
          <dependency deprel="case" dependent="t19" head="t18" id="dep56"/>
          <dependency deprel="dep" dependent="t16" head="t20" id="dep57"/>
          <dependency deprel="nmod" dependent="t18" head="t20" id="dep58"/>
          <dependency deprel="nmod:assmod" dependent="t14" head="t20" id="dep59"/>
          <dependency deprel="dep" dependent="t15" head="t20" id="dep60"/>
          <dependency deprel="nsubj" dependent="t20" head="t21" id="dep61"/>
          <dependency deprel="dobj" dependent="t22" head="t21" id="dep62"/>
          <dependency deprel="punct" dependent="t23" head="t21" id="dep63"/>
          <dependency deprel="punct" dependent="t39" head="t21" id="dep64"/>
          <dependency deprel="conj" dependent="t29" head="t21" id="dep65"/>
          <dependency deprel="compound:nn" dependent="t24" head="t25" id="dep66"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep67"/>
          <dependency deprel="case" dependent="t28" head="t27" id="dep68"/>
          <dependency deprel="ccomp" dependent="t34" head="t29" id="dep69"/>
          <dependency deprel="nsubj" dependent="t25" head="t29" id="dep70"/>
          <dependency deprel="nmod:prep" dependent="t27" head="t29" id="dep71"/>
          <dependency deprel="punct" dependent="t30" head="t29" id="dep72"/>
          <dependency deprel="mark" dependent="t33" head="t32" id="dep73"/>
          <dependency deprel="advmod:dvp" dependent="t32" head="t34" id="dep74"/>
          <dependency deprel="ccomp" dependent="t36" head="t34" id="dep75"/>
          <dependency deprel="nsubj" dependent="t31" head="t34" id="dep76"/>
          <dependency deprel="nsubj" dependent="t35" head="t36" id="dep77"/>
          <dependency deprel="dobj" dependent="t38" head="t36" id="dep78"/>
          <dependency deprel="compound:nn" dependent="t37" head="t38" id="dep79"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t21" head="ROOT" id="dep94"/>
          <dependency deprel="case" dependent="t17" head="t18" id="dep95"/>
          <dependency deprel="case" dependent="t19" head="t18" id="dep96"/>
          <dependency deprel="dep" dependent="t16" head="t20" id="dep97"/>
          <dependency deprel="nmod" dependent="t18" head="t20" id="dep98"/>
          <dependency deprel="nmod:assmod" dependent="t14" head="t20" id="dep99"/>
          <dependency deprel="dep" dependent="t15" head="t20" id="dep100"/>
          <dependency deprel="nsubj" dependent="t20" head="t21" id="dep101"/>
          <dependency deprel="dobj" dependent="t22" head="t21" id="dep102"/>
          <dependency deprel="punct" dependent="t23" head="t21" id="dep103"/>
          <dependency deprel="punct" dependent="t39" head="t21" id="dep104"/>
          <dependency deprel="conj" dependent="t29" head="t21" id="dep105"/>
          <dependency deprel="compound:nn" dependent="t24" head="t25" id="dep106"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep107"/>
          <dependency deprel="case" dependent="t28" head="t27" id="dep108"/>
          <dependency deprel="ccomp" dependent="t34" head="t29" id="dep109"/>
          <dependency deprel="nsubj" dependent="t25" head="t29" id="dep110"/>
          <dependency deprel="nmod:prep" dependent="t27" head="t29" id="dep111"/>
          <dependency deprel="punct" dependent="t30" head="t29" id="dep112"/>
          <dependency deprel="mark" dependent="t33" head="t32" id="dep113"/>
          <dependency deprel="advmod:dvp" dependent="t32" head="t34" id="dep114"/>
          <dependency deprel="ccomp" dependent="t36" head="t34" id="dep115"/>
          <dependency deprel="nsubj" dependent="t31" head="t34" id="dep116"/>
          <dependency deprel="nsubj" dependent="t35" head="t36" id="dep117"/>
          <dependency deprel="dobj" dependent="t38" head="t36" id="dep118"/>
          <dependency deprel="compound:nn" dependent="t37" head="t38" id="dep119"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced">
          <dependency deprel="root" dependent="t21" head="ROOT" id="dep134"/>
          <dependency deprel="case" dependent="t17" head="t18" id="dep135"/>
          <dependency deprel="case" dependent="t19" head="t18" id="dep136"/>
          <dependency deprel="dep" dependent="t16" head="t20" id="dep137"/>
          <dependency deprel="nmod" dependent="t18" head="t20" id="dep138"/>
          <dependency deprel="nmod:assmod" dependent="t14" head="t20" id="dep139"/>
          <dependency deprel="dep" dependent="t15" head="t20" id="dep140"/>
          <dependency deprel="nsubj" dependent="t20" head="t21" id="dep141"/>
          <dependency deprel="dobj" dependent="t22" head="t21" id="dep142"/>
          <dependency deprel="punct" dependent="t23" head="t21" id="dep143"/>
          <dependency deprel="punct" dependent="t39" head="t21" id="dep144"/>
          <dependency deprel="conj" dependent="t29" head="t21" id="dep145"/>
          <dependency deprel="compound:nn" dependent="t24" head="t25" id="dep146"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep147"/>
          <dependency deprel="case" dependent="t28" head="t27" id="dep148"/>
          <dependency deprel="ccomp" dependent="t34" head="t29" id="dep149"/>
          <dependency deprel="nsubj" dependent="t25" head="t29" id="dep150"/>
          <dependency deprel="nmod:prep" dependent="t27" head="t29" id="dep151"/>
          <dependency deprel="punct" dependent="t30" head="t29" id="dep152"/>
          <dependency deprel="mark" dependent="t33" head="t32" id="dep153"/>
          <dependency deprel="advmod:dvp" dependent="t32" head="t34" id="dep154"/>
          <dependency deprel="ccomp" dependent="t36" head="t34" id="dep155"/>
          <dependency deprel="nsubj" dependent="t31" head="t34" id="dep156"/>
          <dependency deprel="nsubj" dependent="t35" head="t36" id="dep157"/>
          <dependency deprel="dobj" dependent="t38" head="t36" id="dep158"/>
          <dependency deprel="compound:nn" dependent="t37" head="t38" id="dep159"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t21" head="ROOT" id="dep174"/>
          <dependency deprel="case" dependent="t17" head="t18" id="dep175"/>
          <dependency deprel="case" dependent="t19" head="t18" id="dep176"/>
          <dependency deprel="dep" dependent="t16" head="t20" id="dep177"/>
          <dependency deprel="nmod" dependent="t18" head="t20" id="dep178"/>
          <dependency deprel="nmod:assmod" dependent="t14" head="t20" id="dep179"/>
          <dependency deprel="dep" dependent="t15" head="t20" id="dep180"/>
          <dependency deprel="nsubj" dependent="t20" head="t21" id="dep181"/>
          <dependency deprel="dobj" dependent="t22" head="t21" id="dep182"/>
          <dependency deprel="punct" dependent="t23" head="t21" id="dep183"/>
          <dependency deprel="punct" dependent="t39" head="t21" id="dep184"/>
          <dependency deprel="conj" dependent="t29" head="t21" id="dep185"/>
          <dependency deprel="compound:nn" dependent="t24" head="t25" id="dep186"/>
          <dependency deprel="case" dependent="t26" head="t27" id="dep187"/>
          <dependency deprel="case" dependent="t28" head="t27" id="dep188"/>
          <dependency deprel="ccomp" dependent="t34" head="t29" id="dep189"/>
          <dependency deprel="nsubj" dependent="t25" head="t29" id="dep190"/>
          <dependency deprel="nmod:prep" dependent="t27" head="t29" id="dep191"/>
          <dependency deprel="punct" dependent="t30" head="t29" id="dep192"/>
          <dependency deprel="mark" dependent="t33" head="t32" id="dep193"/>
          <dependency deprel="advmod:dvp" dependent="t32" head="t34" id="dep194"/>
          <dependency deprel="ccomp" dependent="t36" head="t34" id="dep195"/>
          <dependency deprel="nsubj" dependent="t31" head="t34" id="dep196"/>
          <dependency deprel="nsubj" dependent="t35" head="t36" id="dep197"/>
          <dependency deprel="dobj" dependent="t38" head="t36" id="dep198"/>
          <dependency deprel="compound:nn" dependent="t37" head="t38" id="dep199"/>
        </dependencies>
      </sentence>
    </sentences>
    <mentions annotators="corenlp">
      <mention head="t0" tokens="t0" id="me0"/>
      <mention head="t9" tokens="t9" id="me1"/>
      <mention head="t18" tokens="t18" id="me2"/>
      <mention head="t24" tokens="t24" id="me3"/>
      <mention head="t35" tokens="t35" id="me4"/>
      <mention head="t31" tokens="t31" id="me5"/>
    </mentions>
    <coreferences annotators="corenlp">
      <coreference representative="me0" mentions="me0 me2 me3 me5" id="cr0"/>
      <coreference representative="me1" mentions="me1 me4" id="cr1"/>
    </coreferences>
  </document>
</root>"""

        self.exe = 'java -Xmx3g -cp "jar/*" jigg.pipeline.Pipeline ' \
                   + '-annotators corenlp[tokenize,ssplit,pos,lemma,ner,parse,coref] ' \
                   + '-corenlp.props StanfordCoreNLP-chinese.properties '

    def test_corenlp_chinese_coref(self):
        self.check_equal_with_java(self.exe, self.input_text, self.expected_text)
