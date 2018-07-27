import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestCoreNLPFrenchDepParse(BaseTest):
    '''
    '''
    def setUp(self):
        self.input_text = """Les vestiges préhistoriques de Nouans-les-Fontaines sont très nombreux, traduisant la présence humaine sur le territoire depuis le Paléolithique. La période protohistorique est également bien documentée, avec de multiples preuves d'une importante activité sidérurgique."""

        self.expected_text = """<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="145" characterOffsetBegin="0" id="s0">
        Les vestiges préhistoriques de Nouans-les-Fontaines sont très nombreux, traduisant la présence humaine sur le territoire depuis le Paléolithique.
        <tokens annotators="corenlp">
          <token form="Les" id="t0" characterOffsetBegin="0" characterOffsetEnd="3" pos="DET"/>
          <token form="vestiges" id="t1" characterOffsetBegin="4" characterOffsetEnd="12" pos="NOUN"/>
          <token form="préhistoriques" id="t2" characterOffsetBegin="13" characterOffsetEnd="27" pos="ADJ"/>
          <token form="de" id="t3" characterOffsetBegin="28" characterOffsetEnd="30" pos="ADP"/>
          <token form="Nouans-les-Fontaines" id="t4" characterOffsetBegin="31" characterOffsetEnd="51" pos="PROPN"/>
          <token form="sont" id="t5" characterOffsetBegin="52" characterOffsetEnd="56" pos="VERB"/>
          <token form="très" id="t6" characterOffsetBegin="57" characterOffsetEnd="61" pos="ADV"/>
          <token form="nombreux" id="t7" characterOffsetBegin="62" characterOffsetEnd="70" pos="ADJ"/>
          <token form="," id="t8" characterOffsetBegin="70" characterOffsetEnd="71" pos="PUNCT"/>
          <token form="traduisant" id="t9" characterOffsetBegin="72" characterOffsetEnd="82" pos="VERB"/>
          <token form="la" id="t10" characterOffsetBegin="83" characterOffsetEnd="85" pos="DET"/>
          <token form="présence" id="t11" characterOffsetBegin="86" characterOffsetEnd="94" pos="NOUN"/>
          <token form="humaine" id="t12" characterOffsetBegin="95" characterOffsetEnd="102" pos="ADJ"/>
          <token form="sur" id="t13" characterOffsetBegin="103" characterOffsetEnd="106" pos="ADP"/>
          <token form="le" id="t14" characterOffsetBegin="107" characterOffsetEnd="109" pos="DET"/>
          <token form="territoire" id="t15" characterOffsetBegin="110" characterOffsetEnd="120" pos="NOUN"/>
          <token form="depuis" id="t16" characterOffsetBegin="121" characterOffsetEnd="127" pos="ADP"/>
          <token form="le" id="t17" characterOffsetBegin="128" characterOffsetEnd="130" pos="DET"/>
          <token form="Paléolithique" id="t18" characterOffsetBegin="131" characterOffsetEnd="144" pos="PROPN"/>
          <token form="." id="t19" characterOffsetBegin="144" characterOffsetEnd="145" pos="PUNCT"/>
        </tokens>
        <dependencies annotators="corenlp" type="basic">
          <dependency deprel="root" dependent="t7" head="ROOT" id="dep0"/>
          <dependency deprel="det" dependent="t0" head="t1" id="dep1"/>
          <dependency deprel="amod" dependent="t2" head="t1" id="dep2"/>
          <dependency deprel="nmod" dependent="t4" head="t1" id="dep3"/>
          <dependency deprel="case" dependent="t3" head="t4" id="dep4"/>
          <dependency deprel="nsubj" dependent="t1" head="t7" id="dep5"/>
          <dependency deprel="punct" dependent="t19" head="t7" id="dep6"/>
          <dependency deprel="cop" dependent="t5" head="t7" id="dep7"/>
          <dependency deprel="advmod" dependent="t6" head="t7" id="dep8"/>
          <dependency deprel="punct" dependent="t8" head="t7" id="dep9"/>
          <dependency deprel="conj" dependent="t9" head="t7" id="dep10"/>
          <dependency deprel="nmod" dependent="t15" head="t9" id="dep11"/>
          <dependency deprel="nmod" dependent="t18" head="t9" id="dep12"/>
          <dependency deprel="dobj" dependent="t11" head="t9" id="dep13"/>
          <dependency deprel="det" dependent="t10" head="t11" id="dep14"/>
          <dependency deprel="amod" dependent="t12" head="t11" id="dep15"/>
          <dependency deprel="case" dependent="t13" head="t15" id="dep16"/>
          <dependency deprel="det" dependent="t14" head="t15" id="dep17"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep18"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep19"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed">
          <dependency deprel="root" dependent="t7" head="ROOT" id="dep38"/>
          <dependency deprel="det" dependent="t0" head="t1" id="dep39"/>
          <dependency deprel="amod" dependent="t2" head="t1" id="dep40"/>
          <dependency deprel="nmod" dependent="t4" head="t1" id="dep41"/>
          <dependency deprel="case" dependent="t3" head="t4" id="dep42"/>
          <dependency deprel="nsubj" dependent="t1" head="t7" id="dep43"/>
          <dependency deprel="punct" dependent="t19" head="t7" id="dep44"/>
          <dependency deprel="cop" dependent="t5" head="t7" id="dep45"/>
          <dependency deprel="advmod" dependent="t6" head="t7" id="dep46"/>
          <dependency deprel="punct" dependent="t8" head="t7" id="dep47"/>
          <dependency deprel="conj" dependent="t9" head="t7" id="dep48"/>
          <dependency deprel="nmod" dependent="t15" head="t9" id="dep49"/>
          <dependency deprel="nmod" dependent="t18" head="t9" id="dep50"/>
          <dependency deprel="dobj" dependent="t11" head="t9" id="dep51"/>
          <dependency deprel="det" dependent="t10" head="t11" id="dep52"/>
          <dependency deprel="amod" dependent="t12" head="t11" id="dep53"/>
          <dependency deprel="case" dependent="t13" head="t15" id="dep54"/>
          <dependency deprel="det" dependent="t14" head="t15" id="dep55"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep56"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep57"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t7" head="ROOT" id="dep76"/>
          <dependency deprel="det" dependent="t0" head="t1" id="dep77"/>
          <dependency deprel="amod" dependent="t2" head="t1" id="dep78"/>
          <dependency deprel="nmod" dependent="t4" head="t1" id="dep79"/>
          <dependency deprel="case" dependent="t3" head="t4" id="dep80"/>
          <dependency deprel="nsubj" dependent="t1" head="t7" id="dep81"/>
          <dependency deprel="punct" dependent="t19" head="t7" id="dep82"/>
          <dependency deprel="cop" dependent="t5" head="t7" id="dep83"/>
          <dependency deprel="advmod" dependent="t6" head="t7" id="dep84"/>
          <dependency deprel="punct" dependent="t8" head="t7" id="dep85"/>
          <dependency deprel="conj" dependent="t9" head="t7" id="dep86"/>
          <dependency deprel="nmod" dependent="t15" head="t9" id="dep87"/>
          <dependency deprel="nmod" dependent="t18" head="t9" id="dep88"/>
          <dependency deprel="dobj" dependent="t11" head="t9" id="dep89"/>
          <dependency deprel="det" dependent="t10" head="t11" id="dep90"/>
          <dependency deprel="amod" dependent="t12" head="t11" id="dep91"/>
          <dependency deprel="case" dependent="t13" head="t15" id="dep92"/>
          <dependency deprel="det" dependent="t14" head="t15" id="dep93"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep94"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep95"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced">
          <dependency deprel="root" dependent="t7" head="ROOT" id="dep114"/>
          <dependency deprel="det" dependent="t0" head="t1" id="dep115"/>
          <dependency deprel="amod" dependent="t2" head="t1" id="dep116"/>
          <dependency deprel="nmod" dependent="t4" head="t1" id="dep117"/>
          <dependency deprel="case" dependent="t3" head="t4" id="dep118"/>
          <dependency deprel="nsubj" dependent="t1" head="t7" id="dep119"/>
          <dependency deprel="punct" dependent="t19" head="t7" id="dep120"/>
          <dependency deprel="cop" dependent="t5" head="t7" id="dep121"/>
          <dependency deprel="advmod" dependent="t6" head="t7" id="dep122"/>
          <dependency deprel="punct" dependent="t8" head="t7" id="dep123"/>
          <dependency deprel="conj" dependent="t9" head="t7" id="dep124"/>
          <dependency deprel="nmod" dependent="t15" head="t9" id="dep125"/>
          <dependency deprel="nmod" dependent="t18" head="t9" id="dep126"/>
          <dependency deprel="dobj" dependent="t11" head="t9" id="dep127"/>
          <dependency deprel="det" dependent="t10" head="t11" id="dep128"/>
          <dependency deprel="amod" dependent="t12" head="t11" id="dep129"/>
          <dependency deprel="case" dependent="t13" head="t15" id="dep130"/>
          <dependency deprel="det" dependent="t14" head="t15" id="dep131"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep132"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep133"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t7" head="ROOT" id="dep152"/>
          <dependency deprel="det" dependent="t0" head="t1" id="dep153"/>
          <dependency deprel="amod" dependent="t2" head="t1" id="dep154"/>
          <dependency deprel="nmod" dependent="t4" head="t1" id="dep155"/>
          <dependency deprel="case" dependent="t3" head="t4" id="dep156"/>
          <dependency deprel="nsubj" dependent="t1" head="t7" id="dep157"/>
          <dependency deprel="punct" dependent="t19" head="t7" id="dep158"/>
          <dependency deprel="cop" dependent="t5" head="t7" id="dep159"/>
          <dependency deprel="advmod" dependent="t6" head="t7" id="dep160"/>
          <dependency deprel="punct" dependent="t8" head="t7" id="dep161"/>
          <dependency deprel="conj" dependent="t9" head="t7" id="dep162"/>
          <dependency deprel="nmod" dependent="t15" head="t9" id="dep163"/>
          <dependency deprel="nmod" dependent="t18" head="t9" id="dep164"/>
          <dependency deprel="dobj" dependent="t11" head="t9" id="dep165"/>
          <dependency deprel="det" dependent="t10" head="t11" id="dep166"/>
          <dependency deprel="amod" dependent="t12" head="t11" id="dep167"/>
          <dependency deprel="case" dependent="t13" head="t15" id="dep168"/>
          <dependency deprel="det" dependent="t14" head="t15" id="dep169"/>
          <dependency deprel="case" dependent="t16" head="t18" id="dep170"/>
          <dependency deprel="det" dependent="t17" head="t18" id="dep171"/>
        </dependencies>
      </sentence>
      <sentence characterOffsetEnd="269" characterOffsetBegin="146" id="s1">
        La période protohistorique est également bien documentée, avec de multiples preuves d'une importante activité sidérurgique.
        <tokens annotators="corenlp">
          <token form="La" id="t20" characterOffsetBegin="0" characterOffsetEnd="2" pos="DET"/>
          <token form="période" id="t21" characterOffsetBegin="3" characterOffsetEnd="10" pos="NOUN"/>
          <token form="protohistorique" id="t22" characterOffsetBegin="11" characterOffsetEnd="26" pos="ADJ"/>
          <token form="est" id="t23" characterOffsetBegin="27" characterOffsetEnd="30" pos="AUX"/>
          <token form="également" id="t24" characterOffsetBegin="31" characterOffsetEnd="40" pos="ADV"/>
          <token form="bien" id="t25" characterOffsetBegin="41" characterOffsetEnd="45" pos="ADV"/>
          <token form="documentée" id="t26" characterOffsetBegin="46" characterOffsetEnd="56" pos="VERB"/>
          <token form="," id="t27" characterOffsetBegin="56" characterOffsetEnd="57" pos="PUNCT"/>
          <token form="avec" id="t28" characterOffsetBegin="58" characterOffsetEnd="62" pos="ADP"/>
          <token form="de" id="t29" characterOffsetBegin="63" characterOffsetEnd="65" pos="DET"/>
          <token form="multiples" id="t30" characterOffsetBegin="66" characterOffsetEnd="75" pos="ADJ"/>
          <token form="preuves" id="t31" characterOffsetBegin="76" characterOffsetEnd="83" pos="NOUN"/>
          <token form="d'" id="t32" characterOffsetBegin="84" characterOffsetEnd="86" pos="ADP"/>
          <token form="une" id="t33" characterOffsetBegin="86" characterOffsetEnd="89" pos="DET"/>
          <token form="importante" id="t34" characterOffsetBegin="90" characterOffsetEnd="100" pos="ADJ"/>
          <token form="activité" id="t35" characterOffsetBegin="101" characterOffsetEnd="109" pos="NOUN"/>
          <token form="sidérurgique" id="t36" characterOffsetBegin="110" characterOffsetEnd="122" pos="ADJ"/>
          <token form="." id="t37" characterOffsetBegin="122" characterOffsetEnd="123" pos="PUNCT"/>
        </tokens>
        <dependencies annotators="corenlp" type="basic">
          <dependency deprel="root" dependent="t26" head="ROOT" id="dep20"/>
          <dependency deprel="advmod" dependent="t24" head="t25" id="dep21"/>
          <dependency deprel="auxpass" dependent="t23" head="t26" id="dep22"/>
          <dependency deprel="advmod" dependent="t25" head="t26" id="dep23"/>
          <dependency deprel="punct" dependent="t27" head="t26" id="dep24"/>
          <dependency deprel="nmod" dependent="t31" head="t26" id="dep25"/>
          <dependency deprel="nsubjpass" dependent="t21" head="t26" id="dep26"/>
          <dependency deprel="punct" dependent="t37" head="t26" id="dep27"/>
          <dependency deprel="case" dependent="t28" head="t31" id="dep28"/>
          <dependency deprel="det" dependent="t29" head="t31" id="dep29"/>
          <dependency deprel="amod" dependent="t30" head="t31" id="dep30"/>
          <dependency deprel="nmod" dependent="t35" head="t31" id="dep31"/>
          <dependency deprel="case" dependent="t32" head="t35" id="dep32"/>
          <dependency deprel="det" dependent="t33" head="t35" id="dep33"/>
          <dependency deprel="amod" dependent="t34" head="t35" id="dep34"/>
          <dependency deprel="amod" dependent="t36" head="t35" id="dep35"/>
          <dependency deprel="amod" dependent="t22" head="t21" id="dep36"/>
          <dependency deprel="det" dependent="t20" head="t21" id="dep37"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed">
          <dependency deprel="root" dependent="t26" head="ROOT" id="dep58"/>
          <dependency deprel="advmod" dependent="t24" head="t25" id="dep59"/>
          <dependency deprel="auxpass" dependent="t23" head="t26" id="dep60"/>
          <dependency deprel="advmod" dependent="t25" head="t26" id="dep61"/>
          <dependency deprel="punct" dependent="t27" head="t26" id="dep62"/>
          <dependency deprel="nmod" dependent="t31" head="t26" id="dep63"/>
          <dependency deprel="nsubjpass" dependent="t21" head="t26" id="dep64"/>
          <dependency deprel="punct" dependent="t37" head="t26" id="dep65"/>
          <dependency deprel="case" dependent="t28" head="t31" id="dep66"/>
          <dependency deprel="det" dependent="t29" head="t31" id="dep67"/>
          <dependency deprel="amod" dependent="t30" head="t31" id="dep68"/>
          <dependency deprel="nmod" dependent="t35" head="t31" id="dep69"/>
          <dependency deprel="case" dependent="t32" head="t35" id="dep70"/>
          <dependency deprel="det" dependent="t33" head="t35" id="dep71"/>
          <dependency deprel="amod" dependent="t34" head="t35" id="dep72"/>
          <dependency deprel="amod" dependent="t36" head="t35" id="dep73"/>
          <dependency deprel="amod" dependent="t22" head="t21" id="dep74"/>
          <dependency deprel="det" dependent="t20" head="t21" id="dep75"/>
        </dependencies>
        <dependencies annotators="corenlp" type="collapsed-ccprocessed">
          <dependency deprel="root" dependent="t26" head="ROOT" id="dep96"/>
          <dependency deprel="advmod" dependent="t24" head="t25" id="dep97"/>
          <dependency deprel="auxpass" dependent="t23" head="t26" id="dep98"/>
          <dependency deprel="advmod" dependent="t25" head="t26" id="dep99"/>
          <dependency deprel="punct" dependent="t27" head="t26" id="dep100"/>
          <dependency deprel="nmod" dependent="t31" head="t26" id="dep101"/>
          <dependency deprel="nsubjpass" dependent="t21" head="t26" id="dep102"/>
          <dependency deprel="punct" dependent="t37" head="t26" id="dep103"/>
          <dependency deprel="case" dependent="t28" head="t31" id="dep104"/>
          <dependency deprel="det" dependent="t29" head="t31" id="dep105"/>
          <dependency deprel="amod" dependent="t30" head="t31" id="dep106"/>
          <dependency deprel="nmod" dependent="t35" head="t31" id="dep107"/>
          <dependency deprel="case" dependent="t32" head="t35" id="dep108"/>
          <dependency deprel="det" dependent="t33" head="t35" id="dep109"/>
          <dependency deprel="amod" dependent="t34" head="t35" id="dep110"/>
          <dependency deprel="amod" dependent="t36" head="t35" id="dep111"/>
          <dependency deprel="amod" dependent="t22" head="t21" id="dep112"/>
          <dependency deprel="det" dependent="t20" head="t21" id="dep113"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced">
          <dependency deprel="root" dependent="t26" head="ROOT" id="dep134"/>
          <dependency deprel="advmod" dependent="t24" head="t25" id="dep135"/>
          <dependency deprel="auxpass" dependent="t23" head="t26" id="dep136"/>
          <dependency deprel="advmod" dependent="t25" head="t26" id="dep137"/>
          <dependency deprel="punct" dependent="t27" head="t26" id="dep138"/>
          <dependency deprel="nmod" dependent="t31" head="t26" id="dep139"/>
          <dependency deprel="nsubjpass" dependent="t21" head="t26" id="dep140"/>
          <dependency deprel="punct" dependent="t37" head="t26" id="dep141"/>
          <dependency deprel="case" dependent="t28" head="t31" id="dep142"/>
          <dependency deprel="det" dependent="t29" head="t31" id="dep143"/>
          <dependency deprel="amod" dependent="t30" head="t31" id="dep144"/>
          <dependency deprel="nmod" dependent="t35" head="t31" id="dep145"/>
          <dependency deprel="case" dependent="t32" head="t35" id="dep146"/>
          <dependency deprel="det" dependent="t33" head="t35" id="dep147"/>
          <dependency deprel="amod" dependent="t34" head="t35" id="dep148"/>
          <dependency deprel="amod" dependent="t36" head="t35" id="dep149"/>
          <dependency deprel="amod" dependent="t22" head="t21" id="dep150"/>
          <dependency deprel="det" dependent="t20" head="t21" id="dep151"/>
        </dependencies>
        <dependencies annotators="corenlp" type="enhanced-plus-plus">
          <dependency deprel="root" dependent="t26" head="ROOT" id="dep172"/>
          <dependency deprel="advmod" dependent="t24" head="t25" id="dep173"/>
          <dependency deprel="auxpass" dependent="t23" head="t26" id="dep174"/>
          <dependency deprel="advmod" dependent="t25" head="t26" id="dep175"/>
          <dependency deprel="punct" dependent="t27" head="t26" id="dep176"/>
          <dependency deprel="nmod" dependent="t31" head="t26" id="dep177"/>
          <dependency deprel="nsubjpass" dependent="t21" head="t26" id="dep178"/>
          <dependency deprel="punct" dependent="t37" head="t26" id="dep179"/>
          <dependency deprel="case" dependent="t28" head="t31" id="dep180"/>
          <dependency deprel="det" dependent="t29" head="t31" id="dep181"/>
          <dependency deprel="amod" dependent="t30" head="t31" id="dep182"/>
          <dependency deprel="nmod" dependent="t35" head="t31" id="dep183"/>
          <dependency deprel="case" dependent="t32" head="t35" id="dep184"/>
          <dependency deprel="det" dependent="t33" head="t35" id="dep185"/>
          <dependency deprel="amod" dependent="t34" head="t35" id="dep186"/>
          <dependency deprel="amod" dependent="t36" head="t35" id="dep187"/>
          <dependency deprel="amod" dependent="t22" head="t21" id="dep188"/>
          <dependency deprel="det" dependent="t20" head="t21" id="dep189"/>
        </dependencies>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'java -Xmx3g -cp "jar/*" jigg.pipeline.Pipeline ' \
                   + '-annotators corenlp[tokenize,ssplit,pos,depparse] ' \
                   + '-corenlp.props StanfordCoreNLP-french.properties '

    def test_corenlp_french_depparse(self):
        self.check_equal_with_java(self.exe, self.input_text, self.expected_text)
