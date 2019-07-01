import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestDepccgCcg(BaseTest):

    def setUp(self):
        self.input_text = "Stanford University is located in California. It is a great university, founded in 1891."

        self.expected_text = r"""<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="45" characterOffsetBegin="0" id="s0">
        Stanford University is located in California.
        <tokens annotators="corenlp">
          <token form="Stanford" id="t0" characterOffsetBegin="0" characterOffsetEnd="8"/>
          <token form="University" id="t1" characterOffsetBegin="9" characterOffsetEnd="19"/>
          <token form="is" id="t2" characterOffsetBegin="20" characterOffsetEnd="22"/>
          <token form="located" id="t3" characterOffsetBegin="23" characterOffsetEnd="30"/>
          <token form="in" id="t4" characterOffsetBegin="31" characterOffsetEnd="33"/>
          <token form="California" id="t5" characterOffsetBegin="34" characterOffsetEnd="44"/>
          <token form="." id="t6" characterOffsetBegin="44" characterOffsetEnd="45"/>
        </tokens>
        <ccg id="ccg0" root="ccgsp0" annotators="depccg">
          <span children="ccgsp1 ccgsp14" rule="rp" symbol="S[dcl]" end="7" begin="0" id="ccgsp0"/>
          <span children="ccgsp2 ccgsp6" rule="ba" symbol="S[dcl]" end="6" begin="0" id="ccgsp1"/>
          <span children="ccgsp3" rule="lex" symbol="NP" end="2" begin="0" id="ccgsp2"/>
          <span children="ccgsp4 ccgsp5" rule="fa" symbol="N" end="2" begin="0" id="ccgsp3"/>
          <span children="t0" symbol="N/N" end="1" begin="0" id="ccgsp4"/>
          <span children="t1" symbol="N" end="2" begin="1" id="ccgsp5"/>
          <span children="ccgsp7 ccgsp8" rule="fa" symbol="S[dcl]\NP" end="6" begin="2" id="ccgsp6"/>
          <span children="t2" symbol="(S[dcl]\NP)/(S[pss]\NP)" end="3" begin="2" id="ccgsp7"/>
          <span children="ccgsp9 ccgsp10" rule="ba" symbol="S[pss]\NP" end="6" begin="3" id="ccgsp8"/>
          <span children="t3" symbol="S[pss]\NP" end="4" begin="3" id="ccgsp9"/>
          <span children="ccgsp11 ccgsp12" rule="fa" symbol="(S\NP)\(S\NP)" end="6" begin="4" id="ccgsp10"/>
          <span children="t4" symbol="((S\NP)\(S\NP))/NP" end="5" begin="4" id="ccgsp11"/>
          <span children="ccgsp13" rule="lex" symbol="NP" end="6" begin="5" id="ccgsp12"/>
          <span children="t5" symbol="N" end="6" begin="5" id="ccgsp13"/>
          <span children="t6" symbol="." end="7" begin="6" id="ccgsp14"/>
        </ccg>
      </sentence>
      <sentence characterOffsetEnd="88" characterOffsetBegin="46" id="s1">
        It is a great university, founded in 1891.
        <tokens annotators="corenlp">
          <token form="It" id="t7" characterOffsetBegin="0" characterOffsetEnd="2"/>
          <token form="is" id="t8" characterOffsetBegin="3" characterOffsetEnd="5"/>
          <token form="a" id="t9" characterOffsetBegin="6" characterOffsetEnd="7"/>
          <token form="great" id="t10" characterOffsetBegin="8" characterOffsetEnd="13"/>
          <token form="university" id="t11" characterOffsetBegin="14" characterOffsetEnd="24"/>
          <token form="," id="t12" characterOffsetBegin="24" characterOffsetEnd="25"/>
          <token form="founded" id="t13" characterOffsetBegin="26" characterOffsetEnd="33"/>
          <token form="in" id="t14" characterOffsetBegin="34" characterOffsetEnd="36"/>
          <token form="1891" id="t15" characterOffsetBegin="37" characterOffsetEnd="41"/>
          <token form="." id="t16" characterOffsetBegin="41" characterOffsetEnd="42"/>
        </tokens>
        <ccg id="ccg1" root="ccgsp15" annotators="depccg">
          <span children="ccgsp16 ccgsp35" rule="rp" symbol="S[dcl]" end="10" begin="0" id="ccgsp15"/>
          <span children="ccgsp17 ccgsp18" rule="ba" symbol="S[dcl]" end="9" begin="0" id="ccgsp16"/>
          <span children="t7" symbol="NP" end="1" begin="0" id="ccgsp17"/>
          <span children="ccgsp19 ccgsp20" rule="fa" symbol="S[dcl]\NP" end="9" begin="1" id="ccgsp18"/>
          <span children="t8" symbol="(S[dcl]\NP)/NP" end="2" begin="1" id="ccgsp19"/>
          <span children="ccgsp21 ccgsp28" rule="ba" symbol="NP" end="9" begin="2" id="ccgsp20"/>
          <span children="ccgsp22 ccgsp27" rule="rp" symbol="NP" end="6" begin="2" id="ccgsp21"/>
          <span children="ccgsp23 ccgsp24" rule="fa" symbol="NP" end="5" begin="2" id="ccgsp22"/>
          <span children="t9" symbol="NP[nb]/N" end="3" begin="2" id="ccgsp23"/>
          <span children="ccgsp25 ccgsp26" rule="fa" symbol="N" end="5" begin="3" id="ccgsp24"/>
          <span children="t10" symbol="N/N" end="4" begin="3" id="ccgsp25"/>
          <span children="t11" symbol="N" end="5" begin="4" id="ccgsp26"/>
          <span children="t12" symbol="," end="6" begin="5" id="ccgsp27"/>
          <span children="ccgsp29" rule="lex" symbol="NP\NP" end="9" begin="6" id="ccgsp28"/>
          <span children="ccgsp30 ccgsp31" rule="ba" symbol="S[pss]\NP" end="9" begin="6" id="ccgsp29"/>
          <span children="t13" symbol="S[pss]\NP" end="7" begin="6" id="ccgsp30"/>
          <span children="ccgsp32 ccgsp33" rule="fa" symbol="(S\NP)\(S\NP)" end="9" begin="7" id="ccgsp31"/>
          <span children="t14" symbol="((S\NP)\(S\NP))/NP" end="8" begin="7" id="ccgsp32"/>
          <span children="ccgsp34" rule="lex" symbol="NP" end="9" begin="8" id="ccgsp33"/>
          <span children="t15" symbol="N" end="9" begin="8" id="ccgsp34"/>
          <span children="t16" symbol="." end="10" begin="9" id="ccgsp35"/>
        </ccg>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline ' \
                   + '-annotators corenlp[tokenize,ssplit],depccg '

    def test_depccg_ccg(self):
        import subprocess
        subprocess.check_call('echo "Stanford University is located in California .\nIt is a great university, founded in 1891 .\n####EOD####" > input.txt', shell=True)
        subprocess.check_output('python src/main/resources/python/_depccg.py < input.txt', shell=True)
        self.check_equal(self.exe, self.input_text, self.expected_text)
