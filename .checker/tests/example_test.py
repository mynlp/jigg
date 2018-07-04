import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestName(BaseTest):
    '''
    This is an exmaple (or a based) file of unittest. You want
    to add the new test file, please copy this file and edit
    it as the following.

    1. Copy this file
       please, copy this file as the following command:
       ```
       cp example_test.py {ANNOTATORS}/test_***.py
       ```
       The {ANNOTATORS} is annotator name.
       You need to name the file like `test_***.py`. `***` is any name.
       Note the head to the file name must give the `test`. For example,
       `test_tokenize.py`.
    2. Change the class name
       For each the test case, You change the class name from
       TestName to Test***. `***` is any name, for example,
       Tokenize, Ssplit, ... etc.
    3. Change three variables in the setUp() function
       - self.input_text : a sample text using for test
       - self.expected_text : an expected output text by test run
       - self.exe : an execution command
         This program runs with the sbt runMain command. For example,
         `sbt "runMain jigg.pipeline.Pipeline -annotators corenlp[tokenize]"`.
         You set the part of "runMain ~" in the variable `self.exe`.
    4. Change the function name.
       For each the test case, You also change the function name
       from test_name to test_***. `***` is any name, for example,
       tokenize, ssplit, ... etc. Note that the head of the
       function name must give the `test`.

    For example, the case of the annotator `pos`:
    1. file name -> test_pos.py
    2. class name -> class TestPos(BaseTest):
    3. variables ->
       self.input_text = "This is a sample text."
       self.expected_text = "[the result text]"
       self.exe = 'runMain jigg.pipeline.Pipeline -annotators corenlp[tokenize,ssplit,pos]'
    4. function name -> def test_pos(self):
    '''
    def setUp(self):
        # Set an input (sample) text
        self.input_text = ""

        # Set an expected text
        self.expected_text = ""

        # Set a execution command
        # You need to change the `-annotators` term according to the test case.
        # For example, the case of annotation `lemma`, corenlp[tokenize,ssplit,pos,lemma].
        self.exe = 'runMain jigg.pipeline.Pipeline -annotators corenlp[tokenize]'

    def test_name(self):
        # A function check_equal() is defined on the superclass BaseTest.
        self.check_equal(self.exe, self.input_text, self.expected_text)
